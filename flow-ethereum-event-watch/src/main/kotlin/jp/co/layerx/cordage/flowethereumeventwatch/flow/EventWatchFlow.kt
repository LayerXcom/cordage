package jp.co.layerx.cordage.flowethereumeventwatch.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract.Companion.contractID
import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage
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
        // TODO Use Node Configuration https://github.com/LayerXcom/cordage/issues/20
        val eventMapping = mapOf<String, Event>("Set" to SimpleStorage.SET_EVENT)

        object READING_CONFIG : ProgressTracker.Step("Reading config from file.")
        object CREATING_WATCHERSTATE : ProgressTracker.Step("Creating new WatcherState.")
        object WATCHING_EVENT : ProgressTracker.Step("Getting Ethereum Events.")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a WatcherState transaction.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying a WatcherState transaction.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            READING_CONFIG,
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
        progressTracker.currentStep = READING_CONFIG
        val config = serviceHub.getAppContext().config
        val ETHEREUM_RPC_URL = config.getString("rpcUrl")
        val ETHEREUM_PRIVATE_KEY = config.getString("privateKey")

        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val credentials = Credentials.create(ETHEREUM_PRIVATE_KEY)

        progressTracker.currentStep = WATCHING_EVENT
        val input = serviceHub.toStateAndRef<WatcherState>(stateRef)
        val fromBlockNumber = input.state.data.fromBlockNumber
        val toBlockNumber = input.state.data.toBlockNumber
        val targetContractAddress = input.state.data.targetContractAddress
        val eventName = input.state.data.eventName
        val searchId = input.state.data.searchId
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
                    doSomething(input.state.data, web3, credentials)
                    return "Ethereum Event with id: $searchId watched and send TX Completed"
                }
            }
        }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val newFromBlockNumber = toBlockNumber.inc()
        val output = WatcherState(ourIdentity, newFromBlockNumber, recentBlockNumber, targetContractAddress, eventName, searchId)

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

    private fun doSomething(input: WatcherState, web3: Web3j, credentials: Credentials) {
        val simpleStorage: SimpleStorage = SimpleStorage.load(input.targetContractAddress, web3, credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(500000)))
        simpleStorage.set(input.searchId.inc()).send()
    }
}
