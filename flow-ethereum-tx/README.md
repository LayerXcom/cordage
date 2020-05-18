# Flow Ethereum Transaction CorDapp
<p align="center">
  <img src="https://layerxcom.github.io/cordage/images/corda.png" alt="Corda" width="500">
</p>

<p align="center">
  <img src="https://layerxcom.github.io/cordage/images/ethereum.png" alt="Ethereum" width="500">
</p>

## Sending Transaction to Ethereum
This CorDapp provides a simple example of how to send a transaction to Ethereum from Corda Flow.
In this case, the flow sends a transaction that transfers 1 ether from an account to the other account.

Be aware that support of HTTP requests in flows is currently limited:

- The request must be executed in a BLOCKING way. Flows don't currently support suspending to await an HTTP call's response
- The request must be idempotent. If the flow fails and has to restart from a checkpoint, the request will also be replayed

Also, be aware that there is [okhttp's dependency conflict between Corda Node v4 and web3j (later than 4.5.12)](https://github.com/web3j/web3j/issues/1167).


## Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

### Run ganache-cli
[ganache-cli](https://github.com/trufflesuite/ganache-cli) is a fast Ethereum RPC client for testing and development.

You can run ganache-cli by following [Minimal Ethereum Environment](../minimal-ethereum-env/README.md).


## Usage

### Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.

### Interacting with the nodes:

You'll be interacting with the node via its interactive shell.

To have the node use a flow to send a transaction to ethereum, run the following command in the node's 
shell:

```
flow start jp.co.layerx.cordage.flowethereumtx.Flow
```

You can check the ether decrease/increase by looking at the ethereum wallet.
