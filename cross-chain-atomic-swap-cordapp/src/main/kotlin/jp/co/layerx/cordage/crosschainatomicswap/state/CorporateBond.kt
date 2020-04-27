package jp.co.layerx.cordage.crosschainatomicswap.state

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import jp.co.layerx.cordage.crosschainatomicswap.contract.CorporateBondContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.math.BigDecimal
import java.util.*

@BelongsToContract(CorporateBondContract::class)
class CorporateBond(
    val name: String,
    val unitPriceEther: BigDecimal,
    override val maintainers: List<Party>,
    override val fractionDigits: Int = 0,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : EvolvableTokenType() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CorporateBond

        if (name != other.name) return false
        if (unitPriceEther != other.unitPriceEther) return false
        if (maintainers != other.maintainers) return false
        if (fractionDigits != other.fractionDigits) return false
        if (linearId != other.linearId) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(name, unitPriceEther, maintainers, fractionDigits, linearId)
    }
}
