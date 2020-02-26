package jp.co.layerx.cordage.flowethereumeventwatch

import org.assertj.core.api.Assertions.*

import net.corda.client.rpc.notUsed
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import jp.co.layerx.cordage.flowethereumeventwatch.flow.StartEventWatchFlow
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var node: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(threadPerNode = true, cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("jp.co.layerx.cordage.flowethereumeventwatch"))))
        node = network.createNode()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `get block number`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val recentBlockNumber = web3.ethBlockNumber().send().blockNumber
        assertThat(recentBlockNumber.toInt()).isPositive()
    }

    @Test
    fun `eventwatch occurs every 5 seconds`() {
        val fromBlockNumber = 1.toBigInteger()
        val targetContractAddress = "0xd0a6E6C54DbC68Db5db3A091B171A77407Ff7ccf"
        val eventName = "SETTLEMENT_EVENT"
        val flow = StartEventWatchFlow(fromBlockNumber, targetContractAddress, eventName)
        node.startFlow(flow).get()

        val sleepTime: Long = 6000
        Thread.sleep(sleepTime)

        val recordedTxs = node.transaction {
            val (recordedTxs, futureTxs) = node.services.validatedTransactions.track()
            futureTxs.notUsed()
            recordedTxs
        }

        val totalExpectedTransactions = 7
        assertThat(recordedTxs.size).isEqualTo(totalExpectedTransactions)
    }
}
