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

package io.github.k7t3.voicepeakcw4j.option;

import java.nio.file.Path;
import java.util.List;

/**
 * VOICEPEAKの<code>--text</code>オプションの定義
 */
public class SpeechFileOption implements Option {

    private final Path path;

    /**
     * 読み上げるテキストファイルを設定するオプションを生成する
     * @param path テキストファイルのパス
     */
    public SpeechFileOption(Path path) {
        if (path == null)
            throw new IllegalArgumentException("path is null");
        this.path = path;
    }

    @Override
    public void fill(List<String> commands) {
        commands.add("--text");
        commands.add(path.toAbsolutePath().toString());
    }

}
