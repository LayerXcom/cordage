package jp.co.layerx.cordage.flowethereumeventwatch

import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.http.HttpService


class SampleTest {
    @Test
    fun `get ethereum event test`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

//        val fromBlockNumber = 1.toBigInteger()
//        val toBlockNumber = 50.toBigInteger()
        val targetContractAddress = SimpleStorage.getPreviouslyDeployedAddress("5777")
        val event = SimpleStorage.SET_EVENT

//        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
//                DefaultBlockParameter.valueOf(toBlockNumber),
//                targetContractAddress)
        val filter = EthFilter(DefaultBlockParameter.valueOf("earliest"),
                DefaultBlockParameter.valueOf("latest"),
                targetContractAddress?.substring(2))
//        filter.addSingleTopic(EventEncoder.encode(event))
//        filter.addOptionalTopics("0x" + TypeEncoder.encode(new Address("0x00a329c0648769a73afac7f9381e08fb43dbea72")))

        val ethLogs = web3.ethGetLogs(filter).send()
        val sendEventLog = ethLogs.result


//        web3.ethLogObservable(filter).subscribe(object : Action1<Log?> {
//            fun call(log: Log) {
//                System.out.println("log.toString(): " + log.toString())
//            }
//        })

//        val subscription = web3.ethLogFlowable(filter).subscribe { log ->
//            val test = log.data as Nothing?
//        }
//        subscription.dispose()
        assertThat(2).isEqualTo(1)
    }
}