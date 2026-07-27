package com.streamvault.data.remote.stalker

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Result
import com.streamvault.domain.model.StalkerAuthMode
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.system.measureTimeMillis
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class OkHttpStalkerCancellationTest {

    @Test
    fun `cancelling handshake prevents later recipe requests`(): Unit = runBlocking {
        val started = CountDownLatch(1)
        val actions = CopyOnWriteArrayList<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            actions += chain.request().url.queryParameter("action").orEmpty()
            started.countDown()
            while (!chain.call().isCanceled()) Thread.sleep(5)
            throw IOException("cancelled")
        }.build()
        val service = service(client, StalkerDiscoveryBudget(10_000, 24))
        val job = launch(Dispatchers.IO) {
            service.authenticate(profile())
        }

        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue()
        job.cancelAndJoin()

        assertThat(actions).containsExactly("handshake")
    }

    @Test
    fun `request budget prevents an additional outbound request`(): Unit = runBlocking {
        val actions = CopyOnWriteArrayList<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val action = chain.request().url.queryParameter("action").orEmpty()
            actions += action
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"js":{"token":"token-123"}}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()

        val result = service(client, StalkerDiscoveryBudget(10_000, 1)).authenticate(profile())

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat(actions).containsExactly("handshake")
    }

    @Test
    fun `overall discovery deadline cancels the active request`(): Unit = runBlocking {
        val cancelled = CountDownLatch(1)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            while (!chain.call().isCanceled()) Thread.sleep(5)
            cancelled.countDown()
            throw IOException("cancelled")
        }.build()
        lateinit var result: Result<Pair<StalkerSession, StalkerProviderProfile>>

        val elapsedMillis = measureTimeMillis {
            result = service(client, StalkerDiscoveryBudget(100, 24)).authenticate(profile())
        }

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).message).contains("exceeded")
        assertThat(cancelled.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(elapsedMillis).isLessThan(5_000L)
    }

    private fun service(client: OkHttpClient, budget: StalkerDiscoveryBudget) =
        OkHttpStalkerApiService(client, Json { ignoreUnknownKeys = true }, budget)

    private fun profile() = buildStalkerDeviceProfile(
        portalUrl = "https://portal.example.com/c",
        macAddress = "00:1A:79:12:34:56",
        authMode = StalkerAuthMode.AUTO,
        deviceProfile = "MAG250",
        timezone = "UTC",
        locale = "en"
    )
}
