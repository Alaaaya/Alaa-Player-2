package com.streamvault.data.remote.xtream

import com.streamvault.data.remote.dto.*
import com.streamvault.data.remote.http.HttpRequestProfile
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import okhttp3.HttpUrl.Companion.toHttpUrl

/** Applies to both ordinary and thin/streaming decode paths, before any item leaves the transport. */
internal class PanelXtreamRequestFence(endpoint: String, profile: HttpRequestProfile) {
    private val providerId = profile.ownerTag?.substringAfter("provider:", "")?.substringBefore('/')?.toLongOrNull()
    private val token = providerId?.let(Policies::snapshot)
    private val url = endpoint.toHttpUrl()
    private val action = url.queryParameter("action")
    private val media = when (action) {
        "get_live_categories", "get_live_streams", "get_short_epg", "get_simple_data_table" -> "live"
        "get_vod_categories", "get_vod_streams", "get_vod_info" -> "vod"
        else -> "series"
    }
    fun checkRequest() {
        val id = providerId ?: return
        Policies.requireCurrent(requireNotNull(token))
        if (!Policies.isTracked(id)) return
        when (action) {
            "get_live_streams", "get_vod_streams", "get_series" -> {
                val category = url.queryParameter("category_id")
                check(category != null && Policies.allowsCategory(id, media, category)) { "Category request denied" }
            }
            "get_vod_info", "get_series_info", "get_short_epg", "get_simple_data_table" -> {
                val content = listOf("vod_id", "series_id", "series", "stream_id")
                    .firstNotNullOfOrNull { url.queryParameter(it)?.toLongOrNull() }
                check(Policies.allowsContent(id, media, content)) { "Unknown or hidden content" }
            }
        }
    }
    fun checkCurrent() { token?.let(Policies::requireCurrent) }
    fun accepts(value: Any?): Boolean {
        checkCurrent()
        val id = providerId ?: return true
        if (!Policies.isTracked(id)) return true
        val categories: Set<String>
        val contentId: Long
        when (value) {
            is XtreamCategory -> return Policies.allowsCategory(id, media, value.categoryId)
            is XtreamStream -> {
                categories = (value.categoryIds.orEmpty() + listOfNotNull(value.categoryId)).toSet()
                contentId = value.streamId
            }
            is XtreamLiveStreamRow -> {
                categories = (value.categoryIds.orEmpty() + listOfNotNull(value.categoryId)).toSet()
                contentId = value.streamId
            }
            is XtreamSeriesItem -> { categories = setOfNotNull(value.categoryId); contentId = value.seriesId }
            else -> return true
        }
        if (categories.isEmpty() && Policies.current(id)?.hidden?.get(media)?.isEmpty() == true) return true
        return Policies.recordMembership(requireNotNull(token), media, contentId, categories)
    }
    @Suppress("UNCHECKED_CAST")
    fun <T> filter(value: T): T {
        checkCurrent()
        return if (value is List<*>) value.filter(::accepts) as T else value
    }
}
