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
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

@InitiatingFlow
@StartableByRPC
class AbortAtomicSwapFlow(private val proposalStateLinearId: String): FlowLogic<SignedTransaction>() {
    companion object {
        object PREPARE_INPUTSTATE : Step("Preparing Input ProposalState.")
        object CREATE_OUTPUTSTATE : Step("Creating Output ProposalState.")
        object GENERATING_TRANSACTION : Step("Generating transaction.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : Step("Having Notary abort locked-ether, obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            PREPARE_INPUTSTATE,
            CREATE_OUTPUTSTATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = PREPARE_INPUTSTATE
        val linearId = UniqueIdentifier.fromString(proposalStateLinearId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val proposalStateAndRef =  serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()
        val inputProposal = proposalStateAndRef.state.data

        if (ourIdentity != inputProposal.proposer) {
            throw IllegalArgumentException("Proposal abort can only be initiated by the Proposal proposer.")
        }

        progressTracker.currentStep = CREATE_OUTPUTSTATE
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.ABORTED)

        progressTracker.currentStep = GENERATING_TRANSACTION
        val signers = (inputProposal.participants).map { it.owningKey } - inputProposal.acceptor.owningKey
        val abortCommand = Command(ProposalContract.ProposalCommands.Abort(), signers)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
            .addInputState(proposalStateAndRef)
            .addOutputState(outputProposal, ProposalContract.contractID)
            .addCommand(abortCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        val otherPartySessions = (inputProposal.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        return subFlow(FinalityFlow(signedTx, otherPartySessions))
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

        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))
    }
}
