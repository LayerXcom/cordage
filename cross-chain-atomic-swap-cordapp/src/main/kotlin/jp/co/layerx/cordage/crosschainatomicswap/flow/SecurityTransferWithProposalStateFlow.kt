package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class SecurityTransferWithProposalStateFlow(val proposalStateRef: StateAndRef<ProposalState>): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val inputProposal = proposalStateRef.state.data
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(inputProposal.securityLinearId))
        val securityStateAndRef =  serviceHub.vaultService.queryBy<SecurityState>(queryCriteria).states.single()
        val inputSecurity = securityStateAndRef.state.data

        if (ourIdentity != inputSecurity.owner) {
            throw IllegalArgumentException("Security transfer can only be initiated by the Security Owner.")
        }

        val newOwner = inputProposal.proposer
        val outputSecurity = inputSecurity.withNewOwner(newOwner)
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        val securitySigners = (inputSecurity.participants + newOwner).map { it.owningKey }
        val proposalSigners = (inputProposal.participants).map { it.owningKey }
        val transferWithProposalStateCommand = Command(SecurityContract.SecurityCommands.TransferWithProposalState(), securitySigners)
        val consumeCommand= Command(ProposalContract.ProposalCommands.Consume(), proposalSigners)

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
            .addInputState(securityStateAndRef)
            .addOutputState(outputSecurity, SecurityContract.contractID)
            .addCommand(transferWithProposalStateCommand)
            .addInputState(proposalStateRef)
            .addOutputState(outputProposal,ProposalContract.contractID)
            .addCommand(consumeCommand)

        txBuilder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(txBuilder)

        val sessions = (outputSecurity.participants).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        return subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(SecurityTransferWithProposalStateFlow::class)
class SecurityTransferWithProposalStateFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
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

