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

import io.github.k7t3.voicepeakcw4j.VPExecutable;
import io.github.k7t3.voicepeakcw4j.process.VPProcess;
import io.github.k7t3.voicepeakcw4j.option.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 音声を即時再生するスピーチランナーを生成するビルダー
 * <p>
 *     現在のVOICEPEAKのバージョン(v1.2.11)は、コマンドライン実行で扱える文字数の上限を140としており、
 *     それを超過する文字列を渡した場合は以下のエラーメッセージがエラー出力に排出される。
 *     <code>In this version, the character limit for a single run is 140 characters, but the input string contains XXX characters.</code>
 * </p>
 */
public class SpeechBuilder {

    private static final System.Logger LOGGER = System.getLogger(SpeechBuilder.class.getName());

    private static final String DEFAULT_TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static final int DEFAULT_MAX_SENTENCE_LENGTH = 140; // VOICEPEAKのデフォルト
    private static final float DEFAULT_VOLUME_RATE = 0.5f;

    private final VPExecutable executable;

    private String narrator;
    private String speechText;

    private int pitch = Integer.MIN_VALUE;
    private int speed = Integer.MIN_VALUE;
    private Map<String, Integer> emotion;

    private Path temporalDirectory = Paths.get(DEFAULT_TEMP_DIRECTORY);

    private Locale splitterLocale = Locale.getDefault();
    private int maxSentenceLength = Integer.MIN_VALUE;

    private long delayMilliSeconds = Long.MIN_VALUE;

    private AudioDevice audioDevice;
    private float volumeRate = DEFAULT_VOLUME_RATE;

    private Executor voicepeakExecutor;

    /**
     * 引数の実行ファイルを使用したスピーチランナーインスタンスを作成する
     * @param executable VOICEPEAKの実行ファイル
     */
    SpeechBuilder(VPExecutable executable) {
        this.executable = executable;
    }

    /**
     * 読み上げるナレータを設定する。
     * <p>
     *     非対応のナレーターを設定した場合はVOICEPEAKの実行が正常に終了せず、
     *     エラー出力にエラーメッセージが排出される。
     * </p>
     * @param narrator ナレーター
     * @return このインスタンス
     */
    public SpeechBuilder withNarrator(String narrator) {
        this.narrator = narrator;
        return this;
    }

    /**
     * 読み上げるテキストを設定する。
     * <p>
     *     VOICEPEAKのバージョン1.2.11において、コマンドライン実装の仕様上、
     *     一度に処理できる文字列は140文字までである。
     *     テキストが長い場合は{@link #withMaxSentenceLength(int)}で設定したサイズを
     *     上限として句読点に基づき分割し、分割した文字列を繰り返し読み上げる。
     * </p>
     * @param speechText 読み上げるテキスト
     * @return このインスタンス
     * @see SpeechBuilder#withSplitterLocale(Locale) 文字列の分割時に解釈する言語
     * @see SpeechBuilder#withMaxSentenceLength(int) 分割する文字列の最大長
     */
    public SpeechBuilder withSpeechText(String speechText) {
        this.speechText = speechText;
        return this;
    }

    /**
     * 読み上げ時のピッチを設定する。
     * <p>
     *     サポートされている範囲外の値を設定すると{@link #build()}時に例外がスローされる。
     * </p>
     * @param pitch -300 .. 300
     * @return このインスタンス
     */
    public SpeechBuilder withPitch(int pitch) {
        this.pitch = pitch;
        return this;
    }

    /**
     * 読み上げ時のスピードを設定する。
     * <p>
     *     サポート範囲外の値を設定すると{@link #build()}時に例外がスローされる。
     * </p>
     * @param speed 50 .. 200
     * @return このインスタンス
     */
    public SpeechBuilder withSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    /**
     * 感情パラメータを設定する。
     * <p>
     *     対応する感情はナレーターごとに異なるため、事前に確認しておくこと。
     * </p>
     * <p>
     *     サポート範囲外の値を設定すると{@link #build()}時に例外がスローされる。
     * </p>
     * @param emotion 感情名=設定値 (設定値: 0 .. 100)
     * @return このインスタンス
     */
    public SpeechBuilder withEmotion(Map<String, Integer> emotion) {
        this.emotion = emotion;
        return this;
    }

