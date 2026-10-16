package com.streamvault.app.ui.themes.najma

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlin.math.sin

internal val LocalHorizonArtwork = staticCompositionLocalOf { mutableStateOf<String?>(null) }

/** Offline landscape drawn behind actual provider artwork. Never ships fake film metadata. */
@Composable
internal fun HorizonLandscape(modifier: Modifier = Modifier) {
    NajmaSky(modifier)
}
