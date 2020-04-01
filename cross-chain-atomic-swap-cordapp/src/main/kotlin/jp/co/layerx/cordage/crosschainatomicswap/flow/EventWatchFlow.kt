package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract.Companion.contractID
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.WatcherState
import jp.co.layerx.cordage.crosschainatomicswap.toHex
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
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import java.util.*

@InitiatingFlow
@SchedulableFlow
class EventWatchFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    companion object {
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        // TODO credentials should be imported by .env
        val credentials: Credentials = Credentials.create("0x6370fd033278c143179d81c5526140625662b8daa446c22ee2d73db3707e620c")
        val eventMapping = mapOf<String, Event>("Locked" to Settlement.LOCKED_EVENT)

        val swapDetailType = Arrays.asList<TypeReference<*>?>(
            object : TypeReference<Address?>() {},
            object : TypeReference<Address?>() {},
            object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint8?>() {}
        )

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
        val fromBlockNumber = input.state.data.fromBlockNumber
        val toBlockNumber = input.state.data.toBlockNumber
        val targetContractAddress = input.state.data.targetContractAddress
        val eventName = input.state.data.eventName
        val proposalStateAndRef = input.state.data.proposalStateAndRef
        val proposalState = proposalStateAndRef.state.data
        val searchId = proposalState.swapId
        val event = eventMapping[eventName]

        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
                DefaultBlockParameter.valueOf(toBlockNumber),
                targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()

        val decodedLogs = ethLogs.result?.map { (it.get() as Log).data }
                ?.map { DefaultFunctionReturnDecoder.decode(it, event?.nonIndexedParameters) }
        if (decodedLogs != null && decodedLogs.isNotEmpty()) {
            decodedLogs.forEach { abiTypes ->
                // find event values by searchId
                val eventValues = abiTypes?.map { it.value }
                if (eventValues != null && eventValues.isNotEmpty()) {
                    val lockedEvent = eventValues?.let { LockedEvent.listToLockedEvent(it) }
                    if (lockedEvent.swapId.equals(searchId)) {
                        val encodedSwapDetail = lockedEvent.encodedSwapDetail
                        val stringEncodedSwapDetail = "0x" + encodedSwapDetail.toHex()
                        val decodedSwapDetailList = DefaultFunctionReturnDecoder.decode(stringEncodedSwapDetail, swapDetailType as MutableList<TypeReference<Type<Any>>>?)
                        val swapDetail = SwapDetail.listToSwapDetail(decodedSwapDetailList)
                        // Just pass the LockedEvent's swapDetail to SettleAtomicSwapFlow
                        subFlow(SettleAtomicSwapFlow(proposalStateAndRef, swapDetail))

                        return "SettleAtomicSwapFlow has executed with ${swapDetail.securityAmount} securities."
                    }
                }
            }
        }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val newFromBlockNumber = toBlockNumber.inc()
        val output = WatcherState(ourIdentity, newFromBlockNumber, recentBlockNumber, targetContractAddress, eventName, proposalStateAndRef)

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

        return "Event Watched. (fromBlockNumber: ${fromBlockNumber}, toBlockNumber: ${toBlockNumber})"
    }
}