    /**
     * 読み上げる一時音声ファイルを生成するディレクトリを設定する。
     * <p>
     *     規定値は<code>java.io.tmpdir</code>プロパティで取得できるディレクトリ
     * </p>
     * @param temporalDirectory 読み上げる一時音声ファイルを生成するディレクトリ
     * @return このインスタンス
     */
    public SpeechBuilder withTemporalDirectory(Path temporalDirectory) {
        this.temporalDirectory = temporalDirectory;
        return this;
    }

    /**
     * 読み上げる文字列を分割するときに使用する言語を設定する。
     * <p>
     *     規定値はシステムのロケール
     * </p>
     * @param splitterLocale 言語
     * @return このインスタンス
     * @see SpeechBuilder#withSpeechText(String) 分割される文字列の設定
     * @see SpeechBuilder#withMaxSentenceLength(int) 分割する最大長
     */
    public SpeechBuilder withSplitterLocale(Locale splitterLocale) {
        this.splitterLocale = splitterLocale;
        return this;
    }

    /**
     * 読み上げる文字列を分割するときに使用する最大長を設定する。
     * <p>
     *     規定値はコマンドライン実装に基づき140
     * </p>
     * @param maxSentenceLength 文字列を分割する最大長
     * @return このインスタンス
     * @see SpeechBuilder#withSpeechText(String) 分割される文字列の設定
     * @see SpeechBuilder#withSplitterLocale(Locale) 分割時に使用する言語
     */
    public SpeechBuilder withMaxSentenceLength(int maxSentenceLength) {
        this.maxSentenceLength = maxSentenceLength;
        return this;
    }

    /**
     * 音声を再生するデバイスを設定する。
     * <p>
     *     デフォルト値は既定のデバイス
     * </p>
     * @param audioDevice 音声を再生するデバイス
     * @return このインスタンス
     */
    public SpeechBuilder withAudioDevice(AudioDevice audioDevice) {
        this.audioDevice = audioDevice;
        return this;
    }

    /**
     * 読み上げる音声のゲインの割合を設定する。
     * @param volumeRate 0.0 ~ 1.0
     * @return このインスタンス
     */
    public SpeechBuilder withVolumeRate(float volumeRate) {
        this.volumeRate = volumeRate;
        return this;
    }

    /**
     * 読み上げる文字列が複数回に分割されるときに使用する遅延時間を設定する。
     * <p>
     *     読み上げる文字数が上限である140を超える場合、複数回コマンドラインが実行されるため、
     *     即座に読み上げを開始すると次に続く音声の生成が間に合わず、空白時間が発生する
     *     可能性がある。できるだけ滑らかに読み上げるために指定時間遅延させることができる。
     * </p>
     * <p>
     *     メソッド{@link SpeechBuilder#withDelayTime(TimeUnit, long)}とは排他的。
     * </p>
     * <p>
     *     規定値は 0
     * </p>
     * @param delayMilliSeconds 遅延させるミリ秒
     * @return このインスタンス
     * @see SpeechBuilder#withDelayTime(TimeUnit, long)
     */
    public SpeechBuilder withDelayMilliSeconds(long delayMilliSeconds) {
        this.delayMilliSeconds = delayMilliSeconds;
        return this;
    }

    /**
     * 読み上げる文字列が複数回に分割されるときに使用する遅延時間を設定する。
     * <p>
     *     読み上げる文字数が上限である140を超える場合、複数回コマンドラインが実行されるため、
     *     即座に読み上げを開始すると次に続く音声の生成が間に合わず、空白時間が発生する
     *     可能性がある。できるだけ滑らかに読み上げるために指定時間遅延させることができる。
     * </p>
     * <p>
     *     メソッド{@link SpeechBuilder#withDelayMilliSeconds(long)}とは排他的。
     * </p>
     * @param unit 時間単位
     * @param amount 遅延量
     * @return このインスタンス
     * @see SpeechBuilder#withDelayMilliSeconds(long)
     */
    public SpeechBuilder withDelayTime(TimeUnit unit, long amount) {
        this.delayMilliSeconds = unit.toMillis(amount);
        return this;
    }

