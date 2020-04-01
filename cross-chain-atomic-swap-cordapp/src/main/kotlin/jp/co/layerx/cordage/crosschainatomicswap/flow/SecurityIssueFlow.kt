package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

@InitiatingFlow
@StartableByRPC
class SecurityIssueFlow(val amount: Int,
                        val owner: Party,
                        val name: String): FlowLogic<SignedTransaction>() {
    companion object {
        object CREATE_OUTPUTSTATE : Step("Creating Output SecurityState.")
        object GENERATING_TRANSACTION : Step("Generating transaction based on new SecurityState.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }
        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            CREATE_OUTPUTSTATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATE_OUTPUTSTATE
        val issuer = ourIdentity
        val outputSecurity = SecurityState(amount, owner, issuer, name)

        progressTracker.currentStep = GENERATING_TRANSACTION
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val signers = outputSecurity.participants.map { it.owningKey }
        val issueCommand = Command(SecurityContract.SecurityCommands.Issue(), signers)
        val txBuilder = TransactionBuilder(notary = notary)
            .addOutputState(outputSecurity, SecurityContract.contractID)
            .addCommand(issueCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val partlySignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = GATHERING_SIGS
        val otherPartySessions = (outputSecurity.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val fullySignedTx = subFlow(CollectSignaturesFlow(partlySignedTx, otherPartySessions))

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions))
    }
}

@InitiatedBy(SecurityIssueFlow::class)
class SecurityIssueFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an Security transaction" using (output is SecurityState)
            }
        }
        val txId = subFlow(signedTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txId))
    }
}
