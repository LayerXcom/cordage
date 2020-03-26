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
class LockEtherFlow(val finalizedProposalState: ProposalState): FlowLogic<String>() {
    companion object {
        private const val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3: Web3j = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        const val TARGET_CONTRACT_ADDRESS = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
        // TODO credentials should be imported by .env
        val credentials: Credentials = Credentials.create("0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1")
        object SEND_TRANSACTION_TO_ETHEREUM_CONTRACT: ProgressTracker.Step("Send Tx to Ethereum Contract.")

        fun tracker() = ProgressTracker(
            SEND_TRANSACTION_TO_ETHEREUM_CONTRACT
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = SEND_TRANSACTION_TO_ETHEREUM_CONTRACT

        val swapId = finalizedProposalState.swapId
        val transferFromAddress = finalizedProposalState.FromEthereumAddress
        val transferToAddress = finalizedProposalState.ToEthereumAddress
        val weiAmount = finalizedProposalState.weiAmount
        val securityAmount = finalizedProposalState.securityAmount
        val proposerCordaName = finalizedProposalState.proposer.name.toString()
        val acceptorCordaName = finalizedProposalState.acceptor.name.toString()
        // val weiValue = Convert.toWei(etherAmount, Convert.Unit.WEI)

        // load Smart Contract Wrapper
        val settlement: Settlement = Settlement.load(TARGET_CONTRACT_ADDRESS, web3, credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000)))
        val response = settlement.lock(
             swapId,
             transferFromAddress,
             transferToAddress,
             weiAmount,
             securityAmount,
             proposerCordaName,
             acceptorCordaName,
             weiAmount
        ).send()

        return response.transactionHash
    }
}

