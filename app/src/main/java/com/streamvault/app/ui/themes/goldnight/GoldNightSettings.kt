package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Gold Night Settings: ثلاثة أعمدة — قائمة الأقسام | خيارات القسم (المحتوى المشترك) | معاينة الثيم.
 * المحتوى نفسه (providers، playback، themes...) يبقى من SettingsContent المشترك دون تعديل منطقه.
 */
@Composable
internal fun GoldNightSettingsSurface(
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoldNightScreenHeader(title = "Settings")
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(modifier = Modifier.width(280.dp).fillMaxHeight()) { navigation() }
            GoldNightColumnDivider()
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldNightCanvas.copy(alpha = 0.35f))
            ) { content() }
            GoldNightSettingsPreview(modifier = Modifier.weight(0.8f).fillMaxHeight())
        }
    }
}

/** قائمة أقسام الإعدادات بأسلوب Gold Night: العنصر المحدد بنص ذهبي والمركّز بحد ذهبي متوهج. */
@Composable
internal fun GoldNightSettingsNavigationRail(
    entries: List<Pair<String, String>>,
    selectedCategory: Int,
    focusRequester: FocusRequester,
    onCategorySelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(entries, key = { index, _ -> "gn_settings_nav_$index" }) { index, (label, icon) ->
            val selected = index == selectedCategory
            GoldNightCard(
                onClick = { onCategorySelected(index) },
                selected = selected,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (selected) Modifier.focusRequester(focusRequester) else Modifier)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(BorderStroke(1.dp, if (selected) GoldNightGold else GoldNightMuted), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, style = MaterialTheme.typography.labelMedium, color = if (selected) GoldNightGold else GoldNightMuted)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) GoldNightGold else GoldNightText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldNightSettingsPreview(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GoldNightPanel)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Live Preview", style = MaterialTheme.typography.titleMedium, color = GoldNightText)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldNightCanvasBrush)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(Modifier.width(12.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(5) { index ->
                        Box(Modifier.fillMaxWidth().height(10.dp).background(if (index == 1) GoldNightGold else GoldNightRule, RoundedCornerShape(2.dp)))
                    }
                }
                Column(Modifier.weight(0.8f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { index ->
                        Box(Modifier.fillMaxWidth().height(8.dp).background(if (index == 2) GoldNightGoldSoft else GoldNightPanel, RoundedCornerShape(2.dp)))
                    }
                }
                Column(Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { index ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(GoldNightPanel, RoundedCornerShape(2.dp))
                                .then(if (index == 3) Modifier.border(1.dp, GoldNightGold, RoundedCornerShape(2.dp)) else Modifier)
                        )
                    }
                }
            }
        }
        Text(text = "Gold Night", style = MaterialTheme.typography.titleMedium, color = GoldNightGold)
        Text(
            text = "ثيم كحلي ليلي بلمسات ذهبية وقنوات بأعمدة.",
            style = MaterialTheme.typography.bodySmall,
            color = GoldNightMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(GoldNightCanvas, GoldNightPanel, GoldNightGold, GoldNightText, GoldNightMuted).forEach { swatch ->
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(swatch)
                        .border(1.dp, GoldNightRule, RoundedCornerShape(6.dp))
                )
            }
        }
    }
}
