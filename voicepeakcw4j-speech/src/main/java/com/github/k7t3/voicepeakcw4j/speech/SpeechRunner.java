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

package com.github.k7t3.voicepeakcw4j.speech;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

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
 *     処理速度について、多くの環境において音声の再生速度＜音声の生成速度であることから、
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

    private final float volumeRate;

    private final long delayMilliSeconds;

    SpeechRunner(float volumeRate, long delayMilliSeconds, List<SpeechParameter> parameters) {
        if (parameters == null || parameters.isEmpty())
            throw new IllegalArgumentException();
        this.volumeRate = volumeRate;
        this.delayMilliSeconds = delayMilliSeconds;
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
     */
    public void run() {
        LOGGER.log(System.Logger.Level.INFO, "runner started");

        // 現在のVOICEPEAKのバージョン(v.1.2.11)はコマンドラインの並列実行を許可していない
        // エラー出力
        // In this version, up to 1 command line instance can be executed at same time.
        var voicePeakExecutor = createExecutor("VOICEPEAKCommandLineWrapper");
        var audioPlayerExecutor = createExecutor("AudioPlayer");
        LOGGER.log(System.Logger.Level.DEBUG, "initialized executors");

        var player = new AudioPlayer();
        player.requestVolume(volumeRate);

        for (var parameter : parameters) {
            var process = parameter.process();

            if (standardOutSubscriber != null) {
                process.getStandardOut().subscribe(standardOutSubscriber);
            }
            if (errorOutSubscriber != null) {
                process.getErrorOut().subscribe(errorOutSubscriber);
            }

            var voicePeakFuture = queueProcess(parameter, voicePeakExecutor);
            LOGGER.log(System.Logger.Level.DEBUG, "queued VOICEPEAK process");

            queuePlayAudio(player, voicePeakFuture, audioPlayerExecutor);
            LOGGER.log(System.Logger.Level.DEBUG, "queued play audio request");
        }

        // オーディオプレイヤーの終了処理
        audioPlayerExecutor.submit(player::close);

        voicePeakExecutor.shutdown();
        audioPlayerExecutor.shutdown();
        LOGGER.log(System.Logger.Level.DEBUG, "shutdown executors");
    }

    private record RunnerStage(int status, SpeechParameter parameter) {}

    /**
     * オーディオの再生をキューする
     */
    private void queuePlayAudio(AudioPlayer player, CompletableFuture<RunnerStage> voicePeakFuture, Executor executor) {
        CompletableFuture.supplyAsync(() -> playAudio(player, voicePeakFuture), executor).thenAcceptAsync(file -> {
            if (file == null)
                return;
            try {
                Files.delete(file);
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
        }

        // 正常にVOICEPEAKのコマンドラインが終了しなかったとき
        if (stage.status() != 0) {
            return null;
        }

        var parameter = stage.parameter();

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
    private CompletableFuture<RunnerStage> queueProcess(SpeechParameter parameter, Executor executor) {
        return CompletableFuture.supplyAsync(() -> startProcess(parameter), executor).exceptionally(throwable -> {
            LOGGER.log(System.Logger.Level.WARNING, throwable);
            return new RunnerStage(-1, parameter);
        });
    }

    /**
     * VOICEPEAKコマンドラインを実行する
     * 失敗時はnull
     */
    private RunnerStage startProcess(SpeechParameter parameter) {
        try {
            var process = parameter.process();
            var status = process.start().get();

            var level = (status != 0) ? System.Logger.Level.WARNING : System.Logger.Level.INFO;
            LOGGER.log(level, "done process. index: " + parameter.index() + ", status: " + status);

            return new RunnerStage(status, parameter);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
