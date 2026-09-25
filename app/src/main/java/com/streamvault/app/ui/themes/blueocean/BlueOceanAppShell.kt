package com.streamvault.app.ui.themes.blueocean

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.interaction.TvClickableSurface

/**
 * Blue Ocean's navigation shell is an independent tide-styled rail.
 *
 * It deliberately does not share visual architecture with the Cinematic, Glass,
 * or Alaa shells. Shared navigation routes, callbacks, and D-pad contracts remain
 * unchanged — only the presentation surface is Blue Ocean-specific.
 */

private data class BlueOceanNavEntry(
    val route: String,
    val label: String,
    val tideChar: String,
    val badge: String? = null
)

@Composable
internal fun BlueOceanShellRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = remember {
        listOf(
            BlueOceanNavEntry(Routes.HOME, "TIDE TABLE", "H"),
            BlueOceanNavEntry(Routes.LIVE_TV, "LIVE CURRENT", "L"),
            BlueOceanNavEntry(Routes.MOVIES, "FILM TIDES", "F"),
            BlueOceanNavEntry(Routes.SERIES, "SERIES FLOW", "S"),
            BlueOceanNavEntry(Routes.SEARCH, "DEPTHS", "D"),
            BlueOceanNavEntry(Routes.FAVORITES, "HARBOUR", "B"),
            BlueOceanNavEntry(Routes.SETTINGS, "OPERATIONS", "O")
        )
    }

    Column(
        modifier = modifier
            .width(224.dp)
            .fillMaxHeight()
            .background(BlueOceanSurfaceBrush)
            .padding(horizontal = 14.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "BLUE OCEAN",
            style = MaterialTheme.typography.titleMedium,
            color = BlueOceanFoam,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = "TIDE NAVIGATION",
            style = MaterialTheme.typography.labelSmall,
            color = BlueOceanMuted
        )

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(entries, key = { it.route }) { entry ->
                BlueOceanRailItem(
                    entry = entry,
                    isSelected = currentRoute == entry.route,
                    onClick = { onNavigate(entry.route) }
                )
            }
        }
    }
}

@Composable
private fun BlueOceanRailItem(
    entry: BlueOceanNavEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember(entry.route) { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) BlueOceanSelectedAccent else Color.Transparent,
            focusedContainerColor = BlueOceanPanelRaised,
            contentColor = if (isSelected) BlueOceanFoam else BlueOceanMuted,
            focusedContentColor = BlueOceanText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, BlueOceanAccent),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.tideChar,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected || isFocused) BlueOceanAccent else BlueOceanMuted,
                fontWeight = FontWeight.Black
            )
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected || isFocused) BlueOceanText else BlueOceanMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Wave gradient background for the shell container.
 * Paints a subtle horizontal tide sweep behind all Blue Ocean content.
 */
@Composable
internal fun BlueOceanShellBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueOceanWaveBrush)
    )
}

/**
 * D-pad focus configuration for the Blue Ocean shell.
 * The shell uses a 1.025f focus scale and 190ms motion duration,
 * matching the ThemeFocusSpec registered in ThemePresentation.
 */
internal object BlueOceanShellFocus {
    const val FocusedScale: Float = 1.025f
    const val MotionDurationMs: Int = 190
    val FocusBorderColor: Color = BlueOceanAccent
    val FocusBorderWidth: androidx.compose.ui.unit.Dp = 2.dp
}

/**
 * Content padding for screens rendered inside the Blue Ocean shell.
 * Provides consistent tide-gutter spacing.
 */
internal val BlueOceanShellPadding = PaddingValues(
    start = 24.dp,
    top = 24.dp,
    end = 30.dp,
    bottom = 24.dp
)
