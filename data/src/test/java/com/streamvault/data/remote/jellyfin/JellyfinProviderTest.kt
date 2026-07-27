package com.streamvault.data.remote.jellyfin

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.streamvault.domain.model.Provider
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.model.Result
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Test

class JellyfinProviderTest {

    @Test
    fun `movie pages retain continuation metadata`() = runTest {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            val start = request.url.queryParameter("StartIndex")
            val body = when (start) {
                "0" -> """{"TotalRecordCount":2,"Items":[{"Id":"movie-1","Name":"One"}]}"""
                "1" -> """{"TotalRecordCount":2,"Items":[{"Id":"movie-2","Name":"Two"}]}"""
                else -> error("Unexpected continuation: $start")
            }
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
        val provider = JellyfinProvider(client, Gson())
        val account = Provider(name = "Jellyfin", type = ProviderType.JELLYFIN, serverUrl = "https://demo.example", username = "alice", password = "token")

        val first = provider.fetchMoviesPage(account, 0) as Result.Success
        assertThat(first.data.totalRecordCount).isEqualTo(2)
        assertThat(first.data.nextStartIndex).isEqualTo(1)
        val second = provider.fetchMoviesPage(account, first.data.nextStartIndex) as Result.Success
        assertThat(second.data.items.single().name).isEqualTo("Two")
    }

    @Test
    fun `movie page rejects server that ignores requested limit`() = runTest {
        val items = (1..101).joinToString(",") { "{\"Id\":\"movie-$it\"}" }
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body("{\"TotalRecordCount\":101,\"Items\":[$items]}".toResponseBody("application/json".toMediaType())).build()
        }.build()
        val provider = JellyfinProvider(client, Gson())
        val result = provider.fetchMoviesPage(Provider(name = "Jellyfin", type = ProviderType.JELLYFIN, serverUrl = "https://demo.example", username = "alice", password = "token"), 0)
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isInstanceOf(JellyfinPaginationException::class.java)
    }

    @Test
    fun `fetchMovies does not embed access token in artwork urls`() = runTest {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                assertThat(request.url.queryParameter("StartIndex")).isEqualTo("0")
                assertThat(request.url.queryParameter("Limit")).isEqualTo("100")
                val body = when (request.url.encodedPath) {
                    "/Items" -> {
                        """
                        {
                          "TotalRecordCount": 1,
                          "Items": [
                            {
                              "Id": "movie-1",
                              "Name": "Movie 1",
                              "ImageTags": { "Primary": "poster-tag" },
                              "BackdropImageTags": ["backdrop-tag"]
                            }
                          ]
                        }
                        """.trimIndent()
                    }
                    else -> error("Unexpected request path: ${request.url.encodedPath}")
                }

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val provider = JellyfinProvider(
            okHttpClient = client,
            gson = Gson()
        )

        val result = provider.fetchMoviesPage(
            Provider(
                name = "Jellyfin",
                type = ProviderType.JELLYFIN,
                serverUrl = "https://demo.example",
                username = "alice",
                password = "secret-token"
            ),
            startIndex = 0
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val movie = (result as Result.Success).data.items.single()
        assertThat(movie.posterUrl).isEqualTo("https://demo.example/Items/movie-1/Images/Primary?tag=poster-tag&streamvault_provider_id=0")
        assertThat(movie.backdropUrl).isEqualTo("https://demo.example/Items/movie-1/Images/Backdrop/0?tag=backdrop-tag&streamvault_provider_id=0")
        assertThat(movie.posterUrl).doesNotContain("api_key")
        assertThat(movie.backdropUrl).doesNotContain("api_key")
    }
}
