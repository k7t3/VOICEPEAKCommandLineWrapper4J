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

package io.github.k7t3.voicepeakcw4j.option.command;

import java.util.List;

/**
 * VOICEPEAKの<code>--list-emotion</code>オプションの定義
 */
public class ListEmotionCommand implements Command {

    private final String narrator;

    /**
     * ナレーターに対応する感情を取得するコマンドを生成する
     * @param narrator ナレーター
     */
    public ListEmotionCommand(String narrator) {
        if (narrator == null)
            throw new IllegalArgumentException("narrator is null");
        if (narrator.trim().isEmpty())
            throw new IllegalArgumentException("narrator is empty");
        this.narrator = narrator.trim();
    }

    @Override
    public void fill(List<String> commands) {
        commands.add("--list-emotion");
        commands.add(narrator);
    }
}
