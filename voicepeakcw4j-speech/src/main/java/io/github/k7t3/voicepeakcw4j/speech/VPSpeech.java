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
import io.github.k7t3.voicepeakcw4j.VPExecutable;

/**
 * VOICEPEAKの音声合成機能を使用してテキストを読み上げるためのクラス
 */
public interface VPSpeech extends VPClient {

    /**
     * テキストを読み上げるためのビルダーを返す
     * @return テキストの読み上げビルダー
     */
    SpeechBuilder speech();

    /**
     * 既定のスピーチクライアントを生成する。
     * @param executable VOICEPEAK実行ファイル
     * @return クライアント
     */
    static VPSpeech create(VPExecutable executable) {
        return new DefaultVPSpeech(executable);
    }

}
