package jp.co.layerx.cordage.crosschainatomicswap.notary

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.internal.notary.SinglePartyNotaryService
import net.corda.core.schemas.MappedSchema
import net.corda.node.services.api.ServiceHubInternal
import net.corda.node.services.transactions.NodeNotarySchema
import net.corda.node.services.transactions.PersistentUniquenessProvider
import java.security.PublicKey

class CustomValidatingNotaryService(override val services: ServiceHubInternal, override val notaryIdentityKey: PublicKey) : SinglePartyNotaryService() {
    private val notaryConfig = services.configuration.notary
            ?: throw IllegalArgumentException("Failed to register ${this::class.java}: notary configuration not present")

    init {
        val mode = if (notaryConfig.validating) "validating" else "non-validating"
        log.info("Starting notary in $mode mode")
    }

    override val uniquenessProvider = PersistentUniquenessProvider(
        services.clock,
        services.database,
        services.cacheFactory,
        ::signTransaction
    )

    override fun createServiceFlow(otherPartySession: FlowSession): FlowLogic<Void?> = CustomValidatingNotaryFlow(otherPartySession, this)

    override fun start() {}
    override fun stop() {}
}

object NodeNotarySchema

object NodeNotarySchemaV1 : MappedSchema(schemaFamily = NodeNotarySchema.javaClass, version = 1,
        mappedTypes = listOf(PersistentUniquenessProvider.BaseComittedState::class.java,
                PersistentUniquenessProvider.Request::class.java,
                PersistentUniquenessProvider.CommittedState::class.java,
                PersistentUniquenessProvider.CommittedTransaction::class.java
        )) {
    override val migrationResource = "node-notary.changelog-master"
}
