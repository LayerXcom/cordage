package jp.co.layerx.cordage.crosschainatomicswap.contract

import jp.co.layerx.cordage.crosschainatomicswap.*
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import jp.co.layerx.cordage.crosschainatomicswap.state.SecurityState
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger

import org.junit.Test

class SecurityContractTransferForSettleTest {
    private var ledgerServices = MockServices(listOf("jp.co.layerx.cordage.crosschainatomicswap"))

    @Test
    fun `normal scenario`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
    fun `transfer for settle transaction must have two inputs`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
                // ProposalContract's validation was executed before SecurityContract's
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
                // ProposalContract's validation was executed before SecurityContract's
                this `fails with` "An Proposal Consume transaction should only consume two input states."
            }
        }
    }

    @Test
    fun `transfer for settle transaction must have two outputs`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
                // ProposalContract's validation was executed before SecurityContract's
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
                // ProposalContract's validation was executed before SecurityContract's
                this `fails with` "An Proposal Consume transaction should only create two output states."
            }
        }
    }

    @Test
    fun `transfer for settle transaction can only change owner`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
                output(SecurityContract.contractID, SecurityState(1000, ALICE.party, CHARLIE.party, "LayerX"))
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                // ProposalContract's validation was executed before SecurityContract's
                // this `fails with` "Only the owner property may change."
                this `fails with` "InputProposalState's securityAmount must equal to OutputSecurityState's amount."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, SecurityState(100, ALICE.party, BOB.party, "LayerX"))
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the owner property may change."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, SecurityState(100, ALICE.party, CHARLIE.party, "LayerZ"))
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "Only the owner property may change."
            }
        }
    }

    @Test
    fun `transfer for settle transaction must change owner`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
                output(SecurityContract.contractID, inputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                // ProposalContract's validation was executed before SecurityContract's
                // this `fails with` "The owner property must change in a TransferForSettle."
                this `fails with` "InputProposalState's proposer must equal to OutputSecurityState's owner."
            }
        }
    }

    @Test
    fun `input proposal's acceptor must equal to input security state's owner`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
            inputSecurity.amount.toBigInteger(),
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
            999.toBigInteger(),
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
            inputSecurity.amount.toBigInteger(),
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
            inputSecurity.amount.toBigInteger(),
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
    fun `previous owner, new owner and issuer only must sign transfer for settle transaction`() {
        val inputSecurity = SecurityState(100, BOB.party, CHARLIE.party, "LayerX")
        val outputSecurity = inputSecurity.withNewOwner(ALICE.party)
        val inputProposal = ProposalState(
            inputSecurity.linearId,
            inputSecurity.amount.toBigInteger(),
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
                command(listOf(BOB.publicKey, ALICE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "The issuer, old owner and new owner must sign an Security TransferForSettle transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(BOB.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "The issuer, old owner and new owner must sign an Security TransferForSettle transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(ALICE.publicKey, CHARLIE.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "The issuer, old owner and new owner must sign an Security TransferForSettle transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey, DUMMY_BANK_A.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "The issuer, old owner and new owner must sign an Security TransferForSettle transaction."
            }
            transaction {
                input(ProposalContract.contractID, inputProposal)
                input(SecurityContract.contractID, inputSecurity)
                output(ProposalContract.contractID, outputProposal)
                output(SecurityContract.contractID, outputSecurity)
                command(listOf(ALICE.publicKey, BOB.publicKey), ProposalContract.ProposalCommands.Consume())
                command(listOf(ALICE.publicKey, BOB.publicKey, DUMMY_BANK_A.publicKey), SecurityContract.SecurityCommands.TransferForSettle())
                this `fails with` "The issuer, old owner and new owner must sign an Security TransferForSettle transaction."
            }
        }
    }
}
