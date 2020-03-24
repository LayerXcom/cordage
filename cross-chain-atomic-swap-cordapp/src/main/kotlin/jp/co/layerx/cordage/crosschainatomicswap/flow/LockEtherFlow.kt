package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.SimpleStorage
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger
import org.web3j.protocol.core.methods.request.Transaction

@InitiatingFlow
@StartableByRPC
class LockEtherFlow(val finalizedProposalState: ProposalState): FlowLogic<String>() {
    companion object {
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        const val TARGET_CONTRACT_ADDRESS = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
        // TODO credentials should be imported by .env
        val credentials: Credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
        object SEND_TRANSACTION_TO_ETHEREUM_CONTRACT: ProgressTracker.Step("Send Tx to Ethereum Contract.")

        fun tracker() = ProgressTracker(
            SEND_TRANSACTION_TO_ETHEREUM_CONTRACT
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = SEND_TRANSACTION_TO_ETHEREUM_CONTRACT

        // load Smart Contract Wrapper
        val simpleStorage: SimpleStorage = SimpleStorage.load(TARGET_CONTRACT_ADDRESS, web3, credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(500000)))
        // simpleStorage.set(100).send()

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

