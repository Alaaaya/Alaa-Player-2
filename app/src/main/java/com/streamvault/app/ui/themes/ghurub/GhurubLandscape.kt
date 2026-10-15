package com.streamvault.app.ui.themes.ghurub

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
    val motion=rememberInfiniteTransition(label="landscape")
    val phase by motion.animateFloat(0f,1f,infiniteRepeatable(tween(18000,easing=LinearEasing),RepeatMode.Reverse),label="horizon")
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(IV.PurpleDeep.copy(alpha=.52f), IV.Bg)))
        val w=size.width; val h=size.height
        drawCircle(Brush.radialGradient(listOf(IV.Purple.copy(alpha=.38f),Color.Transparent),Offset(w*.78f,h*.25f),h*.55f),h*.55f,Offset(w*.78f,h*.25f))
        drawCircle(Color(0xFFFFD78C).copy(alpha=.7f),h*.07f,Offset(w*.76f,h*.32f))
        repeat(4) { layer ->
            val p=Path().apply {
                moveTo(0f,h)
                lineTo(0f,h*(.53f+layer*.095f))
                cubicTo(w*.3f,h*(.32f+layer*.09f),w*.62f,h*(.85f-layer*.03f),w,h*(.5f+layer*.09f))
                lineTo(w,h);close()
            }
            drawPath(p,IV.PurpleDeep.copy(alpha=.12f+layer*.065f))
        }
        repeat(28) { i -> drawCircle(Color(0xFFFFE4AD).copy(alpha=.18f),1.8f,Offset((i*137f+phase*35f)%w, h*.22f+(i*79f)% (h*.65f))) }
    }
}
