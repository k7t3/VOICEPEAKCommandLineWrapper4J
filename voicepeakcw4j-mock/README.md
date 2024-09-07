# VOICEPEAK コマンドラインMock

「voicepeakcw4j」のテスト用モックアプリケーションモジュール

## Implementation
* Javaアプリケーションとして作成
* 想定しているナレーターは東北きりたん(Tohoku Kiritan)、ずんだもん(Zundamon)のみ
* コマンドライン実行のオプションの内容を標準出力にプリントする
* サンプル音声をコピーすることで音声を合成したこととする

## Usage
1. [VoicePeak.java](src/main/java/com/github/k7t3/voicepeakcw4j/VoicePeak.java)を作成
2. gradleタスク `buildMock`を実行してモックアプリケーションを生成
3. [TestVPExecutable.java](../voicepeakcw4j/src/test/java/com/github/k7t3/voicepeakcw4j/TestVPExecutable.java)で使用