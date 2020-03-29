package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class AbortAtomicSwapFlow(val linearId: UniqueIdentifier): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val proposalStateAndRef =  serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()
        val inputProposal = proposalStateAndRef.state.data

        if (ourIdentity != inputProposal.proposer) {
            throw IllegalArgumentException("Proposal abort can only be initiated by the Proposal proposer.")
        }

        val outputProposal = inputProposal.withNewStatus(ProposalStatus.ABORTED)

        val signers = inputProposal.proposer.owningKey
        val abortCommand = Command(ProposalContract.Commands.Abort(), signers)

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
            .addInputState(proposalStateAndRef)
            .addOutputState(outputProposal, ProposalContract.contractID)
            .addCommand(abortCommand)

        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        return subFlow(FinalityFlow(signedTx, listOf()))
    }
}

@InitiatedBy(AbortAtomicSwapFlow::class)
class AbortAtomicSwapFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an Proposal transaction" using (output is ProposalState)
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}
