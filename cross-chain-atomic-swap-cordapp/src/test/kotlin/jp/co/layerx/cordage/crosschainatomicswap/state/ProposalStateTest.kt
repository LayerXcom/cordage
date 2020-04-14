package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.UniqueIdentifier
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*


class ProposalStateTest {
    private val security = SecurityState(100, BOB.party, CHARLIE.party, "R3")
    private val expectedUuid = UUID.randomUUID()
    private val actual = ProposalState(
        security.linearId,
        security.amount,
        1_000_000.toBigInteger(),
        "1",
        ALICE.party,
        BOB.party,
        "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0",
        "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b",
        ProposalStatus.PROPOSED,
        UniqueIdentifier(id = expectedUuid)
    )

    @Test
    fun securityLinearId() {
        Assertions.assertThat(actual.securityLinearId == security.linearId)
    }

    @Test
    fun securityAmount() {
        Assertions.assertThat(actual.securityAmount == security.amount)
    }

    @Test
    fun weiAmount() {
        Assertions.assertThat(actual.weiAmount == 1_000_000.toBigInteger())
    }

    @Test
    fun swapId() {
        Assertions.assertThat(actual.swapId == "1")
    }

    @Test
    fun proposer() {
        Assertions.assertThat(actual.proposer == ALICE.party)
    }

    @Test
    fun acceptor() {
        Assertions.assertThat(actual.acceptor == BOB.party)
    }

    @Test
    fun fromEthereumAddress() {
        Assertions.assertThat(actual.fromEthereumAddress == "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0")
    }

    @Test
    fun toEthereumAddress() {
        Assertions.assertThat(actual.toEthereumAddress == "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b")
    }

    @Test
    fun status() {
        Assertions.assertThat(actual.status == ProposalStatus.PROPOSED)
    }

    @Test
    fun linearId() {
        Assertions.assertThat(actual.linearId == UniqueIdentifier(id = expectedUuid))
    }

    @Test
    fun participants() {
        Assertions.assertThat(actual.participants == setOf(ALICE.party, BOB.party))
    }

    @Test
    fun withNewStatus() {
        Assertions.assertThat(actual.withNewStatus(ProposalStatus.CONSUMED) == actual.copy(status = ProposalStatus.CONSUMED))
    }
}
