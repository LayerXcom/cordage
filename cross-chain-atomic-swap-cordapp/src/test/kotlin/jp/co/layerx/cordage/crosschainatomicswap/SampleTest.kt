package jp.co.layerx.cordage.crosschainatomicswap

import jp.co.layerx.cordage.crosschainatomicswap.ethWrapper.Settlement
import org.assertj.core.api.Assertions
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
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.util.*

class SampleTest {
    val swapDetailType = Arrays.asList<TypeReference<*>?>(
    object : TypeReference<Address?>() {},
    object : TypeReference<Address?>() {},
    object : TypeReference<Uint256?>() {},
    object : TypeReference<Uint256?>() {},
    object : TypeReference<Uint8?>() {}
    )

    /*
    Sample solidity code
======================================================
pragma solidity >=0.5.17 <0.7.0;

contract StringBytesEmitter {
    event StringBytes(bytes b);

    function emitStringBytes(string memory s) public {
        bytes memory b = abi.encode(s);
        emit StringBytes(b);
    }
}
======================================================
     */
    @Test
    fun `bytes to string`() {
        val hex = "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000001c4f3d5061727469636970616e74412c4c3d4c6f6e646f6e2c433d474200000000"
        val byteArray = hex.toUpperCase().hexStringToByteArray()
        val actual = byteArray.toString(Charsets.UTF_8)
        Assertions.assertThat(actual == "O=ParticipantA,L=London,C=GB")
    }

    @Test
    fun `send lock tx`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val targetContractAddress = Settlement.getPreviouslyDeployedAddress("5777")
        val credentials: Credentials = Credentials.create("0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1")

        val swapId = "1"
        val transferFromAddress = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0"
        val transferToAddress = "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b"
        val weiAmount = 1000000.toBigInteger()
        val securityAmount = 10.toBigInteger()

        // load Smart Contract Wrapper
        val settlement: Settlement = Settlement.load(targetContractAddress, web3, credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000)))
        val response = settlement.lock(
            swapId,
            transferFromAddress,
            transferToAddress,
            weiAmount,
            securityAmount,
            weiAmount
        ).send()

        Assertions.assertThat(response.transactionHash).startsWith("0x")
    }

    fun `get ethereum event`() {
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))

        val targetContractAddress = Settlement.getPreviouslyDeployedAddress("5777")
        val credentials: Credentials = Credentials.create("0x6370fd033278c143179d81c5526140625662b8daa446c22ee2d73db3707e620c")

        val event = Settlement.LOCKED_EVENT
        val searchId = "1"

        val filter = EthFilter(DefaultBlockParameter.valueOf("earliest"),
                DefaultBlockParameter.valueOf("latest"),
                targetContractAddress)

        val settlement: Settlement = Settlement.load(targetContractAddress, web3, credentials,
            StaticGasProvider(BigInteger.valueOf(1), BigInteger.valueOf(50000000)))

        val ethLogs = web3.ethGetLogs(filter).send()
        val decodedLogs = ethLogs.result?.map { (it.get() as Log).data }
            ?.map { DefaultFunctionReturnDecoder.decode(it, event?.nonIndexedParameters) }
        if (decodedLogs != null && decodedLogs.isNotEmpty()) {
            decodedLogs.forEach { abiTypes ->
                // find event values by searchId
                val eventValues = abiTypes?.map { it.value }
                if (eventValues != null && eventValues.isNotEmpty()) {
                    if (eventValues.get(1).equals(searchId)) {
                        val encodedSwapDetail = eventValues.get(2) as ByteArray
                        val stringEncodedSwapDetail = "0x" + encodedSwapDetail.toHex()
                        val decodedSwapDetail = DefaultFunctionReturnDecoder.decode(stringEncodedSwapDetail, swapDetailType as MutableList<TypeReference<Type<Any>>>?)

                        val fromAddress = decodedSwapDetail[1].value as String
                        Assertions.assertThat(fromAddress).startsWith("0x")
                    }
            }
            }
        }
    }
}

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex() : String {
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }
    return result.toString()
}

fun String.hexStringToByteArray() : ByteArray {
    val hexChars = "0123456789ABCDEF"
    val result = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        val firstIndex = hexChars.indexOf(this[i]);
        val secondIndex = hexChars.indexOf(this[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }
    return result
}
