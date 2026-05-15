package com.streamvault.data.remote.stalker

import com.streamvault.domain.model.StalkerPortalProfile

internal interface StalkerPlaybackAdapter {
    val adapterMode: StalkerPlaybackMode

    fun matches(
        descriptor: StalkerPlaybackDescriptor,
        variant: StalkerCommandVariant,
        portalProfileHint: StalkerPortalProfile,
        preferredMode: StalkerPlaybackMode?
    ): Boolean

    fun allowsDirectBypass(variant: StalkerCommandVariant): Boolean

    fun requiresCreateLink(variant: StalkerCommandVariant): Boolean

    fun allowsRebootstrap(
        descriptor: StalkerPlaybackDescriptor,
        accountProfile: StalkerProviderProfile
    ): Boolean = descriptor.capabilities.usesTemporaryLinks ||
        accountProfile.ambiguousState ||
        descriptor.primaryMode == StalkerPlaybackMode.MULTI_CMD
}

internal object DirectOrCreateLinkAdapter : StalkerPlaybackAdapter {
    override val adapterMode: StalkerPlaybackMode = StalkerPlaybackMode.DIRECT_URL

    override fun matches(
        descriptor: StalkerPlaybackDescriptor,
        variant: StalkerCommandVariant,
        portalProfileHint: StalkerPortalProfile,
        preferredMode: StalkerPlaybackMode?
    ): Boolean = variant.playbackMode == StalkerPlaybackMode.DIRECT_URL ||
        variant.playbackMode == StalkerPlaybackMode.LOCALHOST_CMD ||
        descriptor.primaryMode == StalkerPlaybackMode.MULTI_CMD

    override fun allowsDirectBypass(variant: StalkerCommandVariant): Boolean =
        variant.playbackMode == StalkerPlaybackMode.DIRECT_URL

    override fun requiresCreateLink(variant: StalkerCommandVariant): Boolean =
        variant.playbackMode != StalkerPlaybackMode.DIRECT_URL
}

internal object NginxSecureLinkAdapter : StalkerPlaybackAdapter {
    override val adapterMode: StalkerPlaybackMode = StalkerPlaybackMode.TEMP_LINK_NGINX

    override fun matches(
        descriptor: StalkerPlaybackDescriptor,
        variant: StalkerCommandVariant,
        portalProfileHint: StalkerPortalProfile,
        preferredMode: StalkerPlaybackMode?
    ): Boolean = variant.playbackMode == StalkerPlaybackMode.TEMP_LINK_NGINX ||
        descriptor.capabilities.nginxSecureLink ||
        descriptor.capabilities.useHttpTemporaryLink ||
        portalProfileHint == StalkerPortalProfile.MAG_STRICT

    override fun allowsDirectBypass(variant: StalkerCommandVariant): Boolean = true

    override fun requiresCreateLink(variant: StalkerCommandVariant): Boolean = true
}

internal object FlussonicTemporaryLinkAdapter : StalkerPlaybackAdapter {
    override val adapterMode: StalkerPlaybackMode = StalkerPlaybackMode.TEMP_LINK_FLUSSONIC

    override fun matches(
        descriptor: StalkerPlaybackDescriptor,
        variant: StalkerCommandVariant,
        portalProfileHint: StalkerPortalProfile,
        preferredMode: StalkerPlaybackMode?
    ): Boolean = variant.playbackMode == StalkerPlaybackMode.TEMP_LINK_FLUSSONIC ||
        descriptor.capabilities.flussonicTemporaryLink

    override fun allowsDirectBypass(variant: StalkerCommandVariant): Boolean = true

    override fun requiresCreateLink(variant: StalkerCommandVariant): Boolean = true
}

internal object WowzaTemporaryLinkAdapter : StalkerPlaybackAdapter {
    override val adapterMode: StalkerPlaybackMode = StalkerPlaybackMode.TEMP_LINK_WOWZA

    override fun matches(
        descriptor: StalkerPlaybackDescriptor,
        variant: StalkerCommandVariant,
        portalProfileHint: StalkerPortalProfile,
        preferredMode: StalkerPlaybackMode?
    ): Boolean = variant.playbackMode == StalkerPlaybackMode.TEMP_LINK_WOWZA ||
        descriptor.capabilities.wowzaTemporaryLink

    override fun allowsDirectBypass(variant: StalkerCommandVariant): Boolean = true

    override fun requiresCreateLink(variant: StalkerCommandVariant): Boolean = true
}

internal fun resolveStalkerPlaybackAdapter(
    descriptor: StalkerPlaybackDescriptor,
    variant: StalkerCommandVariant,
    portalProfileHint: StalkerPortalProfile,
    preferredMode: StalkerPlaybackMode?
): StalkerPlaybackAdapter {
    val ordered = buildList {
        if (preferredMode != null) {
            adapterForMode(preferredMode)?.let(::add)
        }
        add(
            when (variant.playbackMode) {
                StalkerPlaybackMode.TEMP_LINK_FLUSSONIC -> FlussonicTemporaryLinkAdapter
                StalkerPlaybackMode.TEMP_LINK_WOWZA -> WowzaTemporaryLinkAdapter
                StalkerPlaybackMode.TEMP_LINK_NGINX -> NginxSecureLinkAdapter
                else -> DirectOrCreateLinkAdapter
            }
        )
        if (portalProfileHint == StalkerPortalProfile.MAG_STRICT) add(NginxSecureLinkAdapter)
        if (descriptor.capabilities.flussonicTemporaryLink) add(FlussonicTemporaryLinkAdapter)
        if (descriptor.capabilities.wowzaTemporaryLink) add(WowzaTemporaryLinkAdapter)
        if (descriptor.capabilities.nginxSecureLink || descriptor.capabilities.useHttpTemporaryLink) {
            add(NginxSecureLinkAdapter)
        }
        add(DirectOrCreateLinkAdapter)
    }.distinctBy { it.adapterMode }
    return ordered.firstOrNull { adapter ->
        adapter.matches(descriptor, variant, portalProfileHint, preferredMode)
    } ?: DirectOrCreateLinkAdapter
}

private fun adapterForMode(mode: StalkerPlaybackMode): StalkerPlaybackAdapter? = when (mode) {
    StalkerPlaybackMode.TEMP_LINK_FLUSSONIC -> FlussonicTemporaryLinkAdapter
    StalkerPlaybackMode.TEMP_LINK_WOWZA -> WowzaTemporaryLinkAdapter
    StalkerPlaybackMode.TEMP_LINK_NGINX -> NginxSecureLinkAdapter
    StalkerPlaybackMode.DIRECT_URL,
    StalkerPlaybackMode.LOCALHOST_CMD,
    StalkerPlaybackMode.MULTI_CMD -> DirectOrCreateLinkAdapter
}
