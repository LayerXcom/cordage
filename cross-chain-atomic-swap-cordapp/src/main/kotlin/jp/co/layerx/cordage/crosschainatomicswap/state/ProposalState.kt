package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.contract.ProposalContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.math.BigInteger

@BelongsToContract(ProposalContract::class)
data class ProposalState(val securityAmount: BigInteger,
                         val moneyAmount: BigInteger,
                         val swapID: BigInteger,
                         val proposer: Party,
                         val acceptor: Party,
                         val FromEthereumAddress: String,
                         val ToEthereumAddress: String,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<Party> get() = listOf(proposer, acceptor)
}
