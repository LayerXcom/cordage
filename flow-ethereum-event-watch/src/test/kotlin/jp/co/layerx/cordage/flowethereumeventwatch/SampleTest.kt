package jp.co.layerx.cordage.flowethereumeventwatch

import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService


class SampleTest {
    @Test
    fun `get ethereum event test`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

//        val fromBlockNumber = 1.toBigInteger()
//        val toBlockNumber = 50.toBigInteger()
//        val targetContractAddress = SimpleStorage.getPreviouslyDeployedAddress("5777")
        val targetContractAddress = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
        val event = SimpleStorage.SET_EVENT

//        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
//                DefaultBlockParameter.valueOf(toBlockNumber),
//                targetContractAddress)
        val filter = EthFilter(DefaultBlockParameter.valueOf("earliest"),
                DefaultBlockParameter.valueOf("latest"),
                targetContractAddress)

        val ethLogs = web3.ethGetLogs(filter).send()
        ethLogs.result.map { (it.get() as Log).data }
                .map { FunctionReturnDecoder.decode(it, event.nonIndexedParameters) }
        val sendEventLog =  ethLogs.result[0].get() as Log
        val logResult = FunctionReturnDecoder.decode(sendEventLog.data, event.nonIndexedParameters)

        assertThat(2).isEqualTo(1)
    }
}