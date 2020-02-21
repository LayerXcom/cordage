package jp.co.layerx.cordage.flowethereumeventwatch.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.SchedulableState
import net.corda.core.contracts.ScheduledActivity
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatchContract
import jp.co.layerx.cordage.flowethereumeventwatch.flow.EventWatchFlow
import java.time.Instant

@BelongsToContract(WatchContract::class)
class WatchState(
        private val me: Party,
        private val nextActivityTime: Instant = Instant.now().plusSeconds(1)
) : SchedulableState {

    override val participants get() = listOf(me)

    // Defines the scheduled activity to be conducted by the SchedulableState.
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        // We get the time when the scheduled activity will occur in the constructor rather than in this method. This is
        // because calling Instant.now() in nextScheduledActivity returns the time at which the function is called, rather
        // than the time at which the state was created.
        return ScheduledActivity(flowLogicRefFactory.create(EventWatchFlow::class.java, thisStateRef), nextActivityTime)
    }

}