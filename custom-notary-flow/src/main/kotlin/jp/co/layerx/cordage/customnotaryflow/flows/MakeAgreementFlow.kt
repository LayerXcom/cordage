package jp.co.layerx.cordage.customnotaryflow.flows

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.customnotaryflow.contracts.AgreementContract
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import jp.co.layerx.cordage.customnotaryflow.states.AgreementStatus
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class MakeAgreementFlow(private val target: Party, private val agreementBody: String): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val agreement = Agreement(ourIdentity, target, AgreementStatus.MADE, agreementBody)

        val tx = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.single())
        tx.addOutputState(agreement, AgreementContract.ID)
        tx.addCommand(Command(AgreementContract.AgreementCommand.Make(), agreement.participants.map { it.owningKey }))
        tx.verify(serviceHub)

        val ptx = serviceHub.signInitialTransaction(tx)
        val targetSession = initiateFlow(target)
        val ftx = subFlow(CollectSignaturesFlow(ptx, setOf(targetSession)))

        return subFlow(FinalityFlow(ftx, setOf(targetSession)))
    }
}
