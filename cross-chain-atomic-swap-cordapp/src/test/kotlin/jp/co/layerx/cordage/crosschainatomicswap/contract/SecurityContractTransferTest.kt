package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.DUMMY_BANK_A
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger

import org.junit.Test

class SecurityContractTransferTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    @Test
    fun `normal scenario`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        val output = input.withNewOwner(BOB.party)

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this.verifies()
            }
        }
    }

    @Test
    fun `transfer transaction must have one input`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        val output = input.withNewOwner(BOB.party)

        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "An Security transfer transaction should only consume one input state."
            }
            transaction {
                input(SecurityContract.contractID, input)
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "An Security transfer transaction should only consume one input state."
            }
        }
    }

    @Test
    fun `transfer transaction must have one output`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        val output = input.withNewOwner(BOB.party)

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "An Security transfer transaction should only create one output state."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "An Security transfer transaction should only create one output state."
            }
        }
    }

    @Test
    fun `transfer transaction can only change owner`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, SecurityState(1000, BOB.party, CHARLIE.party, "LayerX"))
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "Only the owner property may change."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, SecurityState(100, BOB.party, ALICE.party, "LayerX"))
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "Only the owner property may change."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, SecurityState(100, BOB.party, CHARLIE.party, "LayerZ"))
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "Only the owner property may change."
            }
        }
    }

    @Test
    fun `transfer transaction must change owner`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, input)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The owner property must change in a Transfer."
            }
        }
    }

    @Test
    fun `previous owner, new owner and issuer only must sign transfer transaction`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "LayerX")
        val output = input.withNewOwner(BOB.party)

        ledgerServices.ledger {
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The issuer, old owner and new owner only must sign an Security transfer transaction."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The issuer, old owner and new owner only must sign an Security transfer transaction."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The issuer, old owner and new owner only must sign an Security transfer transaction."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey, DUMMY_BANK_A.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The issuer, old owner and new owner only must sign an Security transfer transaction."
            }
            transaction {
                input(SecurityContract.contractID, input)
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey, DUMMY_BANK_A.publicKey), SecurityContract.SecurityCommands.Transfer())
                this `fails with` "The issuer, old owner and new owner only must sign an Security transfer transaction."
            }
        }
    }
}
