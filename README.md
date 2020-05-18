# ![Cordage](https://layerxcom.github.io/cordage/images/cordage.png)

- [English](./README.md)
- [日本語](./README.ja.md)

Cordage makes the interoperability possible between [Corda](https://www.corda.net/) and other Blockchains
by providing several modules that propagate, pull and verify information between Blockchains
without further trusted third party.

In the first stage, we focus on EVM based blockchain such as Ethereum, [Quorum](https://www.goquorum.com/) and [Hyperledger Besu](https://www.hyperledger.org/use/besu).

WARNING: Cordage is alpha quality software, improvements and fixes are made frequently.

## How Cordage makes the interoperability possible
In general, key features to compose the interoperability are as follow:

- Data propagation
- Data verification

Data propagation is a feature that propagates data to each other between blockchains.
Data verification is a feature that verifies propagated data.

Cordage provides these features as executable modules
that do not require additional components except Corda and the other Blockchain.
A combination of these modules enables you to build concrete cross-chain applications.

## Cordage use cases
Put simply, cross-chain applications are abstracted in 2 patterns.

One is the data import.
Implementing the wrapped asset such as Wrapped Bitcoin enables you to move assets from one ledger to the other.
By implementing the cross-chain oracle, you can import external ledger information such as ETH/DAI price.

The other one is the data update with condition triggers.
The cross-chain atomic swap is one of the most appealing applications.
It enables payment-versus-payment and payment-versus-delivery settlement.
This pattern also includes asset encumbrance.
It realizes various applications such as liens, collateral in finance, clawbacks, etc.

## Cordage sample applications
![test for cross-chain-atomic-swap-cordapp](https://github.com/LayerXcom/cordage/workflows/test%20for%20cross-chain-atomic-swap-cordapp/badge.svg)

- [Cross-chain atomic swap](./cross-chain-atomic-swap-cordapp)

## Cordage modules
In the first stage, we focus on Corda and EVM based blockchain interoperability.
Then, we will be connecting with Hyperledger Fabric.

Data propagation and data verification features, that are explained in the section of How Cordage makes the interoperability possible,
are consists of subdivided features as follow:

Data propagation
- P1: Feature to create and send Ethereum Tx from Corda
- P2: Feature to execute Corda Flow from Ethereum (probably not needed)
- P3: Feature to import Tx / Event finalized in Ethereum into Corda
- P4: Feature to import Tx finalized in Corda into Ethereum

Data verification
- V1: Feature to verify Ethereum Tx, Event and Block imported at P3 and to verify finality of Block
- V2: Feature to verify Tx value imported at P4 and to verify finality of Corda Tx

### Implementation status
We have implemented the following modules so far.

P1: [Flow Ethereum TX](./flow-ethereum-tx)
- This CorDapp provides a simple example of how to send a transaction to Ethereum from Corda Flow.

P3: [Flow Ethereum Event Watch](./flow-ethereum-event-watch)
- This CorDapp provides a simple example of how to watch (retrieve) Ethereum Events from Corda Flow.

P1+P4+V2: [Custom Notary Flow](./custom-notary-flow) that creates an ethereum tx and sends to the ethereum node
- This CorDapp provides a simple example of how to propagate data from Corda to Ethereum by creating a Custom Notary Flow.

### How to run
Please follow the README.md in each directory.

## Contact
For further discussions and questions, please talk to us on [Gitter](https://gitter.im/LayerXcom/Cordage)!

## Contributing
We welcome contributions to Cordage.
Feel free to submit your own issues and PRs!

## License
Cordage is licensed under [the Apache License, Version 2.0](./LICENSE).

## Maintainers
- [shun-tak](https://github.com/shun-tak)
- [etaroid](https://github.com/etaroid)

## Sponsors
<a href="https://layerx.co.jp/en/"><img src="https://layerxcom.github.io/cordage/images/layerx.png" alt="LayerX" width="500"></a><br />
