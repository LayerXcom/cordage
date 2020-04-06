package jp.co.layerx.cordage.crosschainatomicswap

import net.corda.core.identity.Party
import java.util.*

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex() : String {
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}

private val ethAddress = Properties()

fun Party.ethAddress(): String {
    if (ethAddress.isEmpty) {
        ethAddress.load(this::class.java.getResource("/ethAddress.properties").openStream())
    }

    return ethAddress.getProperty(this.name.toString(), "0x0")
}
