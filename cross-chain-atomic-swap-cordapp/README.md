# Cross-Chain Atomic Swap Cordapp
![test for cross-chain-atomic-swap-cordapp](https://github.com/LayerXcom/cordage/workflows/test%20for%20cross-chain-atomic-swap-cordapp/badge.svg)

This CorDapp provides a simple example of Cross-Chain Atomic Swap between Corda and EVM based Blockchain without trusted third party.

Be aware that support of HTTP requests in flows is currently limited:

- The request must be executed in a BLOCKING way. Flows don't currently support suspending to await an HTTP call's response
- The request must be idempotent. If the flow fails and has to restart from a checkpoint, the request will also be replayed

Also, be aware that there is [okhttp's dependency conflict between Corda Node v4 and web3j (later than 4.5.12)](https://github.com/web3j/web3j/issues/1167).


## Pre-requisites  
See https://docs.corda.net/getting-set-up.html.

### Run database
```
docker run --name postgres96 -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:9.6

// clean up the container after stop
docker run --rm --name postgres96-rm -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:9.6
```

### Deploy Settlement contract on Ethereum
[ganache-cli](https://github.com/trufflesuite/ganache-cli) is a fast Ethereum RPC client for testing and development.

You can run ganache-cli and deploy Settlement contract by following [Atomic Swap Ethereum Environment](../atomic-swap-ethereum-env/README.md).

### Create SmartContract Wrapper Class by web3j command
You need to install [Web3j CLI](https://docs.web3j.io/command_line_tools/) first.

Then, you can generate the wrapper class
```
web3j truffle generate ../atomic-swap-ethereum-env/build/contracts/Settlement.json -o ./src/main/java -p jp.co.layerx.cordage.crosschainatomicswap.ethWrapper
```


## Usage
### Running the nodes
See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.


## UAT scenario
### Assumption to participants
There are 3 participants.

Party C is an issuer of CorporateBond.

Party A wants to buy 100 amount of corporate bond that is owned by Party B.

- Party A remits by ether to Party B (The unit price is specified with the initial CorporateBond registration)
- Party B transfers 100 amount of corporate bond to Party A

This is expected to happen in an atomic way.

### Existing processes (nodes)
Several processes (nodes) exist in this scenario.

- Party A's Corda Node
- Party B's Corda Node
- Party C's Corda Node
- Corda Notary; run with PostgreSQL
- Ethereum Node; you may easily run by Ganache CLI

Every Data propagation between Corda and Ethereum is executed by Corda Nodes or Notary.
Other processes are not required.

### Setup
#### Register CorporateBond from Party C
```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.CorporateBondRegisterFlow name: "LayerX", unitPriceEther: "0.012345678901234567", observer: "O=ParticipantA,L=London,C=GB"
```

at the same time, CorporateBond state is shared to Party A to notify the unit price of the corporate bond.

Then, get the linearId of CorporateBond
```
run vaultQuery contractStateType: jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
```

#### Issue CorporateBond from PartyC to Party B
```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.CorporateBondIssueFlow linearId: "52a6335d-f71e-43aa-86c4-696afdee0fdd", quantity: 1000, holder: "O=ParticipantB,L=New York,C=US"
```

Then, get the linearId of issued CorporateBond token by running below from Party B
```
run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken
```

### Propose Cross-Chain Atomic Swap
Run ProposeAtomicSwapFlow from ParticipantA with corporateBondLinearId:

```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.ProposeAtomicSwapFlow corporateBondLinearId: "52a6335d-f71e-43aa-86c4-696afdee0fdd", quantity: 100, swapId: "3", acceptor: "O=ParticipantB,L=New York,C=US", fromEthereumAddress: "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0", toEthereumAddress: "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b", mockLockEtherFlow: null
```

The acceptor ParticipantB can validate this Proposal with `checkTransaction()` in `ProposeAtomicSwapFlowResponder`.
The proposer ParticipantA will lock Ether to Settlement Contract in subflow.

### vaultQuery for Proposal State
```
run vaultQuery contractStateType: jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
```

You can get linearId of Proposal State by the result.

### Start EventWatchFlow

Go to the CRaSH shell for ParticipantB, and run the `StartEventWatchFlow` with `proposalStateLinearId`:

```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.StartEventWatchFlow proposalStateLinearId: "c8944c30-3db1-4e76-a0e2-1d06269d6bac"
```

You can now start monitoring the node's flow activity...

```
flow watch
```

...you will see the `EventWatch` flow running every 10 seconds until you close the Flow Watch window using `ctrl/cmd + c`:

```
xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    Event Watched. (fromBlockNumber: x, toBlockNumber: xxxx)
```

...Or if aimed Ethereum Event was emitted on Ethereum network, `EventWatch` flow will end with below log:

```
xxxxxxxx-xxxx-xxxx-xx Event Watch xxxxxxxxxxxxxxxxxxxx    SettleAtomicSwapFlow has executed with xxxx securities.
```

### Abort Proposal State (This is executed instead of StartEventWatchFlow)
Run AbortAtomicSwapFlow from Proposer(ParticipantA) with ProposalState's linearId:

```
flow start jp.co.layerx.cordage.crosschainatomicswap.flow.AbortAtomicSwapFlow proposalStateLinearId: "c8944c30-3db1-4e76-a0e2-1d06269d6bac"
```

The Notary will unlock Ether to ParticipantA's Ethereum address.
