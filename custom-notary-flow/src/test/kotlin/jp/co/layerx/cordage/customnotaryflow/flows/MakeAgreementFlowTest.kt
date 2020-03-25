package jp.co.layerx.cordage.customnotaryflow.flows

import jp.co.layerx.cordage.customnotaryflow.contracts.AgreementContract
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import jp.co.layerx.cordage.customnotaryflow.states.AgreementStatus
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test


internal class MakeAgreementFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            listOf("jp.co.layerx.cordage.customnotaryflow"),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB")))
        )
        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters())
        val startedNodes = arrayListOf(a, b)
        startedNodes.forEach { it.registerInitiatedFlow(MakeAgreementFlowHandler::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `normal scenario`() {
        val origin = a.info.legalIdentities.single()
        val target = b.info.legalIdentities.single()
        val agreementBody = "RESIDENTIAL LEASE AGREEMENT"
        val flow = MakeAgreementFlow(target, agreementBody)
        val future = a.startFlow(flow)
        network.runNetwork()

        val tx = future.getOrThrow()
        val expected = Agreement(origin, target, AgreementStatus.MADE, agreementBody)

        Assertions.assertThat(tx.inputs.isEmpty())
        Assertions.assertThat(tx.tx.outputStates.single() == expected)

        val command = tx.tx.commands.single()
        Assertions.assertThat(command.value is AgreementContract.AgreementCommand.Make)
        Assertions.assertThat(command.signers.toSet() == expected.participants.map { it.owningKey }.toSet())
        tx.verifyRequiredSignatures()
    }
}
