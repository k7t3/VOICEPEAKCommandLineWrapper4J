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

package com.github.k7t3.voicepeakcw4j.process;

import com.github.k7t3.voicepeakcw4j.MessageSubscriber;
import com.github.k7t3.voicepeakcw4j.Subscriber;
import com.github.k7t3.voicepeakcw4j.TestVPExecutable;
import com.github.k7t3.voicepeakcw4j.VPExecutable;
import com.github.k7t3.voicepeakcw4j.exception.VPExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VPProcessBuilderTest {

    private VPExecutable executable;

    @BeforeEach
    void setUp() {
        executable = new TestVPExecutable();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * 実行に必要な最小限のパラメータを持つビルダー
     */
    private VPProcessBuilder essentialBuilder() {
        return new VPProcessBuilder(executable)
                .withSpeechText("speech text");
    }

    /**
     * プロセスを同期実行して終了ステータスを返す
     */
    private int startProcess(VPProcess process) {
        try {
            return process.start().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void withNarrator() {
        // 想定しているナレーターはきりたんとずんだもんのみ

        var process = essentialBuilder()
                .withNarrator("Tohoku Kiritan")
                .build();
        var status = startProcess(process);
        assertEquals(0, status);
    }

    @Test
    void errorNarrator() {
        var errorProcess = essentialBuilder()
                .withNarrator("unexpected narrator")
                .build();
        var status = startProcess(errorProcess);
        assertEquals(1, status);
    }

    @Test
    void withSpeechText() {
        var process = essentialBuilder()
                .withSpeechText("hello")
                .build();
        var status = startProcess(process);
        assertEquals(0, status);
    }

    @Test
    void errorEmptySpeechText() {
        // 空の文字列はエラー
        var emptyError = essentialBuilder()
                .withSpeechText("");
        assertThrows(VPExecutionException.class, emptyError::build);
    }

    @Test
    void errorNullSpeechText() {
        // nullはエラー
        var nullError = essentialBuilder()
                .withSpeechText(null);
        assertThrows(VPExecutionException.class, nullError::build);
    }

    @Test
    void withSpeechFile() throws IOException {
        var speechFile = Files.createTempFile(null, null);
        try {
            Files.writeString(speechFile, "speech text is here");

            var process = new VPProcessBuilder(executable)
                    .withSpeechFile(speechFile)
                    .build();
            var status = startProcess(process);
            assertEquals(0, status);

            // ファイルを削除
            Files.delete(speechFile);

            // ファイルが存在しないときはエラー
            var noneFileError = new VPProcessBuilder(executable)
                    .withSpeechFile(speechFile);
            assertThrows(VPExecutionException.class, noneFileError::build);

        } finally {
            Files.deleteIfExists(speechFile);
        }
    }

    @Test
    void errorNoneSpeechFile() throws IOException {
        var speechFile = Files.createTempFile(null, null);
        Files.delete(speechFile);

        // ファイルが存在しないときはエラー
        var noneFileError = new VPProcessBuilder(executable)
                .withSpeechFile(speechFile);
        assertThrows(VPExecutionException.class, noneFileError::build);
    }

    @Test
    void withOutput() throws IOException {
        var tmpPath = Files.createTempFile(null, null);
        Files.delete(tmpPath); // 一時ファイルのパスが欲しいだけなので…
        try {

            var process = essentialBuilder()
                    .withOutput(tmpPath)
                    .build();
            var status = startProcess(process);
            assertEquals(0, status);
            assertTrue(Files.exists(tmpPath));

        } finally {
            Files.deleteIfExists(tmpPath);
        }
    }

    @Test
    void withPitch() {
        // ピッチオプションの範囲は-300 ~ 300

        int pitch = 0;

        var process = essentialBuilder()
                .withPitch(pitch)
                .build();

        var sub = new MessageSubscriber();
        process.getStandardOut().subscribe(sub);

        var status = startProcess(process);
        assertEquals(0, status);

        var result = sub.getOutputs()
                .stream()
                .filter(s -> s.startsWith("Pitch: "))
                .findAny()
                .orElseThrow();
        assertTrue(result.endsWith(Integer.toString(pitch)));
    }

    @Test
    void errorPitch() {
        // 範囲外
        var pitch = -301;

        var errorBuilder = essentialBuilder()
                .withPitch(pitch);

        assertThrows(VPExecutionException.class, errorBuilder::build);
    }

    @Test
    void withSpeed() {
        // スピードオプションの範囲は50 ~ 200

        int speed = 50;

        var process = essentialBuilder()
                .withSpeed(speed)
                .build();

        var sub = new MessageSubscriber();
        process.getStandardOut().subscribe(sub);

        var status = startProcess(process);
        assertEquals(0, status);

        var result = sub.getOutputs()
                .stream()
                .filter(s -> s.startsWith("Speed: "))
                .findAny()
                .orElseThrow();
        assertTrue(result.endsWith(Integer.toString(speed)));
    }

    @Test
    void errorSpeed() {
        // 範囲外
        var speed = 0;

        var errorBuilder = essentialBuilder()
                .withSpeed(speed);

        assertThrows(VPExecutionException.class, errorBuilder::build);
    }

    @Test
    void withEmotion() {
        var kiritanEmotions = Map.of(
                "bright", 100,
                "angry", 100
        );
        var joinedEmotions = kiritanEmotions.entrySet()
                .stream()
                .map(e -> "%s,%d".formatted(e.getKey(), e.getValue()))
                .sorted()
                .collect(Collectors.joining(","));

        var process = essentialBuilder()
                .withNarrator("Tohoku Kiritan")
                .withEmotion(kiritanEmotions)
                .build();

        var sub = new MessageSubscriber();
        process.getStandardOut().subscribe(sub);
        process.getErrorOut().subscribe(Subscriber.of(System.err::println));

        var status = startProcess(process);
        assertEquals(0, status);

        var emotionValue = sub.getOutputs()
                .stream()
                .filter(s -> s.startsWith("Emotion: "))
                .findAny()
                .orElseThrow();
        assertTrue(emotionValue.endsWith(joinedEmotions));
    }

    @Test
    void errorEmotion() {
        var kiritanEmotions = Map.of(
                "qaws", 100,
                "wasd", 100
        );

        var process = essentialBuilder()
                .withNarrator("Tohoku Kiritan")
                .withEmotion(kiritanEmotions)
                .build();

        var status = startProcess(process);
        assertNotEquals(0, status);
    }
}