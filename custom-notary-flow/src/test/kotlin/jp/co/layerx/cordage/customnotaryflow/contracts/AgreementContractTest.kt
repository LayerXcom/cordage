package jp.co.layerx.cordage.customnotaryflow.contracts

import jp.co.layerx.cordage.customnotaryflow.ALICE
import jp.co.layerx.cordage.customnotaryflow.BOB
import jp.co.layerx.cordage.customnotaryflow.CHARLIE
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import jp.co.layerx.cordage.customnotaryflow.states.AgreementStatus
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test


internal class AgreementContractTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.customnotaryflow"))

    @Test
    fun `verify making an agreement`() {
        val agreementBody = "RESIDENTIAL LEASE AGREEMENT"
        val agreement = Agreement(ALICE.party, BOB.party, AgreementStatus.MADE, agreementBody)

        ledgerServices.ledger {
            transaction {
                output(AgreementContract.ID, agreement)
                command(listOf(ALICE.publicKey, BOB.publicKey), AgreementContract.AgreementCommand.Make())
                this.verifies()
            }
        }
    }

    @Test
    fun `verify terminating an agreement`() {
        val agreementBody = "RESIDENTIAL LEASE AGREEMENT"
        val agreement = Agreement(ALICE.party, BOB.party, AgreementStatus.MADE, agreementBody)

        ledgerServices.ledger {
            transaction {
                input(AgreementContract.ID, agreement)
                output(AgreementContract.ID, agreement.terminate())
                command(listOf(ALICE.publicKey, BOB.publicKey), AgreementContract.AgreementCommand.Terminate())
                this.verifies()
            }
        }
    }

    @Test
    fun `fails with 3 signatures`() {
        val agreementBody = "RESIDENTIAL LEASE AGREEMENT"
        val agreement = Agreement(ALICE.party, BOB.party, AgreementStatus.MADE, agreementBody)

        ledgerServices.ledger {
            transaction {
                input(AgreementContract.ID, agreement)
                output(AgreementContract.ID, agreement.terminate())
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), AgreementContract.AgreementCommand.Terminate())
                this `fails with` "Agreement must be signed by both party only"
            }
        }
    }
}
