package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import net.corda.core.contracts.UniqueIdentifier
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*

class SecurityStateTest {
    private val expectedUuid = UUID.randomUUID()
    private val actual = SecurityState(100, ALICE.party, BOB.party, "R3", UniqueIdentifier(id = expectedUuid))

    @Test
    fun amount() {
        Assertions.assertThat(actual.amount == 100)
    }

    @Test
    fun owner() {
        Assertions.assertThat(actual.owner == ALICE.party)
    }

    @Test
    fun issuer() {
        Assertions.assertThat(actual.issuer == BOB.party)
    }

    @Test
    fun name() {
        Assertions.assertThat(actual.name == "R3")
    }

    @Test
    fun linearId() {
        Assertions.assertThat(actual.linearId.id == expectedUuid)
    }

    @Test
    fun participants() {
        Assertions.assertThat(actual.participants == setOf(ALICE.party, BOB.party))
    }

    @Test
    fun withNewOwner() {
        val securityWithNewOwner = actual.withNewOwner(CHARLIE.party)
        val expected = actual.copy(owner = CHARLIE.party)

        Assertions.assertThat(securityWithNewOwner == expected)
    }
}
