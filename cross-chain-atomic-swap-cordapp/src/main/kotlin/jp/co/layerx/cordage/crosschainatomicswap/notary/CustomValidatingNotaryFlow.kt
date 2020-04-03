package jp.co.layerx.cordage.crosschainatomicswap.notary

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.flows.FlowSession
import net.corda.core.flows.NotarisationPayload
import net.corda.core.flows.NotaryError
import net.corda.core.internal.ResolveTransactionsFlow
import net.corda.core.internal.notary.NotaryInternalException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionWithSignatures
import net.corda.core.utilities.ProgressTracker
import net.corda.node.services.transactions.ValidatingNotaryFlow
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

class CustomValidatingNotaryFlow(otherSide: FlowSession, service: CustomValidatingNotaryService) : ValidatingNotaryFlow(otherSide, service) {
    companion object {
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        private const val ETHEREUM_NETWORK_ID = "5777"
        private const val ETHEREUM_PRIVATE_KEY = "0x646f1ce2fdad0e6deeeb5c7e8e5543bdde65e86029e2fd9fc169899c440a7913"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val targetContractAddress = Settlement.getPreviouslyDeployedAddress(ETHEREUM_NETWORK_ID)!!
        val credentials = Credentials.create(ETHEREUM_PRIVATE_KEY)!!

        object SEND_TRANSACTION_TO_ETHEREUM_CONTRACT: ProgressTracker.Step("Sending a transaction to Settlement Contract for unlocking.")

        fun tracker() = ProgressTracker(
            SEND_TRANSACTION_TO_ETHEREUM_CONTRACT
        )
    }
    override val progressTracker = tracker()

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
        println("CUSTOM NOTARY FLOW: Commands are " + stx.tx.commands.toString())

        if(!stx.tx.commands.map { it.value is SecurityContract.SecurityCommands.TransferForSettle }.contains(true)) {
            // return unless tx commands contain type of SecurityCommands.TransferForSettle
            return
        }

        val finalizedProposalState = stx.tx.outputsOfType<ProposalState>().single()
        val swapId = finalizedProposalState.swapId

        // load Smart Contract Wrapper
        val settlement: Settlement = Settlement.load(
            targetContractAddress,
            web3,
            credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000))
        )

        progressTracker.currentStep = SEND_TRANSACTION_TO_ETHEREUM_CONTRACT

        settlement.unlock(swapId).send()
    }
}
