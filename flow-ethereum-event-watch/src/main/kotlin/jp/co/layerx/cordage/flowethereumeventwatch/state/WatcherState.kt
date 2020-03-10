package jp.co.layerx.cordage.flowethereumeventwatch.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.SchedulableState
import net.corda.core.contracts.ScheduledActivity
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.flow.EventWatchFlow
import java.math.BigInteger
import java.time.Instant

@BelongsToContract(WatcherContract::class)
class WatcherState(
        val me: Party,
        val fromBlockNumber: BigInteger,
        val toBlockNumber: BigInteger,
        val targetContractAddress: String,
        val eventName: String,
        val searchId: BigInteger,
        private val nextActivityTime: Instant = Instant.now().plusSeconds(10)
) : SchedulableState {

    override val participants get() = listOf(me)

    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(EventWatchFlow::class.java, thisStateRef), nextActivityTime)
    }

}