package jp.co.layerx.cordage.crosschainatomicswap.flow

import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.sumTokenStatesOrThrow
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokensHandler
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokensHandler
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokensHandler
import io.mockk.every
import io.mockk.mockk
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import jp.co.layerx.cordage.crosschainatomicswap.types.SwapDetail
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.CHARLIE_NAME
import net.corda.testing.node.*
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.time.Instant


class SettleAtomicSwapFlowTest {
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
            it.registerInitiatedFlow(SettleAtomicSwapFlowResponder::class.java)
            it.registerInitiatedFlow(CreateEvolvableTokensHandler::class.java)
            it.registerInitiatedFlow(IssueTokensHandler::class.java)
            it.registerInitiatedFlow(MoveFungibleTokensHandler::class.java)
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

        c.startFlow(CorporateBondIssueFlow(corporateBond.linearId, 1000L, acceptor))
        network.runNetwork()

        val swapId = "test_swap_id_12345"
        val fromEthAddress = proposer.ethAddress()
        val toEthAddress = acceptor.ethAddress()

        val mockLockEtherFlow = mockk<LockEtherFlow>(relaxed = true)
        val expectedTxHash = "0x1"
        every { mockLockEtherFlow.call() } returns expectedTxHash

        val expectedQuantity = 100L
        val proposeAtomicSwapFlow = ProposeAtomicSwapFlow(corporateBond.linearId, 100L, swapId, acceptor, fromEthAddress, toEthAddress, mockLockEtherFlow)

        val f2 = a.startFlow(proposeAtomicSwapFlow)
        network.runNetwork()

        val proposalState = f2.getOrThrow().first.tx.outputsOfType<ProposalState>().single()

        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalState.linearId))
        val proposalStateAndRef = b.services.vaultService.queryBy<ProposalState>(criteria).states.single()
        val settleAtomicSwapFlow = SettleAtomicSwapFlow(proposalStateAndRef, SwapDetail(
            Address(proposalState.fromEthereumAddress),
            Address(proposalState.toEthereumAddress),
            Uint256(proposalState.priceWei),
            Uint256(proposalState.amount.quantity),
            ProposalStatus.PROPOSED))

        val f3 = b.startFlow(settleAtomicSwapFlow)
        network.runNetwork()

        val stx = f3.getOrThrow()
        stx.verifyRequiredSignatures()

        val actualProposalState = stx.tx.outputsOfType<ProposalState>().single()
        Assertions.assertThat(actualProposalState == proposalState.withNewStatus(ProposalStatus.CONSUMED))

        val actualFungibleTokens = stx.tx.outputsOfType<FungibleToken>()
        Assertions.assertThat(expectedQuantity == actualFungibleTokens.filter { it.holder == proposer }.sumTokenStatesOrThrow().quantity)
    }
}
