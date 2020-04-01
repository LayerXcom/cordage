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
        val output = SecurityState(100, ALICE.party, CHARLIE.party, "R3")

        ledgerServices.ledger {
            transaction {
                output(SecurityContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.Issue())
                this.verifies()
            }
        }
    }

    @Test
    fun `empty input`() {
        val input = SecurityState(100, ALICE.party, CHARLIE.party, "R3")
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
}
