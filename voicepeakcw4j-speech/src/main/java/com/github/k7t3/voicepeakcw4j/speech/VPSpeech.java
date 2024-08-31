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

import com.github.k7t3.voicepeakcw4j.VPExecutable;
import com.github.k7t3.voicepeakcw4j.exception.VPExecutionException;

import java.util.List;

public interface VPSpeech {

    /**
     * テキストを読み上げるためのビルダーを返す
     * @return テキストの読み上げビルダー
     */
    SpeechBuilder builder();

    /**
     * ナレーターに対応する感情の一覧を取得する
     * @param narrator ナレーター
     * @return ナレーターに対応する感情のリスト
     * @throws VPExecutionException VOICEPEAKのコマンドライン実行に失敗
     */
    List<String> getEmotions(String narrator);

    /**
     * インストールされているナレーターの一覧を返す
     * @return インストールされているナレーターの一覧
     * @throws VPExecutionException VOICEPEAKのコマンドライン実行に失敗
     */
    List<String> getNarrators();

    /**
     * 既定のスピーチクライアントを生成する。
     * @param executable VOICEPEAK実行ファイル
     * @return クライアント
     */
    static VPSpeech create(VPExecutable executable) {
        return new DefaultVPSpeech(executable);
    }

}
