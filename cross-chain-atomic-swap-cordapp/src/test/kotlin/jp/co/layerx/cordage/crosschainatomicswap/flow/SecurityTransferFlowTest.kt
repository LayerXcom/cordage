package jp.co.layerx.cordage.crosschainatomicswap.flow

import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
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
import kotlin.test.assertFailsWith

class SecurityTransferFlowTest {
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
        a = network.createNode(MockNodeParameters(legalName = CordaX500Name("Alice", "London", "GB")))
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("Bob", "London", "GB")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("Charlie", "London", "GB")))
        val startedNodes = arrayListOf(a, b, c)
        startedNodes.forEach { it.registerInitiatedFlow(SecurityIssueFlowResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    private fun issueSecurity(amount: Int, owner: Party, name: String): SignedTransaction {
        val flow = SecurityIssueFlow(amount, owner, name)
        val future = c.startFlow(flow)
        network.runNetwork()
        return future.getOrThrow()
    }

    @Test
    fun `normal scenario`() {
        val owner = a.info.legalIdentities.single()
        val newOwner = b.info.legalIdentities.single()
        val signedIssueTx = issueSecurity(100, owner, "R3")
        val inputSecurity = signedIssueTx.tx.outputs.single().data as SecurityState
        val flow = SecurityTransferFlow(inputSecurity.linearId, newOwner)
        val future = a.startFlow(flow)
        network.runNetwork()
        val response = future.getOrThrow()

        val actualSignedTx = response
        val expected = inputSecurity.withNewOwner(newOwner)
        Assertions.assertThat(actualSignedTx.tx.inputs.size == 1)
        Assertions.assertThat(actualSignedTx.tx.outputs.size == 1)
        Assertions.assertThat(actualSignedTx.tx.inputs.single() == StateRef(signedIssueTx.id, 0))
        Assertions.assertThat(actualSignedTx.tx.outputStates.single() == expected)

        val command = actualSignedTx.tx.commands.single()
        Assertions.assertThat(command.value is SecurityContract.SecurityCommands.Issue)
        Assertions.assertThat(command.signers.toSet() == (expected.participants + owner).map { it.owningKey }.toSet())
        actualSignedTx.verifyRequiredSignatures()
    }

    @Test
    fun `security transfer flow only be started by security owner`() {
        val owner = a.info.legalIdentities.single()
        val newOwner = b.info.legalIdentities.single()
        val signedIssueTx = issueSecurity(100, owner, "R3")
        val inputSecurity = signedIssueTx.tx.outputs.single().data as SecurityState
        val flow = SecurityTransferFlow(inputSecurity.linearId, newOwner)
        val future = c.startFlow(flow)
        network.runNetwork()
        assertFailsWith<IllegalArgumentException> { future.getOrThrow() }
    }
}
