# Boids
進化計算論の課題

## ソースコード解説
メインシステムとしてProcessingのライブラリを利用している。

### AppDisplayManager
Mainパート。boidsの更新処理を依頼し、完了したら描画を行う。
ProcessingのPAppletを利用していて、以下の要領で実行していく。

1. setting() … setup()よりも先に呼ばれる。画面サイズ設定を行なっている。
2. setup() … fpsの設定。boidsの初期化
3. draw() … boidsの更新および描画。以降はdrawが繰り返し呼ばれる。

### Boid
データクラスBoidの定義、およびBoidBehaviourでBoidの動きを定義。

### Main
main関数の実行。processingの起動。
