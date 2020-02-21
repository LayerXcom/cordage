package jp.co.layerx.cordage.flowethereumeventwatch.contract

import jp.co.layerx.cordage.flowethereumeventwatch.state.WatcherState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

open class WatcherContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.flowethereumeventwatch.contract.WatcherContract"
    }

    interface Commands : CommandData {
        class Watch : Commands
        class Issue : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<WatcherContract.Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing an Watcher." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an Watcher." using (tx.outputs.size == 1)
                val watcher = tx.outputsOfType<WatcherState>().single()
                "A newly issued Watcher's fromBlockNumber must be more than zero." using (watcher.fromBlockNumber >= 0)
                "A newly issued Watcher must have a positive toBlockNumber." using (watcher.toBlockNumber > 0)
                "The toBlockNumber must be greater than the fromBlockNumber." using (watcher.toBlockNumber > watcher.fromBlockNumber)
                "The targetContractAddress must start with 0x." using (watcher.targetContractAddress.startsWith("0x"))
            }
            is Commands.Watch -> requireThat {
                "Input state shoud be only one state." using (tx.inputs.size == 1)
                "Output state shoud be only one state." using (tx.outputs.size == 1)
                val input = tx.inputsOfType<WatcherState>().single()
                val output = tx.outputsOfType<WatcherState>().single()
                "The me property should not be change." using (input.me == output.me)
                "Output WatcherState must have a positive toBlockNumber." using (output.toBlockNumber > 0)
                "Output's fromBlockNumber should be next number after input's toBlockNumber." using (input.toBlockNumber + 1 == output.fromBlockNumber)
                "The toBlockNumber must be greater than the fromBlockNumber." using (output.toBlockNumber > output.fromBlockNumber)
                "The targetContractAddress property should not be change." using (input.targetContractAddress == output.targetContractAddress)
            }
        }
    }
}