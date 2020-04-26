package jp.co.layerx.cordage.crosschainatomicswap.state

import com.r3.corda.lib.tokens.contracts.types.TokenType
import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import jp.co.layerx.cordage.crosschainatomicswap.ethAddress
import jp.co.layerx.cordage.crosschainatomicswap.types.ProposalStatus
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.math.BigInteger

@BelongsToContract(ProposalContract::class)
data class ProposalState(val corporateBondLinearId: UniqueIdentifier,
                         val amount: Amount<TokenType>,
                         val priceWei: BigInteger,
                         val swapId: String,
                         val proposer: Party,
                         val acceptor: Party,
                         var fromEthereumAddress: String,
                         val toEthereumAddress: String,
                         val status: ProposalStatus = ProposalStatus.PROPOSED,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    constructor(
        corporateBondLinearId: UniqueIdentifier,
        amount: Amount<TokenType>,
        priceWei: BigInteger,
        swapId: String,
        proposer: Party,
        acceptor: Party
    ) : this(corporateBondLinearId, amount, priceWei, swapId, proposer, acceptor, proposer.ethAddress(), acceptor.ethAddress())

    override val participants: List<Party> get() = listOf(proposer, acceptor)

    fun withNewStatus(newStatus: ProposalStatus) = copy(status = newStatus)
}
