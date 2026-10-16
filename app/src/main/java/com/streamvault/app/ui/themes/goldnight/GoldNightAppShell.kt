package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import androidx.tv.material3.MaterialTheme
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.shell.rememberDestinationItems
import com.streamvault.app.ui.interaction.TvClickableSurface

/**
 * Gold Night: شريط أيقونات ضيّق يساراً. العنصر النشط/المركّز يمتلئ بالذهبي، والإعدادات في الأسفل.
 * يستعمل نفس عناصر الوجهات المشتركة وعقد onNavigate دون أي مسارات جديدة.
 */
@Composable
internal fun GoldNightDestinationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberDestinationItems()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val primaryItems = items.filterNot { it.route == Routes.SETTINGS }
    val settingsItem = items.firstOrNull { it.route == Routes.SETTINGS }
    val activeRoute = items.filter { currentRoute.startsWith(it.route) }.maxByOrNull { it.route.length }?.route
    Column(
        modifier = modifier
            .background(GoldNightCanvas.copy(alpha = 0.92f))
            .focusProperties {
                onEnter = { focusRequesters[activeRoute] ?: FocusRequester.Default }
            }
            .padding(vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "A", style = MaterialTheme.typography.headlineSmall, color = GoldNightGold)
        Spacer(Modifier.height(30.dp))
        primaryItems.forEach { item ->
            val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
            GoldNightRailItem(
                label = stringResource(item.labelRes),
                icon = item.icon,
                selected = item.route == activeRoute,
                modifier = Modifier.focusRequester(requester),
                onClick = { if (item.route != activeRoute) onNavigate(item.route) }
            )
        }
        Spacer(Modifier.weight(1f))
        if (settingsItem != null) {
            val requester = focusRequesters.getOrPut(settingsItem.route) { FocusRequester() }
            GoldNightRailItem(
                label = stringResource(settingsItem.labelRes),
                icon = settingsItem.icon,
                selected = settingsItem.route == activeRoute,
                modifier = Modifier.focusRequester(requester),
                onClick = { if (settingsItem.route != activeRoute) onNavigate(settingsItem.route) }
            )
        }
    }
}

@Composable
private fun GoldNightRailItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(64.dp),
        shape = ClickableSurfaceDefaults.shape(RectangleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) GoldNightGold.copy(alpha = 0.85f) else Color.Transparent,
            focusedContainerColor = GoldNightGold,
            contentColor = if (selected) GoldNightOnGold else GoldNightMuted,
            focusedContentColor = GoldNightOnGold
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, GoldNightGoldBright), shape = RectangleShape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(28.dp).padding(0.dp))
        }
    }
}
