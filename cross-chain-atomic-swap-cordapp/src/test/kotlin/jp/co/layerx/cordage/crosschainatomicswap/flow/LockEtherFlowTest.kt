package jp.co.layerx.cordage.crosschainatomicswap.flow

import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.utilities.of
import io.mockk.every
import io.mockk.mockk
import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.readConfig
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.node.*
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.util.*


class LockEtherFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode

    @Before
    fun setUp() {

        val customConfig: MutableMap<String, String> = LinkedHashMap()
        customConfig["rpcUrl"] = readConfig("rpcUrl")
        customConfig["networkId"] = readConfig("networkId")
        customConfig["privateKey"] = readConfig("privateKey")

        network = MockNetwork(
            MockNetworkParameters()
                .withCordappsForAllNodes(
                    ImmutableList.of(
                        TestCordapp.findCordapp("jp.co.layerx.cordage.crosschainatomicswap")
                            .withConfig(customConfig))))

        a = network.createNode(MockNodeParameters(legalName = ALICE_NAME, configOverrides = MockNodeConfigOverrides()))
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `normal scenario`() {
        val corporateBond = CorporateBond("LayerX", BigDecimal(1.1), listOf(CHARLIE.party))
        val expectedQuantity = 100L
        val priceEther = corporateBond.unitPriceEther.multiply(BigDecimal(expectedQuantity))
        val expectedPriceWei = Convert.toWei(priceEther, Convert.Unit.ETHER).toBigInteger()

        val proposalState = ProposalState(
            UniqueIdentifier(),
            expectedQuantity of corporateBond.toPointer<CorporateBond>(),
            expectedPriceWei,
            "test_swap_id_123",
            ALICE.party,
            BOB.party)

        val mockSettlement = mockk<Settlement>(relaxed = true)
        every {
            mockSettlement.lock(
                proposalState.swapId,
                proposalState.fromEthereumAddress,
                proposalState.toEthereumAddress,
                proposalState.priceWei,
                proposalState.amount.quantity.toBigInteger(),
                proposalState.priceWei
            ).send()
        } returns TransactionReceipt("0x0", "", "", "", "", "", "", "", "", "", "", listOf(), "")

        val flow = LockEtherFlow(proposalState, mockSettlement)
        val future = a.startFlow(flow)
        network.runNetwork()
        val actual = future.getOrThrow()
        Assertions.assertThat(actual).startsWith("0x")
    }
}
