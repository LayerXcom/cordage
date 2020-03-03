package jp.co.layerx.cordage.flowethereumeventwatch

import org.assertj.core.api.*
import org.junit.Test
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import java.math.BigInteger


class SampleTest {
    @Test
    fun `get ethereum event test`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val targetContractAddress = SimpleStorage.getPreviouslyDeployedAddress("5777")
        val event = SimpleStorage.SET_EVENT

        val filter = EthFilter(DefaultBlockParameter.valueOf("earliest"),
                DefaultBlockParameter.valueOf("latest"),
                targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()
//        ethLogs.result.map { (it.get() as Log).data }
//                .map { FunctionReturnDecoder.decode(it, event.nonIndexedParameters) }
        val sendEventLog =  ethLogs.result[0].get() as Log
        val logResult = FunctionReturnDecoder.decode(sendEventLog.data, event.nonIndexedParameters)
        val value = logResult[0].value as BigInteger

        BigIntegerAssert(value)
    }
}