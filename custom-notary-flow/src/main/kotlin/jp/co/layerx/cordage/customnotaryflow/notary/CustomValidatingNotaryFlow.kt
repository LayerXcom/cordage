package jp.co.layerx.cordage.customnotaryflow.notary

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.customnotaryflow.contracts.AgreementContract
import jp.co.layerx.cordage.customnotaryflow.hexStringToByteArray
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import net.corda.core.contracts.Command
import net.corda.core.flows.FlowSession
import net.corda.core.flows.NotarisationPayload
import net.corda.core.flows.NotaryError
import net.corda.core.internal.ResolveTransactionsFlow
import net.corda.core.internal.notary.NotaryInternalException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionWithSignatures
import net.corda.core.utilities.toHex
import net.corda.node.services.transactions.ValidatingNotaryFlow
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

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
        val command = stx.tx.commands.single() as Command
        if (command.value !is AgreementContract.AgreementCommand.Terminate) {
            // custom verification works only if command is Terminate
            return
        }
        println("TERMINATE AGREEMENT")

        val agreement = stx.tx.outputsOfType<Agreement>().single()
        val data = "Terminate: " + agreement.agreementBody

        // Add custom verification logic
        val ETHEREUM_RPC_URL = serviceHub.getAppContext().config.getString("rpcUrl")
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val tx = Transaction.createFunctionCallTransaction(
            "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1",
            null,
            BigInteger.valueOf(1),
            BigInteger.valueOf(50000),
            "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0",
            BigInteger.valueOf(1_000_000_000_000_000_000),
            data.toByteArray(Charsets.UTF_8).toHex()
        )

        val response = web3.ethSendTransaction(tx).send()
        println("Tx hash: " + response.transactionHash)
        println("Tx data: " + tx.data.toUpperCase().hexStringToByteArray().toString(Charsets.UTF_8))
    }
}
