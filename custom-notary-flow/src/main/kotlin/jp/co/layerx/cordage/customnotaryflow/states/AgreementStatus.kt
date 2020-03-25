package jp.co.layerx.cordage.customnotaryflow.states

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class AgreementStatus {
    MADE,
    TERMINATED,
}
