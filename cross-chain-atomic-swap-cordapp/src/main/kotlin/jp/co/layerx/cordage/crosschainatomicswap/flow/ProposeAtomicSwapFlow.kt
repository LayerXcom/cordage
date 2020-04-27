package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.of
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import org.web3j.utils.Convert
import java.math.BigDecimal

@InitiatingFlow
@StartableByRPC
class ProposeAtomicSwapFlow(
    private val corporateBondLinearId: UniqueIdentifier,
    private val quantity: Long,
    private val swapId: String,
    private val acceptor: Party,
    private val fromEthereumAddress: String,
    private val toEthereumAddress: String,
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
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(corporateBondLinearId))
        val corporateBond = serviceHub.vaultService.queryBy<CorporateBond>(criteria).states.single().state.data

        val priceEther = corporateBond.unitPriceEther.multiply(BigDecimal(quantity))
        val priceWei = Convert.toWei(priceEther, Convert.Unit.ETHER).toBigInteger()

        progressTracker.currentStep = CREATE_OUTPUTSTATE
        val proposer = ourIdentity
        val outputProposal = ProposalState(
            corporateBondLinearId,
            quantity of corporateBond.toPointer<CorporateBond>(),
            priceWei,
            swapId,
            proposer,
            acceptor,
            fromEthereumAddress,
            toEthereumAddress,
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
        requireThat {
            "ourIdentity's address must equal to fromEthereumAddress." using (outputProposal.fromEthereumAddress == proposer.ethAddress())
            "Acceptor's address must equal to toEthereumAddress." using (outputProposal.toEthereumAddress == acceptor.ethAddress())
        }
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
                val proposalState = stx.tx.outputs.single().data
                "This must be an Proposal transaction" using (proposalState is ProposalState)
                proposalState as ProposalState
                "ourIdentity must be an acceptor." using (proposalState.acceptor == ourIdentity)
                "Proposer's address must equal to fromEthereumAddress." using (proposalState.fromEthereumAddress == proposalState.proposer.ethAddress())
                "ourIdentity's address must equal to toEthereumAddress." using (proposalState.toEthereumAddress == ourIdentity.ethAddress())
            }
        }

        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))
    }
}
