package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.*

open class ProposalContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract"
    }

    interface Commands : CommandData {
        class Propose : Commands
        class Abort : Commands
        class Consume : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Propose -> requireThat {
                "No inputs should be consumed when issuing a Proposal." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a Proposal." using (tx.outputs.size == 1)
                val proposal = tx.outputsOfType<ProposalState>().single()
                // add some validations
            }
            is Commands.Abort -> requireThat {
                "Output state should be only one state." using (tx.outputs.size == 1)
                val inputs = tx.inputsOfType<ProposalState>()
                val output = tx.outputsOfType<ProposalState>().single()
                // add some validations
            }
            is Commands.Consume -> requireThat {
                "Output state should be only one state." using (tx.outputs.size == 1)
                val inputs = tx.inputsOfType<ProposalState>()
                val output = tx.outputsOfType<ProposalState>().single()
                // add some validations
            }
        }
    }
}
