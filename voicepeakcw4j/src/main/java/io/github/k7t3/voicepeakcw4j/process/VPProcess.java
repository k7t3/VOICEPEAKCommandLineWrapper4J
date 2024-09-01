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

package io.github.k7t3.voicepeakcw4j.process;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

/**
 * VOICEPEAKコマンドラインを実行するダミープロセス
 */
public class VPProcess {

    private static final System.Logger LOGGER = System.getLogger(VPProcess.class.getName());

    private final ProcessBuilder processBuilder;

    private final MessagePublisher standardOut = new MessagePublisher();
    private final MessagePublisher errorOut = new MessagePublisher();

    public VPProcess(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    /**
     * VOICEPEAKコマンドラインを実行し、その将来的な実行結果を表す{@link CompletableFuture}を返す。
     * <p>
     *     このメソッドは同じインスタンスにおいて繰り返し実行できるが、
     *     同時に実行すると{@link #getStandardOut()}及び
     *     {@link #getErrorOut()}からメッセージが同時に配信されるため区別できない可能性がある。
     * </p>
     * <p>
     *     コマンドラインの実行に失敗したときは{@link RuntimeException}をスローする。
     * </p>
     * @return VOICEPEAKコマンドラインの将来的な実行結果を表すfuture
     * @throws RuntimeException コマンドラインの実行に失敗したとき
     */
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

    /**
     * VOICEPEAKコマンドラインの標準出力パブリッシャーを返す
     * <p>
     *     {@link #start()}メソッドが実行されていなくても取得できる
     * </p>
     * @return VOICEPEAKコマンドラインの標準出力パブリッシャー
     */
    public Flow.Publisher<String> getStandardOut() {
        return standardOut;
    }

    /**
     * VOICEPEAKコマンドラインのエラー出力パブリッシャーを返す
     * <p>
     *     {@link #start()}メソッドが実行されていなくても取得できる
     * </p>
     * @return VOICEPEAKコマンドラインのエラー出力パブリッシャー
     */
    public Flow.Publisher<String> getErrorOut() {
        return errorOut;
    }
}
