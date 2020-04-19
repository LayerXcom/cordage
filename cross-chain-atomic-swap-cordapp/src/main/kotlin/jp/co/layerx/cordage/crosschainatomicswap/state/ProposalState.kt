package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.math.BigInteger

@BelongsToContract(ProposalContract::class)
data class ProposalState(val corporateBondLinearId: UniqueIdentifier,
                         val quantity: Int,
                         val priceWei: BigInteger,
                         val swapId: String,
                         val proposer: Party,
                         val acceptor: Party,
                         var fromEthereumAddress: String,
                         val toEthereumAddress: String,
                         val status: ProposalStatus = ProposalStatus.PROPOSED,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    constructor(
        security: SecurityState,
        priceWei: BigInteger,
        swapId: String,
        proposer: Party,
        acceptor: Party
    ) : this(security.linearId, security.amount, priceWei, swapId, proposer, acceptor, proposer.ethAddress(), acceptor.ethAddress())

    override val participants: List<Party> get() = listOf(proposer, acceptor)

    fun withNewStatus(newStatus: ProposalStatus) = copy(status = newStatus)
}
