package com.streamvault.app.ui.components.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.interaction.rememberTvInteractionSounds
import com.streamvault.app.ui.themes.glass.GlassAccent
import com.streamvault.app.ui.themes.glass.GlassCanvasDeep
import com.streamvault.app.ui.themes.glass.GlassFocus
import com.streamvault.app.ui.themes.glass.GlassFocusMotionMs
import com.streamvault.app.ui.themes.glass.GlassMuted
import com.streamvault.app.ui.themes.glass.GlassPane
import com.streamvault.app.ui.themes.glass.GlassPaneFocused
import com.streamvault.app.ui.themes.glass.GlassRule
import com.streamvault.app.ui.themes.glass.GlassText

/** Rail زجاجي عائم: تكوينه وارتفاعه وتمدد التركيز خاصان بـGlassmorphism. */
@Composable
internal fun GlassmorphismDestinationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberDestinationItems()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    Surface(
        modifier = modifier
            .padding(start = 18.dp, top = 20.dp, bottom = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(GlassPane, GlassCanvasDeep.copy(alpha = .78f))))
            .focusProperties {
                val active = items.filter { currentRoute.startsWith(it.route) }.maxByOrNull { it.route.length }
                onEnter = { focusRequesters[active?.route] ?: FocusRequester.Default }
            },
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = Color.Transparent),
        border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 14.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ALAA PLAYER", style = MaterialTheme.typography.titleLarge, color = GlassText)
            Text("GLASS NAVIGATION", style = MaterialTheme.typography.labelSmall, color = GlassMuted)
            Spacer(modifier = Modifier.height(16.dp))
            items.forEach { item ->
                val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
                GlassDestinationItem(
                    label = stringResource(item.labelRes),
                    icon = item.icon,
                    selected = currentRoute.startsWith(item.route),
                    modifier = Modifier.focusRequester(requester),
                    onClick = { if (!currentRoute.startsWith(item.route)) onNavigate(item.route) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("FOCUS TO EXPAND", style = MaterialTheme.typography.labelSmall, color = GlassMuted)
        }
    }
}

@Composable
private fun GlassDestinationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sounds = rememberTvInteractionSounds()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1f,
        animationSpec = tween(GlassFocusMotionMs),
        label = "glassRailExpand"
    )
    val shape = RoundedCornerShape(20.dp)
    TvClickableSurface(
        onClick = { sounds.playSelect(); onClick() },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.isFocused && !isFocused) sounds.playNavigate(); isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) GlassAccent.copy(alpha = .22f) else Color.Transparent,
            focusedContainerColor = GlassPaneFocused,
            contentColor = if (selected) GlassText else GlassMuted,
            focusedContentColor = GlassText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = if (selected) GlassAccent else GlassMuted, modifier = Modifier.size(19.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
