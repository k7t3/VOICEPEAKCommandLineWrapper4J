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

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同じタイプの音声ファイルを繰り返し再生できるオーディオプレイヤー
 * <p>並列実行はサポートされない</p>
 */
class AudioPlayer implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(AudioPlayer.class.getName());

    private final ArrayBlockingQueue<Float> volumeQueue = new ArrayBlockingQueue<>(32);

    private final AtomicBoolean cancelRequest = new AtomicBoolean(false);

    private volatile boolean closed = false;
    private volatile boolean initialized = false;

    private SourceDataLine line;
    private FloatControl volumeControl;

    public AudioPlayer() {
    }

    /**
     * 0.0 ~ 1.0
     *
     * @param volumeRate ボリュームの比率
     */
    public void requestVolume(float volumeRate) {
        if (volumeRate < 0.0f || 1.0f < volumeRate)
            throw new IllegalArgumentException("volume rate is out of range.");
        volumeQueue.offer(volumeRate);
    }

    public void requestCancel() {
        cancelRequest.set(true);
    }

    private void initialize(AudioFormat format) {
        try {

            initialized = true;

            // オーディオフォーマットに対応したラインを取得
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            // ラインが対応している場合はそのコントロールを使用
            // ラインを開いてからでないと取得できない
            try {
                volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                // 初期ボリューム設定
                setVolumeIfRequested(volumeControl);
            } catch (IllegalArgumentException ignored) {
                // ゲインが非対応
                LOGGER.log(System.Logger.Level.WARNING, "SourceDataLine is not support MASTER_GAIN");
            }

        } catch (LineUnavailableException e) {
            LOGGER.log(System.Logger.Level.ERROR, "audio format is not supported " + format.toString());
            close();
        }
    }

    private void setVolumeIfRequested(FloatControl volumeControl) {
        if (volumeControl == null) return;

        var volumeRate = volumeQueue.poll();
        if (volumeRate == null) return;

        var decibel = (volumeRate <= 0.0f) ? -80.0f : 20 * (float) Math.log10(volumeRate);
        volumeControl.setValue(decibel);
        LOGGER.log(System.Logger.Level.INFO, "set decibel " + decibel);
    }

    public void play(Path audioFile) throws IOException, UnsupportedAudioFileException {
        if (closed) {
            LOGGER.log(System.Logger.Level.WARNING, "player is already closed");
            return;
        }

        try (var input = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(audioFile)))) {

            if (!initialized) {
                var audioFormat = input.getFormat();
                initialize(audioFormat);
            }

            // オーディオを読み込んでラインに書き込む
            var buffer = new byte[1024 * 4];
            int read;
            while (0 <= (read = input.read(buffer))) {

                if (closed) {
                    break;
                }

                // ラインに書き込む
                // 消費に対して書き込みが追い付かない場合は無音になるのでバッファサイズを調整する
                line.write(buffer, 0, read);

                // ボリューム変更チェック
                setVolumeIfRequested(volumeControl);

                if (cancelRequest.get()) {
                    close();
                    break;
                }
            }
        }
    }

    @Override
    public void close() {
        if (!initialized || closed)
            return;

        closed = true;
        line.drain();
        line.close();
        volumeControl = null;
        line = null;
    }
}