package jp.co.layerx.cordage.flowethereumeventwatch.ethWrapper

import io.reactivex.Flowable
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.BaseEventResponse
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.*

/**
 *
 * Auto generated code.
 *
 * **Do not modify!**
 *
 * Please use the [web3j command line tools](https://docs.web3j.io/command_line.html),
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * [codegen module](https://github.com/web3j/web3j/tree/master/codegen) to update.
 *
 *
 * Generated with web3j version 4.5.15.
 */
class SimpleStorage : Contract {
    companion object {
        const val BINARY = "0x608060405234801561001057600080fd5b506040516101843803806101848339818101604052602081101561003357600080fd5b810190808051906020019092919050505080600081905550506101298061005b6000396000f3fe6080604052348015600f57600080fd5b5060043610603c5760003560e01c806360fe47b11460415780636d4ce63c14606c57806373d4a13a146088575b600080fd5b606a60048036036020811015605557600080fd5b810190808035906020019092919050505060a4565b005b607260e5565b6040518082815260200191505060405180910390f35b608e60ee565b6040518082815260200191505060405180910390f35b7fdf7a95aebff315db1b7716215d602ab537373cdb769232aae6055c06e798425b816040518082815260200191505060405180910390a18060008190555050565b60008054905090565b6000548156fea265627a7a723158209e6b8d809f306a6fac8d6bf44d718ab5160f80edc898263785b6b46e9e4d28c064736f6c63430005100032"
        const val FUNC_DATA = "data"
        const val FUNC_SET = "set"
        const val FUNC_GET = "get"
        val SET_EVENT = Event("Set",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {}))
        protected var _addresses: HashMap<String, String>? = null
        @Deprecated("")
        fun load(contractAddress: String?, web3j: Web3j?, credentials: Credentials?, gasPrice: BigInteger?, gasLimit: BigInteger?): SimpleStorage {
            return SimpleStorage(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        @Deprecated("")
        fun load(contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?, gasPrice: BigInteger?, gasLimit: BigInteger?): SimpleStorage {
            return SimpleStorage(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }

        fun load(contractAddress: String?, web3j: Web3j?, credentials: Credentials?, contractGasProvider: ContractGasProvider?): SimpleStorage {
            return SimpleStorage(contractAddress, web3j, credentials, contractGasProvider)
        }

        fun load(contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?, contractGasProvider: ContractGasProvider?): SimpleStorage {
            return SimpleStorage(contractAddress, web3j, transactionManager, contractGasProvider)
        }

        fun deploy(web3j: Web3j?, credentials: Credentials?, contractGasProvider: ContractGasProvider?, initVal: BigInteger?): RemoteCall<SimpleStorage> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList<Type<*>>(Uint256(initVal)))
            return deployRemoteCall(SimpleStorage::class.java, web3j, credentials, contractGasProvider, BINARY, encodedConstructor)
        }

        fun deploy(web3j: Web3j?, transactionManager: TransactionManager?, contractGasProvider: ContractGasProvider?, initVal: BigInteger?): RemoteCall<SimpleStorage> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList<Type<*>>(Uint256(initVal)))
            return deployRemoteCall(SimpleStorage::class.java, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor)
        }

        @Deprecated("")
        fun deploy(web3j: Web3j?, credentials: Credentials?, gasPrice: BigInteger?, gasLimit: BigInteger?, initVal: BigInteger?): RemoteCall<SimpleStorage> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList<Type<*>>(Uint256(initVal)))
            return deployRemoteCall(SimpleStorage::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        @Deprecated("")
        fun deploy(web3j: Web3j?, transactionManager: TransactionManager?, gasPrice: BigInteger?, gasLimit: BigInteger?, initVal: BigInteger?): RemoteCall<SimpleStorage> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList<Type<*>>(Uint256(initVal)))
            return deployRemoteCall(SimpleStorage::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun getPreviouslyDeployedAddress(networkId: String?): String? {
            return _addresses!![networkId]
        }

        init {
            _addresses = HashMap()
            _addresses!!["15777"] = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
            _addresses!!["5777"] = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
            _addresses!!["1582785820509"] = "0xCfEB869F69431e42cdB54A4F4f105C19C080A601"
        }
    }

    @Deprecated("")
    protected constructor(contractAddress: String?, web3j: Web3j?, credentials: Credentials?, gasPrice: BigInteger?, gasLimit: BigInteger?) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {
    }

    protected constructor(contractAddress: String?, web3j: Web3j?, credentials: Credentials?, contractGasProvider: ContractGasProvider?) : super(BINARY, contractAddress, web3j, credentials, contractGasProvider) {}
    @Deprecated("")
    protected constructor(contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?, gasPrice: BigInteger?, gasLimit: BigInteger?) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {
    }

    protected constructor(contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?, contractGasProvider: ContractGasProvider?) : super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider) {}

    fun getSetEvents(transactionReceipt: TransactionReceipt?): List<SetEventResponse> {
        val valueList = extractEventParametersWithLog(SET_EVENT, transactionReceipt)
        val responses = ArrayList<SetEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = SetEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.x = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun setEventFlowable(filter: EthFilter?): Flowable<SetEventResponse> {
        return web3j.ethLogFlowable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(SET_EVENT, log)
            val typedResponse = SetEventResponse()
            typedResponse.log = log
            typedResponse.x = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse
        }
    }

    fun setEventFlowable(startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?): Flowable<SetEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(SET_EVENT))
        return setEventFlowable(filter)
    }

    fun data(): RemoteFunctionCall<BigInteger> {
        val function = org.web3j.abi.datatypes.Function(FUNC_DATA,
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {}))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun set(x: BigInteger?): RemoteFunctionCall<TransactionReceipt> {
        val function = org.web3j.abi.datatypes.Function(
                FUNC_SET,
                Arrays.asList<Type<*>>(Uint256(x)), emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun get(): RemoteFunctionCall<BigInteger> {
        val function = org.web3j.abi.datatypes.Function(FUNC_GET,
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {}))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    override fun getStaticDeployedAddress(networkId: String): String? {
        return _addresses!![networkId]
    }

    class SetEventResponse : BaseEventResponse() {
        var x: BigInteger? = null
    }
}