package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract.Companion.contractID
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.WatcherState
import jp.co.layerx.cordage.crosschainatomicswap.types.LockedEvent
import jp.co.layerx.cordage.crosschainatomicswap.types.SwapDetail
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.abi.DefaultFunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService

@InitiatingFlow
@SchedulableFlow
class EventWatchFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    companion object {
        val eventMapping = mapOf<String, Event>("Locked" to Settlement.LOCKED_EVENT)

        val swapDetailType = listOf(
            object : TypeReference<Address?>() {},
            object : TypeReference<Address?>() {},
            object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint8?>() {}
        )

        object READING_CONFIG: ProgressTracker.Step("Reading config from node config file.")
        object LOADING_WEB3: ProgressTracker.Step("Loading web3 instance.")
        object CREATING_WATCHERSTATE: ProgressTracker.Step("Creating new WatcherState.")
        object WATCHING_EVENT: ProgressTracker.Step("Getting Ethereum Events.")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a WatcherState transaction.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying a WatcherState transaction.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            WATCHING_EVENT,
            CREATING_WATCHERSTATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = READING_CONFIG

        val config = serviceHub.getAppContext().config
        val ETHEREUM_RPC_URL = config.getString("rpcUrl")

        progressTracker.currentStep = LOADING_WEB3

        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        progressTracker.currentStep = WATCHING_EVENT
        val input = serviceHub.toStateAndRef<WatcherState>(stateRef)
        val watcherState = input.state.data
        val proposalState = watcherState.proposalStateAndRef.state.data
        val searchId = proposalState.swapId
        val event = eventMapping[watcherState.eventName]

        val filter = EthFilter(DefaultBlockParameter.valueOf(watcherState.fromBlockNumber),
                DefaultBlockParameter.valueOf(watcherState.toBlockNumber),
                watcherState.targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()

        val decodedLogs = ethLogs.result?.map { (it.get() as Log).data }
                ?.map { DefaultFunctionReturnDecoder.decode(it, event?.nonIndexedParameters) }
        if (decodedLogs != null && decodedLogs.isNotEmpty()) {
            decodedLogs.forEach { abiTypes ->
                // find event values by searchId
                val eventValues = abiTypes?.map { it.value }
                if (eventValues != null && eventValues.isNotEmpty()) {
                    val lockedEvent = LockedEvent.listToLockedEvent(eventValues)
                    if (lockedEvent.swapId == searchId) {
                        val swapDetail = SwapDetail.fromLockedEvent(lockedEvent)
                        // Just pass the LockedEvent's swapDetail to SettleAtomicSwapFlow
                        subFlow(SettleAtomicSwapFlow(watcherState.proposalStateAndRef, swapDetail))

                        return "SettleAtomicSwapFlow has executed with ${swapDetail.securityAmount} securities."
                    }
                }
            }
        }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val output = WatcherState(
            ourIdentity,
            watcherState.toBlockNumber.inc(),
            recentBlockNumber,
            watcherState.targetContractAddress,
            watcherState.eventName,
            watcherState.proposalStateAndRef
        )

        progressTracker.currentStep = GENERATING_TRANSACTION
        val watchCmd = Command(WatcherContract.WatcherCommands.Watch(), ourIdentity.owningKey)
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
