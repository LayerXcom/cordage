package jp.co.layerx.cordage.flowethereumeventwatch.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract.Companion.contractID
import jp.co.layerx.cordage.flowethereumeventwatch.state.WatcherState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.abi.DefaultFunctionReturnDecoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

@InitiatingFlow
@SchedulableFlow
class EventWatchFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    companion object {
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        private const val ETHEREUM_NETWORK_ID = "5777"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        object CREATING_WATCHERSTATE: ProgressTracker.Step("Creating new WatcherState.")
        object WATCHING_EVENT: ProgressTracker.Step("Getting Ethereum Events.")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a WatcherState transaction.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying a WatcherState transaction.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                CREATING_WATCHERSTATE,
                WATCHING_EVENT,
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = WATCHING_EVENT
        val input = serviceHub.toStateAndRef<WatcherState>(stateRef)
        val watcherState = input.state.data
        val targetContract = watcherState.targetContract
        val event = watcherState.event
        val eventParameters = watcherState.eventParameters
        val searchId = watcherState.searchId
        val followingFlow = watcherState.followingFlow

        val filter = EthFilter(DefaultBlockParameter.valueOf(watcherState.fromBlockNumber),
                DefaultBlockParameter.valueOf(watcherState.toBlockNumber),
                targetContract.getDeployedAddress(ETHEREUM_NETWORK_ID))

        val ethLogs = web3.ethGetLogs(filter).send()

        val decodedLogs = ethLogs.result?.map { (it.get() as Log).data }
                ?.map { DefaultFunctionReturnDecoder.decode(it, event?.nonIndexedParameters) }
        if (decodedLogs != null && decodedLogs.isNotEmpty()) {
            decodedLogs.forEach { abiTypes ->
                // find event values by searchId
                val eventValues = abiTypes?.map { it.value }
                if (eventValues != null && eventValues.isNotEmpty()) {
                    val eventParameters = eventParameters.fromList(eventValues)
                    if (eventParameters.searchId == searchId) {
                        // Just pass the target event's parameters to following Flow
                        subFlow(followingFlow(watcherState, eventParameters))
                        return "Following Flow has executed."
                    }
                }
            }
        }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val output = watcherState.copy(
            fromBlockNumber = watcherState.toBlockNumber.inc(),
            toBlockNumber = recentBlockNumber
        )

        progressTracker.currentStep = GENERATING_TRANSACTION
        val watchCmd = Command(WatcherContract.Commands.Watch(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addInputState(input)
                .addOutputState(output, contractID)
                .addCommand(watchCmd)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        subFlow(FinalityFlow(signedTx, listOf(), FINALISING_TRANSACTION.childProgressTracker()))

        return "Event Watched. (fromBlockNumber: ${watcherState.fromBlockNumber}, toBlockNumber: ${watcherState.toBlockNumber})"
    }
}
