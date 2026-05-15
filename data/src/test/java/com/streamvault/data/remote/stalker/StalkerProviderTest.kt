package com.streamvault.data.remote.stalker

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.ProviderStatus
import com.streamvault.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StalkerProviderTest {

    @Test
    fun authenticate_treats_status_zero_as_partial_not_expired() = runTest {
        val provider = StalkerProvider(
            providerId = 7,
            api = FakeStalkerApiService(
                profile = StalkerProviderProfile(
                    accountId = "758423",
                    accountName = "Room",
                    statusLabel = "0",
                    authAccess = false
                )
            ),
            portalUrl = "https://portal.example.com/c/",
            macAddress = "00:1A:79:12:34:56",
            deviceProfile = "MAG250",
            timezone = "UTC",
            locale = "en"
        )

        val result = provider.authenticate()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.status).isEqualTo(ProviderStatus.PARTIAL)
    }

    @Test
    fun authenticate_maps_expired_date_to_expired() = runTest {
        val provider = StalkerProvider(
            providerId = 7,
            api = FakeStalkerApiService(
                profile = StalkerProviderProfile(
                    accountName = "Room",
                    expirationDate = 1L
                )
            ),
            portalUrl = "https://portal.example.com/c/",
            macAddress = "00:1A:79:12:34:56",
            deviceProfile = "MAG250",
            timezone = "UTC",
            locale = "en"
        )

        val result = provider.authenticate()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.status).isEqualTo(ProviderStatus.EXPIRED)
    }

    private class FakeStalkerApiService(
        private val profile: StalkerProviderProfile
    ) : StalkerApiService {
        override suspend fun authenticate(profile: StalkerDeviceProfile): Result<Pair<StalkerSession, StalkerProviderProfile>> =
            Result.success(
                StalkerSession(
                    loadUrl = "https://portal.example.com/server/load.php",
                    portalReferer = "https://portal.example.com/c/",
                    token = "token"
                ) to this.profile
            )

        override suspend fun getLiveCategories(
            session: StalkerSession,
            profile: StalkerDeviceProfile
        ) = Result.success(emptyList<StalkerCategoryRecord>())

        override suspend fun getLiveStreams(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            categoryId: String?
        ) = Result.success(emptyList<StalkerItemRecord>())

        override suspend fun streamLiveStreams(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            onItem: suspend (StalkerItemRecord) -> Unit
        ) = Result.success(0)

        override suspend fun getVodCategories(
            session: StalkerSession,
            profile: StalkerDeviceProfile
        ) = Result.success(emptyList<StalkerCategoryRecord>())

        override suspend fun getVodStreams(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            categoryId: String?
        ) = Result.success(emptyList<StalkerItemRecord>())

        override suspend fun getVodStreamsPage(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            categoryId: String?,
            page: Int
        ) = Result.success(StalkerPagedItems(emptyList(), page, page, 0))

        override suspend fun getSeriesCategories(
            session: StalkerSession,
            profile: StalkerDeviceProfile
        ) = Result.success(emptyList<StalkerCategoryRecord>())

        override suspend fun getSeries(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            categoryId: String?
        ) = Result.success(emptyList<StalkerItemRecord>())

        override suspend fun getSeriesPage(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            categoryId: String?,
            page: Int
        ) = Result.success(StalkerPagedItems(emptyList(), page, page, 0))

        override suspend fun getSeriesDetails(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            seriesId: String
        ) = Result.success(
            StalkerSeriesDetails(
                series = StalkerItemRecord(id = seriesId, name = "Series"),
                seasons = emptyList()
            )
        )

        override suspend fun getShortEpg(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            channelId: String,
            limit: Int
        ) = Result.success(emptyList<StalkerProgramRecord>())

        override suspend fun getEpg(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            channelId: String
        ) = Result.success(emptyList<StalkerProgramRecord>())

        override suspend fun getBulkEpg(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            periodHours: Int
        ) = Result.success(emptyList<StalkerProgramRecord>())

        override suspend fun streamBulkEpg(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            periodHours: Int,
            onProgram: suspend (StalkerProgramRecord) -> Unit
        ) = Result.success(0)

        override suspend fun streamEpg(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            channelId: String,
            periodHours: Int,
            onProgram: suspend (StalkerProgramRecord) -> Unit
        ) = Result.success(0)

        override suspend fun createLink(
            session: StalkerSession,
            profile: StalkerDeviceProfile,
            kind: StalkerStreamKind,
            cmd: String,
            seriesNumber: Int?
        ) = Result.success("http://cdn.example.com/stream.ts")
    }
}
