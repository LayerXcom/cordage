package jp.co.layerx.cordage.flowethereumeventwatch.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.state.WatcherState
import jp.co.layerx.cordage.flowethereumeventwatch.types.EventParameters
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Contract
import java.math.BigInteger
import java.time.Instant

@InitiatingFlow
@StartableByRPC
class StartEventWatchFlow(
    private val targetContract: Contract,
    private val event: Event,
    private val eventParameters: Class<EventParameters>,
    private val searchId: String,
    private val followingFlow: Class<FlowLogic<Any>>,
    private val fromBlockNumber: BigInteger = BigInteger.ZERO,
    private val pollingInterbal: Long = 10
) : FlowLogic<Unit>() {
    companion object {
        // TODO Some ethereum parameters should be imported by .env
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))

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
        // toBlockNumber is always recentBlockNumber
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        val nextActivityTime = Instant.now().plusSeconds(pollingInterbal)
        val output = WatcherState(
            ourIdentity,
            fromBlockNumber,
            recentBlockNumber,
            targetContract,
            event,
            eventParameters,
            searchId,
            followingFlow,
            nextActivityTime
        )

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
