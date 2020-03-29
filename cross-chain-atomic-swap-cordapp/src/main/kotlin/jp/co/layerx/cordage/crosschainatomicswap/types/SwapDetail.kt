package jp.co.layerx.cordage.crosschainatomicswap.types

import net.corda.core.serialization.CordaSerializable
import java.math.BigInteger

@CordaSerializable
class SwapDetail(val fromEthereumAddress: String,
                 val toEthereumAddress: String,
                 val weiAmount: BigInteger,
                 val securityAmount: BigInteger,
                 val status: ProposalStatus) {
    companion object {
        fun listToSwapDetail(list: List<Any>): SwapDetail {
            if (list.size != 5) {
                throw IllegalArgumentException("An argument must be list which size is 5.")
            }
            return SwapDetail(
                fromEthereumAddress = list[0] as String,
                toEthereumAddress = list[1] as String,
                weiAmount = list[2] as BigInteger,
                securityAmount = list[3] as BigInteger,
                status = ProposalStatus.fromInt(list[4] as Int)
            )
        }
    }
}
