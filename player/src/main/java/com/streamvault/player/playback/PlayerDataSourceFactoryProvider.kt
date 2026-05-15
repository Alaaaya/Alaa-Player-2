package com.streamvault.player.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.streamvault.domain.model.VodHttpProtocolMode
import com.streamvault.domain.model.StreamInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Protocol

@UnstableApi
class PlayerDataSourceFactoryProvider(
    private val context: Context,
    private val baseClient: OkHttpClient
) {
    private data class ClientKey(
        val profile: PlayerTimeoutProfile,
        val forceHttp1: Boolean
    )

    private val clientsByKey = ConcurrentHashMap<ClientKey, OkHttpClient>()

    fun createFactory(
        streamInfo: StreamInfo,
        resolvedStreamType: ResolvedStreamType,
        vodHttpProtocolMode: VodHttpProtocolMode = VodHttpProtocolMode.COMPATIBILITY_HTTP1,
        preload: Boolean = false
    ): Pair<PlayerTimeoutProfile, DataSource.Factory> {
        val profile = PlayerTimeoutProfile.resolve(streamInfo, resolvedStreamType, preload)
        val headers = streamInfo.headers
        val forceHttp1 = PlayerHttpProtocolPolicy.forceHttp1(
            resolvedStreamType = resolvedStreamType,
            vodHttpProtocolMode = vodHttpProtocolMode
        )
        val client = clientsByKey.computeIfAbsent(ClientKey(profile, forceHttp1)) {
            baseClient.newBuilder()
                .connectTimeout(profile.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(profile.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(profile.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .apply {
                    if (forceHttp1) {
                        protocols(listOf(Protocol.HTTP_1_1))
                    }
                }
                .build()
        }
        if (forceHttp1) {
            Log.i(TAG, "data-source streamType=$resolvedStreamType timeout=$profile httpProtocol=HTTP_1_1")
        }
        val upstreamFactory = OkHttpDataSource.Factory(client).apply {
            streamInfo.userAgent?.takeIf { it.isNotBlank() }?.let(::setUserAgent)
            if (headers.isNotEmpty()) {
                setDefaultRequestProperties(headers)
            }
        }
        val factory = DefaultDataSource.Factory(context, upstreamFactory)
        return profile to factory
    }

    private companion object {
        const val TAG = "PlayerDataSourceFactory"
    }
}
