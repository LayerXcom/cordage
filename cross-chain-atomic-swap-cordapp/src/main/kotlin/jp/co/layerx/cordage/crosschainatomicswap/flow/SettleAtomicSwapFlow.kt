package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import jp.co.layerx.cordage.crosschainatomicswap.types.SwapDetail
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

@InitiatingFlow
@StartableByRPC
class SettleAtomicSwapFlow(val proposalStateRef: StateAndRef<ProposalState>, val swapDetail: SwapDetail): FlowLogic<SignedTransaction>() {
    companion object {
        object VERIFY_ETHEREUM_EVENT: Step("Verify swapDetail against inputProposal (Verify Ethereum Event Content).")
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
            "swapDetail from Ethereum Event must have the same fromEthereumAddress to ProposalState's." using (swapDetail.fromEthereumAddress.toString() == inputProposal.fromEthereumAddress)
            "swapDetail from Ethereum Event must have the same toEthereumAddress to ProposalState's." using (swapDetail.toEthereumAddress.toString() == inputProposal.toEthereumAddress)
            "swapDetail from Ethereum Event must have the same weiAmount to ProposalState's." using (swapDetail.weiAmount == inputProposal.weiAmount)
            "swapDetail from Ethereum Event must have the same securityAmount to ProposalState's." using (swapDetail.securityAmount == inputProposal.securityAmount)
            "swapDetail from Ethereum Event must have the same status to ProposalState's." using (swapDetail.status == inputProposal.status)
        }

        progressTracker.currentStep = PREPARE_INPUTSTATES
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(inputProposal.securityLinearId))
        val securityStateAndRef =  serviceHub.vaultService.queryBy<SecurityState>(queryCriteria).states.single()
        val inputSecurity = securityStateAndRef.state.data

        if (ourIdentity != inputProposal.acceptor) {
            throw IllegalArgumentException("Proposal settle can only be initiated by the Proposal acceptor.")
        }

        if (ourIdentity != inputSecurity.owner) {
            throw IllegalArgumentException("Security transfer can only be initiated by the Security owner.")
        }

        progressTracker.currentStep = CREATE_OUTPUTSTATES
        val newOwner = inputProposal.proposer
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)
        val outputSecurity = inputSecurity.withNewOwner(newOwner)

        progressTracker.currentStep = GENERATING_TRANSACTION
        val proposalSigners = (inputProposal.participants).map { it.owningKey }
        val securitySigners = (inputSecurity.participants + newOwner).map { it.owningKey }
        val consumeCommand= Command(ProposalContract.ProposalCommands.Consume(), proposalSigners)
        val transferForSettleCommand = Command(SecurityContract.SecurityCommands.TransferForSettle(), securitySigners)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
            .addInputState(proposalStateRef)
            .addOutputState(outputProposal,ProposalContract.contractID)
            .addCommand(consumeCommand)
            .addInputState(securityStateAndRef)
            .addOutputState(outputSecurity, SecurityContract.contractID)
            .addCommand(transferForSettleCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val partlySignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = GATHERING_SIGS
        val sessionParties = outputSecurity.participants union inputProposal.participants - ourIdentity
        val otherPartySessions = (sessionParties).map { initiateFlow(it) }.toSet()
        val fullySignedTx = subFlow(CollectSignaturesFlow(partlySignedTx, otherPartySessions))

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions))
    }
}

@InitiatedBy(SettleAtomicSwapFlow::class)
class SettleAtomicSwapFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // add any validation by yourself
                // val securityOutput = stx.tx.outputsOfType<SecurityState>().first()
                // val proposalOutput = stx.tx.outputsOfType<ProposalState>().first()
            }
        }

        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))
    }
}

