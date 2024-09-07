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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 読み上げの状態に関する情報を提供するクラス
 */
public class SpeechState {

    private final CompletableFuture<?>[] allFutures;

    private final CompletableFuture<Void> speechFuture;

    private final int sentenceCount;

    private final AtomicInteger currentPosition;

    private final AtomicBoolean cancel;

    private final AudioPlayer player;

    SpeechState(
            CompletableFuture<?>[] allFutures,
            CompletableFuture<Void> speechFuture,
            int sentenceCount,
            AtomicInteger currentPosition,
            AtomicBoolean cancel,
            AudioPlayer player
    ) {
        this.allFutures = allFutures;
        this.speechFuture = speechFuture;
        this.sentenceCount = sentenceCount;
        this.currentPosition = currentPosition;
        this.cancel = cancel;
        this.player = player;
    }

    /**
     * 読み上げタスクのFutureを返す
     * @return 読み上げたスクのFuture
     */
    public CompletableFuture<Void> getSpeechFuture() {
        return speechFuture;
    }

    /**
     * 読み上げる文章の数を返す
     * <p>
     *     「読み上げる文章」の定義は、VOICEPEAKが処理できる文字数に
     *     準拠して分割した文字列のことを示す。
     * </p>
     * @return 読み上げる文章の数
     */
    public int getSentenceCount() {
        return sentenceCount;
    }

    /**
     * 現在読み上げている文章の位置を返す
     * <p>
     *     「読み上げる文章」の定義は、VOICEPEAKが処理できる文字数に
     *     準拠して分割した文字列のことを示す。
     * </p>
     * @return 現在読み上げている文章の位置
     */
    public int getCurrentPosition() {
        return currentPosition.get();
    }

    /**
     * 音量が指定の割合になるようリクエストする
     * @param volumeRate 音量の割合 0.0f ~ 2.0f
     */
    public void requestVolume(float volumeRate) {
        if (player != null) {
            player.requestVolume(volumeRate);
        }
    }

    /**
     * 読み上げを停止するようリクエストする
     */
    public void requestStop() {
        if (player != null) {
            cancel.set(true);
            player.requestCancel();
            for (var future : allFutures)
                future.cancel(false);
            speechFuture.cancel(false);
        }
    }

    /**
     * 読み上げが完了しているかを返す
     * @return 読み上げが完了しているときはtrue
     */
    public boolean isDone() {
        return currentPosition.get() == sentenceCount;
    }

}
