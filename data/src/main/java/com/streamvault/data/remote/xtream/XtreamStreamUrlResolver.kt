package com.streamvault.data.remote.xtream

import com.streamvault.data.provider.ProviderCapabilityResolver
import com.streamvault.data.remote.stalker.StalkerPlaybackResolutionException
import com.streamvault.data.remote.stalker.StalkerStreamKind
import com.streamvault.data.remote.stalker.StalkerUrlFactory
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackTransportPolicy
import com.streamvault.domain.model.Result
import com.streamvault.domain.provider.CapabilityResolution
import com.streamvault.domain.provider.PlaybackRequest
import com.streamvault.domain.provider.ProviderContentReference
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

data class ResolvedStreamUrl(
    val url: String,
    val expirationTime: Long? = null,
    val containerExtension: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val userAgent: String? = null,
    val playbackTransportPolicy: PlaybackTransportPolicy? = null,
    val allowInvalidSsl: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int? = null
)

/** Provider-neutral playback entry point. Provider selection is owned by the capability registry. */
@Singleton
class XtreamStreamUrlResolver @Inject constructor(
    private val providerCapabilities: ProviderCapabilityResolver
) {
    fun isInternalStreamUrl(url: String?): Boolean =
        XtreamUrlFactory.isInternalStreamUrl(url) || StalkerUrlFactory.isInternalStreamUrl(url)

    suspend fun resolve(
        url: String,
        fallbackProviderId: Long? = null,
        fallbackStreamId: Long? = null,
        fallbackContentType: ContentType? = null,
        fallbackContainerExtension: String? = null,
        preferStableUrl: Boolean = false
    ): String? = resolveWithMetadata(
        url = url,
        fallbackProviderId = fallbackProviderId,
        fallbackStreamId = fallbackStreamId,
        fallbackContentType = fallbackContentType,
        fallbackContainerExtension = fallbackContainerExtension,
        preferStableUrl = preferStableUrl
    )?.url

    suspend fun resolveWithMetadata(
        url: String,
        fallbackProviderId: Long? = null,
        fallbackStreamId: Long? = null,
        fallbackContentType: ContentType? = null,
        fallbackContainerExtension: String? = null,
        preferStableUrl: Boolean = false
    ): ResolvedStreamUrl? {
        val xtreamToken = XtreamUrlFactory.parseInternalStreamUrl(url)
        val stalkerToken = StalkerUrlFactory.parseInternalStreamUrl(url)
        val providerId = xtreamToken?.providerId
            ?: stalkerToken?.providerId
            ?: fallbackProviderId?.takeIf { it > 0L }
            ?: return url.takeIf(String::isNotBlank)?.toPassthrough(fallbackContainerExtension)
        val contentType = fallbackContentType
            ?: xtreamToken?.kind?.toContentType()
            ?: stalkerToken?.kind?.toContentType()
            ?: ContentType.LIVE
        val streamId = xtreamToken?.streamId
            ?: stalkerToken?.itemId
            ?: fallbackStreamId?.takeIf { it > 0L }

        val capabilitySet = when (val resolution = providerCapabilities.resolve(providerId)) {
            is CapabilityResolution.Available -> resolution.capability
            is CapabilityResolution.ConfigurationError -> return unavailable(url, resolution.reason, stalkerToken != null)
            is CapabilityResolution.Restricted -> return unavailable(url, resolution.reason, stalkerToken != null)
            is CapabilityResolution.Unsupported -> return unavailable(url, resolution.reason, stalkerToken != null)
        }
        val playback = when (val resolution = capabilitySet.playback()) {
            is CapabilityResolution.Available -> resolution.capability
            is CapabilityResolution.ConfigurationError -> return unavailable(url, resolution.reason, stalkerToken != null)
            is CapabilityResolution.Restricted -> return unavailable(url, resolution.reason, stalkerToken != null)
            is CapabilityResolution.Unsupported -> return unavailable(url, resolution.reason, stalkerToken != null)
        }
        return when (val result = playback.resolve(
            PlaybackRequest(
                sourceUrl = url,
                content = ProviderContentReference(
                    providerId = providerId,
                    streamId = streamId
                ),
                contentType = contentType,
                containerExtension = xtreamToken?.containerExtension
                    ?: stalkerToken?.containerExtension
                    ?: fallbackContainerExtension,
                preferStableUrl = preferStableUrl
            )
        )) {
            is Result.Success -> result.data.let { resolved ->
                ResolvedStreamUrl(
                    url = resolved.url,
                    expirationTime = resolved.expirationTime ?: extractStreamExpirationTime(resolved.url),
                    containerExtension = resolved.containerExtension,
                    headers = resolved.headers,
                    userAgent = resolved.userAgent,
                    playbackTransportPolicy = resolved.playbackTransportPolicy,
                    allowInvalidSsl = resolved.allowInvalidSsl,
                    proxyHost = resolved.proxyHost,
                    proxyPort = resolved.proxyPort
                )
            }
            is Result.Error -> {
                if (stalkerToken != null) {
                    throw StalkerPlaybackResolutionException(result.message, result.exception)
                }
                null
            }
            is Result.Loading -> null
        }
    }

    private fun unavailable(url: String, reason: String, stalker: Boolean): ResolvedStreamUrl? {
        if (stalker) throw StalkerPlaybackResolutionException(reason)
        return url.takeIf { it.isNotBlank() && !isInternalStreamUrl(it) }?.toPassthrough(null)
    }

    private fun String.toPassthrough(containerExtension: String?): ResolvedStreamUrl =
        ResolvedStreamUrl(
            url = this,
            expirationTime = extractStreamExpirationTime(this),
            containerExtension = containerExtension
        )
}

private fun XtreamStreamKind.toContentType(): ContentType = when (this) {
    XtreamStreamKind.LIVE -> ContentType.LIVE
    XtreamStreamKind.MOVIE -> ContentType.MOVIE
    XtreamStreamKind.SERIES -> ContentType.SERIES_EPISODE
}

private fun StalkerStreamKind.toContentType(): ContentType = when (this) {
    StalkerStreamKind.LIVE,
    StalkerStreamKind.ARCHIVE -> ContentType.LIVE
    StalkerStreamKind.MOVIE -> ContentType.MOVIE
    StalkerStreamKind.EPISODE -> ContentType.SERIES_EPISODE
}

internal fun extractStreamExpirationTime(url: String): Long? {
    val query = runCatching { URI(url).rawQuery }.getOrNull()
        ?: url.substringAfter('?', missingDelimiterValue = "").takeIf { it.isNotBlank() }
        ?: return null
    val expirationKeys = setOf(
        "expire", "expires", "expiry", "expiration", "expires_at", "exp",
        "token_exp", "token_expires", "token_expiry"
    )
    return query.split('&')
        .asSequence()
        .mapNotNull { part ->
            val key = part.substringBefore('=', missingDelimiterValue = "")
                .lowercase()
                .takeIf(String::isNotBlank)
                ?: return@mapNotNull null
            if (key !in expirationKeys) return@mapNotNull null
            parseXtreamExpirationDate(
                XtreamUrlCodec.decode(part.substringAfter('=', missingDelimiterValue = ""))
            )
        }
        .firstOrNull()
}
