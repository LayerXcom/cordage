package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

@InitiatingFlow
@StartableByRPC
class ProposeAtomicSwapFlow(
    private val securityLinearId: String,
    private val securityAmount: Int,
    private val weiAmount: Long,
    private val swapId: String,
    private val acceptor: Party,
    private val FromEthereumAddress: String,
    private val ToEthereumAddress: String,
    private val mockLockEtherFlow: LockEtherFlow? = null
) : FlowLogic<Pair<SignedTransaction, String>>() {
    companion object {
        object CREATE_OUTPUTSTATE : Step("Creating Output ProposalState.")
        object GENERATING_TRANSACTION : Step("Generating transaction based on new ProposalState.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        object EXECUTING_LOCKETHERFLOW : Step("Executing LockEtherFlow with finalized Proposal.") {
            override fun childProgressTracker() = LockEtherFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            CREATE_OUTPUTSTATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION,
            EXECUTING_LOCKETHERFLOW
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): Pair<SignedTransaction, String> {
        progressTracker.currentStep = CREATE_OUTPUTSTATE
        val proposer = ourIdentity
        val linearId = UniqueIdentifier.fromString(securityLinearId)
        val outputProposal = ProposalState(
            linearId,
            securityAmount.toBigInteger(),
            weiAmount.toBigInteger(),
            swapId,
            proposer,
            acceptor,
            FromEthereumAddress,
            ToEthereumAddress,
            ProposalStatus.PROPOSED
        )

        progressTracker.currentStep = GENERATING_TRANSACTION
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val signers = outputProposal.participants.map { it.owningKey }
        val proposeCommand = Command(ProposalContract.ProposalCommands.Propose(), signers)
        val txBuilder = TransactionBuilder(notary = notary)
            .addOutputState(outputProposal, ProposalContract.contractID)
            .addCommand(proposeCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val partlySignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = GATHERING_SIGS
        val otherPartySessions = (outputProposal.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val fullySignedTx = subFlow(CollectSignaturesFlow(partlySignedTx, otherPartySessions))

        progressTracker.currentStep = FINALISING_TRANSACTION
        val finalizedTx = subFlow(FinalityFlow(fullySignedTx, otherPartySessions))

        progressTracker.currentStep = EXECUTING_LOCKETHERFLOW
        val finalizedProposalState = finalizedTx.coreTransaction.outputsOfType<ProposalState>().first()

        val txHash = subFlow(mockLockEtherFlow ?: LockEtherFlow(finalizedProposalState))
        return Pair(finalizedTx, txHash)
    }
}

@InitiatedBy(ProposeAtomicSwapFlow::class)
class ProposeAtomicSwapFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an Proposal transaction" using (output is ProposalState)
                // add any validation by yourself
            }
        }

        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))

        // If you agree the Proposal in checkTransaction function, execute StartEventWatchFlow automatically as below.
        // val signedTx = subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
        // val signedProposalState = signedTx.coreTransaction.outputsOfType<ProposalState>().first()
        // subFlow(StartEventWatchFlow(signedProposalState.linearId))
    }
}
