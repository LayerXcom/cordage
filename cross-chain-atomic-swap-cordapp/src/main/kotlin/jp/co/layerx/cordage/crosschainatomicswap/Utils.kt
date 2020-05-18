package jp.co.layerx.cordage.crosschainatomicswap

import net.corda.core.identity.Party
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(): String {
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
        val url = javaClass.classLoader.getResource("config.conf")
        if (url != null) {
            ethAddress.load(url.openStream())
        }
        val root = System.getProperty("user.dir")
        val path = Paths.get("$root/cordapps/config/cross-chain-atomic-swap-cordapp-0.1.conf")
        if (Files.isReadable(path)) {
            ethAddress.load(Files.newInputStream(path))
        }
    }

    return ethAddress.getProperty(this.name.organisation.replace(" ", ""), "0x0")
}
