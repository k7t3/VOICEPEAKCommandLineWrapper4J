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

package io.github.k7t3.voicepeakcw4j.speech;

import io.github.k7t3.voicepeakcw4j.VPClient;
import io.github.k7t3.voicepeakcw4j.VPExecutable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpeechBuilderTest {

    private static VPExecutable executable;

    @BeforeAll
    static void setUpAll() {
        executable = new TestVPExecutable();
    }

    private SpeechBuilder builder;

    @BeforeEach
    void setUp() {
        VPClient client = VPClient.create(executable);
        builder = new SpeechBuilder(client.builder());
    }

    private SpeechBuilder essentialBuilder() {
        return builder.withSpeechText("hello");
    }

    @Test
    void withSpeechText() {
        var builder = this.builder;
        builder.withSpeechText("not null");

        assertDoesNotThrow(builder::build);
    }

    @Test
    void errorSpeechText() {
        var builder = this.builder;
        builder.withSpeechText(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void withPitch() {
        var builder = essentialBuilder();
        builder.withPitch(-300);
        assertDoesNotThrow(builder::build);

        builder = essentialBuilder();
        builder.withPitch(300);
        assertDoesNotThrow(builder::build);
    }

    @Test
    void errorWithPitch() {
        var builder = essentialBuilder();
        builder.withPitch(-301);
        assertThrows(IllegalStateException.class, builder::build);

        builder = essentialBuilder();
        builder.withPitch(301);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void withSpeed() {
        var builder = essentialBuilder();
        builder.withSpeed(50);
        assertDoesNotThrow(builder::build);

        builder = essentialBuilder();
        builder.withSpeed(200);
        assertDoesNotThrow(builder::build);
    }

    @Test
    void errorWithSpeed() {
        var builder = essentialBuilder();
        builder.withSpeed(49);
        assertThrows(IllegalStateException.class, builder::build);

        builder = essentialBuilder();
        builder.withSpeed(201);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void withEmotion() {
        var builder = essentialBuilder();
        builder.withEmotion(Map.of("sad", 0));
        assertDoesNotThrow(builder::build);

        builder = essentialBuilder();
        builder.withEmotion(Map.of("sad", 100));
        assertDoesNotThrow(builder::build);
    }

    @Test
    void errorWithEmotion() {
        var builder = essentialBuilder();
        builder.withEmotion(Map.of("sad", -1));
        assertThrows(IllegalStateException.class, builder::build);

        builder = essentialBuilder();
        builder.withEmotion(Map.of("sad", 101));
        assertThrows(IllegalStateException.class, builder::build);
    }
}