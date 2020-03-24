package jp.co.layerx.cordage.customnotaryflow.notary

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowSession
import net.corda.core.flows.NotarisationPayload
import net.corda.core.flows.NotaryError
import net.corda.core.internal.ResolveTransactionsFlow
import net.corda.core.internal.notary.NotaryInternalException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionWithSignatures
import net.corda.node.services.transactions.ValidatingNotaryFlow

class CustomValidatingNotaryFlow(otherSide: FlowSession, service: CustomValidatingNotaryService) : ValidatingNotaryFlow(otherSide, service) {
    @Suspendable
    override fun verifyTransaction(requestPayload: NotarisationPayload) {
        try {
            val stx = requestPayload.signedTransaction
            resolveAndContractVerify(stx)
            verifySignatures(stx)
            customVerify(stx)
        } catch (e: Exception) {
            throw  NotaryInternalException(NotaryError.TransactionInvalid(e))
        }
    }

    @Suspendable
    private fun resolveAndContractVerify(stx: SignedTransaction) {
        subFlow(ResolveTransactionsFlow(stx, otherSideSession))
        stx.verify(serviceHub, false)
    }

    private fun verifySignatures(stx: SignedTransaction) {
        val transactionWithSignatures = stx.resolveTransactionWithSignatures(serviceHub)
        checkSignatures(transactionWithSignatures)
    }

    private fun checkSignatures(tx: TransactionWithSignatures) {
        tx.verifySignaturesExcept(service.notaryIdentityKey)
    }

    private fun customVerify(stx: SignedTransaction) {
        // Add custom verification logic
    }
}
