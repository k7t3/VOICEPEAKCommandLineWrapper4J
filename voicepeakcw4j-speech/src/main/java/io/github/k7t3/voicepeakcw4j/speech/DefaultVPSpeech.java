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

import java.util.List;

class DefaultVPSpeech implements VPSpeech {

    private final VPExecutable executable;

    private final VPClient client;

    DefaultVPSpeech(VPExecutable executable) {
        this.executable = executable;
        this.client = VPClient.create(executable);
    }

    @Override
    public SpeechBuilder builder() {
        return new SpeechBuilder(executable);
    }

    @Override
    public List<String> getEmotions(String narrator) {
        return client.getEmotions(narrator);
    }

    @Override
    public List<String> getNarrators() {
        return client.getNarrators();
    }

}
