package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

@InitiatingFlow
@StartableByRPC
class LockEtherFlow(
    private val proposalState: ProposalState,
    private val settlement: Settlement? = null
) : FlowLogic<String>() {
    companion object {

        object READING_CONFIG : ProgressTracker.Step("Reading config from node config file.")
        object LOADING_WEB3 : ProgressTracker.Step("Loading web3 instance.")
        object SEND_TRANSACTION_TO_ETHEREUM_CONTRACT : ProgressTracker.Step("Sending ether to Settlement Contract for locking.")

        fun tracker() = ProgressTracker(
            READING_CONFIG,
            LOADING_WEB3,
            SEND_TRANSACTION_TO_ETHEREUM_CONTRACT
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = READING_CONFIG
        var _settlement: Settlement
        val config = serviceHub.getAppContext().config
        val ETHEREUM_RPC_URL = config.getString("rpcUrl")
        val ETHEREUM_NETWORK_ID = config.getString("networkId")
        val ETHEREUM_PRIVATE_KEY = config.getString("privateKey")

        progressTracker.currentStep = LOADING_WEB3

        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val targetContractAddress = Settlement.getPreviouslyDeployedAddress(ETHEREUM_NETWORK_ID)!!
        val credentials: Credentials = Credentials.create(ETHEREUM_PRIVATE_KEY)

        progressTracker.currentStep = SEND_TRANSACTION_TO_ETHEREUM_CONTRACT

        if (settlement != null) {
            _settlement = settlement
        } else {
            _settlement = Settlement.load(targetContractAddress, web3, credentials, StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000)))
        }

        // load Smart Contract Wrapper then send the transaction
        val response = _settlement.lock(
            proposalState.swapId,
            proposalState.fromEthereumAddress,
            proposalState.toEthereumAddress,
            proposalState.priceWei,
            proposalState.amount.quantity.toBigInteger(),
            proposalState.priceWei
        ).send()

        return response.transactionHash
    }
}

