package com.streamvault.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import com.streamvault.domain.model.Channel
import kotlinx.coroutines.launch

/**
 * Channel options from the fullscreen OK list (long-press): favorite toggle + persisted reorder.
 * The order scope matches HomeViewModel.channelOrderScope so a saved order shows up everywhere.
 */
internal fun PlayerViewModel.playerChannelOrderScope(categoryId: Long = currentCategoryId): String {
    val source = currentCombinedProfileId?.let { "combined_${it}_all" } ?: "provider_$currentProviderId"
    return "${source}_category_$categoryId"
}

internal fun List<Channel>.withPlayerStoredOrder(orderedIds: List<Long>): List<Channel> {
    if (orderedIds.isEmpty() || isEmpty()) return this
    val pos = orderedIds.withIndex().associate { (i, id) -> id to i }
    return withIndex()
        .sortedWith(compareBy<IndexedValue<Channel>> { pos[it.value.id] ?: Int.MAX_VALUE }.thenBy { it.index })
        .map { it.value }
}

fun PlayerViewModel.toggleChannelFavorite(channel: Channel) {
    val shouldBeFavorite = !channel.isFavorite
    viewModelScope.launch {
        if (playerChannelCoordinator.setChannelFavorite(channel, shouldBeFavorite).isSuccess) {
            if (currentChannelFlow.value?.id == channel.id) {
                currentChannelFlow.value = currentChannelFlow.value?.copy(isFavorite = shouldBeFavorite)
            }
            val updated = currentChannelFlowList.value.map {
                if (it.id == channel.id && it.providerId == channel.providerId) it.copy(isFavorite = shouldBeFavorite) else it
            }
            channelList = updated
            currentChannelFlowList.value = updated
        }
    }
}

/** Persists [orderedIds] for the current category and applies it right away to the zapping list. */
fun PlayerViewModel.saveChannelOrder(orderedIds: List<Long>) {
    val byId = channelList.associateBy { it.id }
    val reordered = orderedIds.mapNotNull { byId[it] } + channelList.filter { it.id !in orderedIds.toSet() }
    val currentId = channelList.getOrNull(currentChannelIndex)?.id
    channelList = reordered
    currentChannelFlowList.value = reordered
    if (currentId != null) currentChannelIndex = reordered.indexOfFirst { it.id == currentId }
    val scope = playerChannelOrderScope()
    viewModelScope.launch { playerPreferencesCoordinator.setChannelOrder(scope, reordered.map(Channel::id)) }
}
