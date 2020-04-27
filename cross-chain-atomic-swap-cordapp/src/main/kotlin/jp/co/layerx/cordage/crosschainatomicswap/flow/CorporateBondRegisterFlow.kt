package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import net.corda.core.contracts.TransactionState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import java.math.BigDecimal

@StartableByRPC
class CorporateBondRegisterFlow(
    private val name: String,
    private val unitPriceEther: BigDecimal,
    private val observer: Party
) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val corporateBond = CorporateBond(name, unitPriceEther, listOf(ourIdentity))
        return subFlow(CreateEvolvableTokens(TransactionState(
            data = corporateBond,
            notary = serviceHub.networkMapCache.notaryIdentities.first()
        ), listOf(observer)))
    }
}
