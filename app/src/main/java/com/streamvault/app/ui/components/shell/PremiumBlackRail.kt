package com.streamvault.app.ui.components.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.themes.premium.PremiumCanvasRaised
import com.streamvault.app.ui.themes.premium.PremiumFocus
import com.streamvault.app.ui.themes.premium.PremiumGold
import com.streamvault.app.ui.themes.premium.PremiumMetal
import com.streamvault.app.ui.themes.premium.PremiumMuted
import com.streamvault.app.ui.themes.premium.PremiumPanel
import com.streamvault.app.ui.themes.premium.PremiumPanelFocused
import com.streamvault.app.ui.themes.premium.PremiumText

/** Premium Black: قائمة نحيفة بالأيقونات فقط مع بيانات تعريف قصيرة ومساحات سوداء واسعة. */
@Composable
internal fun PremiumBlackDestinationRail(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val items = rememberDestinationItems()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    Surface(
        modifier = modifier.padding(start = 14.dp, top = 18.dp, bottom = 18.dp).focusProperties {
            val active = items.filter { currentRoute.startsWith(it.route) }.maxByOrNull { it.route.length }
            onEnter = { focusRequesters[active?.route] ?: FocusRequester.Default }
        },
        shape = RoundedCornerShape(12.dp),
        colors = SurfaceDefaults.colors(containerColor = PremiumPanel),
        border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.fillMaxHeight().padding(horizontal = 10.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("A", style = MaterialTheme.typography.titleLarge, color = PremiumGold)
            Text("P", style = MaterialTheme.typography.labelSmall, color = PremiumMuted)
            Spacer(Modifier.size(12.dp))
            items.forEach { item ->
                val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
                PremiumRailItem(stringResource(item.labelRes), item.icon, currentRoute.startsWith(item.route), Modifier.focusRequester(requester)) { if (!currentRoute.startsWith(item.route)) onNavigate(item.route) }
            }
            Spacer(Modifier.weight(1f))
            Text("TV", style = MaterialTheme.typography.labelSmall, color = PremiumMuted)
        }
    }
}

@Composable
private fun PremiumRailItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier.width(64.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) PremiumCanvasRaised else Color.Transparent, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText),
        border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) PremiumGold.copy(alpha = .62f) else Color.Transparent), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (focused) 1.015f else 1f)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = label, tint = if (selected) PremiumGold else PremiumMuted, modifier = Modifier.size(21.dp))
            Text(label.take(1).uppercase(), style = MaterialTheme.typography.labelSmall, color = if (selected) PremiumText else PremiumMuted)
        }
    }
}
