package jp.co.layerx.cordage.flowethereumeventwatch.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.SchedulableState
import net.corda.core.contracts.ScheduledActivity
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract
import jp.co.layerx.cordage.flowethereumeventwatch.flow.EventWatchFlow
import jp.co.layerx.cordage.flowethereumeventwatch.types.EventParameters
import net.corda.core.flows.FlowLogic
import org.web3j.abi.datatypes.Event
import org.web3j.tx.Contract
import java.math.BigInteger
import java.time.Instant

@BelongsToContract(WatcherContract::class)
data class WatcherState(
        val me: Party,
        val fromBlockNumber: BigInteger,
        val toBlockNumber: BigInteger,
        val targetContract: Contract,
        val event: Event,
        val eventParameters: Class<EventParameters>,
        val searchId: String,
        val followingFlow: Class<FlowLogic<Any>>,
        private val nextActivityTime: Instant = Instant.now().plusSeconds(10)
) : SchedulableState {

    override val participants get() = listOf(me)

    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(EventWatchFlow::class.java, thisStateRef), nextActivityTime)
    }

}