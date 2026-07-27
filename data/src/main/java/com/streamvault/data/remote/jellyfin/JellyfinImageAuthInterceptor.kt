package com.streamvault.data.remote.jellyfin

import com.streamvault.data.local.dao.ProviderDao
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.security.CredentialCrypto
import com.streamvault.domain.model.ProviderType
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor

@Singleton
class JellyfinImageAuthInterceptor @Inject constructor(
    private val providerDao: ProviderDao,
    private val credentialCrypto: CredentialCrypto
) : Interceptor {
    private companion object {
        private const val CACHE_TTL_MILLIS = 30_000L
    }

    @Volatile
    private var cachedProviders: List<ProviderEntity> = emptyList()

    @Volatile
    private var cacheExpiresAtMillis: Long = 0L

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        if (!request.header("Authorization").isNullOrBlank()) {
            return chain.proceed(request)
        }

        // Image credentials are account-scoped.  A URL without this marker is
        // deliberately not guessed from its host: two accounts may share it.
        val providerId = request.url.queryParameter("streamvault_provider_id")?.toLongOrNull()
            ?: return chain.proceed(request)
        val provider = jellyfinProviders().firstOrNull { it.id == providerId }
            ?: return chain.proceed(request)
        if (!provider.matches(request.url)) return chain.proceed(request)
        val accessToken = runCatching { credentialCrypto.decryptIfNeeded(provider.password) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .url(request.url.newBuilder().removeAllQueryParameters("streamvault_provider_id").build())
                .header(
                    "Authorization",
                    buildJellyfinAuthorizationHeader(provider.serverUrl, provider.username, accessToken)
                )
                .build()
        )
    }

    private fun jellyfinProviders(): List<ProviderEntity> {
        val now = System.currentTimeMillis()
        if (now < cacheExpiresAtMillis) {
            return cachedProviders
        }
        val refreshed = runCatching { providerDao.getByTypeSync(ProviderType.JELLYFIN) }
            .getOrDefault(emptyList())
        cachedProviders = refreshed
        cacheExpiresAtMillis = now + CACHE_TTL_MILLIS
        return refreshed
    }

    private fun ProviderEntity.matches(url: HttpUrl): Boolean {
        val baseUrl = serverUrl.toHttpUrlOrNull() ?: return false
        if (url.scheme != baseUrl.scheme || url.host != baseUrl.host || url.port != baseUrl.port) {
            return false
        }

        val baseSegments = baseUrl.pathSegments.filter { it.isNotEmpty() }
        val requestSegments = url.pathSegments.filter { it.isNotEmpty() }
        if (requestSegments.size < baseSegments.size) {
            return false
        }

        return baseSegments.indices.all { index -> requestSegments[index] == baseSegments[index] }
    }
}
