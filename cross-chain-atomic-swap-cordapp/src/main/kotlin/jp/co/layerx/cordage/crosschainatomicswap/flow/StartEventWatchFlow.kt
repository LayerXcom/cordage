package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.WatcherState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

@InitiatingFlow
@StartableByRPC
class StartEventWatchFlow(private val proposalStateLinearId: UniqueIdentifier) : FlowLogic<Unit>() {
    companion object {
        // TODO Use Node Configuration https://github.com/LayerXcom/cordage/issues/20
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        private const val ETHEREUM_NETWORK_ID = "5777"
        const val EVENT_NAME = "Locked"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val targetContractAddress = Settlement.getPreviouslyDeployedAddress(ETHEREUM_NETWORK_ID)!!
        object CREATING_WATCHERSTATE: ProgressTracker.Step("Creating new WatcherState.")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a WatcherState transaction.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying a WatcherState transaction.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                CREATING_WATCHERSTATE,
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalStateLinearId))
        val signedProposalStateAndRef =  serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val fromBlockNumber = BigInteger.ZERO
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val output = WatcherState(ourIdentity, fromBlockNumber, recentBlockNumber, targetContractAddress, EVENT_NAME, signedProposalStateAndRef)

        progressTracker.currentStep = GENERATING_TRANSACTION
        val cmd = Command(WatcherContract.WatcherCommands.Issue(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addOutputState(output, WatcherContract.contractID)
                .addCommand(cmd)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        subFlow(FinalityFlow(signedTx, listOf(), FINALISING_TRANSACTION.childProgressTracker()))
    }
}
