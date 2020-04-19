package jp.co.layerx.cordage.crosschainatomicswap.contract

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class CorporateBondContract: EvolvableTokenContract(), Contract {
    override fun additionalCreateChecks(tx: LedgerTransaction) {
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
    }
}
