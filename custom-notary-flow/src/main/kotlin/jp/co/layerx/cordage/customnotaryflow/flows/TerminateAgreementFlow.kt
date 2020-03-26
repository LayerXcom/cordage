package jp.co.layerx.cordage.customnotaryflow.flows

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class TerminateAgreementFlow(private val linearId: String): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(linearId)))
        val state = serviceHub.vaultService.queryBy<Agreement>(criteria).states.single()
    }
}
