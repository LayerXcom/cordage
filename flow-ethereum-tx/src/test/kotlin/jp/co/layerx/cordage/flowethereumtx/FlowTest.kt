package jp.co.layerx.cordage.flowethereumtx

import org.assertj.core.api.Assertions.*

import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowTest {
    private val network: MockNetwork = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("jp.co.layerx.cordage.flowethereumtx"))))
    private lateinit var a: StartedMockNode

    @Before
    fun setup() {
        a = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `should return correctly`() {
        val flow = Flow()
        val future = a.startFlow(flow)
        network.runNetwork()
        val returnValue = future.get()

        assertThat(returnValue).startsWith("0x")
    }
}