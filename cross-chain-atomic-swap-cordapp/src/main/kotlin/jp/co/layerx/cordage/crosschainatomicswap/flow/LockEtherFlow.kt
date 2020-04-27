package jp.co.layerx.cordage.crosschainatomicswap.flow

import co.paralleluniverse.fibers.Suspendable
import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import jp.co.layerx.cordage.crosschainatomicswap.state.ProposalState
import net.corda.core.flows.*
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
    private val settlement: Settlement = Settlement.load(targetContractAddress, web3, credentials, StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000)))
) : FlowLogic<String>() {
    companion object {
        // TODO Use Node Configuration https://github.com/LayerXcom/cordage/issues/20
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        private const val ETHEREUM_NETWORK_ID = "5777"
        private const val ETHEREUM_PRIVATE_KEY = "0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val targetContractAddress = Settlement.getPreviouslyDeployedAddress(ETHEREUM_NETWORK_ID)!!
        val credentials: Credentials = Credentials.create(ETHEREUM_PRIVATE_KEY)

        object SEND_TRANSACTION_TO_ETHEREUM_CONTRACT : ProgressTracker.Step("Sending ether to Settlement Contract for locking.")

        fun tracker() = ProgressTracker(
            SEND_TRANSACTION_TO_ETHEREUM_CONTRACT
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = SEND_TRANSACTION_TO_ETHEREUM_CONTRACT

        // load Smart Contract Wrapper then send the transaction
        val response = settlement.lock(
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

