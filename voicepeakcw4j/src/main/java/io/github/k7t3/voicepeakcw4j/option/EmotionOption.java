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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VOICEPEAKのコマンドラインオプション<code>--emotion</code>の定義
 */
public class EmotionOption implements Option {

    private final Map<String, Integer> emotions;

    public EmotionOption(Map<String, Integer> emotions) {
        if (emotions == null)
            throw new IllegalArgumentException("emotions is null");
        if (emotions.isEmpty())
            throw new IllegalArgumentException("emotions is empty");
        if (emotions.values().stream().anyMatch(v -> v < 0 || 100 < v))
            throw new IllegalArgumentException("contains unexpected emotion value");
        this.emotions = new HashMap<>(emotions);
    }

    @Override
    public void fill(List<String> commands) {
        commands.add("--emotion");
        var values = emotions.entrySet()
                .stream()
                .map(e -> "%s=%d".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));
        commands.add(values);
    }
}
