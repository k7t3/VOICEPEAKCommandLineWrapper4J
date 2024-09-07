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

import io.github.k7t3.voicepeakcw4j.Subscriber;
import io.github.k7t3.voicepeakcw4j.VPExecutable;

import java.io.IOException;
import java.nio.file.Paths;

public class SpeechTest {

    public static void main(String...args) {
        var executable = new VPExecutable(Paths.get(System.getenv("voicepeak_bin")));

        var text = """
                恥の多い生涯を送って来ました。
                自分には、人間の生活というものが、見当つかないのです。
                自分は東北の田舎に生れましたので、汽車をはじめて見たのは、よほど大きくなってからでした。
                自分は停車場のブリッジを、上って、降りて、そうしてそれが線路をまたぎ越えるために造られたものだという事には全然気づかず、
                ただそれは停車場の構内を外国の遊戯場みたいに、複雑に楽しく、ハイカラにするためにのみ、設備せられてあるものだとばかり思っていました。
                しかも、かなり永い間そう思っていたのです。ブリッジの上ったり降りたりは、自分にはむしろ、ずいぶん垢抜けのした遊戯で、
                それは鉄道のサーヴィスの中でも、最も気のきいたサーヴィスの一つだと思っていたのですが、
                のちにそれはただ旅客が線路をまたぎ越えるための頗る実利的な階段に過ぎないのを発見して、にわかに興が覚めました。
                また、自分は子供の頃、絵本で地下鉄道というものを見て、これもやはり、実利的な必要から案出せられたものではなく、地上の車に乗るよりは、
                地下の車に乗ったほうが風がわりで面白い遊びだから、とばかり思っていました。""";

        var speech = VPSpeech.create(executable)
                .speech()
                .withNarrator("Tohoku Kiritan")
                .withSpeechText(text)
                .withVolumeRate(0.1f)
                .withSpeed(125)
                .withMaxSentenceLength(140)
                .withAudioDevice(AudioDevice.getDefaultDevice())
                .build();

        Subscriber<String> standardSub = System.out::println;
        Subscriber<String> errorSub = System.err::println;

        speech.setStandardOutSubscriber(standardSub);
        speech.setErrorOutSubscriber(errorSub);

        var state = speech.start();

        state.getSpeechFuture().whenComplete((nil, ex) -> {
            System.out.println("done");
            if (ex != null) {
                System.err.println(ex.getMessage());
            } else {
                System.out.println("waiting for entering any key");
            }
        });

        try {
            var read = System.in.read();

            var c = (char) read;

            if (c != '\n') {
                System.out.printf("char = '%s'%n", c);
            }

            if (c == 'q') {
                System.out.println("request stop");
                state.requestStop();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