    /**
     * VOICEPEAKコマンドを実行するExecutorを設定する
     * <p>
     *     VOICEPEAK v1.2.11現在、コマンドライン実行は並列実行が許可されていないため、
     *     複数スレッドで動作させることを目的として{@link Executor}を設定するべきではない。
     * </p>
     * <p>
     *     むしろVOICEPEAKの並列実行を制限するために、
     *     シングルスレッドのExecutorを割り当てるために使用する。
     * </p>
     * @param executor Executor
     * @return このインスタンス
     */
    public SpeechBuilder withVoicePeakExecutor(Executor executor) {
        this.voicepeakExecutor = executor;
        return this;
    }

    private boolean isNullSpeechText() {
        return speechText == null || speechText.trim().isEmpty();
    }

    private String generateTemporalFileName() {
        return UUID.randomUUID() + ".vpcw4j";
    }

    private Path determineOutputPath() throws IOException {
        if (temporalDirectory == null) {
            temporalDirectory = Paths.get(DEFAULT_TEMP_DIRECTORY);
            return temporalDirectory.resolve(generateTemporalFileName());
        }
        if (!Files.exists(temporalDirectory)) {
            Files.createDirectories(temporalDirectory);
            return temporalDirectory.resolve(generateTemporalFileName());
        }
        if (!Files.isDirectory(temporalDirectory)) {
            throw new IllegalStateException("temporal directory is not directory. " + temporalDirectory);
        }
        return temporalDirectory.resolve(generateTemporalFileName());
    }

    /**
     * 設定値にもとづいて読み上げインスタンスを生成する。
     * <p>
     *     設定値に違反がある場合、{@link IllegalStateException}がスローされる。
     * </p>
     * @return 読み上げインスタンス
     * @throws IllegalStateException 適切なパラメータが設定されていないとき
     */
    public SpeechRunner build() {
        if (isNullSpeechText()) {
            throw new IllegalStateException("require speech text or file");
        }

        if (maxSentenceLength < 4) {
            maxSentenceLength = DEFAULT_MAX_SENTENCE_LENGTH;
        }

        // VOICEPEAKのコマンドライン実装で処理できるようにテキストを分割
        var sentenceSplitter = new SentenceSplitter(splitterLocale);
        var sentences = sentenceSplitter.splitByWord(speechText, maxSentenceLength);
        LOGGER.log(System.Logger.Level.DEBUG, "split sentence count = " + sentences.size());

        // デバッグログが有効な場合は読み上げるテキストを記録する
        if (LOGGER.isLoggable(System.Logger.Level.DEBUG)) {
            sentences.forEach(s -> LOGGER.log(System.Logger.Level.DEBUG, s));
        }

        if (audioDevice == null) {
            audioDevice = AudioDevice.getDefaultDevice();
        }

        var volumeRate = Math.clamp(this.volumeRate, 0.0f, 1.0f);
        var delayMilliSeconds = Math.max(0, this.delayMilliSeconds);

        // 読み上げに使用するパラメータを生成
        var parameters = new ArrayList<SpeechParameter>(sentences.size());
        for (int i = 0; i < sentences.size(); i++) {
            parameters.add(createParameter(i, sentences.get(i)));
        }
        return new SpeechRunner(audioDevice, volumeRate, delayMilliSeconds, voicepeakExecutor, parameters);
    }

    private SpeechParameter createParameter(int index, String sentence) {
        if (isNullSpeechText()) {
            throw new IllegalStateException("require speech text or file");
        }

        var commands = new ArrayList<String>();
        executable.fill(commands);

        new SpeechTextOption(sentence).fill(commands);

        if (narrator != null) {
            new NarratorOption(this.narrator).fill(commands);
        }

        if (pitch != Integer.MIN_VALUE) {
            try {
                new PitchOption(pitch).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
        }

        if (speed != Integer.MIN_VALUE) {
            try {
                new SpeedOption(speed).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
        }

        if (emotion != null) {
            try {
                new EmotionOption(emotion).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
        }

        Path outputPath;
        try {
            outputPath = determineOutputPath();
            new OutputFileOption(outputPath).fill(commands);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        var process = new VPProcess(new ProcessBuilder(commands));
        return new SpeechParameter(index, process, outputPath);
    }

}
