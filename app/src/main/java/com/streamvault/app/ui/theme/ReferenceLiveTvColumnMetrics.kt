package com.streamvault.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.streamvault.app.device.rememberIsTelevisionDevice

/**
 * هندسة Live TV المرجعية المشتركة بين الثيم الأساسي والثيمات ذات البصمات البصرية المستقلة.
 * يحتفظ كل ثيم بألوانه وأسـطحه وحوافه، بينما تتطابق فقط علاقة أعمدة الفئات والقنوات والمعاينة.
 */
internal data class ReferenceLiveTvColumnMetrics(
    val categoryWidth: Dp,
    val channelWeight: Float = 0.98f,
    val previewWeight: Float = 1.08f,
    val columnSpacing: Dp = 16.dp
)

/**
 * يطابق حساب [HomeScreen] المرجعي: يتكيف شريط الفئات مع عرض الجهاز ثم تتقاسم القنوات
 * والمعاينة المساحة المتبقية بنسبة 0.98 إلى 1.08، من دون فرض أي قالب بصري مشترك.
 */
@Composable
internal fun rememberReferenceLiveTvColumnMetrics(): ReferenceLiveTvColumnMetrics {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    return remember(screenWidth, isTelevisionDevice) {
        val categoryWidth = if (screenWidth < 900.dp) {
            (screenWidth * 0.36f).coerceIn(188.dp, 220.dp)
        } else if (!isTelevisionDevice && screenWidth < 1280.dp) {
            (screenWidth * 0.28f).coerceIn(220.dp, 252.dp)
        } else {
            272.dp
        }
        ReferenceLiveTvColumnMetrics(categoryWidth = categoryWidth)
    }
}
