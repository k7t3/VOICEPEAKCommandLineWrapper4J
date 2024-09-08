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

package io.github.k7t3.voicepeakcw4j.process.impl;

import io.github.k7t3.voicepeakcw4j.process.VPProcess;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

/**
 * VOICEPEAKコマンドラインを実行するダミープロセス
 */
public class DefaultVPProcess implements VPProcess {

    private static final System.Logger LOGGER = System.getLogger(DefaultVPProcess.class.getName());

    private final ProcessBuilder processBuilder;

    private final MessagePublisher standardOut = new MessagePublisher();
    private final MessagePublisher errorOut = new MessagePublisher();

    /**
     * コンストラクタ
     * @param processBuilder プロセスビルダー
     */
    public DefaultVPProcess(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    @Override
    public CompletableFuture<Integer> start() {
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.log(System.Logger.Level.DEBUG, "started process pid = " + process.pid());

        var executor = Executors.newVirtualThreadPerTaskExecutor();
        standardOut.start(process.inputReader(), executor);
        errorOut.start(process.errorReader(), executor);
        executor.shutdown();

        return process.onExit().thenApply(Process::exitValue);
    }

    @Override
    public Flow.Publisher<String> getStandardOut() {
        return standardOut;
    }

    @Override
    public Flow.Publisher<String> getErrorOut() {
        return errorOut;
    }
}
