package jp.co.layerx.cordage.crosschainatomicswap.contract

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
        class TransferWithProposalState : SecurityCommands
    }

    override fun verify(tx: LedgerTransaction) {
        val securityCommand = tx.commandsOfType<SecurityCommands>().first()
        when (securityCommand.value) {
            is SecurityCommands.Issue -> requireThat {
                "No inputs should be consumed when issuing a Security." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Security." using (tx.outputs.size == 1)
                val security = tx.outputsOfType<SecurityState>().single()
                // add some validations
                // TODO "SecurityIssue Tx must have issuer's and owner's signature."
            }
            is SecurityCommands.Transfer -> requireThat {
                "Output state should be only one state." using (tx.outputs.size == 1)
                val inputs = tx.inputsOfType<SecurityState>()
                val output = tx.outputsOfType<SecurityState>().single()
                // add some validations
                // TODO "SecurityTransfer Tx must have previous owner's, new owner's and issuer's signature signature."
            }
            is SecurityCommands.TransferWithProposalState -> requireThat {
//            val inputs = tx.inputsOfType<SecurityState>()
//            val output = tx.outputsOfType<SecurityState>().first()
            // add some validations
            // TODO "SecurityTransferWithProposalState Tx must have previous owner's, new owner's and issuer's signature."
        }
        }
    }
}
