package com.streamvault.app.ui.components.shell

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.FocusedMarqueeText
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.design.FocusSpec
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import com.streamvault.app.ui.theme.LocalIsAlaaTheme
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.app.ui.interaction.TvIconButton

data class VodClassicCategoryOption(
    val key: String,
    val label: String,
    val count: Int,
    val isSelected: Boolean,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
    val isLocked: Boolean = false
)

@Composable
fun VodClassicSplitLayout(
    railTitle: String,
    railSearchValue: String,
    onRailSearchValueChange: (String) -> Unit,
    railSearchPlaceholder: String,
    categories: List<VodClassicCategoryOption>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isAlaaTheme = LocalIsAlaaTheme.current
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(if (isAlaaTheme) 16.dp else 20.dp)
    ) {
        CategoryRailPanel(
            title = railTitle,
            searchValue = railSearchValue,
            onSearchValueChange = onRailSearchValueChange,
            searchPlaceholder = railSearchPlaceholder,
            modifier = Modifier
                .width(if (isAlaaTheme) AlaaThemeDimensions.RailWidth else 320.dp)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(categories, key = { it.key }) { category ->
                var isFocused by remember(category.key) { mutableStateOf(false) }
                TvClickableSurface(
                    onClick = category.onClick,
                    onLongClick = category.onLongClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isAlaaTheme) 2.dp else 0.dp)
                        .onFocusChanged { isFocused = it.isFocused },
                    shape = ClickableSurfaceDefaults.shape(
                        RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else 14.dp)
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = when {
                            isAlaaTheme && category.isSelected -> AlaaThemeColors.AccentMuted
                            isAlaaTheme -> androidx.compose.ui.graphics.Color.Transparent
                            category.isSelected -> AppColors.Brand.copy(alpha = 0.22f)
                            else -> AppColors.SurfaceElevated
                        },
                        focusedContainerColor = if (isAlaaTheme) AlaaThemeColors.SurfaceFocused else AppColors.SurfaceEmphasis,
                        contentColor = when {
                            isAlaaTheme && category.isSelected -> AlaaThemeColors.Accent
                            isAlaaTheme -> AlaaThemeColors.TextPrimary
                            category.isSelected -> AppColors.BrandStrong
                            else -> AppColors.TextPrimary
                        },
                        focusedContentColor = if (isAlaaTheme) AlaaThemeColors.TextPrimary else AppColors.TextPrimary
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(
                                if (isAlaaTheme) AlaaThemeDimensions.FocusBorder else FocusSpec.BorderWidth,
                                if (isAlaaTheme) AlaaThemeColors.Accent else AppColors.Focus
                            ),
                            shape = RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else 14.dp)
                        )
                    ),
                    scale = ClickableSurfaceDefaults.scale(
                        focusedScale = if (isAlaaTheme) 1f else FocusSpec.FocusedScale
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isAlaaTheme) 12.dp else 14.dp,
                                vertical = if (isAlaaTheme) 10.dp else 12.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FocusedMarqueeText(
                            text = category.label,
                            isFocused = isFocused,
                            style = if (isAlaaTheme) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                            color = when {
                                isAlaaTheme && category.isSelected -> AlaaThemeColors.Accent
                                isAlaaTheme -> AlaaThemeColors.TextPrimary
                                category.isSelected -> AppColors.BrandStrong
                                else -> AppColors.TextPrimary
                            },
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (category.isLocked) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.home_locked_short),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAlaaTheme) AlaaThemeColors.Accent else AppColors.BrandStrong
                            )
                        }
                        if (category.count > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                            text = category.count.toString(),
                            style = if (isAlaaTheme) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                            color = when {
                                isAlaaTheme && isFocused -> AlaaThemeColors.TextPrimary
                                isAlaaTheme -> AlaaThemeColors.TextTertiary
                                else -> AppColors.TextSecondary
                            }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (isAlaaTheme) AlaaThemeColors.BrowseContent else AppColors.Canvas,
                    RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerLarge else 28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun VodClassicContentHeader(
    title: String,
    subtitle: String,
    actions: List<VodActionChip>,
    selectedActionKey: String? = null,
    modifier: Modifier = Modifier
) {
    val isAlaaTheme = LocalIsAlaaTheme.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isAlaaTheme) AlaaThemeColors.TextPrimary else AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAlaaTheme) AlaaThemeColors.TextSecondary else AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { action ->
                VodClassicHeaderActionButton(
                    action = action,
                    isSelected = action.key == selectedActionKey
                )
            }
        }
    }
}

@Composable
private fun VodClassicHeaderActionButton(
    action: VodActionChip,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isAlaaTheme = LocalIsAlaaTheme.current
    val shape = RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else 18.dp)
    TvClickableSurface(
        onClick = action.onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = when {
                isAlaaTheme && isSelected -> AlaaThemeColors.AccentMuted
                isAlaaTheme -> AlaaThemeColors.BrowseRail
                isSelected -> AppColors.Brand.copy(alpha = 0.18f)
                else -> AppColors.SurfaceElevated
            },
            focusedContainerColor = if (isAlaaTheme) AlaaThemeColors.BrowseContentFocused else AppColors.SurfaceEmphasis,
            contentColor = when {
                isAlaaTheme && isSelected -> AlaaThemeColors.AccentStrong
                isAlaaTheme -> AlaaThemeColors.TextPrimary
                isSelected -> AppColors.BrandStrong
                else -> AppColors.TextPrimary
            },
            focusedContentColor = if (isAlaaTheme) AlaaThemeColors.TextPrimary else AppColors.TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    if (isAlaaTheme) AlaaThemeDimensions.FocusBorder else FocusSpec.BorderWidth,
                    if (isAlaaTheme) AlaaThemeColors.Accent else AppColors.Focus
                ),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            action.detail?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isAlaaTheme && isSelected -> AlaaThemeColors.Accent
                        isAlaaTheme -> AlaaThemeColors.TextSecondary
                        isSelected -> AppColors.Brand
                        else -> AppColors.TextSecondary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
