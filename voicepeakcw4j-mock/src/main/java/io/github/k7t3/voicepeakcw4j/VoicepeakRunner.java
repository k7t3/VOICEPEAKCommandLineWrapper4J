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

package io.github.k7t3.voicepeakcw4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VOICEPEAK v1.2.11
 */
@SuppressWarnings("UnusedReturnValue")
class VoicepeakRunner {

    private String narrator;

    private String speechText;
    private Path speechFile;

    private Path output;
    private int pitch = Integer.MIN_VALUE;
    private int speed = Integer.MIN_VALUE;
    private Map<String, Integer> emotion;

    public VoicepeakRunner() {
    }

    public VoicepeakRunner withNarrator(String narrator) {
        this.narrator = narrator;
        return this;
    }

    public VoicepeakRunner withSpeechText(String speechText) {
        this.speechText = speechText;
        return this;
    }

    public VoicepeakRunner withSpeechFile(Path file) {
        this.speechFile = file;
        return this;
    }

    public VoicepeakRunner withOutput(Path output) {
        this.output = output;
        return this;
    }

    public VoicepeakRunner withPitch(int pitch) {
        this.pitch = pitch;
        return this;
    }

    public VoicepeakRunner withSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public VoicepeakRunner withEmotion(Map<String, Integer> emotion) {
        this.emotion = emotion;
        return this;
    }

    /**
     * 対応するナレーターが存在しないとき、あるいはナレーターに対応する感情が指定されたときに出力されるエラー
     */
    private void errorPrint() {
        System.err.println("Internal BUG occurred. Please report what you are doing and these details to us.");
        System.err.println("===== BEGIN BUG REPORT =====");
        System.err.println("bad exception");
        System.err.println("===== END BUG REPORT =====");
    }

    public void run() {
        if (speechText == null && speechFile == null) {
            System.err.println("Please specify text to say");
            System.exit(1);
            return;
        }
        if (speechFile != null) {
            if (!Files.exists(speechFile)) {
                System.exit(1);
                return;
            }
        }


        if (narrator != null) {
            // 東北きりたんとずんだもんのみサポート
            if (!narrator.equals("Tohoku Kiritan") && !narrator.equals("Zundamon")) {
                errorPrint();
                System.exit(1);
            }
            System.out.println("Narrator: " + narrator);
        }

        if (speechText != null) {
            System.out.println("Speech Text: " + speechText);
        }

        if (speechFile != null) {
            System.out.println("Speech File: " + speechFile.toAbsolutePath());
        }

        var o = output;
        if (o == null) {
            // 既定の出力場所はホームディレクトリのoutput.wav
            o = Path.of(System.getProperty("user.home"), "output.wav");
        }
        {
            try {
                // 生成完了までの遅延として遅延をエミュレート
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("failed to sleep");
            }
            try {
                // テスト音声を出力ファイルとしてコピーする
                try (var input = getClass().getResourceAsStream("/test.wav")) {
                    Files.copy(Objects.requireNonNull(input), o, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                System.err.println("failed to output");
            }
            System.out.println("Output: " + o.toAbsolutePath());
        }

        if (pitch != Integer.MIN_VALUE) {
            //      --pitch Value            Pitch (-300 - 300)
            // 範囲外でも問題ないっぽい
            System.out.println("Pitch: " + pitch);
        }

        if (speed != Integer.MIN_VALUE) {
            //       --speed Value            Speed (50 - 200)
            // 範囲外でも問題ないっぽい
            System.out.println("Speed: " + speed);
        }

        if (emotion != null) {
            // 非対応の感情を指定するとエラーになる
            var values = emotion.entrySet()
                    .stream()
                    .map(e -> "%s,%d".formatted(e.getKey(), e.getValue()))
                    .sorted()
                    .collect(Collectors.joining(","));

            var emotions = NarratorEmotions.get(narrator);
            var containsUnsupported = emotion.keySet().stream().noneMatch(emotions::contains);
            if (containsUnsupported) {
                errorPrint();
                System.exit(1);
            }

            System.out.println("Emotion: " + values);
        }
    }

}
