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

import com.github.k7t3.voicepeakcw4j.exception.VPExecutionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VPClientTest {

    @Test
    void create() {
        var client = VPClient.create(new TestVPExecutable());
        assertInstanceOf(DefaultVPClient.class, client);
    }

    @Test
    void builder() {
        var client = VPClient.create(new TestVPExecutable());
        assertThrows(VPExecutionException.class, () -> client.builder().build());
    }
}