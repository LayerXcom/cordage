# ![Cordage](https://layerxcom.github.io/cordage/images/cordage.png)

- [English](./README.md)
- [日本語](./README.ja.md)

Cordageは、[Corda](https://www.corda.net/)と他のブロックチェーン間の相互運用を可能にします。
信頼できる第三者を追加しなくともブロックチェーン間で情報を伝播、取得、検証することを可能にするいくつかのモジュールを提供します。

最初の段階では、Ethereumや[Quorum](https://www.goquorum.com/)、[Hyperledger Besu](https://www.hyperledger.org/use/besu)などのEVMベースのブロックチェーンに焦点を当てます。

警告：Cordageはアルファ品質のソフトウェアであり、改善と修正は頻繁に行われます。

## Cordageが相互運用を可能にする方法
一般に、相互運用性を構成する主要な機能は次のとおりです：

- データ伝搬
- データ検証

データ伝播は、ブロックチェーン間でデータを相互に伝播する機能です。
データ検証は、伝播されたデータを検証する機能です。

Cordageはこれらの機能を、Cordaおよびその他のブロックチェーン以外の追加コンポーネントを必要としない実行可能モジュールとして提供します。
これらのモジュールの組み合わせにより、具体的なクロスチェーンアプリケーションを構築できます。

## Cordageの使用例
簡単に言えば、クロスチェーンアプリケーションは2つのパターンに抽象化されます。

1つはデータのインポートです。
Wrapped Bitcoinなどのラップされたアセットを実装すると、1つの台帳から別の台帳にアセットを移動できます。
クロスチェーンオラクルを実装することにより、ETH/DAI価格などの外部の台帳の情報をインポートできます。

もう1つは、条件トリガーを使用したデータ更新です。
クロスチェーンアトミックスワップは、最も魅力的なアプリケーションの1つです。
PvP (Payment vs Payment) およびDvP (Payment vs Delivery) での決済が可能です。
このパターンには、資産の担保も含まれます。
先取特権、金融担保、クローバックなどのさまざまなアプリケーションを実現します。

## Cordageサンプルアプリケーション
![test for cross-chain-atomic-swap-cordapp](https://github.com/LayerXcom/cordage/workflows/test%20for%20cross-chain-atomic-swap-cordapp/badge.svg)

- [クロスチェーンアトミックスワップ](./cross-chain-atomic-swap-cordapp)

## Cordageモジュール
最初の段階では、CordaおよびEVMベースのブロックチェーンとの相互運用性に焦点を当てます。
次に、Hyperledger Fabricに接続します。

「Cordageが相互運用を可能にする方法」の章で説明したデータ伝播およびデータ検証機能は、次のように細分化された機能で構成されます。

データ伝搬
- P1：CordeからEthereum Txを作成して送信する機能
- P2：EthereumからCorda Flowを実行する機能（おそらく不要）
- P3：EthereumでファイナライズされたTx /イベントをCordaにインポートする機能
- P4：CordaでファイナライズされたTxをEthereumにインポートする機能

データ検証
- V1：P3でインポートされたEthereum Tx、イベント、ブロックを検証し、ブロックのファイナリティを検証する機能
- V2：P4でインポートされたTx値を検証し、Corda Txのファイナリティを検証する機能

### 実装ステータス
これまでに以下のモジュールを実装しました。

P1: [Flow Ethereum TX](./flow-ethereum-tx)
- このCorDappは、Corda FlowからEthereumにトランザクションを送信する方法の簡単な例を提供します。

P3: [Flow Ethereum Event Watch](./flow-ethereum-event-watch)
- このCorDappは、Corda FlowからEthereumイベントを監視（取得）する方法の簡単な例を提供します。

P1+P4+V2: [Custom Notary Flow](./custom-notary-flow) はEthereum txを作成し、Ethereumノードに送信します
- このCorDappは、Custom Notary Flowを作成してCordaからEthereumにデータを伝播する方法の簡単な例を提供します。

### 実行方法
各ディレクトリ内のREADMEファイルに従って実行してください。

## 連絡先
さらなるディスカッションや質問は[Gitter](https://gitter.im/LayerXcom/Cordage)でご連絡ください。

## プロジェクトへの貢献
私たちはCordageへの貢献を歓迎します。
ぜひお気軽にissueやpull requestを作成してください！

## ライセンス
Cordageは[the Apache License, Version 2.0](./LICENSE)にて提供されています。

## 管理者
- [shun-tak](https://github.com/shun-tak)
- [etaroid](https://github.com/etaroid)

## スポンサー
<a href="https://layerx.co.jp/en/"><img src="https://layerxcom.github.io/cordage/images/layerx.png" alt="LayerX" width="500"></a><br />
