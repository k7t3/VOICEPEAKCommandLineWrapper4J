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

package io.github.k7t3.voicepeakcw4j.process;

import io.github.k7t3.voicepeakcw4j.VPExecutable;
import io.github.k7t3.voicepeakcw4j.exception.VPExecutionException;
import io.github.k7t3.voicepeakcw4j.option.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

/**
 * VOICEPEAKをコマンドライン実行するためのビルダー
 * <p>
 *     現在のVOICEPEAKのバージョン(v1.2.11)は、コマンドライン実行で扱える文字数の上限を140としており、
 *     それを超過する文字列を渡した場合は以下のエラーメッセージがエラー出力に排出される。
 *     <code>In this version, the character limit for a single run is 140 characters, but the input string contains XXX characters.</code>
 * </p>
 * <p>以下のオプションは使用できない</p>
 * <ul>
 *     <li>--list-emotion</li>
 *     <li>--list-narrator</li>
 * </ul>
 */
public class VPProcessBuilder {

    private final VPExecutable executable;

    private String narrator;

    private String speechText;
    private Path speechFile;

    private Path output;
    private int pitch = Integer.MIN_VALUE;
    private int speed = Integer.MIN_VALUE;
    private Map<String, Integer> emotion;

    /**
     * コンストラクタ
     * @param executable 実行するファイル
     */
    public VPProcessBuilder(VPExecutable executable) {
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
    public VPProcessBuilder withNarrator(String narrator) {
        this.narrator = narrator;
        return this;
    }

    /**
     * 読み上げるテキストを設定する。
     * <p>
     *     VOICEPEAKのバージョン1.2.11において、コマンドライン実装の仕様上、
     *     一度に処理できる文字列は140文字までである。
     * </p>
     * @param speechText 読み上げるテキスト
     * @return このインスタンス
     */
    public VPProcessBuilder withSpeechText(String speechText) {
        this.speechText = speechText;
        return this;
    }

    /**
     * 読み上げるテキストが記述されるテキストファイルを設定する。
     * <p>
     *     VOICEPEAKのバージョン1.2.11において、コマンドライン実装の仕様上、
     *     一度に処理できる文字列は140文字までである。
     * </p>
     * @param file テキストファイル
     * @return このインスタンス
     */
    public VPProcessBuilder withSpeechFile(Path file) {
        this.speechFile = file;
        return this;
    }

    /**
     * 出力するファイルパスを設定する。
     * <p>
     *     明示しないときはカレントディレクトリに<code>output.wav</code>として出力される。
     * </p>
     * @param output 出力するパス
     * @return このインスタンス
     */
    public VPProcessBuilder withOutput(Path output) {
        this.output = output;
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
    public VPProcessBuilder withPitch(int pitch) {
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
    public VPProcessBuilder withSpeed(int speed) {
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
    public VPProcessBuilder withEmotion(Map<String, Integer> emotion) {
        this.emotion = emotion;
        return this;
    }

    private boolean isNullSpeechText() {
        return speechText == null || speechText.trim().isEmpty();
    }

    /**
     * パラメータに応じてプロセスを生成する
     * @return 生成したプロセス
     * @throws VPExecutionException コマンドラインの実行に必要な条件を満たしていないとき
     */
    public VPProcess build() {
        if (isNullSpeechText() && speechFile == null) {
            throw new VPExecutionException("require speech text or file");
        }
        if (!isNullSpeechText() && speechFile != null) {
            // 二つとも指定した場合はエラーにならずにテキストが優先される模様
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                    "If both the --say and --text options are specified, the --say option takes precedence.");
        }
        if (speechFile != null) {
            if (!Files.exists(speechFile)) {
                throw new VPExecutionException("not exist speech file");
            }
        }

        var commands = new ArrayList<String>();
        executable.fill(commands);

        if (narrator != null) {
            new NarratorOption(this.narrator).fill(commands);
        }

        if (!isNullSpeechText()) {
            new SpeechTextOption(speechText).fill(commands);
        }

        if (speechFile != null) {
            new SpeechFileOption(speechFile).fill(commands);
        }

        if (output != null) {
            new OutputFileOption(output).fill(commands);
        }

        if (pitch != Integer.MIN_VALUE) {
            try {
                new PitchOption(pitch).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new VPExecutionException(e);
            }
        }

        if (speed != Integer.MIN_VALUE) {
            try {
                new SpeedOption(speed).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new VPExecutionException(e);
            }
        }

        if (emotion != null && !emotion.isEmpty()) {
            try {
                new EmotionOption(emotion).fill(commands);
            } catch (IllegalArgumentException e) {
                throw new VPExecutionException(e);
            }
        }

        return new VPProcess(new ProcessBuilder(commands));
    }

}
