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

package io.github.k7t3.voicepeakcw4j.exception;

/**
 * VOICEPEAKコマンドラインの実行時に発生した例外
 */
public class VPExecutionException extends RuntimeException {

    /**
     * 例外インスタンスを生成する
     * @param e ソース
     */
    public VPExecutionException(Exception e) {
        super(e);
    }

    /**
     * 例外インスタンスを生成する
     * @param message 例外メッセージ
     */
    public VPExecutionException(String message) {
        super(message);
    }

}
