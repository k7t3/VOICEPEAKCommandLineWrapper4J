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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * VOICEPEAKで生成したオーディオファイルを再生できるデバイス
 */
public class AudioDevice {

    /**
     * VOICEPEAKのコマンドライン実行で生成されるWAVEフォーマット
     */
    private static final AudioFormat VOICEPEAK_DEFAULT_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            48000,
            16,
            1,
            2,
            48000,
            false
    );

    private final Mixer mixer;

    private final Mixer.Info info;

    AudioDevice(Mixer mixer) {
        this.mixer = mixer;
        this.info = mixer.getMixerInfo();
    }

    /**
     * オーディオデバイスの名称を返す
     * @return オーディオデバイスの名称
     */
    public String getName() {
        return info.getName();
    }

    /**
     * オーディオデバイスの概要を返す
     * @return オーディオデバイスの概要
     */
    public String getDescription() {
        return info.getDescription();
    }

    /**
     * オーディオデバイスのベンダーを返す
     * @return オーディオデバイスのベンダー
     */
    public String getVendor() {
        return info.getVendor();
    }

    /**
     * オーディオデバイスのバージョンを返す
     * @return オーディオデバイスのバージョン
     */
    public String getVersion() {
        return info.getVersion();
    }

    SourceDataLine getSourceDataLine() {
        var info = new DataLine.Info(SourceDataLine.class, VOICEPEAK_DEFAULT_FORMAT);
        try {
            return (SourceDataLine) mixer.getLine(info);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 既定のデバイスを返す
     * <p>
     *     既定のデバイスが読み上げ音声を再生できないときはnullを返す
     * </p>
     * @return 既定のデバイス、あるいはそのデバイスが再生できないときはnull
     */
    public static AudioDevice getDefaultDevice() {
        try {
            var sourceDataLineInfo = new DataLine.Info(SourceDataLine.class, VOICEPEAK_DEFAULT_FORMAT);
            var mixer = AudioSystem.getMixer(null);
            if (mixer.isLineSupported(sourceDataLineInfo)) {
                return new AudioDevice(mixer);
            }
        } catch (IllegalArgumentException ignored) {
            // 対応するミキサーがないときにIllegalArgumentExceptionがスローされるため無視する
            return null;
        }
        return null;
    }

    /**
     * 読み上げ音声を再生可能なデバイスのリストを返す
     * @return 読み上げ音声を再生可能なデバイスのリスト
     */
    public static List<AudioDevice> getAvailableDevices() {
        var sourceDataLineInfo = new DataLine.Info(SourceDataLine.class, VOICEPEAK_DEFAULT_FORMAT);
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .filter(m -> m.isLineSupported(sourceDataLineInfo))
                .map(AudioDevice::new)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioDevice that)) return false;
        var info = mixer.getMixerInfo();
        var oi = that.mixer.getMixerInfo();
        return Objects.equals(info.getName(), oi.getName())
                && Objects.equals(info.getDescription(), oi.getDescription())
                && Objects.equals(info.getVendor(), oi.getVendor())
                && Objects.equals(info.getVersion(), oi.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mixer);
    }

    @Override
    public String toString() {
        return mixer.getMixerInfo().toString();
    }
}
