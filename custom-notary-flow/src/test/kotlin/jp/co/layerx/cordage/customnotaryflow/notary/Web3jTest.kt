package jp.co.layerx.cordage.customnotaryflow.notary

import jp.co.layerx.cordage.customnotaryflow.hexStringToByteArray
import net.corda.core.utilities.toHex
import org.assertj.core.api.Assertions
import org.junit.Test
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

class Web3jTest {
    @Test
    fun `send tx with input data`() {
        val data = "Terminate: RESIDENTIAL LEASE AGREEMENT"
        val ETHEREUM_RPC_URL = "http://localhost:8545"
        val web3 = Web3j.build(HttpService(ETHEREUM_RPC_URL))
        val tx = Transaction.createFunctionCallTransaction(
            "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1",
            null,
            BigInteger.valueOf(1),
            BigInteger.valueOf(50000),
            "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0",
            BigInteger.valueOf(1_000_000_000_000_000_000),
            data.toByteArray(Charsets.UTF_8).toHex()
        )

        val response = web3.ethSendTransaction(tx).send()
        Assertions.assertThat(response.transactionHash).startsWith("0x")
        Assertions.assertThat(data == tx.data.toUpperCase().hexStringToByteArray().toString(Charsets.UTF_8))
    }
}
