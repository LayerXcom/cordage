package jp.co.layerx.cordage.customnotaryflow.flows

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.customnotaryflow.contracts.AgreementContract
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class TerminateAgreementFlow(private val linearId: String): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(linearId)))
        val input = serviceHub.vaultService.queryBy<Agreement>(criteria).states.single()
        val output = input.state.data.terminate()

        val tx = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.single())
        tx.addInputState(input)
        tx.addOutputState(output, AgreementContract.ID)
        tx.addCommand(Command(AgreementContract.AgreementCommand.Terminate(), output.participants.map { it.owningKey }))
        tx.verify(serviceHub)

        val ptx = serviceHub.signInitialTransaction(tx)
        val targetSession = initiateFlow(output.target)
        val ftx = subFlow(CollectSignaturesFlow(ptx, setOf(targetSession)))

        return subFlow(FinalityFlow(ftx, setOf(targetSession)))
    }
}
