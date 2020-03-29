package jp.co.layerx.cordage.crosschainatomicswap.types

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class ProposalStatus {
    PROPOSED, CONSUMED, ABORTED;

    companion object {
        fun fromInt(index: Int): ProposalStatus {
            return values().firstOrNull { it.ordinal == index } ?: PROPOSED
        }
    }
}
