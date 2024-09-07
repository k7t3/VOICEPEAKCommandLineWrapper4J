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

package io.github.k7t3.voicepeakcw4j;

import org.apache.commons.cli.*;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * VOICEPEAK v1.2.11
 */
public class VoicePeak {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("s", "say", true, "Text to say");
        options.addOption("t", "text", true, "Text file to say");
        options.addOption("o", "out", true, "Path of output file");
        options.addOption("n", "narrator", true, "Name of voice, check --list-narrator");
        options.addOption("e", "emotion", true, "Emotion expression, for example: happy=50,sad=50. Also check --list-emotion");
        options.addOption(null, "list-narrator", false, "Print voice list");
        options.addOption(null, "list-emotion", true, "Print emotion list for given voice");
        options.addOption("h", "help", false, "Print help");
        options.addOption(null, "speed", true, "Speed (50 - 200)");
        options.addOption(null, "pitch", true, "Pitch (-300 - 300)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("voicepeak", options);
                return;
            }

            if (cmd.hasOption("list-narrator")) {
                System.out.println("Zundamon");
                System.out.println("Tohoku Kiritan");
                return;
            }

            if (cmd.hasOption("list-emotion")) {
                String narrator = cmd.getOptionValue("list-emotion");
                if (narrator == null || narrator.trim().isEmpty()) {
                    System.out.println("error parsing options: Option 'list-emotion' is missing an argument");
                    return;
                }

                NarratorEmotions.get(narrator).forEach(System.out::println);
                return;
            }

            var builder = new VoicepeakRunner();

            if (cmd.hasOption("s")) {
                builder.withSpeechText(cmd.getOptionValue("s"));
            }

            if (cmd.hasOption("t")) {
                try {
                    var file = Path.of(cmd.getOptionValue("t"));
                    builder.withSpeechFile(file);
                } catch (InvalidPathException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                    return;
                }
            }

            if (cmd.hasOption("o")) {
                try {
                    var file = Path.of(cmd.getOptionValue("o"));
                    builder.withOutput(file);
                } catch (InvalidPathException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                    return;
                }
            }

            if (cmd.hasOption("n")) {
                builder.withNarrator(cmd.getOptionValue("n"));
            }

            if (cmd.hasOption("e")) {
                var emotion = Arrays.stream(cmd.getOptionValue("e").split(","))
                        .map(s -> s.split("="))
                        .filter(s -> s.length == 2)
                        .collect(Collectors.toMap(s -> s[0], s -> Integer.parseInt(s[1])));
                builder.withEmotion(emotion);
            }

            if (cmd.hasOption("speed")) {
                builder.withSpeed(Integer.parseInt(cmd.getOptionValue("speed")));
            }

            if (cmd.hasOption("pitch")) {
                builder.withPitch(Integer.parseInt(cmd.getOptionValue("pitch")));
            }

            builder.run();

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("voicepeak", options);
        }
    }

}