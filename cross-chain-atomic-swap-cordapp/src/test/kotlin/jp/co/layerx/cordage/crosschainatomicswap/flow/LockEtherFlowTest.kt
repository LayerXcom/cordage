package jp.co.layerx.cordage.crosschainatomicswap.flow

import io.mockk.every
import io.mockk.mockk
import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.web3j.protocol.core.methods.response.TransactionReceipt


class LockEtherFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode

    @Before
    fun setUp() {
        network = MockNetwork(
            listOf("jp.co.layerx.cordage.crosschainatomicswap"),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB")))
        )
        a = network.createNode(MockNodeParameters(legalName = ALICE_NAME))
        val startedNodes = arrayListOf(a)
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `normal scenario`() {
        val securityState = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val proposalState = ProposalState(securityState, 1_000_000.toBigInteger(), "test_swap_id_123", ALICE.party, BOB.party)

        val mockSettlement = mockk<Settlement>(relaxed = true)
        every {
            mockSettlement.lock(
                proposalState.swapId,
                proposalState.fromEthereumAddress,
                proposalState.toEthereumAddress,
                proposalState.weiAmount,
                proposalState.securityAmount.toBigInteger(),
                proposalState.weiAmount
            ).send()
        } returns TransactionReceipt("0x0", "", "", "", "", "", "", "", "", "", "", listOf(), "")

        val flow = LockEtherFlow(proposalState, mockSettlement)
        val future = a.startFlow(flow)
        network.runNetwork()
        val actual = future.getOrThrow()
        Assertions.assertThat(actual).startsWith("0x")
    }
}
