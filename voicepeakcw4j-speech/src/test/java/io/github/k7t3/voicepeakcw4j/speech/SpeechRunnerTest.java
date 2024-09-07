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

import io.github.k7t3.voicepeakcw4j.VPClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class SpeechRunnerTest {

    private SpeechParameter parameter;

    private Path temporalFilePath;

    @BeforeEach
    void setUp() throws IOException {
        temporalFilePath = Files.createTempFile(null, ".voicepeakcw4jspeech");

        var client = VPClient.create(new TestVPExecutable());
        var process = client.builder()
                .withSpeechText("not empty text") // ダミーテキスト
                .withOutput(temporalFilePath)
                .build();

        parameter = new SpeechParameter(0, process, temporalFilePath);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(temporalFilePath);
        } catch (IOException ignored) {
        }
    }

    @Test
    void test() {
        var device = AudioDevice.getDefaultDevice(); // オーディオデバイスはない場合もある
        var executor = Executors.newSingleThreadExecutor();
        // 音量最小で実質無音としたい
        var runner = new SpeechRunner(device, 0.0f, 0, executor, List.of(parameter));
        var state = runner.start();
        executor.shutdown();

        // パラメータは一つ
        assertEquals(1, state.getSentenceCount());

        var future = state.getSpeechFuture();

        // モックの音声ファイル生成処理にダミーの遅延処理があるため
        // まだ終了していないはず
        assertFalse(future.isDone());
        assertFalse(state.isDone());

        // 処理終了まで待機
        future.join();

        // 終了状態になっているはず
        assertTrue(future.isDone());
        assertTrue(state.isDone());
    }

    @Test
    void testUnsupportedAudio() {
        // 対応するオーディオデバイスがなくてもクラッシュしない
        AudioDevice device = null;
        var runner = new SpeechRunner(device, 0.0f, 0, null, List.of(parameter));
        var state = runner.start();
        var future = state.getSpeechFuture();
        future.join();
        assertTrue(state.isDone());
    }

    @Test
    void testEmptyParameters() {
        // パラメータが空のときは終了済みのはず
        var runner = new SpeechRunner(null, 0.0f, 0, null, List.of());
        var state = runner.start();
        var future = state.getSpeechFuture();
        future.join();
        assertTrue(state.isDone());
    }

    @Test
    void testCancel() {
        var runner = new SpeechRunner(null, 0.0f, 0, null, List.of(parameter));
        var state = runner.start();
        var future = state.getSpeechFuture();

        // 読み上げをキャンセルする
        state.requestStop();

        // 完了例外(Cause: CancellationException)がスローされるはず
        assertThrows(CompletionException.class, future::join);
    }
}