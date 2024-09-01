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

import java.nio.file.Path;
import java.util.List;

/**
 * VOICEPEAK実行ファイル
 * <p>
 *     {@link VPExecutable#VPExecutable(Path)}コンストラクタに
 *     VOICEPEAK実行ファイルを渡すことでインスタンス化できる。
 * </p>
 * <p>
 *     あるいはVOICEPEAK実行ファイルがパスに含まれている場合は
 *     {@link VPExecutable#VPExecutable()}コンストラクタを使用して
 *     インスタンス化できる。
 * </p>
 */
public class VPExecutable {

    private final String executable;

    /**
     * VOICEPEAKコマンドの実行可能インスタンスを生成するコンストラクタ。
     * <p>
     *     このコンストラクタを使用するには、
     *     VOICEPEAK実行ファイルがパスに含まれている必要がある。
     * </p>
     */
    public VPExecutable() {
        this(executableName());
    }

    /**
     * VOICEPEAKコマンドの実行可能インスタンスを生成するコンストラクタ。
     * @param executable VOICEPEAK実行ファイル
     */
    public VPExecutable(Path executable) {
        this(executable.toAbsolutePath().toString());
    }

    private VPExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * VOICEPEAKを実行するコマンドの内容を書き込む
     * @param commands VOICEPEAKを実行するコマンドのリスト
     */
    public void fill(List<String> commands) {
        commands.add(executable);
    }

    private static String executableName() {
        var os = System.getProperty("os.name", "").toLowerCase();
        if (os.startsWith("win")) {
            return "voicepeak.exe";
        } else if (os.startsWith("mac")) {
            return "voicepeak";
        } else {
            return "voicepeak";
        }
    }

}
