package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.transactions.LedgerTransaction
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.core.contracts.*

open class SecurityContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract"
    }

    interface SecurityCommands : CommandData {
        class Issue : SecurityCommands
        class Transfer : SecurityCommands
        class TransferForSettle: SecurityCommands
    }

    override fun verify(tx: LedgerTransaction) {
        val securityCommand = tx.commandsOfType<SecurityCommands>().first()
        when (securityCommand.value) {
            is SecurityCommands.Issue -> requireThat {
                "No inputs should be consumed when issuing a Security." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Security." using (tx.outputs.size == 1)
                val security = tx.outputsOfType<SecurityState>().single()
                "A newly issued Security must have a positive amount." using (security.amount > 0)
                // SecurityIssue Tx must have issuer's and owner's signature
                "Both issuer and owner together only may sign Security issue transaction." using
                    (securityCommand.signers.toSet() == security.participants.map { it.owningKey }.toSet())
            }
            is SecurityCommands.Transfer -> requireThat {
                "An Security transfer transaction should only consume one input state." using (tx.inputs.size == 1)
                "An Security transfer transaction should only create one output state." using (tx.outputs.size == 1)
                val input = tx.inputsOfType<SecurityState>().single()
                val output = tx.outputsOfType<SecurityState>().single()
                "Only the owner property may change." using (input == output.withNewOwner(input.owner))
                "The owner property must change in a Transfer." using (input.owner != output.owner)
                // SecurityTransfer Tx must have previous owner's, new owner's and issuer's signature signature
                "The issuer, old owner and new owner only must sign an Security transfer transaction." using
                    (securityCommand.signers.toSet() == (input.participants.map { it.owningKey }.toSet() `union`
                        output.participants.map { it.owningKey }.toSet()))
            }
            is SecurityCommands.TransferForSettle -> requireThat {
                "An Security TransferForSettle transaction should only consume two input state." using (tx.inputs.size == 2)
                "An Security TransferForSettle transaction should only create two output state." using (tx.outputs.size == 2)

                val inputSecurity = tx.inputsOfType<SecurityState>().first()
                val outputSecurity = tx.outputsOfType<SecurityState>().first()
                "Only the owner property may change." using (inputSecurity == outputSecurity.withNewOwner(inputSecurity.owner))
                "The owner property must change in a TransferForSettle." using (inputSecurity.owner != outputSecurity.owner)

                val inputProposal = tx.inputsOfType<ProposalState>().first()
                "InputProposalState's acceptor must equal to InputSecurityState's owner." using (inputProposal.acceptor == inputSecurity.owner)
                "InputProposalState's proposer must equal to OutputSecurityState's owner." using (inputProposal.proposer == outputSecurity.owner)
                "InputProposalState's securityAmount must equal to OutputSecurityState's amount." using (inputProposal.securityAmount == outputSecurity.amount.toBigInteger())
                "InputProposalState's fromEthereumAddress must equal to OutputSecurityState's owner ethAddress." using (inputProposal.fromEthereumAddress == outputSecurity.owner.ethAddress())
                "InputProposalState's toEthereumAddress must equal to InputSecurityState's owner ethAddress." using (inputProposal.toEthereumAddress == inputSecurity.owner.ethAddress())

                // TransferForSettle Tx must have previous owner's, new owner's and issuer's signature.
                "The issuer, old owner and new owner must sign an Security TransferForSettle transaction." using
                    (securityCommand.signers.toSet() == (inputSecurity.participants.map { it.owningKey }.toSet() union
                        outputSecurity.participants.map { it.owningKey }.toSet()))
            }
        }
    }
}
