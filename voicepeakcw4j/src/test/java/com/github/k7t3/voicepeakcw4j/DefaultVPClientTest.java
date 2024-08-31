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

package com.github.k7t3.voicepeakcw4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultVPClientTest {

    private DefaultVPClient client;

    @BeforeEach
    void setUp() {
        var executable = new TestVPExecutable();
        client = new DefaultVPClient(executable);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getEmotions() {
        // ずんだもんの感情
        // amaama, aori, hisohiso, live, tsuntsun
        var emotions = client.getEmotions("Zundamon");
        assertTrue(emotions.contains("amaama"));
        assertTrue(emotions.contains("aori"));
        assertTrue(emotions.contains("hisohiso"));
        assertTrue(emotions.contains("live"));
        assertTrue(emotions.contains("tsuntsun"));

        // きりたん
        // bright, dere, dull, angry, teary
        emotions = client.getEmotions("Tohoku Kiritan");
        assertTrue(emotions.contains("bright"));
        assertTrue(emotions.contains("dere"));
        assertTrue(emotions.contains("dull"));
        assertTrue(emotions.contains("angry"));
        assertTrue(emotions.contains("teary"));

        // 該当なし
        emotions = client.getEmotions("Yukkuri");
        assertTrue(emotions.isEmpty());
    }

    @Test
    void getNarrators() {
        var narrators = client.getNarrators();
        assertTrue(narrators.contains("Zundamon"));
        assertTrue(narrators.contains("Tohoku Kiritan"));
        assertFalse(narrators.contains("Yukkuri"));
    }
}