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
import net.corda.core.utilities.ProgressTracker.Step
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.*
import org.web3j.tx.gas.DefaultGasProvider as DefaultGasProvider1

@InitiatingFlow
@SchedulableFlow
class EventWatchFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    companion object {
        const val ETHEREUM_RPC_URL = "http://localhost:8545"
        object CREATING_WATCHERSTATE: ProgressTracker.Step("Creating new WatcherState.")
        object WATCHING_EVENT: ProgressTracker.Step("Getting Ethereum Events.")
        object GENERATING_TRANSACTION : Step("Generating a WatcherState transaction.")
        object VERIFYING_TRANSACTION : Step("Verifying a WatcherState transaction.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : Step("Recording transaction.") {
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
        val event = Event(eventName,
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {}))

        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
                DefaultBlockParameter.valueOf(toBlockNumber),
                targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()
        if (ethLogs != null) {
            val sendEventLog =  ethLogs.result[0].get() as Log
            val logResult = FunctionReturnDecoder.decode(sendEventLog.data, event.nonIndexedParameters)
            val value = logResult[0].value as BigInteger

            val credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
            val simpleStorage: SimpleStorage = SimpleStorage.load("0xCfEB869F69431e42cdB54A4F4f105C19C080A601", web3, credentials, DefaultGasProvider1())
            val result = simpleStorage.set(value).send()
        }


        // for (event in events) {
            // filter event by swapId
                // if match:
                    // search vault for UTXO State by swapId
                        // if match: subflow(UnlockFlow(UTXO State, event parameters))
                            // In UnlockFlow, verify ethereum event and unlock lockedState
                        // else: do nothing
                // else: do nothing
        // }

        progressTracker.currentStep = CREATING_WATCHERSTATE
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val newFromBlockNumber = toBlockNumber.inc()
        val newToBlockNumber = recentBlockNumber
        val output = WatcherState(ourIdentity, newFromBlockNumber, newToBlockNumber, targetContractAddress, eventName)

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
        subFlow(FinalityFlow(signedTx, listOf()))

        return "Ethereum Event Watching Complete! fromBlockNumber: ${fromBlockNumber}, toBlockNumber: ${toBlockNumber}, targetContractAddress: ${targetContractAddress}, eventName: $eventName"
    }
}