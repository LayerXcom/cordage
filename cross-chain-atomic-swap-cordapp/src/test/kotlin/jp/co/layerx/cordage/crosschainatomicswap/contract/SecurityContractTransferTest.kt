package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
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
}
