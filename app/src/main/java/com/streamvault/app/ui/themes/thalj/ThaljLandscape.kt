package com.streamvault.app.ui.themes.thalj

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
        repeat(5) { layer ->
            val x=w*(.35f+layer*.14f)
            val p=Path().apply { moveTo(x-w*.23f,h);lineTo(x,h*(.28f+layer*.045f));lineTo(x+w*.28f,h);close() }
            drawPath(p,Color(0xFFBCEBFF).copy(alpha=.12f+layer*.035f))
            val cap=Path().apply { moveTo(x-w*.06f,h*(.46f+layer*.035f));lineTo(x,h*(.28f+layer*.045f));lineTo(x+w*.07f,h*(.48f+layer*.035f));close() }
            drawPath(cap,Color.White.copy(alpha=.23f))
        }
        repeat(65) { i ->
            drawCircle(Color.White.copy(alpha=.32f),1.5f+(i%3),Offset((i*137f+sin(i.toFloat())*phase*25f)%w,(i*83f+phase*120f)%h))
        }
    }
}
