package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.sumTokenStatesOrThrow
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveFungibleTokens
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import jp.co.layerx.cordage.crosschainatomicswap.types.SwapDetail
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256

@InitiatingFlow
@StartableByRPC
class SettleAtomicSwapFlow(
    private val proposalStateRef: StateAndRef<ProposalState>,
    private val swapDetail: SwapDetail
) : FlowLogic<SignedTransaction>() {
    companion object {
        object VERIFY_ETHEREUM_EVENT : Step("Verify swapDetail against inputProposal (Verify Ethereum Event Content).")
        object PREPARE_INPUTSTATES : Step("Preparing Input ProposalState and SecurityState.")
        object CREATE_OUTPUTSTATES : Step("Creating Output ProposalState and SecurityState.")
        object GENERATING_TRANSACTION : Step("Generating transaction.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Having notary unlock locked-ether, obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            VERIFY_ETHEREUM_EVENT,
            PREPARE_INPUTSTATES,
            CREATE_OUTPUTSTATES,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = VERIFY_ETHEREUM_EVENT
        // TODO Verify Ethereum Event Structure and Finality
        val inputProposal = proposalStateRef.state.data

        requireThat {
            "Proposer's address must equal to fromEthereumAddress." using (inputProposal.fromEthereumAddress == inputProposal.proposer.ethAddress())
            "ourIdentity's address must equal to toEthereumAddress." using (inputProposal.toEthereumAddress == ourIdentity.ethAddress())
            "swapDetail from Ethereum Event must have the same fromEthereumAddress to ProposalState's." using (swapDetail.fromEthereumAddress == Address(inputProposal.fromEthereumAddress))
            "swapDetail from Ethereum Event must have the same toEthereumAddress to ProposalState's." using (swapDetail.toEthereumAddress == Address(inputProposal.toEthereumAddress))
            "swapDetail from Ethereum Event must have the same weiAmount to ProposalState's." using (swapDetail.weiAmount == Uint256(inputProposal.priceWei))
            "swapDetail from Ethereum Event must have the same securityAmount to ProposalState's." using (swapDetail.securityAmount == Uint256(inputProposal.amount.quantity.toBigInteger()))
            "swapDetail from Ethereum Event must have the same status to ProposalState's." using (swapDetail.status == inputProposal.status)
        }

        progressTracker.currentStep = PREPARE_INPUTSTATES
        if (ourIdentity != inputProposal.acceptor) {
            throw IllegalArgumentException("Proposal settle can only be initiated by the Proposal acceptor.")
        }

        progressTracker.currentStep = CREATE_OUTPUTSTATES
        val newOwner = inputProposal.proposer
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        progressTracker.currentStep = GENERATING_TRANSACTION
        val proposalSigners = (inputProposal.participants).map { it.owningKey }
        val consumeCommand = Command(ProposalContract.ProposalCommands.Consume(), proposalSigners)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
            .addInputState(proposalStateRef)
            .addOutputState(outputProposal, ProposalContract.contractID)
            .addCommand(consumeCommand)

        addMoveFungibleTokens(txBuilder, serviceHub, proposalStateRef.state.data.amount, newOwner, ourIdentity)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.apply {
            require(outputProposal.amount.quantity == outputStates().map { it.data }.filterIsInstance<FungibleToken>()
                .filter { it.holder == newOwner }.sumTokenStatesOrThrow().quantity)
            verify(serviceHub)
        }

        progressTracker.currentStep = SIGNING_TRANSACTION
        val partlySignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = GATHERING_SIGS
        val sessionParties = inputProposal.participants - ourIdentity
        val otherPartySessions = (sessionParties).map { initiateFlow(it) }.toSet()
        val fullySignedTx = subFlow(CollectSignaturesFlow(partlySignedTx, otherPartySessions))

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions))
    }
}

@InitiatedBy(SettleAtomicSwapFlow::class)
class SettleAtomicSwapFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val proposalState = stx.tx.outputsOfType<ProposalState>().single()
                val fungibleTokens = stx.tx.outputsOfType<FungibleToken>()
                requireThat {
                    "ourIdentity's address must equal to fromEthereumAddress." using (proposalState.fromEthereumAddress == ourIdentity.ethAddress())
                    "Acceptor's address must equal to toEthereumAddress." using (proposalState.toEthereumAddress == proposalState.acceptor.ethAddress())
                    "The Sum of tokens belong to us must equal to the proposed amount." using
                        (proposalState.amount.quantity == fungibleTokens.filter { it.holder == ourIdentity }.sumTokenStatesOrThrow().quantity)
                }
            }
        }

        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))
    }
}
