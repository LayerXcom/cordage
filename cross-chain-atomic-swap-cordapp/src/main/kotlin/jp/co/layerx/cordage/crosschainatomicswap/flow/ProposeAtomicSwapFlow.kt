package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class ProposeAtomicSwapFlow(val state: ProposalState): FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val proposeCommand = Command(ProposalContract.Commands.Propose(), state.participants.map { it.owningKey })

        val builder = TransactionBuilder(notary = notary)

        builder.addOutputState(state, ProposalContract.contractID)
        builder.addCommand(proposeCommand)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        val finalizedTx = subFlow(FinalityFlow(stx, sessions))

        val finalizedProposalState = finalizedTx.coreTransaction.outputsOfType<ProposalState>().first()
        return subFlow(LockEtherFlow(finalizedProposalState))
    }
}

@InitiatedBy(ProposeAtomicSwapFlow::class)
class ProposeAtomicSwapFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an Proposal transaction" using (output is ProposalState)
                // add any validation by yourself
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        val signedTx = subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
//        val signedProposalState = signedTx.coreTransaction.outputsOfType<ProposalState>().first()
        val signedProposalStateAndRef = signedTx.coreTransaction.outRefsOfType<ProposalState>().first()
        subFlow(StartEventWatchFlow(signedProposalStateAndRef))
    }
}
