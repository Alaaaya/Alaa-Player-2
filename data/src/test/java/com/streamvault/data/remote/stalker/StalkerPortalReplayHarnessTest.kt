package com.streamvault.data.remote.stalker

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Result
import com.streamvault.domain.model.StalkerAuthMode
import com.streamvault.domain.model.StalkerPortalProfile
import java.io.InputStreamReader
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class StalkerPortalReplayHarnessTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun replayFixtures_cover_supported_portal_families() = runTest {
        listOf(
            "stalker/fixtures/mac_basic.json",
            "stalker/fixtures/auth_required.json",
            "stalker/fixtures/auth_plus_mag.json",
            "stalker/fixtures/module_gated.json",
            "stalker/fixtures/nginx_temp_link.json"
        ).forEach { path ->
            val fixture = loadFixture(path)
            val requestedActions = mutableListOf<String>()
            val service = OkHttpStalkerApiService(
                okHttpClient = fakeReplayClient(fixture, requestedActions),
                json = json
            )

            val profile = buildStalkerDeviceProfile(
                portalUrl = fixture.device.portalUrl,
                macAddress = fixture.device.macAddress,
                authMode = StalkerAuthMode.valueOf(fixture.device.authMode),
                username = fixture.device.username,
                password = fixture.device.password,
                deviceProfile = fixture.device.deviceProfile,
                timezone = fixture.device.timezone,
                locale = fixture.device.locale
            )
            val authResult = service.authenticate(profile)
            assertThat(authResult).isInstanceOf(Result.Success::class.java)
            val authSuccess = authResult as Result.Success
            assertThat(authSuccess.data.first.effectiveAuthMode.name).isEqualTo(fixture.expected.authMode)
            assertThat(authSuccess.data.first.portalProfile.name).isEqualTo(fixture.expected.portalProfile)
            assertThat(authSuccess.data.first.bootstrapEvidence).containsExactlyElementsIn(fixture.expected.bootstrapEvidence).inOrder()

            fixture.expected.playbackMode?.let { expectedPlaybackMode ->
                val liveResult = service.getLiveStreams(
                    session = authSuccess.data.first,
                    profile = profile,
                    categoryId = null
                )
                assertThat(liveResult).isInstanceOf(Result.Success::class.java)
                val firstItem = (liveResult as Result.Success).data.first()
                fixture.expected.resolvedPlaybackUrl?.let { expectedUrl ->
                    val createLinkResult = service.createLink(
                        session = authSuccess.data.first,
                        profile = profile,
                        kind = StalkerStreamKind.LIVE,
                        cmd = firstItem.cmd.orEmpty(),
                        seriesNumber = null
                    )
                    assertThat(createLinkResult).isInstanceOf(Result.Success::class.java)
                    val resolvedUrl = (createLinkResult as Result.Success).data
                    assertThat(resolvedUrl).isEqualTo(expectedUrl)
                    assertThat(detectStalkerPlaybackMode(resolvedUrl, firstItem.portalCapabilities).name)
                        .isEqualTo(expectedPlaybackMode)
                } ?: assertThat(firstItem.playbackDescriptor?.primaryMode?.name).isEqualTo(expectedPlaybackMode)
            }
            assertThat(requestedActions).containsAtLeastElementsIn(fixture.expected.requestOrder).inOrder()
        }
    }

    private fun fakeReplayClient(
        fixture: ReplayFixture,
        requestedActions: MutableList<String>
    ): OkHttpClient {
        val responsesByAction = fixture.responses.groupBy { "${it.method.uppercase()}:${it.action}" }
            .mapValues { (_, items) -> items.toMutableList() }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val action = request.url.queryParameter("action").orEmpty()
                val method = request.method.uppercase()
                requestedActions += action
                val key = "$method:$action"
                val scripted = responsesByAction[key]?.removeFirstOrNull()
                    ?: error("Missing replay response for $key in fixture ${fixture.name}")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(scripted.code)
                    .message("OK")
                    .body(scripted.body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
    }

    private fun loadFixture(path: String): ReplayFixture {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Missing replay fixture $path"
        }
        return stream.use { json.decodeFromString(ReplayFixture.serializer(), InputStreamReader(it).readText()) }
    }
}

@Serializable
private data class ReplayFixture(
    val name: String,
    val device: ReplayDevice,
    val responses: List<ReplayResponse>,
    val expected: ReplayExpectation
)

@Serializable
private data class ReplayDevice(
    val portalUrl: String,
    val macAddress: String,
    val authMode: String,
    val username: String = "",
    val password: String = "",
    val deviceProfile: String = "MAG250",
    val timezone: String = "UTC",
    val locale: String = "en"
)

@Serializable
private data class ReplayResponse(
    val action: String,
    val method: String = "GET",
    val code: Int = 200,
    val body: String
)

@Serializable
private data class ReplayExpectation(
    val authMode: String,
    val portalProfile: String,
    val bootstrapEvidence: List<String>,
    val requestOrder: List<String>,
    val playbackMode: String? = null,
    val resolvedPlaybackUrl: String? = null
)
