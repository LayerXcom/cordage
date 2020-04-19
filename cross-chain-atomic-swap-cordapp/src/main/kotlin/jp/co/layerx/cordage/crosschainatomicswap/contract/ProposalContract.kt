package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.*
import java.math.BigInteger

open class ProposalContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract"
    }

    interface ProposalCommands : CommandData {
        class Propose : ProposalCommands
        class Consume : ProposalCommands
    }

    override fun verify(tx: LedgerTransaction) {
        val proposalCommand = tx.commandsOfType<ProposalCommands>().first()
        when (proposalCommand.value) {
            is ProposalCommands.Propose -> requireThat {
                "No inputs should be consumed when issuing a Proposal." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Proposal." using (tx.outputs.size == 1)
                val proposal = tx.outputsOfType<ProposalState>().single()
                "A newly issued Proposal must have a positive securityAmount." using (proposal.quantity > 0)
                "A newly issued Proposal must have a positive weiAmount." using (proposal.priceWei > BigInteger.ZERO)
                "A newly issued Proposal must have a not-empty swapId." using (proposal.swapId.isNotEmpty())
                "fromEthereumAddress must equal to proposer's ethAddress." using (proposal.fromEthereumAddress == proposal.proposer.ethAddress())
                "toEthereumAddress must equal to acceptor's ethAddress." using (proposal.toEthereumAddress == proposal.acceptor.ethAddress())
                "Proposal status must be PROPOSED." using (proposal.status == ProposalStatus.PROPOSED)
                // Propose Tx must have proposer's and acceptor's signature.
                "Both proposer and acceptor together only may sign Proposal issue transaction." using
                    (proposalCommand.signers.toSet() == proposal.participants.map { it.owningKey }.toSet())
            }
            is ProposalCommands.Consume -> requireThat {
                "An Proposal Consume transaction should only consume two input states." using (tx.inputs.size == 2)
                "An Proposal Consume transaction should only create two output states." using (tx.outputs.size == 2)
                "An Proposal Consume transaction should consume only one input proposal state." using (tx.inputsOfType<ProposalState>().size == 1)
                "An Proposal Consume transaction should create only one output proposal state." using (tx.outputsOfType<ProposalState>().size == 1)
                "An Proposal Consume transaction should consume only one input security state." using (tx.inputsOfType<SecurityState>().size == 1)
                "An Proposal Consume transaction should create only one output security state." using (tx.outputsOfType<SecurityState>().size == 1)

                val inputProposal = tx.inputsOfType<ProposalState>().single()
                val outputProposal = tx.outputsOfType<ProposalState>().single()
                "Input ProposalState's status must be PROPOSED." using (inputProposal.status == ProposalStatus.PROPOSED)
                "Only the status property may change." using (outputProposal == inputProposal.withNewStatus(ProposalStatus.CONSUMED))

                // Consume Tx must have proposer's and acceptor's signature.
                "Both proposer and acceptor together only may sign Proposal consume transaction." using
                    (proposalCommand.signers.toSet() == (inputProposal.participants).map { it.owningKey }.toSet())
            }
        }
    }
}
