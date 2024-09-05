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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 文字列を意味のある区切りで分割するクラス。
 * <p>
 *     文字列を分割するときに{@link SentenceSplitter#SentenceSplitter(Locale)}コンストラクタで
 *     指定したロケールに応じて解釈され、{@link SentenceSplitter#SentenceSplitter()}
 *     コンストラクタを使用するとシステムロケールが使用される。
 * </p>
 * @see SentenceSplitter#splitByWord(String, int)
 */
public class SentenceSplitter {

    private final Locale locale;

    /**
     * コンストラクタ
     * @param locale 解釈するロケール
     */
    public SentenceSplitter(Locale locale) {
        this.locale = locale == null ? Locale.getDefault() : locale;
    }

    /**
     * コンストラクタ
     */
    public SentenceSplitter() {
        this(null);
    }

    /**
     * 任意のテキストを指定のチャンクサイズに収まるように文字列を分割する。
     * <p>
     *     文章を句読点に基づいて解釈し分解するが、句読点が見つからず
     *     意図した粒度に分解できない場合は極力意味のある文字の区切りで分解する。
     * </p>
     * <p>改行文字は取り除かれる。</p>
     * @param text 分割するテキスト
     * @param chunkSize チャンクサイズ(4以上)
     * @return 分割したリスト。分割するテキストが空の場合は空のリスト。
     * @throws NullPointerException 分割するテキストがnullのとき
     * @throws IllegalArgumentException チャンクサイズが4未満のとき
     */
    public List<String> splitByWord(String text, int chunkSize) {
        if (chunkSize < 4)
            throw new IllegalArgumentException("too small chunk size");
        if (text == null)
            throw new NullPointerException("text is null");
        if (text.length() < chunkSize)
            return List.of(text);

        var list = new ArrayList<String>();

        var fragments = breakFragments(text, chunkSize);

        var builder = new StringBuilder();
        for (var fragment : fragments) {
            builder = appendText(list, builder, fragment, chunkSize);
        }

        if (!builder.isEmpty())
            list.add(builder.toString());

        return list;
    }

    /**
     * テキストを読点・空白を基準に分解する。
     * 読点・空白が見つからないときは最大長に応じて品詞分解する。
     */
    private List<String> breakFragments(String text, int maxLength) {
        var list = new ArrayList<String>();

        var bi = BreakIterator.getWordInstance(locale);
        bi.setText(text);

        var builder = new StringBuilder();

        var begin = bi.first();
        for (var end = bi.next(); end != BreakIterator.DONE; begin = end, end = bi.next()) {
            var word = text.substring(begin, end);

            // 句読点・空白のときに要素とする
            if (!Character.isLetter(word.codePointAt(0))) {

                builder.append(word);

                if (!builder.isEmpty()) {
                    list.add(builder.toString());
                    builder = new StringBuilder();
                }
            } else {

                // 検出したワード単体でmaxLengthを超過しているときは文字数で区切る
                if (maxLength < word.length()) {

                    if (!builder.isEmpty()) {
                        list.add(builder.toString());
                        builder = new StringBuilder();
                    }

                    // サロゲートペアの文字がVOICEPEAKでどう扱われるか
                    // わからないのでとりあえずこれで…
                    var characters = breakCharacters(word);

                    var b = new StringBuilder();
                    for (int i = 0; i < characters.size(); i++) {
                        if (i == maxLength) {
                            list.add(b.toString());
                            b = new StringBuilder();
                        }
                        b.append(characters.get(i));
                    }
                    list.add(b.toString());

                } else {

                    // 空白や読点がなく、超過する場合は諦める
                    if (maxLength < builder.length() + word.length()) {
                        list.add(builder.toString());
                        builder = new StringBuilder();
                    }

                    builder.append(word);
                }
            }
        }

        if (!builder.isEmpty()) {
            list.add(builder.toString());
        }

        return list;
    }

    /**
     * 単語を文字に分解する
     */
    private List<String> breakCharacters(String word) {
        var list = new ArrayList<String>();

        var bi = BreakIterator.getCharacterInstance(locale);
        bi.setText(word);

        var begin = bi.first();
        for (var end = bi.next(); end != BreakIterator.DONE; begin = end, end = bi.next()) {
            list.add(word.substring(begin, end));
        }

        return list;
    }

    /**
     * 文字列を連結、及びチャンクリストに追加する。
     * 引数のテキスト(breakText)が単体でチャンクサイズを超えないようにすること。
     */
    private StringBuilder appendText(List<String> chunk, StringBuilder builder, String breakText, int chunkSize) {
        if (chunkSize < breakText.length())
            throw new IllegalArgumentException();

        var buffer = builder;

        if (chunkSize < buffer.length() + breakText.length()) {
            chunk.add(buffer.toString());
            buffer = new StringBuilder();
        }

        buffer.append(breakText);

        return buffer;
    }

}