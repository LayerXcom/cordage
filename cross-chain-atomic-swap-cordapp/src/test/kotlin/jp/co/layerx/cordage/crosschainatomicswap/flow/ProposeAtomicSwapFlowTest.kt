package jp.co.layerx.cordage.crosschainatomicswap.flow

import io.mockk.every
import io.mockk.mockk
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.CHARLIE_NAME
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test

class ProposeAtomicSwapFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    @Before
    fun setUp() {
        network = MockNetwork(
            listOf("jp.co.layerx.cordage.crosschainatomicswap"),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB")))
        )
        a = network.createNode(MockNodeParameters(legalName = ALICE_NAME))
        b = network.createNode(MockNodeParameters(legalName = BOB_NAME))
        c = network.createNode(MockNodeParameters(legalName = CHARLIE_NAME))
        val startedNodes = arrayListOf(a, b, c)
        startedNodes.forEach {
            it.registerInitiatedFlow(SecurityIssueFlowResponder::class.java)
            it.registerInitiatedFlow(ProposeAtomicSwapFlowResponder::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `normal scenario`() {
        val proposer = a.info.legalIdentities.single()
        val acceptor = b.info.legalIdentities.single()

        val securityIssueFlow = SecurityIssueFlow(100, proposer, "R3")
        val f1 = c.startFlow(securityIssueFlow)
        network.runNetwork()

        val expectedSecurityLinearId = (f1.getOrThrow().tx.outputStates.single() as SecurityState).linearId.id.toString()
        val expectedSecurityAmount = 100
        val expectedWeiAmount = 100000
        val expectedSwapId = "test_swap_id_12345"
        val expectedFromEthAddress = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0"
        val expectedToEthAddress = "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b"

        val mockLockEtherFlow = mockk<LockEtherFlow>(relaxed = true)
        val expectedTxHash = "0x1"
        every { mockLockEtherFlow.call() } returns expectedTxHash

        val proposeAtomicSwapFlow = ProposeAtomicSwapFlow(expectedSecurityLinearId, expectedSecurityAmount, expectedWeiAmount, expectedSwapId, acceptor, expectedFromEthAddress, expectedToEthAddress, mockLockEtherFlow)

        val f2 = a.startFlow(proposeAtomicSwapFlow)
        network.runNetwork()

        val response = f2.getOrThrow()
        Assertions.assertThat(response.second == expectedTxHash)

        val actualProposalTx = response.first
        Assertions.assertThat(actualProposalTx.inputs.isEmpty())

        val actualProposalState = actualProposalTx.tx.outputsOfType<ProposalState>().single()
        Assertions.assertThat(actualProposalState.securityAmount == expectedSecurityAmount.toBigInteger())
        Assertions.assertThat(actualProposalState.weiAmount == expectedWeiAmount.toBigInteger())
        Assertions.assertThat(actualProposalState.swapId == expectedSwapId)
        Assertions.assertThat(actualProposalState.status == ProposalStatus.PROPOSED)

        val command = actualProposalTx.tx.commands.single()
        Assertions.assertThat(command.value is ProposalContract.ProposalCommands.Propose)
        Assertions.assertThat(command.signers.toSet() == setOf(proposer.owningKey, acceptor.owningKey))
        actualProposalTx.verifyRequiredSignatures()
    }
}
