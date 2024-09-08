/**
 * 株式会社AHSの展開する音声合成ソフトウェアVOICEPEAKの
 * ラッパーモジュールvoicepeakcw4jを使用して音声を読み上げるためのモジュール
 */
module voicepeakcw4j.speech {
    requires java.desktop;
    requires transitive voicepeakcw4j;

    exports io.github.k7t3.voicepeakcw4j.speech;
}