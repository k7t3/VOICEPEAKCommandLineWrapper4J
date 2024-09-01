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

import java.util.List;

class NarratorEmotions {

    public static List<String> get(String narrator) {
        if (narrator == null) return List.of();
        return switch (narrator) {
            case "Zundamon" -> List.of("amaama", "aori", "hisohiso", "live", "tsuntsun");
            case "Tohoku Kiritan" -> List.of("bright", "dere", "dull", "angry", "teary");
            default -> List.of();
        };
    }

}
