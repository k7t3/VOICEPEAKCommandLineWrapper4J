# VOICEPEAK コマンドラインラッパー for Java

VOICEPEAKは、[株式会社AHS](https://www.ah-soft.com/)が開発した入力文字読み上げソフトで、[Dreamtonics社](https://dreamtonics.com/synthesizerv/)が開発した高品質なAI音声合成エンジン「Syllaflow」を搭載しています。

2024年8月現在、最新バージョンである「VOICEPEAK 1.2.11」では、コマンドラインから実行して任意のテキストをもとに音声を生成することができます。
```
Usage:
  voicepeak.exe [OPTION...]

  -s, --say Text               Text to say
  -t, --text File              Text file to say
  -o, --out File               Path of output file
  -n, --narrator Name          Name of voice, check --list-narrator
  -e, --emotion Expr           Emotion expression, for example:
                               happy=50,sad=50. Also check --list-emotion
      --list-narrator          Print voice list
      --list-emotion Narrator  Print emotion list for given voice
  -h, --help                   Print help
      --speed Value            Speed (50 - 200)
      --pitch Value            Pitch (-300 - 300)
```

このリポジトリはコマンドラインを**Javaアプリケーションから実行可能**とし、音声ファイルを生成するだけでなく、**リアルタイムに音声を読み上げる**ための機能を提供します。


## Usage - コマンドラインラッパー

コマンドラインラッパーライブラリを使用するには、ライブラリの依存関係をプロジェクトに追加します。
```groovy
implementation group: 'com.github.k7t3', name: 'voicepeakcw4j', version: 'X.X.X'
```

1. コマンドラインを実行するために、実行ファイルのパスを指定します。
    ```java
    // VOICEPEAK実行ファイルのパスを指定します
    var executable = new VPExecutable(Paths.get(System.getenv("voicepeak_bin")));
            
    // あるいは、VOICEPEAK実行ファイルがPATHに含まれている場合は何も指定する必要はありません
    var executable = new VPExecutable();
    ```

2. コマンドラインを操作するクライアントを生成します。
    ```java
    // クライアントインスタンスを生成します
    var client = VPClient.create(executable);
    ```

3. クライアントからオプションコマンドの実行、音声生成のためのビルダーを生成します。
    ```java
    // システムにインストールされているナレーターの一覧を取得できます。
    var narrators = client.getNarrators();
    // => [Zundamon, Tohoku Kiritan]

    // ナレーターが対応している感情の一覧を取得できます。
    var emotions = client.getEmotions("Zundamon");
    // => [amaama, aori, hisohiso, live, tsuntsun]

    // VOICEPEAKの実行パラメータを設定するビルダーを取得し、
    // プロセスを実行できます。
    var process = client.builder()
            .withNarrator("Zundamon")
            .withEmotion(Map.of("aori", 100, "tsuntsun", 20))
            .withSpeechText("このライブラリはVOICEPEAKのコマンドラインラッパーです。")
            .withOutput(Paths.get(filePath))
            .build();
   
    // VOICEPEAKプロセスの開始
    var future = process.start();
    ```

4. プロセスが出力する標準出力及び標準エラー出力を購読できます。
    ```java
   process.getStandardOut().subscribe(Subscriber.of(System.out::println));
   process.getErrorOut().subscribe(Subscriber.of(System.err::println));
   var future = process.start();
    ```

## Usage - スピーチ

音声の即時読み上げスピーチライブラリを使用するには、ライブラリの依存関係をプロジェクトに追加します。
```groovy
implementation group: 'com.github.k7t3', name: 'voicepeakcw4j-speech', version: 'X.X.X'
```

1. VOICEPEAK実行パスを指定し、スピーチクライアントを生成します。
    ```java
    var executable = new VPExecutable(Paths.get(System.getenv("voicepeak_bin")));
           
    // スピーチクライアント を生成
    var client = VPSpeech.create(executable);
    ```

2. クライアントからオプションコマンドの実行、音声読み上げのためのビルダーを生成します。
    ```java
    // システムにインストールされているナレーターの一覧を取得できます。
    var narrators = client.getNarrators();
    // => [Zundamon, Tohoku Kiritan]

    // ナレーターが対応している感情の一覧を取得できます。
    var emotions = client.getEmotions("Zundamon");
    // => [amaama, aori, hisohiso, live, tsuntsun]

    // 読み上げに関するパラメータを設定するビルダーを取得し、
    // 音声を読み上げることができます。
    var speech = client.builder()
            .withNarrator("Tohoku Kiritan")
            .withSpeechText(text)
            .withVolumeRate(0.2f)
            .withSpeed(110)
            .build();
    speech.run();
    ```


現在、VOICEPEAKコマンドライン実行で処理できる文字数には140文字までの制限がありますが、このライブラリはその制限を隠ぺいしています。

140文字を超える文字列がパラメータに設定される場合、言語仕様に基づいて最大限文脈を破綻させないように維持しながら複数の文字列に分割し、その文字列の分割個数に基づいてVOICEPEAKプロセスを繰り返し実行します。このように、長い文字列であっても一度の実行でシームレスに読み上げることができます。

また、現在のVOICEPEAKのバージョンにはコマンドラインの並列実行に関しても制約があります。このライブラリの観測できない場所からプロセスが実行されていると(ターミナルなど)、正常に動作しない可能性があります。


## License

これらのライブラリは[Apache License, Version 2.0](LICENSE)のもと公開されています。