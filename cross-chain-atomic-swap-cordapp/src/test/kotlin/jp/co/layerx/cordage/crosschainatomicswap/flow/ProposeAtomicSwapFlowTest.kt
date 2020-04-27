package jp.co.layerx.cordage.crosschainatomicswap.flow

import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokensHandler
import io.mockk.every
import io.mockk.mockk
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.CHARLIE_NAME
import net.corda.testing.node.*
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.time.Instant

class ProposeAtomicSwapFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    @Before
    fun setUp() {
        val notaryInfo = emptyList<NotaryInfo>()
        val networkParameters = NetworkParameters(
            4,
            notaryInfo,
            10485760,
            524288000,
            Instant.now(),
            1,
            emptyMap())
        val mockNetworkParameters = MockNetworkParameters(
            false,
            false,
            InMemoryMessagingNetwork.ServicePeerAllocationStrategy.Random(),
            listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB"))),
            networkParameters,
            listOf(
                TestCordapp.findCordapp("jp.co.layerx.cordage.crosschainatomicswap"),
                TestCordapp.findCordapp("com.r3.corda.lib.tokens.contracts")))
        network = MockNetwork(mockNetworkParameters)
        a = network.createNode(MockNodeParameters(legalName = ALICE_NAME))
        b = network.createNode(MockNodeParameters(legalName = BOB_NAME))
        c = network.createNode(MockNodeParameters(legalName = CHARLIE_NAME))
        val startedNodes = arrayListOf(a, b, c)
        startedNodes.forEach {
            it.registerInitiatedFlow(ProposeAtomicSwapFlowResponder::class.java)
            it.registerInitiatedFlow(CreateEvolvableTokensHandler::class.java)
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

        val corporateBondRegisterFlow = CorporateBondRegisterFlow("LayerX", BigDecimal(1.1), proposer)
        val f1 = c.startFlow(corporateBondRegisterFlow)
        network.runNetwork()

        val corporateBond = f1.getOrThrow().tx.outputsOfType<CorporateBond>().single()
        val expectedQuantity = 100L
        val priceEther = corporateBond.unitPriceEther.multiply(BigDecimal(expectedQuantity))
        val expectedPriceWei = Convert.toWei(priceEther, Convert.Unit.ETHER).toBigInteger()

        val expectedSwapId = "test_swap_id_12345"
        val expectedFromEthAddress = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0"
        val expectedToEthAddress = "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b"

        val mockLockEtherFlow = mockk<LockEtherFlow>(relaxed = true)
        val expectedTxHash = "0x1"
        every { mockLockEtherFlow.call() } returns expectedTxHash

        val proposeAtomicSwapFlow = ProposeAtomicSwapFlow(corporateBond.linearId, expectedQuantity, expectedSwapId, acceptor, expectedFromEthAddress, expectedToEthAddress, mockLockEtherFlow)

        val f2 = a.startFlow(proposeAtomicSwapFlow)
        network.runNetwork()

        val response = f2.getOrThrow()
        Assertions.assertThat(response.second).isEqualTo(expectedTxHash)

        val actualProposalTx = response.first
        Assertions.assertThat(actualProposalTx.inputs).hasSize(0)

        val actualProposalState = actualProposalTx.tx.outputsOfType<ProposalState>().single()
        Assertions.assertThat(actualProposalState.amount.quantity).isEqualTo(expectedQuantity)
        Assertions.assertThat(actualProposalState.priceWei).isEqualTo(expectedPriceWei)
        Assertions.assertThat(actualProposalState.swapId).isEqualTo(expectedSwapId)
        Assertions.assertThat(actualProposalState.status).isEqualTo(ProposalStatus.PROPOSED)

        val command = actualProposalTx.tx.commands.single()
        Assertions.assertThat(command.value).isInstanceOf(ProposalContract.ProposalCommands.Propose::class.java)
        Assertions.assertThat(command.signers.toSet()).isEqualTo(setOf(proposer.owningKey, acceptor.owningKey))
        actualProposalTx.verifyRequiredSignatures()
    }
}
