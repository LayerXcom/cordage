package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalStatus
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class ProposeAtomicSwapFlow(private val securityLinearIdString: String,
                            private val securityAmount: Int,
                            private val etherAmount: Int,
                            private val swapId: String,
                            private val proposer: Party,
                            private val acceptor: Party,
                            private val FromEthereumAddress: String,
                            private val ToEthereumAddress: String): FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        val securityLinearId = UniqueIdentifier.fromString(securityLinearIdString)
        val status: ProposalStatus = ProposalStatus.PROPOSED
        val state = ProposalState(securityLinearId,
            securityAmount,
            etherAmount.toBigDecimal(),
            swapId,
            proposer,
            acceptor,
            FromEthereumAddress,
            ToEthereumAddress,
            status)
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
