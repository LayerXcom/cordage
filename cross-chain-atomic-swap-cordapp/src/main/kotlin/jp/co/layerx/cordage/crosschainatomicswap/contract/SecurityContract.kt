package jp.co.layerx.cordage.crosschainatomicswap.contract

import net.corda.core.transactions.LedgerTransaction
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.core.contracts.*

open class SecurityContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract"
    }

    interface Commands : CommandData {
        class Issue : Commands
        class Transfer : Commands
        class TransferToOtherChain : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing a Security." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Security." using (tx.outputs.size == 1)
                val security = tx.outputsOfType<SecurityState>().single()
                // add some validations
            }
            is Commands.Transfer -> requireThat {
                "Output state should be only one state." using (tx.outputs.size == 1)
                val inputs = tx.inputsOfType<SecurityState>()
                val output = tx.outputsOfType<SecurityState>().single()
                // add some validations
            }
            is Commands.TransferToOtherChain -> requireThat {
            "Output state should be only one state." using (tx.outputs.size == 1)
            val inputs = tx.inputsOfType<SecurityState>()
            val output = tx.outputsOfType<SecurityState>().single()
            // add some validations
        }
        }
    }
}
