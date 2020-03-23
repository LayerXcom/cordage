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

You can run ganache-cli and deploy sample Contract by following [Minimal Ethereum Environment](../minimal-ethereum-env/README.md).

### Create SmartContract Wrapper Class by web3j command
 
 ```
 web3j truffle generate ../minimal-ethereum-env/build/contracts/SimpleStorage.json -o ./src/main/java -p jp.co.layerx.cordage.crosschainatomicswap.ethWrapper ```

# Usage

### Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.

## Interacting with the nodes:

Go to the CRaSH shell for ParticipantA, and run the `StartEventWatchFlow` with `searchId`:

    flow start jp.co.layerx.cordage.flowethereumeventwatch.flow.StartEventWatchFlow searchId: 8

You can now start monitoring the node's flow activity...

    flow watch

...you will see the `EventWatch` flow running every 10 seconds until you close the Flow Watch window using `ctrl/cmd + c`:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Event Watched. (fromBlockNumber: x, toBlockNumber: xxxx)

...Or if aimed Ethereum Event was emitted on ethereum network, `EventWatch` flow will end with below log:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Ethereum Event with id: xx watched and send TX Completed

You can send Ethereum tx to emit event using minimal ethereum env [scripts](../minimal-ethereum-env/scripts).
