package jp.co.layerx.cordage.crosschainatomicswap.state

import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import jp.co.layerx.cordage.crosschainatomicswap.contract.WatcherContract
import jp.co.layerx.cordage.crosschainatomicswap.flow.EventWatchFlow
import net.corda.core.contracts.*
import java.math.BigInteger
import java.time.Instant
import javax.swing.plaf.nimbus.State

@BelongsToContract(WatcherContract::class)
class WatcherState(
        val me: Party,
        val fromBlockNumber: BigInteger,
        val toBlockNumber: BigInteger,
        val targetContractAddress: String,
        val eventName: String,
        val proposalStateAndRef: StateAndRef<ProposalState>,
        private val nextActivityTime: Instant = Instant.now().plusSeconds(10)
) : SchedulableState {

    override val participants get() = listOf(me)

    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(EventWatchFlow::class.java, thisStateRef), nextActivityTime)
    }

}
