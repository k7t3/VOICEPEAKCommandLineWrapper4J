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

package com.github.k7t3.voicepeakcw4j.option;

import java.util.List;

/**
 * VOICEPEAKの<code>--pitch</code>オプションの定義
 */
public class PitchOption implements Option {

    private final int pitch;

    public PitchOption(int pitch) {
        if (pitch < -300 || 300 < pitch)
            throw new IllegalArgumentException("pitch is out of range");
        this.pitch = pitch;
    }

    @Override
    public void fill(List<String> commands) {
        commands.add("--pitch");
        commands.add(Integer.toString(pitch));
    }
}