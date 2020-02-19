package jp.co.layerx.cordage.flowethereum

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import okhttp3.OkHttpClient
import okhttp3.Request

const val ETHEREUM_URL = "https://raw.githubusercontent.com/bitcoin/bitcoin/4405b78d6059e536c36974088a8ed4d9f0f29898/readme.txt"

@InitiatingFlow
@StartableByRPC
class Flow: FlowLogic<String>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val httpRequest = Request.Builder().url(ETHEREUM_URL).build()
        val httpResponse = OkHttpClient().newCall(httpRequest).execute()

        return httpResponse.body().string()
    }
}
