package com.streamvault.app.ui.screens.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.streamvault.app.panel.PanelDeviceCode
import com.streamvault.app.panel.PanelManager
import com.streamvault.app.panel.PanelStatus
import com.streamvault.app.ui.themes.bespoke.tr
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PanelSettingsEntryPoint {
    fun panelManager(): PanelManager
}

/** "Device code / panel" block: code, MAC, status, and a refresh row. */
internal fun LazyListScope.settingsPanelSection() {
    item(key = "settings_panel_section") { PanelSettingsBlock() }
}

@Composable
private fun PanelSettingsBlock() {
    val context = LocalContext.current
    val manager = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, PanelSettingsEntryPoint::class.java).panelManager()
    }
    val state by manager.state.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(manager) { manager.ensureIdentity() }

    SettingsSectionHeader(
        title = tr("Device code / panel", "كود الجهاز / اللوحة"),
        subtitle = tr("Give this code to your provider to activate playlists", "أعطِ هذا الكود لمزوّدك لتفعيل القوائم"),
    )
    SettingsRow(label = tr("Device code", "كود الجهاز"), value = PanelDeviceCode.display(state.deviceCode))
    SettingsRow(label = "MAC", value = state.mac)
    val status = when (state.status) {
        PanelStatus.ACTIVE -> tr("Active", "مفعّل") +
            (state.expiresAt?.let { tr(" · until ", " · حتى ") + it.take(10) } ?: "") +
            " · " + tr("${state.playlistCount} playlists", "${state.playlistCount} قوائم")
        PanelStatus.EXPIRED -> tr("Expired", "منتهي") + (state.expiresAt?.let { " · ${it.take(10)}" } ?: "")
        PanelStatus.INACTIVE -> tr("Not activated", "غير مفعّل")
        PanelStatus.UNKNOWN, null -> tr("Not checked yet", "لم يتم التحقق بعد")
    } + if (state.offline) tr(" (offline)", " (بدون إنترنت)") else ""
    SettingsRow(label = tr("Status", "الحالة"), value = status)
    ClickableSettingsRow(
        label = tr("Refresh from panel", "تحديث من اللوحة"),
        value = if (state.syncing) tr("Checking…", "جارٍ التحقق…") else tr("Check now", "تحقق الآن"),
        enabled = !state.syncing,
        onClick = { scope.launch { manager.sync(force = true) } },
    )
}
