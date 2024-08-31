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

import com.github.k7t3.voicepeakcw4j.option.command.CommandRunner;
import com.github.k7t3.voicepeakcw4j.option.command.ListEmotionCommand;
import com.github.k7t3.voicepeakcw4j.option.command.ListNarratorCommand;
import com.github.k7t3.voicepeakcw4j.process.VPProcessBuilder;

import java.util.List;

class DefaultVPClient implements VPClient {

    private final VPExecutable defaultExecutable;

    public DefaultVPClient(VPExecutable executable) {
        this.defaultExecutable = executable;
    }

    @Override
    public VPProcessBuilder builder() {
        return new VPProcessBuilder(defaultExecutable);
    }

    @Override
    public List<String> getEmotions(String narrator) {
        var runner = new CommandRunner(defaultExecutable, new ListEmotionCommand(narrator));
        return runner.run();
    }

    @Override
    public List<String> getNarrators() {
        var runner = new CommandRunner(defaultExecutable, new ListNarratorCommand());
        return runner.run();
    }
}
