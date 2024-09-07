/*
 * Copyright 2024 k7t3
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.k7t3.voicepeakcw4j.speech;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文字列を読み上げるランナー
 * <p>{@link SpeechBuilder}に与えた設定に基づいてインスタンスが生成される。</p>
 * <p>
 *     VOICEPEAKのコマンドライン実装の制約により、一度に処理できる文字数が140で制限されるため、
 *     より長い文字列は複数回にわけてVOICEPEAKプロセスが実行される。
 * </p>
 * <p>
 *     また、VOICEPEAKのコマンドライン実装には並列処理に関しても制約があり、
 *     同時に一つのインスタンスしか実行できず、このランナーの認識外(ターミナルやコマンドプロンプト)において
 *     手動で実行していると正常に動作しないことがある。
 * </p>
 * <p>
 *     処理速度について、多くの環境において音声の再生速度＞音声の生成速度であることから、
 *     シームレスな読み上げを実現するために最初の読み上げに遅延時間を設定することができる。
 *     {@link SpeechBuilder#withDelayTime(TimeUnit, long)},
 *     {@link SpeechBuilder#withDelayMilliSeconds(long)}
 * </p>
 */
public class SpeechRunner {

    private static final System.Logger LOGGER = System.getLogger(SpeechRunner.class.getName());

    private Flow.Subscriber<String> standardOutSubscriber;
    private Flow.Subscriber<String> errorOutSubscriber;

    private final List<SpeechParameter> parameters;

    private final AudioDevice audioDevice;

    private final float volumeRate;

    private final long delayMilliSeconds;

    private final Executor voicePeakExecutor;

    /**
     * コンストラクタ
     * @param audioDevice オーディオを再生するデバイス default value: null
     * @param volumeRate ボリュームの割合 0.0f .. 2.0f default value: 1.0f
     * @param delayMilliSeconds 遅延時間 default value: 0
     * @param voicePeakExecutor VOICEPEAKを実行するExecutor default value: 0
     * @param parameters スピーチに関するパラメータ
     */
    SpeechRunner(
            AudioDevice audioDevice,
            float volumeRate,
            long delayMilliSeconds,
            Executor voicePeakExecutor,
            List<SpeechParameter> parameters
    ) {
        this.audioDevice = audioDevice;
        this.volumeRate = volumeRate;
        this.delayMilliSeconds = delayMilliSeconds;
        this.voicePeakExecutor = voicePeakExecutor;
        this.parameters = parameters;
    }

    /**
     * VOICEPEAKのコマンドライン実行における標準出力の購読を設定する。
     * @param standardOutSubscriber 標準出力のサブスクライバー
     */
    public void setStandardOutSubscriber(Flow.Subscriber<String> standardOutSubscriber) {
        this.standardOutSubscriber = standardOutSubscriber;
    }

    /**
     * VOICEPEAKのコマンドライン実行における標準エラー出力の購読を設定する。
     * @param errorOutSubscriber 標準エラー出力のサブスクライバー
     */
    public void setErrorOutSubscriber(Flow.Subscriber<String> errorOutSubscriber) {
        this.errorOutSubscriber = errorOutSubscriber;
    }

