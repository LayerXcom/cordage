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
import net.corda.core.utilities.trace
import org.web3j.abi.DefaultFunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Uint256
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
        const val ETHEREUM_RPC_URL = "http://localhost:8545"
//        object CREATING_WATCHERSTATE: ProgressTracker.Step("Creating new WatcherState.")
//        object WATCHING_EVENT: ProgressTracker.Step("Getting Ethereum Events.")
//        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a WatcherState transaction.")
//        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying a WatcherState transaction.")
//        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
//        object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
//            override fun childProgressTracker() = FinalityFlow.tracker()
//        }
//
//        fun tracker() = ProgressTracker(
//                CREATING_WATCHERSTATE,
//                WATCHING_EVENT,
//                GENERATING_TRANSACTION,
//                VERIFYING_TRANSACTION,
//                SIGNING_TRANSACTION,
//                FINALISING_TRANSACTION
//        )
    }

//    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
//        progressTracker.currentStep = WATCHING_EVENT
        val input = serviceHub.toStateAndRef<WatcherState>(stateRef)
        val fromBlockNumber = input.state.data.fromBlockNumber
        val toBlockNumber = input.state.data.toBlockNumber
        val targetContractAddress = input.state.data.targetContractAddress
        val searchId = input.state.data.searchId
        val eventName = input.state.data.eventName
        val event = Event(eventName,
                listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {}))

        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
                DefaultBlockParameter.valueOf(toBlockNumber),
                targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()
        val decodedLogs = ethLogs.result?.map { (it.get() as Log).data }
                ?.map { DefaultFunctionReturnDecoder.decode(it, event.nonIndexedParameters) }
        if (decodedLogs != null && decodedLogs.isNotEmpty()) {
            for (decodedLog in decodedLogs) {
                val eventValues = decodedLog?.map { it.value as BigInteger }
                // EventのvalueがsearchIdと一致する場合にはそれをincrementして新たな値としてsetし、WatcherState Loopを終了する
                val filteredEventValues = eventValues?.filter { e -> e == searchId }
                if (filteredEventValues != null && filteredEventValues.isNotEmpty()) {
                    for (filteredEventValue in filteredEventValues) {
                        val credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
                        val simpleStorage: SimpleStorage = SimpleStorage.load(targetContractAddress, web3, credentials, StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(500000)))
                        val result = simpleStorage.set(filteredEventValue.inc()).send()
                        return "Ethereum Event with id: ${searchId} watched and send TX Completed"
                    }
                }
            }
        }
        logger.trace{ "passed through if sentence." }

//        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        logger.trace{ "after get recent block number." }
        web3.shutdown()
        val newFromBlockNumber = toBlockNumber.inc()
        val newToBlockNumber = recentBlockNumber
        val output = WatcherState(ourIdentity, newFromBlockNumber, newToBlockNumber, targetContractAddress, eventName, searchId)
        logger.trace{ "after make output state." }

//        progressTracker.currentStep = GENERATING_TRANSACTION
        val watchCmd = Command(WatcherContract.Commands.Watch(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addInputState(input)
                .addOutputState(output, contractID)
                .addCommand(watchCmd)

        logger.trace{ "after transaction builder" }
//        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)
        logger.trace{ "after txBuilder." }

//        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        logger.trace{ "after signInitialTransaction." }

//        progressTracker.currentStep = FINALISING_TRANSACTION
//        subFlow(FinalityFlow(signedTx, listOf(), FINALISING_TRANSACTION.childProgressTracker()))
        subFlow(FinalityFlow(signedTx, listOf()))
        logger.trace{ "after FinalityFlow." }

        return "Event Watched. fromBlockNumber: ${fromBlockNumber}, toBlockNumber: ${toBlockNumber}"
    }
}