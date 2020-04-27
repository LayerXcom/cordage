package jp.co.layerx.cordage.crosschainatomicswap.contract

import com.r3.corda.lib.tokens.contracts.utilities.of
import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.CorporateBond
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class ProposalContractProposeTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    private val corporateBond = CorporateBond("LayerX", BigDecimal(1.1), listOf(CHARLIE.party))
    private val quantity = 100
    private val priceEther = corporateBond.unitPriceEther.multiply(BigDecimal(quantity))
    private val priceWei = Convert.toWei(priceEther, Convert.Unit.ETHER).toBigInteger()
    private val normalOutput = ProposalState(
        corporateBond.linearId,
        quantity of corporateBond.toPointer<CorporateBond>(),
        priceWei,
        "test_123",
        ALICE.party,
        BOB.party)

    @Test
    fun `normal scenario`() {
        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this.verifies()
            }
        }
    }

    @Test
    fun `propose transaction must have no inputs`() {
        val input = normalOutput.copy(proposer = CHARLIE.party)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, input)
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "No inputs should be consumed when issuing a Proposal."
            }
        }
    }

    @Test
    fun `propose transaction must have one output`() {
        val secondOutput = normalOutput.copy(proposer = CHARLIE.party)

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, normalOutput)
                output(ProposalContract.contractID, secondOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Only one output state should be created when issuing a Proposal."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have positive securityAmount`() {
        val zeroOutput = normalOutput.copy(amount = 0 of corporateBond.toPointer<CorporateBond>())

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, zeroOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a positive securityAmount."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have positive weiAmount`() {
        val zeroOutput = normalOutput.copy(priceWei = BigInteger.ZERO)
        val negativeOutput = normalOutput.copy(priceWei = (-1).toBigInteger())

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, zeroOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a positive weiAmount."
            }
            transaction {
                output(ProposalContract.contractID, negativeOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a positive weiAmount."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have not empty swapId`() {
        val output = normalOutput.copy(swapId = "")

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a not-empty swapId."
            }
        }
    }

    @Test
    fun `output's fromEthereumAddress must equal to proposer's ethAddress`() {
        val output = normalOutput.copy(fromEthereumAddress = CHARLIE.party.ethAddress())

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "fromEthereumAddress must equal to proposer's ethAddress."
            }
        }
    }

    @Test
    fun `output's toEthereumAddress must equal to acceptor's ethAddress`() {
        val output = normalOutput.copy(toEthereumAddress = CHARLIE.party.ethAddress())

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "toEthereumAddress must equal to acceptor's ethAddress."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have PROPOSED status`() {
        val output = normalOutput.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Proposal status must be PROPOSED."
            }
        }
    }

    @Test
    fun `proposer and acceptor together only must sign Propose transaction`() {
        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, normalOutput)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
        }
    }
}
