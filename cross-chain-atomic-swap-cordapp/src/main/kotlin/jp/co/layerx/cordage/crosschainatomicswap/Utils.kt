package jp.co.layerx.cordage.crosschainatomicswap

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party

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

private val notary = CordaX500Name("Notary", "London", "GB")
private val participantA = CordaX500Name("ParticipantA", "London", "GB")
private val participantB = CordaX500Name("ParticipantB", "New York", "US")
private val participantC = CordaX500Name("ParticipantC", "Paris", "FR")

fun Party.ethAddress(): String {
    return when (this.name) {
        // TODO import Ethereum Addresses from .env
        participantA -> {
            "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0"
        }
        participantB -> {
            "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b"
        }
        participantC -> {
            "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1"
        }
        notary -> {
            "0xE11BA2b4D45Eaed5996Cd0823791E0C93114882d"
        }
        else -> {
            throw IllegalArgumentException("This party's ethAddress doesn't exist.")
        }
    }
}