    private ExecutorService createExecutor(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            var t = new Thread(r);
            t.setName(name);
            return t;
        });
    }

    /**
     * ランナーを実行する。
     * <p>
     *     {@link SpeechBuilder}で設定した内容に基づいて読み上げを実行する。
     * </p>
     * <p>
     *     パラメータが空の場合は{@link SpeechState#getSpeechFuture()}は完了状態となる。
     * </p>
     * @return 読み上げの状態
     */
    public SpeechState start() {
        LOGGER.log(System.Logger.Level.DEBUG, "runner started");

        var counter = new AtomicInteger(0);
        var cancel = new AtomicBoolean(false);

        if (parameters.isEmpty()) {
            return new SpeechState(
                    new CompletableFuture[0],
                    CompletableFuture.completedFuture(null),
                    parameters.size(),
                    counter,
                    cancel,
                    null
            );
        }

        // 現在のVOICEPEAKのバージョン(v.1.2.11)はコマンドラインの並列実行を許可していない
        // エラー出力
        // In this version, up to 1 command line instance can be executed at same time.
        var executor = voicePeakExecutor == null
                ? createExecutor("VOICEPEAKCommandLineWrapper")
                : voicePeakExecutor;
        var audioPlayerExecutor = createExecutor("AudioPlayer");
        LOGGER.log(System.Logger.Level.DEBUG, "initialized executors");

        // 合成した読み上げ音声を再生するプレイヤー
        var player = new AudioPlayer(audioDevice);
        player.requestVolume(volumeRate);

        // 音声を合成するFuture、音声を読み上げるFuture
        var futures = new CompletableFuture<?>[parameters.size() * 2];
        int i = 0;

        // 読み上げのパラメータごとに処理を実行し、futuresに登録する
        for (var parameter : parameters) {
            var process = parameter.process();

            if (standardOutSubscriber != null) {
                process.getStandardOut().subscribe(standardOutSubscriber);
            }
            if (errorOutSubscriber != null) {
                process.getErrorOut().subscribe(errorOutSubscriber);
            }

            var voicePeakFuture = queueProcess(parameter, executor, cancel);
            LOGGER.log(System.Logger.Level.DEBUG, "queued VOICEPEAK process");

            var playAudioFuture = queuePlayAudio(player, voicePeakFuture, audioPlayerExecutor);
            LOGGER.log(System.Logger.Level.DEBUG, "queued play audio request");

            // 再生が終わったらインクリメント
            playAudioFuture.thenRun(counter::incrementAndGet);

            futures[i++] = voicePeakFuture;
            futures[i++] = playAudioFuture;
        }

        // オーディオプレイヤーの終了処理
        CompletableFuture.runAsync(player::close, audioPlayerExecutor);

        if (voicePeakExecutor == null)
            ((ExecutorService) executor).shutdown();
        audioPlayerExecutor.shutdown();
        LOGGER.log(System.Logger.Level.DEBUG, "shutdown executors");

        return new SpeechState(futures, CompletableFuture.allOf(futures), parameters.size(), counter, cancel, player);
    }

    private record RunnerStage(int status, SpeechParameter parameter) {}

    /**
     * オーディオの再生をキューする
     */
    private CompletableFuture<Void> queuePlayAudio(AudioPlayer player, CompletableFuture<RunnerStage> voicePeakFuture, Executor executor) {
        return CompletableFuture.supplyAsync(() -> playAudio(player, voicePeakFuture), executor).thenAcceptAsync(file -> {
            if (file == null)
                return;
            try {
                Files.deleteIfExists(file);
                LOGGER.log(System.Logger.Level.DEBUG, "delete audio file " + file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(throwable -> {
            LOGGER.log(System.Logger.Level.WARNING, throwable);
            return null;
        });
    }

    /**
     * VOICEPEAKの実行Futureを受け取って成功しているときはオーディオを再生する
     */
    private Path playAudio(AudioPlayer player, CompletableFuture<RunnerStage> voicePeakFuture) {
        RunnerStage stage;
        try {
            stage = voicePeakFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (CompletionException | CancellationException e) {
            return null;
        }

        // 正常にVOICEPEAKのコマンドラインが終了しなかったとき
        if (stage.status() != 0) {
            return null;
        }

        // 読み上げのパラメータを取得
        var parameter = stage.parameter();

        // タスクがキャンセルされていた場合は終了
        if (voicePeakFuture.isCancelled()) {
            return parameter.audioFile();
        }

        // 初回の再生開始前の遅延
        // 音声ファイルの生成を先行して行うため
        if (parameter.index() == 0 && 0 < delayMilliSeconds && 1 < parameters.size()) {
            try {
                LOGGER.log(System.Logger.Level.DEBUG, "delay milliseconds: " + delayMilliSeconds);
                Thread.sleep(delayMilliSeconds);
            } catch (InterruptedException ignored) {
            }
        }

        LOGGER.log(System.Logger.Level.DEBUG, "play audio index: " + parameter.index());

        var file = parameter.audioFile();

        try {

            player.play(file);

            return file;

        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * VOICEPEAKコマンドラインの実行をキューする
     */
    private CompletableFuture<RunnerStage> queueProcess(SpeechParameter parameter, Executor executor, AtomicBoolean cancel) {
        return CompletableFuture.supplyAsync(() -> startProcess(parameter, cancel), executor);
    }

    /**
     * VOICEPEAKコマンドラインを実行する
     * 失敗時はnull
     */
    private RunnerStage startProcess(SpeechParameter parameter, AtomicBoolean cancel) {
        try {
            var process = parameter.process();
            var status = process.start().get();

            var level = (status != 0) ? System.Logger.Level.WARNING : System.Logger.Level.DEBUG;
            LOGGER.log(level, "done process. index: " + parameter.index() + ", status: " + status);

            // 読み上げがキャンセル要求されている場合は作成したファイルを削除する
            if (status == 0 && cancel.get()) {
                var file = parameter.audioFile();
                Files.delete(file);
                LOGGER.log(System.Logger.Level.DEBUG, "delete audio file " + file);
            }

            return new RunnerStage(status, parameter);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
