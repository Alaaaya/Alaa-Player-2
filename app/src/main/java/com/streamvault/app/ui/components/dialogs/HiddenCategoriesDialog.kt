package com.streamvault.app.ui.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.OnBackground
import com.streamvault.app.ui.theme.OnSurface
import com.streamvault.app.ui.theme.OnSurfaceDim
import com.streamvault.app.ui.theme.Primary
import com.streamvault.app.ui.theme.PrimaryLight
import com.streamvault.app.ui.theme.SurfaceElevated
import com.streamvault.app.ui.theme.SurfaceHighlight
import com.streamvault.domain.model.Category

/**
 * Quick-action dialog that lists the currently-hidden Live categories and lets
 * the user restore them one by one (apply-immediate) or in bulk via "Unhide all".
 *
 * Hosted by `HomeScreen` from the Live TV *Filtres rapides* block (M5). Backend
 * mutations are routed through `HomeViewModel.unhideCategory` /
 * `unhideAllLiveCategories`, which in turn call David's existing
 * `PreferencesRepository.setCategoryHidden` / `setHiddenCategoryIds` — no schema
 * change, just a new entry point into the existing visibility plumbing.
 */
@Composable
fun HiddenCategoriesDialog(
    hiddenCategories: List<Category>,
    onUnhide: (Category) -> Unit,
    onUnhideAll: () -> Unit,
    onDismiss: () -> Unit
) {
    PremiumDialog(
        title = stringResource(R.string.hidden_categories_dialog_title),
        subtitle = stringResource(R.string.hidden_categories_dialog_subtitle),
        onDismissRequest = onDismiss,
        widthFraction = 0.42f,
        heightFraction = null,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TvClickableSurface(
                    onClick = onUnhideAll,
                    enabled = hiddenCategories.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = SurfaceElevated,
                        focusedContainerColor = SurfaceHighlight
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, PrimaryLight),
                            shape = RoundedCornerShape(10.dp)
                        )
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.hidden_categories_dialog_unhide_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(hiddenCategories, key = { it.id }) { category ->
                        HiddenCategoryRow(
                            category = category,
                            onUnhide = { onUnhide(category) }
                        )
                    }
                }
            }
        },
        footer = {
            Spacer(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
            PremiumDialogFooterButton(
                label = stringResource(R.string.hidden_categories_dialog_close),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun HiddenCategoryRow(
    category: Category,
    onUnhide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(end = 12.dp)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )
            if (category.count > 0) {
                Text(
                    text = category.count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
        Switch(
            checked = false,
            onCheckedChange = { onUnhide() },
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = OnBackground,
                uncheckedTrackColor = SurfaceHighlight,
                uncheckedBorderColor = SurfaceHighlight,
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.4f)
            )
        )
    }
}
