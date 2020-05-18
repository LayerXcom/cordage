# Custom Notary Flow
<p align="center">
  <img src="https://layerxcom.github.io/cordage/images/corda.png" alt="Corda" width="500">
</p>

<p align="center">
  <img src="https://layerxcom.github.io/cordage/images/ethereum.png" alt="Ethereum" width="500">
</p>

This CorDapp provides a simple example of how to propagate data from Corda to Ethereum by creating a Custom Notary Flow.
This case consists of 2 flows and 1 custom notary flow:

- MakeAgreementFlow - Making an agreement between 2 parties
- TerminateAgreementFlow - Terminating the agreement
- CustomValidatingNotaryFlow - Sending a transaction that transfers 1 ether from an account to the other account only if the command is Terminate 

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

### Running the database:
```
docker run --name postgres96 -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:9.6
```

### Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

Use the `deployNodes` task and `./build/nodes/runnodes` script.

### Interacting with the nodes:

You'll be interacting with the node via its interactive shell.

Run MakeAgreementFlow from PartyA:
```
flow start jp.co.layerx.cordage.customnotaryflow.flows.MakeAgreementFlow target: "O=ParticipantB,L=Tokyo,C=JP", agreementBody: "RESIDENTIAL LEASE AGREEMENT"
```

Run this from both PartyA and PartyB:
```
run vaultQuery contractStateType: jp.co.layerx.cordage.customnotaryflow.states.Agreement
```

Run TerminateAgreementFlow from PartyA or PartyB:
```
flow start jp.co.layerx.cordage.customnotaryflow.flows.TerminateAgreementFlow linearId: "661504cb-ba74-4bd5-9b93-940201ca7a11"
```
