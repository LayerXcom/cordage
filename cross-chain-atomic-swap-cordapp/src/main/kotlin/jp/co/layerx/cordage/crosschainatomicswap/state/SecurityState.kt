package jp.co.layerx.cordage.crosschainatomicswap.state

import jp.co.layerx.cordage.crosschainatomicswap.contract.SecurityContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(SecurityContract::class)
data class SecurityState(val amount: Int,
                    val owner: Party,
                    val issuer: Party,
                    val name: String,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<Party> get() = listOf(owner, issuer)

    fun withNewOwner(newOwner: Party) = copy(owner = newOwner)
}
