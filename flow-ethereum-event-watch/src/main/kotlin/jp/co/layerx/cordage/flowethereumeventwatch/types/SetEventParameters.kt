package jp.co.layerx.cordage.flowethereumeventwatch.types

import java.math.BigInteger

class SetEventParameters(val data: BigInteger): EventParameters {
    override fun fromList(list: List<Any>): SetEventParameters {
        if (list.size != 1) {
            throw IllegalArgumentException("An argument must be list which size is 3.")
        }
        return SetEventParameters(
            data = list[0] as BigInteger
        )
    }
}
