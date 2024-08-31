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

package com.github.k7t3.voicepeakcw4j.speech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SentenceSplitterTest {

    private SentenceSplitter splitter;

    @BeforeEach
    void setUp() {
        splitter = new SentenceSplitter(Locale.JAPANESE);
    }

    private String getText88Length() {
        // 15文字 + 1文字(改行)
        // 27文字 + 1文字(改行)
        // 44文字
        // → 88文字
        return """
                恥の多い生涯を送って来ました。
                自分には、人間の生活というものが、見当つかないのです。
                自分は東北の田舎に生れましたので、汽車をはじめて見たのは、よほど大きくなってからでした。""";
    }

    @Test
    void testRange() {
        // 15文字 + 1文字(改行)
        // 27文字 + 1文字(改行)
        //   → 44文字
        // 44文字
        //   → 88文字
        var text = getText88Length();

        var split = splitter.splitByWord(text, 44);

        assertEquals(2, split.size());
        assertEquals("恥の多い生涯を送って来ました。自分には、人間の生活というものが、見当つかないのです。", split.getFirst());
        assertEquals("自分は東北の田舎に生れましたので、汽車をはじめて見たのは、よほど大きくなってからでした。", split.getLast());
    }

    @Test
    void testNoSplit() {
        // 15文字 + 1文字(改行)
        // 27文字 + 1文字(改行)
        //   → 44文字
        // 44文字
        //   → 88文字
        var text = getText88Length();

        var split = splitter.splitByWord(text, 100);
        assertEquals(1, split.size());
    }

    @Test
    void testSplit() {
        // 15文字 + 1文字(改行)
        // 27文字 + 1文字(改行)
        //   → 44文字
        // 44文字
        //   → 88文字
        var text = getText88Length();

        // ギリはみ出すチャンクサイズ
        // 最後の行の読点基準で分割
        var split = splitter.splitByWord(text, 80);

        assertEquals(2, split.size());
        assertEquals("恥の多い生涯を送って来ました。自分には、人間の生活というものが、見当つかないのです。自分は東北の田舎に生れましたので、汽車をはじめて見たのは、", split.getFirst());
        assertEquals("よほど大きくなってからでした。", split.getLast());
    }

    @Test
    void testSplitForcibly() {
        var text = "あいうえお、かきくけこさしすせそたちつてと";

        // 句読点がなければぶつ切り
        var split = splitter.splitByWord(text, 10);

        assertEquals(3, split.size());
        assertEquals("あいうえお、", split.getFirst());
        assertEquals("かきくけこさしすせそ", split.get(1));
        assertEquals("たちつてと", split.get(2));
    }
}