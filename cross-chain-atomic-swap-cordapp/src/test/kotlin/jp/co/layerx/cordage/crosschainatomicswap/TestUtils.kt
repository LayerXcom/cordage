package jp.co.layerx.cordage.crosschainatomicswap

import net.corda.testing.core.*
import java.util.*

val ALICE = TestIdentity(ALICE_NAME)
val BOB = TestIdentity(BOB_NAME)
val CHARLIE = TestIdentity(CHARLIE_NAME)
val DUMMY_BANK_A = TestIdentity(DUMMY_BANK_A_NAME)

fun readConfig(key: String): String {
    val properties = Properties()

    val url = object{}.javaClass.classLoader.getResource("config.conf")
    if (url != null) {
        properties.load(url.openStream())
    }
    return properties.getProperty(key, "0x0")
}
