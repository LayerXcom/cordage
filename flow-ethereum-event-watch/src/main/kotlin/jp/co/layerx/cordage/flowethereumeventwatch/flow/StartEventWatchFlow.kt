package jp.co.layerx.cordage.flowethereumeventwatch.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.state.WatcherState

@InitiatingFlow
@StartableByRPC
class StartEventWatchFlow(private val fromBlockNumber: Int, private val targetContractAddress: String) : FlowLogic<Unit>() {
    companion object {
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
        // val recentBlockNumber = get recent BlockNumber with Web3.j
        val toBlockNumber = 100
        // val output = WatcherState(ourIdentity, fromBlockNumber, recentBlockNumber, targetContractAddress)
        val output = WatcherState(ourIdentity, fromBlockNumber, toBlockNumber, targetContractAddress)

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
