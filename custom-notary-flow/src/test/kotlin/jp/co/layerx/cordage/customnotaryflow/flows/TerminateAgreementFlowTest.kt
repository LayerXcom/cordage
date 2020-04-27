package jp.co.layerx.cordage.customnotaryflow.flows

import jp.co.layerx.cordage.customnotaryflow.contracts.AgreementContract
import jp.co.layerx.cordage.customnotaryflow.states.Agreement
import net.corda.core.contracts.StateRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test

class TerminateAgreementFlowTest {
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

    private fun makeAgreement(target: Party, agreementBody: String): SignedTransaction {
        val flow = MakeAgreementFlow(target, agreementBody)
        val future = a.startFlow(flow)
        network.runNetwork()
        return future.getOrThrow()
    }

    @Test
    fun `normal scenario`() {
        // make agreement
        val target = b.info.legalIdentities.single()
        val agreementBody = "RESIDENTIAL LEASE AGREEMENT"
        val stx = makeAgreement(target, agreementBody)

        // set up for terminating
        val input = stx.tx.outputStates.single() as Agreement
        val flow = TerminateAgreementFlow(input.linearId.toString())
        val future = a.startFlow(flow)
        network.runNetwork()
        val tx = future.getOrThrow()

        val expected = input.terminate()

        Assertions.assertThat(tx.inputs.single()).isEqualTo(StateRef(stx.id, 0))
        Assertions.assertThat(tx.tx.outputStates.single()).isEqualTo(expected)

        val command = tx.tx.commands.single()
        Assertions.assertThat(command.value).isInstanceOf(AgreementContract.AgreementCommand.Terminate::class.java)
        Assertions.assertThat(command.signers.toSet()).isEqualTo(expected.participants.map { it.owningKey }.toSet())
        tx.verifyRequiredSignatures()
    }
}
