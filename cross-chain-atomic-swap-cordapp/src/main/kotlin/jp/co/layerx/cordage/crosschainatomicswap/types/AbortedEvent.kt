package jp.co.layerx.cordage.crosschainatomicswap.types

import net.corda.core.serialization.CordaSerializable
import java.math.BigInteger

@CordaSerializable
class AbortedEvent(val settlementId: BigInteger, val swapId: String, val encodedSwapDetail: String) {
    companion object {
        fun listToAbortedEvent(list: List<Any>): AbortedEvent {
            if (list.size != 3) {
                throw IllegalArgumentException("An argument must be list which size is 3.")
            }
            return AbortedEvent(
                settlementId = list[0] as BigInteger,
                swapId = list[1] as String,
                encodedSwapDetail = list[2] as String
            )
        }
    }
}
