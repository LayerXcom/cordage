package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger

import org.junit.Test
import java.math.BigInteger

class ProposalContractProposeTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    @Test
    fun `normal scenario`() {
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this.verifies()
            }
        }
    }

    @Test
    fun `propose transaction must have no inputs`() {
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val input = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val output = input.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, input)
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "No inputs should be consumed when issuing a Proposal."
            }
        }
    }

    @Test
    fun `propose transaction must have one output`() {
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Only one output state should be created when issuing a Proposal."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have positive securityAmount`() {
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val zeroOutput = ProposalState(
            security.linearId,
            BigInteger.ZERO,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val negativeOutput = ProposalState(
            security.linearId,
            (-100).toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, zeroOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a positive securityAmount."
            }
            transaction {
                output(ProposalContract.contractID, negativeOutput)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "A newly issued Proposal must have a positive securityAmount."
            }
        }
    }

    @Test
    fun `propose transaction's output state must have positive weiAmount`() {
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val zeroOutput = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            BigInteger.ZERO,
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val negativeOutput = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            (-100).toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

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
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

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
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            CHARLIE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

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
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            CHARLIE.party.ethAddress(),
            ProposalStatus.PROPOSED
        )

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
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.CONSUMED
        )

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
        val security = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val output = ProposalState(
            security.linearId,
            security.amount.toBigInteger(),
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        ledgerServices.ledger {
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
            transaction {
                output(ProposalContract.contractID, output)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Propose())
                this `fails with` "Both proposer and acceptor together only may sign Proposal issue transaction."
            }
        }
    }
}
