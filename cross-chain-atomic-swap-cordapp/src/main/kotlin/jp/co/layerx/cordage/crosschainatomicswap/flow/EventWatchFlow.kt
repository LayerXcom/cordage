package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract.Companion.contractID
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.SimpleStorage
import jp.co.layerx.cordage.crosschainatomicswap.state.WatcherState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.abi.DefaultFunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.crypto.Credentials
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
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        // TODO credentials should be imported by .env
        val credentials: Credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
        val eventMapping = mapOf<String, Event>("Set" to SimpleStorage.SET_EVENT)
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
        val searchId = proposalState.swapID
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
                val eventValues = abiTypes?.map { it.value as BigInteger }
                val filteredEventValues = eventValues?.filter { e -> e == searchId }
                if (filteredEventValues != null && filteredEventValues.isNotEmpty()) {
//                    doSomething(input.state.data)
                    subFlow(SecurityTransferToOtherChainFlow(proposalStateAndRef))
                    return "Ethereum Event with id: $searchId watched and send TX Completed"
                }
            }
        }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val newFromBlockNumber = toBlockNumber.inc()
        val output = WatcherState(ourIdentity, newFromBlockNumber, recentBlockNumber, targetContractAddress, eventName, proposalStateAndRef)

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

        return "Event Watched. (fromBlockNumber: ${fromBlockNumber}, toBlockNumber: ${toBlockNumber})"
    }

    private fun doSomething(input: WatcherState) {
        val simpleStorage: SimpleStorage = SimpleStorage.load(input.targetContractAddress, web3, credentials,
                StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(500000)))
        simpleStorage.set(input.proposalStateAndRef.state.data.swapID.inc()).send()
    }
}
