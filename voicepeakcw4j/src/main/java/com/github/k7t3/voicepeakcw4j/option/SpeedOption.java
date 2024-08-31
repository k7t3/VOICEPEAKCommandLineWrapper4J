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
 * VOICEPEAKの<code>--speed</code>オプションの定義
 */
public class SpeedOption implements Option {

    private final int speed;

    public SpeedOption(int speed) {
        if (speed < 50 || 200 < speed)
            throw new IllegalArgumentException("speed is out of range");
        this.speed = speed;
    }

    @Override
    public void fill(List<String> commands) {
        commands.add("--speed");
        commands.add(Integer.toString(speed));
    }
}
