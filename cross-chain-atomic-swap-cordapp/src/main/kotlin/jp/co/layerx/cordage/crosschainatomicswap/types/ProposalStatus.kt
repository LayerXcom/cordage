package jp.co.layerx.cordage.crosschainatomicswap.types

import net.corda.core.serialization.CordaSerializable
import org.web3j.abi.datatypes.generated.Uint8

@CordaSerializable
enum class ProposalStatus {
    PROPOSED, CONSUMED, ABORTED;

    companion object {
        fun fromInt(index: Uint8): ProposalStatus {
            return values().firstOrNull { Uint8(it.ordinal.toBigInteger()) == index } ?: PROPOSED
        }
    }
}
