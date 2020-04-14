package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger

import org.junit.Test

class SecurityContractIssueTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    @Test
    fun `normal scenario`() {
        val output = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")

        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this.verifies()
            }
        }
    }

    @Test
    fun `issue transaction must have no inputs`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        val output = input.withNewOwner(BOB.party)

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "No inputs should be consumed when issuing a Security."
            }
       }
    }

    @Test
    fun `issue transaction must have one output`() {
        val output = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")

        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, output)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Only one output state should be created when issuing a Security."
            }
        }
    }

    @Test
    fun `issue transaction's output state must have positive amount`() {
        val zeroOutput = SecurityState(0, ALICE.party, CHARLIE.party, "LayerX")
        val negativeOutput = SecurityState(-100, ALICE.party, CHARLIE.party, "LayerX")
        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, zeroOutput)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "A newly issued Security must have a positive amount."
            }
            transaction {
                output(SecurityContract.contractID, negativeOutput)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "A newly issued Security must have a positive amount."
            }
        }
    }

    @Test
    fun `issuer and owner together only must sign issue transaction`() {
        val output = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Both issuer and owner together only may sign Security issue transaction."
            }
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Both issuer and owner together only may sign Security issue transaction."
            }
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Both issuer and owner together only may sign Security issue transaction."
            }
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Both issuer and owner together only may sign Security issue transaction."
            }
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey, BOB.publicKey), SecurityContract.SecurityCommands.Issue())
                this `fails with` "Both issuer and owner together only may sign Security issue transaction."
            }
        }
    }
}
