package com.streamvault.app.panel

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.tr

/**
 * Full-screen "waiting for activation / expired" card. Uses the active theme's colour scheme so it
 * matches whichever of the 12 themes is selected. BACK or "Continue" dismisses it for this session
 * so manually added providers stay usable.
 */
@Composable
fun PanelActivationScreen(
    state: PanelUiState,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "panelPulse").animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "pulse",
    )
    val retryFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { retryFocus.requestFocus() } }
    val expired = state.status == PanelStatus.EXPIRED

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(cs.primary.copy(alpha = 0.22f), cs.background, cs.background))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .background(cs.surface.copy(alpha = 0.92f), RoundedCornerShape(28.dp))
                .border(1.dp, cs.primary.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .padding(horizontal = 56.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (expired) tr("Subscription expired", "انتهى الاشتراك") else tr("Waiting for activation", "بانتظار التفعيل"),
                style = MaterialTheme.typography.headlineMedium, color = cs.onSurface, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = tr("Send this code to your provider to activate this device", "أرسل هذا الكود لمزوّدك لتفعيل الجهاز"),
                style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Box(
                Modifier
                    .border(2.dp, cs.primary.copy(alpha = pulse), RoundedCornerShape(20.dp))
                    .background(cs.primary.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 40.dp, vertical = 18.dp),
            ) {
                // Code is always LTR Latin, even in Arabic UI.
                Text(
                    text = PanelDeviceCode.display(state.deviceCode),
                    fontSize = 64.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp, color = cs.primary,
                )
            }
            Spacer(Modifier.height(20.dp))
            InfoLine(tr("MAC", "MAC"), state.mac)
            InfoLine(
                tr("Status", "الحالة"),
                when {
                    state.offline -> tr("Offline, showing last known status", "بدون إنترنت، آخر حالة معروفة")
                    expired -> tr("Expired", "منتهي") + (state.expiresAt?.let { " · ${it.take(10)}" } ?: "")
                    else -> tr("Not activated yet", "غير مفعّل بعد")
                },
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onRetry, modifier = Modifier.focusRequester(retryFocus), enabled = !state.syncing) {
                    Text(if (state.syncing) tr("Checking…", "جارٍ التحقق…") else tr("Retry", "إعادة المحاولة"))
                }
                OutlinedButton(onClick = onContinue) { Text(tr("Continue", "متابعة")) }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tr("You can still use playlists you added yourself.", "لسا فيك تستخدم القوائم اللي ضفتها بنفسك."),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.alpha(0.8f),
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$label  ", color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = cs.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
