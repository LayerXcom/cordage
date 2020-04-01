package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import net.corda.core.contracts.UniqueIdentifier
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*

class SecurityStateTest {
    private val expectedUuid = UUID.randomUUID()
    private val actual = SecurityState(100, ALICE.party, BOB.party, "R3 Limited.", UniqueIdentifier(id = expectedUuid))

    @Test
    fun amount() {
    }

    @Test
    fun owner() {
    }

    @Test
    fun issuer() {
    }

    @Test
    fun name() {
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
    }
}
