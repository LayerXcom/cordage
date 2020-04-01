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
        class Abort : ProposalCommands
        class Consume : ProposalCommands
    }

    override fun verify(tx: LedgerTransaction) {
        val proposalCommand = tx.commandsOfType<ProposalCommands>().first()
        when (proposalCommand.value) {
            is ProposalCommands.Propose -> requireThat {
                "No inputs should be consumed when issuing a Proposal." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Proposal." using (tx.outputs.size == 1)
                val proposal = tx.outputsOfType<ProposalState>().single()
                "A newly issued Proposal must have a not-null securityLinearId." using (proposal.securityLinearId !== UniqueIdentifier(""))
                "A newly issued Proposal must have a positive securityAmount." using (proposal.securityAmount > BigInteger.ZERO)
                "A newly issued Proposal must have a positive weiAmount." using (proposal.weiAmount > BigInteger.ZERO)
                "A newly issued Proposal must have a not-empty swapId." using (proposal.swapId.isNotEmpty())
                "fromEthereumAddress must equal to proposer's ethAddress" using (proposal.fromEthereumAddress == proposal.proposer.ethAddress())
                "toEthereumAddress must equal to acceptor's ethAddress" using (proposal.toEthereumAddress == proposal.acceptor.ethAddress())
                "Proposal status must be PROPOSED" using (proposal.status == ProposalStatus.PROPOSED)
                // Propose Tx must have proposer's and acceptor's signature.
                "Both proposer and acceptor together only may sign Proposal issue transaction." using
                    (proposalCommand.signers.toSet() == proposal.participants.map { it.owningKey }.toSet())
            }
            is ProposalCommands.Abort -> requireThat {
                "An Proposal Abort transaction should only consume one input state." using (tx.inputs.size == 1)
                "Output state should be only one state." using (tx.outputs.size == 1)
                val input = tx.inputsOfType<ProposalState>().single()
                val output = tx.outputsOfType<ProposalState>().single()
                "Input State's status must be PROPOSED." using (input.status == ProposalStatus.PROPOSED)
                "Only the status property may change." using (output == input.withNewStatus(ProposalStatus.ABORTED))
                // Abort Tx must have proposer's signature.
                val expectedSigners = (input.participants).map { it.owningKey } - input.acceptor.owningKey
                "The proposer only must sign an Proposal abort transaction" using
                    (proposalCommand.signers.toSet() == expectedSigners.toSet())
            }
            is ProposalCommands.Consume -> requireThat {
                "An Security TransferForSettle transaction should only consume two input state." using (tx.inputs.size == 2)
                "An Security TransferForSettle transaction should only create two output state." using (tx.outputs.size == 2)

                val inputProposal = tx.inputsOfType<ProposalState>().first()
                val outputProposal = tx.outputsOfType<ProposalState>().first()
                "Input ProposalState's status must be PROPOSED." using (inputProposal.status == ProposalStatus.PROPOSED)
                "Only the owner property may change." using (outputProposal == inputProposal.withNewStatus(ProposalStatus.CONSUMED))

                val inputSecurity = tx.inputsOfType<SecurityState>().first()
                val outputSecurity = tx.outputsOfType<SecurityState>().first()
                "InputProposalState's acceptor must equal to InputSecurityState's owner" using (inputProposal.acceptor == inputSecurity.owner)
                "InputProposalState's proposer must equal to OutputSecurityState's owner" using (inputProposal.proposer == outputSecurity.owner)
                "InputProposalState's securityAmount must equal to OutputSecurityState's amount" using (inputProposal.securityAmount == outputSecurity.amount.toBigInteger())
                "InputProposalState's fromEthereumAddress must equal to OutputSecurityState's owner ethAddress" using (inputProposal.fromEthereumAddress == outputSecurity.owner.ethAddress())
                "InputProposalState's toEthereumAddress must equal to InputSecurityState's owner ethAddress" using (inputProposal.toEthereumAddress == inputSecurity.owner.ethAddress())

                // Consume Tx must have proposer's and acceptor's signature.
                "The proposer, acceptor must sign an Proposal Consume transaction" using
                    (proposalCommand.signers.toSet() == (inputProposal.participants).map { it.owningKey }.toSet())
            }
        }
    }
}
