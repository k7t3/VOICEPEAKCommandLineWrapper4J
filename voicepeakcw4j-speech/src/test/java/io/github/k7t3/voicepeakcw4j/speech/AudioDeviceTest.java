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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sound.sampled.Mixer;

import static org.junit.jupiter.api.Assertions.*;

class AudioDeviceTest {

    private static final String DEFAULT_NAME = "TEST DEVICE NAME";
    private static final String DEFAULT_DESCRIPTION = "TEST DESCRIPTION";
    private static final String DEFAULT_VENDOR = "TEST VENDOR";
    private static final String DEFAULT_VERSION = "TEST VERSION";

    private AudioDevice device;

    @BeforeEach
    void setUp() {
        var mixer = Mockito.mock(Mixer.class);
        var info = Mockito.mock(Mixer.Info.class);
        Mockito.when(mixer.getMixerInfo()).thenReturn(info);
        Mockito.when(info.getName()).thenReturn(DEFAULT_NAME);
        Mockito.when(info.getDescription()).thenReturn(DEFAULT_DESCRIPTION);
        Mockito.when(info.getVendor()).thenReturn(DEFAULT_VENDOR);
        Mockito.when(info.getVersion()).thenReturn(DEFAULT_VERSION);

        device = new AudioDevice(mixer);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getName() {
        assertEquals(DEFAULT_NAME, device.getName());
    }

    @Test
    void getDescription() {
        assertEquals(DEFAULT_DESCRIPTION, device.getDescription());
    }

    @Test
    void getVendor() {
        assertEquals(DEFAULT_VENDOR, device.getVendor());
    }

    @Test
    void getVersion() {
        assertEquals(DEFAULT_VERSION, device.getVersion());
    }

    @Test
    void testDeviceSupportCheck() {
        var defaultDevice = AudioDevice.getDefaultDevice();

        // 既定のデバイスが見つからないときは有効なデバイスの一覧も空のはず
        // 既定のデバイスが存在するときは有効なデバイスに含まれるはず
        if (defaultDevice == null) {
            assertTrue(AudioDevice.getAvailableDevices().isEmpty());
        } else {
            var availableDevices = AudioDevice.getAvailableDevices();
            assertFalse(availableDevices.isEmpty());
            assertTrue(availableDevices.contains(defaultDevice));
        }
    }

}