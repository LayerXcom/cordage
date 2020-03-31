package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class SecurityIssueFlow(val amount: Int,
                        val owner: Party,
                        val name: String): FlowLogic<UniqueIdentifier>() {
    @Suspendable
    override fun call(): UniqueIdentifier {
        val issuer = ourIdentity
        val state = SecurityState(amount, owner, issuer, name)
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val issueCommand = Command(SecurityContract.SecurityCommands.Issue(), state.participants.map { it.owningKey })

        val builder = TransactionBuilder(notary = notary)

        builder.addOutputState(state, SecurityContract.contractID)
        builder.addCommand(issueCommand)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        val finalizedTx = subFlow(FinalityFlow(stx, sessions))
        val issuedSecurityState = finalizedTx.coreTransaction.outputsOfType<SecurityState>().first()
        return issuedSecurityState.linearId
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

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}
