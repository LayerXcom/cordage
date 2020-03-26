# Cross-Chain Atomic Swap Cordapp
This CorDapp provides a simple example of Cross-Chain Atomic Swap between Corda and EVM based Blockchain without trusted third party.

Be aware that support of HTTP requests in flows is currently limited:

- The request must be executed in a BLOCKING way. Flows don't currently support suspending to await an HTTP call's response
- The request must be idempotent. If the flow fails and has to restart from a checkpoint, the request will also be replayed

Also, be aware that there is [okhttp's dependency conflict between Corda Node v4 and web3j (later than 4.5.12)](https://github.com/web3j/web3j/issues/1167).


## Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

### Run ganache-cli
[ganache-cli](https://github.com/trufflesuite/ganache-cli) is a fast Ethereum RPC client for testing and development.

You can run ganache-cli and deploy sample Contract by following [Atomic Swap Ethereum Environment](../atomic-swap-ethereum-env/README.md).

### Create SmartContract Wrapper Class by web3j command
 
 ```
 web3j truffle generate ../atomic-swap-ethereum-env/build/contracts/Settlement.json -o ./src/main/java -p jp.co.layerx.cordage.crosschainatomicswap.ethWrapper
 ```

# Usage

### Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.

## Interacting with the nodes:

### Issue Security State
```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.SecurityIssueFlow amount: 100, owner: "O=ParticipantB,L=New York,C=US", issuer: "O=ParticipantC,L=Paris,C=FR", name: "inPublic"
```
This flow returns linearId of SecurityState

### vaultQuery for Security State
```
run vaultQuery contractStateType: jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
```
You can get linearId of Security State by the result.

### Transfer Security State
```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.SecurityIssueFlow amount: 100, owner: "O=ParticipantB,L=New York,C=US", issuer: "O=ParticipantC,L=Paris,C=FR", name: "inPublic"
```

This flow returns linearId of SecurityState.

### Propose Cross-Chain Atomic Swap
```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.ProposeAtomicSwapFlow securityLinearIdString: "1b06f3ae-47b6-409d-8b01-625b7522156c", securityAmount: 10, weiAmount: 1000000, swapId: "1", proposer: "O=ParticipantA,L=London,C=GB", acceptor: "O=ParticipantB,L=New York,C=US", FromEthereumAddress: "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0", ToEthereumAddress: "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b"
```

The acceptor can validate this Proposal with `checkTransaction()` in `ProposeAtomicSwapFlowResponder`.

### vaultQuery for Proposal State
```
run vaultQuery contractStateType: jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
```

You can get linearId of Proposal State by the result.

## Start EventWatchFlow

Go to the CRaSH shell for ParticipantA, and run the `StartEventWatchFlow` with `proposalStateLinearId`:

    flow start jp.co.layerx.cordage.crosschainatomicswap.flow.StartEventWatchFlow proposalStateLinearId: "24a11297-d9f4-47dc-a904-9af9aa75f640"

You can now start monitoring the node's flow activity...

    flow watch

...you will see the `EventWatch` flow running every 10 seconds until you close the Flow Watch window using `ctrl/cmd + c`:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Event Watched. (fromBlockNumber: x, toBlockNumber: xxxx)

...Or if aimed Ethereum Event was emitted on ethereum network, `EventWatch` flow will end with below log:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Ethereum Event with id: xx watched and send TX Completed

