package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.ALICE
import jp.co.layerx.cordage.crosschainatomicswap.BOB
import jp.co.layerx.cordage.crosschainatomicswap.CHARLIE
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger

import org.junit.Test
import java.util.*

class ProposalContractConsumeTest  {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    @Test
    fun `normal scenario`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this.verifies()
            }
        }
    }

    @Test
    fun `consume transaction must have two inputs`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "An Proposal Consume transaction should only consume two input states."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "An Proposal Consume transaction should only consume two input states."
            }
        }
    }

    @Test
    fun `consume transaction must have two outputs`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "An Proposal Consume transaction should only create two output states."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "An Proposal Consume transaction should only create two output states."
            }
        }
    }

    @Test
    fun `consume transaction's input proposal state must be PROPOSED`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.CONSUMED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Input ProposalState's status must be PROPOSED."
            }
        }
    }

    @Test
    fun `consume transaction can only change status`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
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
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        UniqueIdentifier(id = UUID.randomUUID()),
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "1",
                        ALICE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        99,
                        10000.toBigInteger(),
                        "1",
                        ALICE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        99.toBigInteger(),
                        "1",
                        ALICE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "99",
                        ALICE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "1",
                        CHARLIE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "1",
                        ALICE.party,
                        CHARLIE.party,
                        ALICE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "1",
                        ALICE.party,
                        BOB.party,
                        CHARLIE.party.ethAddress(),
                        BOB.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID,
                    ProposalState(
                        inputSecurity.linearId,
                        inputSecurity.amount,
                        10000.toBigInteger(),
                        "1",
                        ALICE.party,
                        BOB.party,
                        ALICE.party.ethAddress(),
                        CHARLIE.party.ethAddress(),
                        ProposalStatus.CONSUMED
                    )
                )
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
        }
    }

    @Test
    fun `consume transaction must change proposal status to CONSUMED`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.PROPOSED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the status property may change."
            }
        }
    }

    @Test
    fun `input proposal's acceptor must equal to input security state's owner`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            CHARLIE.party,
            ALICE.party.ethAddress(),
            CHARLIE.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "InputProposalState's acceptor must equal to InputSecurityState's owner."
            }
        }
    }

    @Test
    fun `input proposal's proposer must equal to output security state's owner`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            CHARLIE.party,
            BOB.party,
            CHARLIE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "InputProposalState's proposer must equal to OutputSecurityState's owner."
            }
        }
    }

    @Test
    fun `input proposal's securityAmount must equal to output security state's amount`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            999,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "InputProposalState's securityAmount must equal to OutputSecurityState's amount."
            }
        }
    }

    @Test
    fun `input proposal's fromEthereumAddress must equal to output security state's owner ethAddress`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            "0x0",
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "InputProposalState's fromEthereumAddress must equal to OutputSecurityState's owner ethAddress."
            }
        }
    }

    @Test
    fun `input proposal's toEthereumAddress must equal to input security state's owner ethAddress`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            "0x0",
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "InputProposalState's toEthereumAddress must equal to InputSecurityState's owner ethAddress."
            }
        }
    }

    @Test
    fun `proposer and acceptor together only must sign Propose transaction`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount,
            10000.toBigInteger(),
            "1",
            ALICE.party,
            BOB.party,
            ALICE.party.ethAddress(),
            BOB.party.ethAddress(),
            ProposalStatus.PROPOSED
        )
        val outputProposal = inputProposal.withNewStatus(ProposalStatus.CONSUMED)

        ledgerServices.ledger {
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Both proposer and acceptor together only may sign Proposal consume transaction."
            }
        }
    }
}
