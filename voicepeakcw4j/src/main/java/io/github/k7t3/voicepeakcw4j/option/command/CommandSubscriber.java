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

package io.github.k7t3.voicepeakcw4j.option.command;

import io.github.k7t3.voicepeakcw4j.Subscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CommandSubscriber implements Subscriber<String> {

    private final List<String> outputs = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onNext(String item) {
        var output = (item == null) ? "" : item.trim();
        if (output.isEmpty())
            return;
        outputs.add(output);
    }

    public List<String> getOutputs() {
        return outputs;
    }

}
