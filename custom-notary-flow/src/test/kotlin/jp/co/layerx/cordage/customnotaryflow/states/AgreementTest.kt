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
        Assertions.assertThat(actual.origin == ALICE.party)
    }

    @Test
    fun target() {
        Assertions.assertThat(actual.target == BOB.party)
    }

    @Test
    fun status() {
        Assertions.assertThat(actual.status == AgreementStatus.MADE)
    }

    @Test
    fun agreementBody() {
        Assertions.assertThat(actual.agreementBody == expectedAgreementBody)
    }

    @Test
    fun linearId() {
        Assertions.assertThat(actual.linearId.toString() == expectedUuid.toString())
    }

    @Test
    fun participants() {
        Assertions.assertThat(actual.participants == setOf(ALICE.party, BOB.party))
    }

    @Test
    fun terminate() {
        val terminatedAgreement = actual.terminate()
        Assertions.assertThat(terminatedAgreement.status == AgreementStatus.TERMINATED)

        val expected = actual.copy(status = AgreementStatus.TERMINATED)
        Assertions.assertThat(terminatedAgreement == expected)
    }

}
