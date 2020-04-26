package jp.co.layerx.cordage.crosschainatomicswap.contract

import com.r3.corda.lib.tokens.contracts.utilities.of
import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class ProposalContractConsumeTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    private val corporateBond = CorporateBond("LayerX", BigDecimal(1.1), listOf(CHARLIE.party))
    private val quantity = 100
    private val priceEther = corporateBond.unitPriceEther.multiply(BigDecimal(quantity))
    private val priceWei = Convert.toWei(priceEther, Convert.Unit.ETHER).toBigInteger()
    private val normalInput = ProposalState(
        corporateBond.linearId,
        quantity of corporateBond.toPointer<CorporateBond>(),
        priceWei,
        "test_123",
        ALICE.party,
        BOB.party)
    private val normalOutput = normalInput.withNewStatus(ProposalStatus.CONSUMED)

    @Test
    fun `normal scenario`() {
        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                this.verifies()
            }
        }
    }

    @Test
    fun `consume transaction's input proposal state must be PROPOSED`() {
        val input = normalInput.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, input)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Input ProposalState's status must be PROPOSED."
            }
        }
    }

    @Test
    fun `consume transaction can only change status`() {
        val output = normalOutput.copy(priceWei = BigInteger.valueOf(100000))

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Only the status property may change."
            }
        }
    }

    @Test
    fun `consume transaction must change proposal status to CONSUMED`() {
        val output = normalInput

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Only the status property may change."
            }
        }
    }

    @Test
    fun `proposer and acceptor together only must sign Propose transaction`() {
        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, normalInput)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
        }
    }
}
