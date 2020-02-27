package jp.co.layerx.cordage.flowethereumeventwatch

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.http.HttpService
import jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper.SimpleStorage

class SampleTest {
    @Test
    fun `get ethereum event test`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val fromBlockNumber = 1.toBigInteger()
        val toBlockNumber = 50.toBigInteger()
        val targetContractAddress = SimpleStorage.getPreviouslyDeployedAddress("15777")
        val event = SimpleStorage.SET_EVENT.name

//        val json = File("./SimpleStorage.json")
//        val mapper = jacksonObjectMapper()
//        val truffleArtifacts = mapper.readValue<TruffleArtifacts>(json)

        val filter = EthFilter(DefaultBlockParameter.valueOf(fromBlockNumber),
                DefaultBlockParameter.valueOf(toBlockNumber),
                targetContractAddress).addSingleTopic(event)
//        filter.addSingleTopic(EventEncoder.encode(SETTLED_EVENT));
//        filter.addOptionalTopics("0x" + TypeEncoder.encode(new Address("0x00a329c0648769a73afac7f9381e08fb43dbea72")));

        val subscription = web3.ethLogFlowable(filter).subscribe { log ->
            print(log.data)
        }
        subscription.dispose()

        assertThat(1).isEqualTo(1)
    }
}