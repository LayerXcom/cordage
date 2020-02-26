package jp.co.layerx.cordage.flowethereumtx

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

const val ETHEREUM_RPC_URL = "http://localhost:8545"

@InitiatingFlow
@StartableByRPC
class Flow: FlowLogic<String>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val tx = Transaction.createEtherTransaction(
                "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1",
                null,
                BigInteger.valueOf(1),
                BigInteger.valueOf(21000),
                "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0",
                BigInteger.valueOf(1_000_000_000_000_000_000)
        )

        val response = web3.ethSendTransaction(tx).send()
        return response.transactionHash
    }
}
