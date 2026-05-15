package com.streamvault.data.remote.stalker

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class OkHttpStalkerApiServiceTest {

    @Test
    fun authenticate_reads_token_and_profile_from_js_wrapper() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "handshake" to """{"js":{"token":"token-123"}}""",
                "get_profile" to """{"js":{"name":"Living Room","status":"1","max_online":"2"}}"""
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.authenticate(
            buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.first.token).isEqualTo("token-123")
        assertThat(success.data.second.accountName).isEqualTo("Living Room")
        assertThat(success.data.second.maxConnections).isEqualTo(2)
    }

    @Test
    fun createLink_reads_cmd_from_js_wrapper() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "create_link" to """{"js":{"cmd":"ffmpeg http://cdn.example.com/live/stream.ts"}}"""
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.createLink(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            kind = StalkerStreamKind.LIVE,
            cmd = "ffmpeg http://placeholder"
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data).isEqualTo("http://cdn.example.com/live/stream.ts")
    }

    @Test
    fun createLink_uses_episode_number_as_series_selector_for_stalker_shell_episode() = runTest {
        var requestedSeries: String? = null
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    requestedSeries = request.url.queryParameter("series")
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(
                            """{"js":{"cmd":"ffmpeg http://cdn.example.com/series/episode11.mkv"}}"""
                                .toResponseBody("application/json".toMediaType())
                        )
                        .build()
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.createLink(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            kind = StalkerStreamKind.EPISODE,
            cmd = "eyJzZXJpZXNfaWQiOjUzOTk5LCJzZWFzb25fbnVtIjoxLCJ0eXBlIjoic2VyaWVzIn0=",
            seriesNumber = 11
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(requestedSeries).isEqualTo("11")
        val success = result as Result.Success
        assertThat(success.data).isEqualTo("http://cdn.example.com/series/episode11.mkv")
    }

    @Test
    fun authenticate_reads_json_from_callback_wrapper_and_control_char_noise() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "handshake" to "\u0000callback({\"js\":{\"token\":\"token-123\"}});",
                "get_profile" to "\u0000callback({\"js\":{\"name\":\"Living Room\",\"status\":\"1\"}});"
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.authenticate(
            buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.first.token).isEqualTo("token-123")
        assertThat(success.data.second.accountName).isEqualTo("Living Room")
    }

    @Test
    fun authenticate_retains_server_cookies_for_follow_up_playback_requests() = runTest {
        val observedCookies = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val action = request.url.queryParameter("action").orEmpty()
                    if (action == "create_link") {
                        observedCookies += request.header("Cookie").orEmpty()
                    }
                    val body = when (action) {
                        "handshake" -> """{"js":{"token":"token-123"}}"""
                        "get_profile" -> """{"js":{"name":"Living Room","status":"1"}}"""
                        "create_link" -> """{"js":{"cmd":"ffmpeg http://cdn.example.com/live/stream.ts"}}"""
                        else -> error("Unexpected action '$action'")
                    }
                    val responseBuilder = Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(body.toResponseBody("application/json".toMediaType()))
                    if (action == "handshake") {
                        responseBuilder.addHeader("Set-Cookie", "PHPSESSID=session-42; Path=/; HttpOnly")
                    }
                    responseBuilder.build()
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val authResult = service.authenticate(
            buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        ) as Result.Success

        val createLinkResult = service.createLink(
            session = authResult.data.first,
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            kind = StalkerStreamKind.LIVE,
            cmd = "ffmpeg http://localhost/ch/1234_"
        )

        assertThat(createLinkResult).isInstanceOf(Result.Success::class.java)
        assertThat(authResult.data.first.serverCookieHeader).contains("PHPSESSID=session-42")
        assertThat(observedCookies.single()).contains("PHPSESSID=session-42")
    }

    @Test
    fun authenticate_reports_access_denied_html_clearly() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "handshake" to """<!DOCTYPE html><html><body>Access Denied.</body></html>"""
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.authenticate(
            buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        )

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        assertThat(error.message).isEqualTo("Portal denied the request for handshake.")
    }

    @Test
    fun getLiveCategories_retries_alternate_endpoint_for_authenticated_requests() = runTest {
        val requestedUrls = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    requestedUrls += request.url.toString()
                    val body = when (request.url.encodedPath) {
                        "/server/load.php" -> if (request.url.queryParameter("action") == "get_genres") {
                            throw java.io.IOException("\\n not found: limit=1 content=0d…")
                        } else {
                            """{"js":{"token":"token-123"}}"""
                        }
                        "/portal.php" -> """{"js":[{"id":"10","title":"News"}]}"""
                        else -> error("Unexpected path ${request.url.encodedPath}")
                    }
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(body.toResponseBody("application/json".toMediaType()))
                        .build()
                })
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getLiveCategories(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.map { it.name }).containsExactly("News")
        assertThat(requestedUrls).containsAtLeast(
            "https://portal.example.com/server/load.php?type=itv&action=get_genres&JsHttpRequest=1-xml",
            "https://portal.example.com/portal.php?type=itv&action=get_genres&JsHttpRequest=1-xml"
        )
    }

    @Test
    fun getLiveStreams_prefers_get_all_channels_for_bulk_live_loads() = runTest {
        val requestedActions = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val action = request.url.queryParameter("action").orEmpty()
                    requestedActions += action
                    val body = when (action) {
                        "get_all_channels" -> """
                            {"js":{"data":[{"id":"100","name":"News","tv_genre_id":"10","cmd":"ffmpeg http://example.com/live.ts"}]}}
                        """.trimIndent()
                        else -> error("Unexpected action '$action'")
                    }
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(body.toResponseBody("application/json".toMediaType()))
                        .build()
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getLiveStreams(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            categoryId = null
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.map { it.name }).containsExactly("News")
        assertThat(requestedActions).containsExactly("get_all_channels")
    }

    @Test
    fun getLiveStreams_preserves_command_variants_and_temp_link_flags() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "get_all_channels" to """
                    {"js":{"data":[
                        {
                            "id":"100",
                            "name":"News",
                            "tv_genre_id":"10",
                            "cmd":"ffmpeg http://localhost/ch/100_",
                            "cmd_1":"ffmpeg http://backup.example.com/play/live.php?stream=100",
                            "cmd_2":"ffmpeg http://edge.example.com/live/news.m3u8",
                            "mc_cmd":"ffmpeg http://mc.example.com/live/100.ts",
                            "cmds":[{"url":"ffmpeg http://multi.example.com/live/100.ts"}],
                            "use_http_tmp_link":"1",
                            "nginx_secure_link":"1",
                            "allow_local_timeshift":"1",
                            "archive":"1"
                        }
                    ]}}
                """.trimIndent()
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getLiveStreams(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            categoryId = null
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val item = (result as Result.Success).data.single()
        assertThat(item.commandVariants.map { it.sourceKey })
            .containsAtLeast("cmd", "cmd_1", "cmd_2", "mc_cmd", "cmds[0]")
        assertThat(item.commandVariants.map { it.cmd })
            .contains("ffmpeg http://edge.example.com/live/news.m3u8")
        assertThat(item.playbackDescriptor?.primaryMode).isEqualTo(StalkerPlaybackMode.MULTI_CMD)
        assertThat(item.portalCapabilities.useHttpTemporaryLink).isTrue()
        assertThat(item.portalCapabilities.nginxSecureLink).isTrue()
        assertThat(item.portalCapabilities.allowLocalTimeshift).isTrue()
        assertThat(item.portalCapabilities.archiveAvailable).isTrue()
    }

    @Test
    fun streamLiveStreams_emits_bulk_channels_from_js_data_without_list_materialization() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "get_all_channels" to """
                    {"js":{"data":[
                        {"id":"100","name":"News","tv_genre_id":"10","cmd":"ffmpeg http://example.com/news.ts"},
                        {"id":"101","name":"Sports","tv_genre_id":"11","cmd":"ffmpeg http://example.com/sports.ts"}
                    ]}}
                """.trimIndent()
            ),
            json = Json { ignoreUnknownKeys = true }
        )
        val streamed = mutableListOf<StalkerItemRecord>()

        val result = service.streamLiveStreams(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            )
        ) { item ->
            streamed += item
        }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data).isEqualTo(2)
        assertThat(streamed.map { it.name }).containsExactly("News", "Sports").inOrder()
        assertThat(streamed.map { it.categoryId }).containsExactly("10", "11").inOrder()
    }

    @Test
    fun getLiveStreams_falls_back_to_paged_get_ordered_list_when_all_channels_is_unavailable() = runTest {
        val requestedUrls = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    requestedUrls += request.url.toString()
                    val action = request.url.queryParameter("action").orEmpty()
                    val response = when (action) {
                        "get_all_channels" -> Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body("".toResponseBody("application/json".toMediaType()))
                            .build()
                        "get_ordered_list" -> Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(
                                """
                                    {"js":{"total_items":"1","max_page_items":"50","data":[{"id":"100","name":"News","tv_genre_id":"10","cmd":"ffmpeg http://example.com/live.ts"}]}}
                                """.trimIndent().toResponseBody("application/json".toMediaType())
                            )
                            .build()
                        else -> error("Unexpected action '$action'")
                    }
                    response
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getLiveStreams(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            categoryId = null
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.map { it.name }).containsExactly("News")
        assertThat(requestedUrls).containsAtLeast(
            "https://portal.example.com/server/load.php?type=itv&action=get_all_channels&JsHttpRequest=1-xml",
            "https://portal.example.com/server/load.php?type=itv&action=get_ordered_list&JsHttpRequest=1-xml&force_ch_link_check=0&fav=0&p=1"
        )
    }

    @Test
    fun getSeriesPage_requests_only_requested_page_and_reports_total_pages() = runTest {
        val requestedUrls = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    requestedUrls += request.url.toString()
                    val page = request.url.queryParameter("p")
                    check(page == "3") { "Unexpected page '$page'" }
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(
                            """
                                {"js":{"total_items":"45","max_page_items":"15","data":[{"id":"300","name":"Drama","category_id":"147"}]}}
                            """.trimIndent().toResponseBody("application/json".toMediaType())
                        )
                        .build()
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getSeriesPage(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            categoryId = "147",
            page = 3
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.items.map { it.name }).containsExactly("Drama")
        assertThat(success.data.page).isEqualTo(3)
        assertThat(success.data.totalPages).isEqualTo(3)
        assertThat(success.data.isComplete).isTrue()
        assertThat(requestedUrls).containsExactly(
            "https://portal.example.com/server/load.php?type=series&action=get_ordered_list&JsHttpRequest=1-xml&category=147&p=3"
        )
    }

    @Test
    fun getBulkEpg_parses_channel_ids_from_bulk_response_rows() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "get_epg_info" to """
                    {"js":[
                        {"id":"p1","ch_id":"100","name":"Morning News","descr":"Top stories","start_timestamp":"1700000000","stop_timestamp":"1700003600"},
                        {"id":"p2","channel_id":"sports-guide-id","name":"Live Sports","descr":"Match coverage","start_timestamp":"1700003600","stop_timestamp":"1700007200"}
                    ]}
                """.trimIndent()
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getBulkEpg(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            periodHours = 6
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.map { it.channelId }).containsExactly("100", "sports-guide-id")
        assertThat(success.data.map { it.title }).containsExactly("Morning News", "Live Sports")
    }

    @Test
    fun getSeriesDetails_expands_season_shell_rows_into_episode_placeholders() = runTest {
        val service = OkHttpStalkerApiService(
            okHttpClient = fakeClient(
                "get_ordered_list" to """
                    {"js":{"total_items":1,"max_page_items":14,"data":[{"id":"55000:1","name":"Season 1","description":"Doc","series":[1,2,3,4],"cmd":"eyJzZXJpZXNfaWQiOjU1MDAwLCJzZWFzb25fbnVtIjoxLCJ0eXBlIjoic2VyaWVzIn0=","screenshot_uri":"https://img.example.com/season1.jpg"}]}}
                """.trimIndent()
            ),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getSeriesDetails(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            seriesId = "55000:55000"
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.series.name).isEmpty()
        assertThat(success.data.seasons).hasSize(1)
        val season = success.data.seasons.single()
        assertThat(season.seasonNumber).isEqualTo(1)
        assertThat(season.episodes.map { it.episodeNumber }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(season.episodes.first().cmd).isEqualTo("eyJzZXJpZXNfaWQiOjU1MDAwLCJzZWFzb25fbnVtIjoxLCJ0eXBlIjoic2VyaWVzIn0=")
    }

    @Test
    fun getSeriesDetails_fetches_shell_season_page_for_explicit_episode_cmds() = runTest {
        val requestedSeasonIds = mutableListOf<String>()
        val service = OkHttpStalkerApiService(
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val action = request.url.queryParameter("action").orEmpty()
                    val seasonId = request.url.queryParameter("season_id").orEmpty()
                    val body = when {
                        action != "get_ordered_list" -> error("Unexpected action '$action'")
                        seasonId == "0" -> """
                            {"js":{"total_items":1,"max_page_items":14,"data":[{"id":"55000:1","name":"Season 1","description":"Doc","series":[1,2,3,4],"cmd":"eyJzZXJpZXNfaWQiOjU1MDAwLCJzZWFzb25fbnVtIjoxLCJ0eXBlIjoic2VyaWVzIn0=","screenshot_uri":"https://img.example.com/season1.jpg"}]}}
                        """.trimIndent()
                        seasonId == "1" -> {
                            requestedSeasonIds += seasonId
                            """
                                {"js":{"total_items":1,"max_page_items":14,"data":[{"id":"episode-1","name":"Episode 1","series_number":"1","season_id":"1","cmd":"ffmpeg http://example.com/episode1.mp4","screenshot_uri":"https://img.example.com/episode1.jpg"}]}}
                            """.trimIndent()
                        }
                        else -> error("Unexpected season_id '$seasonId'")
                    }
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(body.toResponseBody("application/json".toMediaType()))
                        .build()
                }
                .build(),
            json = Json { ignoreUnknownKeys = true }
        )

        val result = service.getSeriesDetails(
            session = StalkerSession(
                loadUrl = "https://portal.example.com/server/load.php",
                portalReferer = "https://portal.example.com/c/",
                token = "token-123"
            ),
            profile = buildStalkerDeviceProfile(
                portalUrl = "https://portal.example.com/c",
                macAddress = "00:1A:79:12:34:56",
                deviceProfile = "MAG250",
                timezone = "UTC",
                locale = "en"
            ),
            seriesId = "55000:55000"
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(requestedSeasonIds).containsExactly("1")
        assertThat(success.data.seasons).hasSize(1)
        val season = success.data.seasons.single()
        assertThat(season.episodes).hasSize(1)
        assertThat(season.episodes.single().cmd).isEqualTo("ffmpeg http://example.com/episode1.mp4")
    }

    private fun fakeClient(vararg responses: Pair<String, String>): OkHttpClient {
        val byAction = responses.toMap()
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val action = request.url.queryParameter("action").orEmpty()
                val body = byAction[action] ?: error("Missing fake response for action '$action'")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
    }
}
