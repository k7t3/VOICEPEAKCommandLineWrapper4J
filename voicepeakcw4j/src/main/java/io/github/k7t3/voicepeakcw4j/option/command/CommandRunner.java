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

import io.github.k7t3.voicepeakcw4j.VPExecutable;
import io.github.k7t3.voicepeakcw4j.process.impl.DefaultVPProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * VOICEPEAKの実行可能なコマンドラインオプションの定義
 */
public class CommandRunner {

    private final VPExecutable executable;

    private final Command command;

    /**
     * コマンドを実行するランナーインスタンスを生成する
     * @param executable 実行可能ファイル
     * @param command 実行するコマンド
     */
    public CommandRunner(VPExecutable executable, Command command) {
        this.executable = executable;
        this.command = command;
    }

    /**
     * コマンドを実行する。
     * <p>標準出力の内容を返す</p>
     * @return 標準出力
     */
    public List<String> run() {
        var commands = new ArrayList<String>();
        executable.fill(commands);
        command.fill(commands);

        var sub = new CommandSubscriber();

        var process = new DefaultVPProcess(new ProcessBuilder(commands));
        process.getStandardOut().subscribe(sub);

        try {
            return process.start().thenApply(status -> {
                if (status == 0) {
                    return Collections.unmodifiableList(sub.getOutputs());
                } else {
                    return List.<String>of();
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
