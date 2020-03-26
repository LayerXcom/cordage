package jp.co.layerx.cordage.customnotaryflow.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(TerminateAgreementFlow::class)
class TerminateAgreementHandler(val targetPartySession: FlowSession): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(targetPartySession) {
            override fun checkTransaction(stx: SignedTransaction) {}
        }

        val txId = subFlow(signedTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(targetPartySession, expectedTxId = txId))
    }
}
