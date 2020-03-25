package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal
import java.math.BigInteger

@BelongsToContract(ProposalContract::class)
data class ProposalState(val securityLinearId: UniqueIdentifier,
                         val securityAmount: Int,
                         val etherAmount: BigDecimal,
                         val swapId: String,
                         val proposer: Party,
                         val acceptor: Party,
                         val FromEthereumAddress: String,
                         val ToEthereumAddress: String,
                         val status: ProposalStatus = ProposalStatus.PROPOSED,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<Party> get() = listOf(proposer, acceptor)

    fun withNewStatus(newStatus: ProposalStatus) = copy(status = newStatus)
}

@CordaSerializable
enum class ProposalStatus {
    PROPOSED, CONSUMED, ABORTED
}
