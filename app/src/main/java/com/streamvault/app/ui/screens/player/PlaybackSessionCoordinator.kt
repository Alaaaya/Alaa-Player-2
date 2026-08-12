package com.streamvault.app.ui.screens.player

import java.util.concurrent.atomic.AtomicLong

/**
 * Immutable identity gate for one player preparation/session.
 *
 * Network, recovery, guide, and token-renewal callbacks may outlive the request that created
 * them. Callers must validate the returned id before applying any result to UI or the player.
 */
internal class PlaybackSessionCoordinator {
    data class Session(val id: Long)

    private val nextId = AtomicLong()

    @Volatile
    private var activeSession = Session(0L)

    val currentId: Long
        get() = activeSession.id

    fun begin(): Session {
        val session = Session(nextId.incrementAndGet())
        activeSession = session
        return session
    }

    fun isCurrent(id: Long): Boolean = activeSession.id == id

    fun invalidate() {
        activeSession = Session(nextId.incrementAndGet())
    }
}
