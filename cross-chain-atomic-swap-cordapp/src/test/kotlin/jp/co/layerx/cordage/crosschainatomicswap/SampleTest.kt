package jp.co.layerx.cordage.crosschainatomicswap

import org.assertj.core.api.Assertions
import org.junit.Test


class SampleTest {
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
