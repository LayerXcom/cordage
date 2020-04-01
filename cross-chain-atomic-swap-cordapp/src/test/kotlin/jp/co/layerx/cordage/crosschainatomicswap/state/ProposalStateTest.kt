package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*


class ProposalStateTest {
    private val security = SecurityState(100, BOB.party, CHARLIE.party, "R3")
    private val expectedUuid = UUID.randomUUID()
    private val actual = ProposalState(security, 1_000_000.toBigInteger(), "1", ALICE.party, BOB.party, "", "")

    @Test
    fun securityLinearId() {
        Assertions.assertThat(actual.securityLinearId == security.linearId)
    }

    @Test
    fun securityAmount() {
    }

    @Test
    fun weiAmount() {
    }

    @Test
    fun swapId() {
    }

    @Test
    fun proposer() {
    }

    @Test
    fun acceptor() {
    }

    @Test
    fun fromEthereumAddress() {
    }

    @Test
    fun toEthereumAddress() {
    }

    @Test
    fun status() {
    }

    @Test
    fun linearId() {
    }

    @Test
    fun participants() {
    }

    @Test
    fun withNewStatus() {
        Assertions.assertThat(actual.withNewStatus(ProposalStatus.CONSUMED) == actual.copy(status = ProposalStatus.CONSUMED))
    }
}
