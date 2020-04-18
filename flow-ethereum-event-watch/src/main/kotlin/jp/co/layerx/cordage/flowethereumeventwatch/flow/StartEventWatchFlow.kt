package jp.co.layerx.cordage.flowethereumeventwatch.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage
import jp.co.layerx.cordage.flowethereumeventwatch.state.WatcherState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

@InitiatingFlow
@StartableByRPC
class StartEventWatchFlow(private val searchId: Int) : FlowLogic<Unit>() {
    companion object {
        // TODO Some ethereum parameters should be imported by .env
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        private const val ETHEREUM_NETWORK_ID = "5777"
        const val EVENT_NAME = "Set"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val targetContractAddress = SimpleStorage.getPreviouslyDeployedAddress(ETHEREUM_NETWORK_ID)
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
        progressTracker.currentStep = CREATING_WATCHERSTATE
        val fromBlockNumber = BigInteger.ZERO
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val output = WatcherState(ourIdentity, fromBlockNumber, recentBlockNumber, targetContractAddress, EVENT_NAME, searchId.toBigInteger())

        progressTracker.currentStep = GENERATING_TRANSACTION
        val cmd = Command(WatcherContract.Commands.Issue(), ourIdentity.owningKey)
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
