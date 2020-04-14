package jp.co.layerx.cordage.crosschainatomicswap.types

import net.corda.core.serialization.CordaSerializable
import java.math.BigInteger

@CordaSerializable
class LockedEvent(val settlementId: BigInteger, val swapId: String, val encodedSwapDetail: ByteArray) {
    companion object {
        fun listToLockedEvent(list: List<Any>): LockedEvent {
            if (list.size != 3) {
                throw IllegalArgumentException("An argument must be list which size is 3.")
            }
            return LockedEvent(
                settlementId = list[0] as BigInteger,
                swapId = list[1] as String,
                encodedSwapDetail = list[2] as ByteArray
            )
        }
    }
}
