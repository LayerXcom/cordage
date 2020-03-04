package jp.co.layerx.cordage.flowethereumeventwatch

import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage
import org.assertj.core.api.Assertions
import org.assertj.core.api.BigIntegerAssert
import org.junit.Test
import org.web3j.abi.DefaultFunctionReturnDecoder
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
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
        val result = ethLogs.result
        if (result.size != 0) {
            val sendEventLog =  ethLogs.result[0].get() as Log
            val logResult = DefaultFunctionReturnDecoder.decode(sendEventLog.data, event.nonIndexedParameters)
            if (logResult.size != 0) {
                val value = logResult[0].value as BigInteger
                BigIntegerAssert(value)
            }
        }
        BigIntegerAssert(BigInteger.ONE)
    }

    @Test
    fun `send ethereum tx test`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
        val simpleStorage: SimpleStorage = SimpleStorage.load("0xCfEB869F69431e42cdB54A4F4f105C19C080A601", web3, credentials, StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(500000)))
        val result = simpleStorage.set(3.toBigInteger()).send()
        val returnValue = result.transactionHash

        Assertions.assertThat(returnValue).startsWith("0x")
    }
}