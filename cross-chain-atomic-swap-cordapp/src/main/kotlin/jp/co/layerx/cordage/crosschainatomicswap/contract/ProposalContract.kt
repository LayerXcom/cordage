package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.*

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
                // add some validations
                // TODO "Propose Tx must have proposer's and acceptor's signature."
            }
            is ProposalCommands.Abort -> requireThat {
                "Output state should be only one state." using (tx.outputs.size == 1)
                val inputs = tx.inputsOfType<ProposalState>()
                val output = tx.outputsOfType<ProposalState>().single()
                // add some validations
                // TODO "Abort Tx must have proposer's signature."
            }
            is ProposalCommands.Consume -> requireThat {
                val inputs = tx.inputsOfType<ProposalState>()
                val output = tx.outputsOfType<ProposalState>().first()
                // add some validations
                // TODO "Consume Tx must have proposer's and acceptor's signature. (maybe security issuer's signature)"
            }
        }
    }
}
