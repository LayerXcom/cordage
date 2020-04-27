package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
class CorporateBondIssueFlow(
    private val linearId: UniqueIdentifier,
    private val quantity: Long,
    private val holder: Party
) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val corporateBond = serviceHub.vaultService.queryBy<CorporateBond>(criteria).states.single().state.data
        val tokenPointer = corporateBond.toPointer<CorporateBond>()
        val corporateBondToken = Amount(quantity, tokenPointer issuedBy ourIdentity) heldBy holder
        return subFlow(IssueTokens(listOf(corporateBondToken)))
    }
}
