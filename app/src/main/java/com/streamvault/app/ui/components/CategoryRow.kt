package com.streamvault.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.shell.AppSectionHeader
import com.streamvault.app.ui.theme.FocusBorder
import com.streamvault.app.ui.theme.OnSurface
import com.streamvault.app.ui.theme.Primary
import com.streamvault.app.ui.theme.SurfaceElevated
import com.streamvault.app.ui.theme.SurfaceHighlight
import com.streamvault.app.ui.interaction.mouseClickable
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.LocalIsAlaaTheme

// ── Netflix-style horizontal category row ─────────────────────────

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> CategoryRow(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    keySelector: ((T) -> Any)? = null,
    contentTypeSelector: ((T) -> Any?)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val resolvedContentTypeSelector: (T) -> Any? = contentTypeSelector ?: { null }
    val isAlaaTheme = LocalIsAlaaTheme.current
    val horizontalPadding = if (isAlaaTheme) 0.dp else 20.dp
    val rowGap = if (isAlaaTheme) 16.dp else 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .suppressParentVerticalScroll()
    ) {
        if (onSeeAll != null) {
            val seeAllFocusRequester = remember { FocusRequester() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 14.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSectionHeader(title = title)
                Surface(
                    onClick = onSeeAll,
                    modifier = Modifier
                        .focusRequester(seeAllFocusRequester)
                        .mouseClickable(
                            focusRequester = seeAllFocusRequester,
                            onClick = onSeeAll
                        ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isAlaaTheme) AlaaThemeColors.SurfaceElevated else SurfaceElevated,
                        focusedContainerColor = if (isAlaaTheme) AlaaThemeColors.SurfaceFocused else SurfaceHighlight,
                        contentColor = if (isAlaaTheme) AlaaThemeColors.Accent else Primary,
                        focusedContentColor = if (isAlaaTheme) AlaaThemeColors.TextPrimary else OnSurface
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(999.dp)),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(
                                if (isAlaaTheme) AlaaThemeDimensions.FocusBorder else 2.dp,
                                if (isAlaaTheme) AlaaThemeColors.Accent else FocusBorder
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                    )
                ) {
                    Text(
                        text = stringResource(R.string.category_see_all),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else {
            AppSectionHeader(
                title = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 14.dp, bottom = 6.dp)
            )
        }

        LazyRow(
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(rowGap)
        ) {
            items(
                items = items,
                key = keySelector,  // null = index-based keys (safe default)
                contentType = resolvedContentTypeSelector
            ) { item ->
                itemContent(item)
            }
        }
    }
}
