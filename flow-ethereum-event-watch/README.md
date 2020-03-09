<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

<p align="center">
  <img src="https://ethereum.org/assets/img/ethereum-logo-landscape-purple.7c3c27fd.png" alt="Corda" width="500">
</p>

# Flow Ethereum Event Watch
This CorDapp provides a simple example of how to watch(get) Ethereum Events and send a transaction to Ethereum Contract from Corda Flow.

A node starts its event watching by calling the `StartEventWatchFlow`. This creates a `WatchState` on the ledger. This 
`WatchState` has a scheduled activity to start the `EventWatchFlow` 10 seconds later.

When the `EventWatchFlow` runs 10 seconds later, it consumes the existing `WatchState` and
the Flow gets Ethereum Logs and takes out some Ethereum Events object.
If the Flow can find aimed Ethereum Event, it sends Ethereum Transaction to the Ethereum Contract or if not, the Flow creates a new `WatchState`. 

The new `WatchState` also has a scheduled activity to start the `EventWatchFlow` in 10 seconds.

In this way, calling the `StartEventWatchFlow` creates an endless chain of `EventWatchFlow`s 10 seconds apart
as long as the Flow cannot find aimed Ethereum Event.


Be aware that support of HTTP requests in flows is currently limited:

- The request must be executed in a BLOCKING way. Flows don't currently support suspending to await an HTTP call's response
- The request must be idempotent. If the flow fails and has to restart from a checkpoint, the request will also be replayed

Also, be aware that there is [okhttp's dependency conflict between Corda Node v4 and web3j (later than 4.5.12)](https://github.com/web3j/web3j/issues/1167).


## Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

### Run ganache-cli
[ganache-cli](https://github.com/trufflesuite/ganache-cli) is a fast Ethereum RPC client for testing and development.

You can run ganache-cli and deploy sample Contract by following [Minimal Ethereum Environment](https://github.com/LayerXcom/cordage/blob/master/minimal-ethereum-env/README.md).

### Create SmartContract Wrapper Class by web3j command
 
 ```
 web3j truffle generate ../minimal-ethereum-env/build/contracts/SimpleStorage.json -o ./src/main/java -p jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper
 ```

# Usage

### Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.

## Interacting with the nodes:

Go to the CRaSH shell for PartyA, and run the `StartEventWatchFlow` with `searchId`:

    flow start jp.co.layerx.cordage.flowethereumeventwatch.flow.StartEventWatchFlow searchId: 8

If you now start monitoring the node's flow activity...

    flow watch

...you will see the `EventWatch` flow running every 10 seconds until you close the Flow Watch window using `ctrl/cmd + c`:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Evlent Watched. (fromBlockNumber: x, toBlockNumber: xxxx)

...Or if aimed Ethereum Event was emitted on ganache network, `EventWatch` flow will end with below log:

    xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Ethereum Event with id: xx watched and send TX Completed
