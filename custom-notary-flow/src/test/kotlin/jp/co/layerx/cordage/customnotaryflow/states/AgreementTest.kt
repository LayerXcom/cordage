package jp.co.layerx.cordage.customnotaryflow.states

import jp.co.layerx.cordage.customnotaryflow.ALICE
import jp.co.layerx.cordage.customnotaryflow.BOB
import net.corda.core.contracts.UniqueIdentifier
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*


internal class AgreementTest {
    private val expectedAgreementBody = "RESIDENTIAL LEASE AGREEMENT"
    private val expectedUuid = UUID.randomUUID()
    private val actual = Agreement(ALICE.party, BOB.party, AgreementStatus.MADE, expectedAgreementBody, UniqueIdentifier(id = expectedUuid))

    @Test
    fun origin() {
        Assertions.assertThat(actual.origin).isEqualTo(ALICE.party)
    }

    @Test
    fun target() {
        Assertions.assertThat(actual.target).isEqualTo(BOB.party)
    }

    @Test
    fun status() {
        Assertions.assertThat(actual.status).isEqualTo(AgreementStatus.MADE)
    }

    @Test
    fun agreementBody() {
        Assertions.assertThat(actual.agreementBody).isEqualTo(expectedAgreementBody)
    }

    @Test
    fun linearId() {
        Assertions.assertThat(actual.linearId.toString()).isEqualTo(expectedUuid.toString())
    }

    @Test
    fun participants() {
        Assertions.assertThat(actual.participants.toSet()).isEqualTo(setOf(ALICE.party, BOB.party))
    }

    @Test
    fun terminate() {
        val terminatedAgreement = actual.terminate()
        Assertions.assertThat(terminatedAgreement.status).isEqualTo(AgreementStatus.TERMINATED)

        val expected = actual.copy(status = AgreementStatus.TERMINATED)
        Assertions.assertThat(terminatedAgreement).isEqualTo(expected)
    }

}
