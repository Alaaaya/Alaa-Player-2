package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.components.ChannelLogoBadge
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Program
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Gold Night: رموز العرض فقط. خلفية كحلية ليلية، لوحات زرقاء داكنة، وتركيز ذهبي متوهّج.
 * كل البيانات والتشغيل تبقى في ViewModels والمستودعات المشتركة.
 */
internal val GoldNightCanvas = Color(0xFF0B1220)
internal val GoldNightCanvasRaised = Color(0xFF111A2E)
internal val GoldNightPanel = Color(0xFF18223A)
internal val GoldNightPanelFocused = Color(0xFF223052)
internal val GoldNightRule = Color(0xFF27324D)
internal val GoldNightGold = Color(0xFFD4AF37)
internal val GoldNightGoldBright = Color(0xFFF1D27A)
internal val GoldNightGoldSoft = Color(0x33D4AF37)
internal val GoldNightText = Color(0xFFFFFFFF)
internal val GoldNightMuted = Color(0xFF9AA4B8)
internal val GoldNightOnGold = Color(0xFF151006)
internal const val GoldNightFocusMotionMs = 180
internal const val GoldNightFocusedScale = 1.03f
internal val GoldNightFocusBorderWidth = 2.5.dp

internal val GoldNightCanvasBrush: Brush = Brush.verticalGradient(listOf(GoldNightCanvas, GoldNightCanvasRaised))

/** بطاقة قابلة للتركيز: حدّ ذهبي متوهج 2.5dp وتكبير طفيف 1.03 كما في التصميم. */
@Composable
internal fun GoldNightCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    cornerRadius: Dp = 12.dp,
    containerColor: Color = GoldNightPanel,
    focusedScale: Float = GoldNightFocusedScale,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) GoldNightPanelFocused else containerColor,
            focusedContainerColor = GoldNightPanelFocused,
            contentColor = GoldNightText,
            focusedContentColor = GoldNightText
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, if (selected) GoldNightGold.copy(alpha = 0.55f) else Color.Transparent),
                shape = shape
            ),
            focusedBorder = Border(border = BorderStroke(GoldNightFocusBorderWidth, GoldNightGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = focusedScale),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = GoldNightGold.copy(alpha = 0.45f), elevation = 10.dp)
        ),
        content = content
    )
}

/** زر Gold Night: الأساسي ممتلئ بالذهبي، والثانوي لوحة داكنة. */
@Composable
internal fun GoldNightButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    icon: ImageVector? = null
) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) GoldNightGold else GoldNightPanel,
            focusedContainerColor = if (primary) GoldNightGoldBright else GoldNightPanelFocused,
            contentColor = if (primary) GoldNightOnGold else GoldNightText,
            focusedContentColor = if (primary) GoldNightOnGold else GoldNightText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(GoldNightFocusBorderWidth, GoldNightGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = GoldNightFocusedScale),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = GoldNightGold.copy(alpha = 0.45f), elevation = 8.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (primary) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** عنوان عمود مع قيمة ثانوية اختيارية. */
@Composable
internal fun GoldNightColumnTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = GoldNightText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (!trailing.isNullOrBlank()) {
            Text(text = trailing, style = MaterialTheme.typography.labelMedium, color = GoldNightGold, maxLines = 1)
        }
    }
}

/** شريط تقدم ذهبي رفيع (يستعمل لتقدّم البرنامج الحالي والمشاهدة). */
@Composable
internal fun GoldNightProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp
) {
    val safe = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(GoldNightRule)
    ) {
        if (safe > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(safe)
                    .background(Brush.horizontalGradient(listOf(GoldNightGold, GoldNightGoldBright)))
            )
        }
    }
}

/** شعار قناة دائري على خلفية فاتحة كما في التصميم. */
@Composable
internal fun GoldNightRoundLogo(
    channelName: String,
    logoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    ChannelLogoBadge(
        channelName = channelName,
        logoUrl = logoUrl,
        modifier = modifier.size(size),
        shape = CircleShape,
        backgroundColor = Color(0xFFF4F1E8),
        contentPadding = PaddingValues(5.dp),
        textStyle = MaterialTheme.typography.labelMedium,
        textColor = GoldNightOnGold
    )
}

/** ملصق صغير أو خلفية؛ عند غياب الصورة تظهر الأحرف الأولى على لوحة كحلية. */
@Composable
internal fun GoldNightArtwork(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.verticalGradient(listOf(GoldNightPanelFocused, GoldNightCanvas))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.trim().take(2).uppercase(Locale.ROOT),
            style = MaterialTheme.typography.titleMedium,
            color = GoldNightGold
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** لوحة حالة (تحميل / فراغ / خطأ) بأسلوب Gold Night. */
@Composable
internal fun GoldNightStatePanel(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldNightPanel)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = GoldNightGold)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = GoldNightMuted)
    }
}

/** الساعة والعلامة أعلى اليمين: «Alaa Player» بلمسة ذهبية. */
@Composable
internal fun GoldNightTopStatus(modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            now = System.currentTimeMillis()
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = goldNightClock(now), style = MaterialTheme.typography.titleLarge, color = GoldNightMuted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GoldNightGold, modifier = Modifier.size(28.dp))
            Text(text = "Alaa", style = MaterialTheme.typography.titleLarge, color = GoldNightGold, fontWeight = FontWeight.Bold)
            Text(text = "Player", style = MaterialTheme.typography.titleLarge, color = GoldNightText)
        }
    }
}

/** رأس الشاشة: مسار تنقل قصير يساراً والساعة/العلامة يميناً. */
@Composable
internal fun GoldNightScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    breadcrumb: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!breadcrumb.isNullOrBlank()) {
                Text(text = breadcrumb, style = MaterialTheme.typography.titleLarge, color = GoldNightGold)
                Text(text = "›", style = MaterialTheme.typography.titleLarge, color = GoldNightMuted)
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = GoldNightText)
        }
        GoldNightTopStatus()
    }
}

/** فاصل عمودي رفيع بين الأعمدة. */
@Composable
internal fun GoldNightColumnDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(GoldNightRule)
    )
}

internal fun goldNightClock(epochMs: Long): String =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        .format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

internal fun goldNightProgramProgress(program: Program?, now: Long = System.currentTimeMillis()): Float {
    if (program == null) return 0f
    val span = (program.endTime - program.startTime).coerceAtLeast(1L)
    return ((now - program.startTime).toFloat() / span.toFloat()).coerceIn(0f, 1f)
}

internal fun goldNightProgramWindow(program: Program?): String =
    if (program == null || program.startTime <= 0L) "" else "${goldNightClock(program.startTime)} – ${goldNightClock(program.endTime)}"

internal fun goldNightRating(rating: Float): String {
    if (rating <= 0f) return ""
    val stars = (rating / 2f).coerceIn(0f, 5f)
    val full = stars.toInt()
    return "★".repeat(full) + "☆".repeat(5 - full)
}

internal fun goldNightDuration(positionMs: Long): String {
    val totalMinutes = (positionMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes.toString().padStart(2, '0')}m" else "${minutes}m"
}

/** يحوّل D-pad يميناً إلى طلب نقل التركيز للعمود التالي (نفس عقد بقية الثيمات). */
internal fun Modifier.goldNightOnDpadRight(onRight: () -> Boolean): Modifier =
    onPreviewKeyEvent { event ->
        event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
            event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
            onRight()
    }
