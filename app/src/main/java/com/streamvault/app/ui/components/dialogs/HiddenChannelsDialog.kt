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
import com.streamvault.app.ui.theme.Primary
import com.streamvault.app.ui.theme.PrimaryLight
import com.streamvault.app.ui.theme.SurfaceElevated
import com.streamvault.app.ui.theme.SurfaceHighlight
import com.streamvault.domain.model.Channel

/**
 * Quick-action dialog that lists the currently-hidden Live channels and lets
 * the user restore them one by one (apply-immediate) or in bulk via "Unhide all".
 *
 * Hosted by `HomeScreen` from the Live TV *Filtres rapides* block (M4). Backend
 * mutations are routed through `HomeViewModel.unhideChannel` /
 * `unhideAllChannels`, which in turn call `PreferencesRepository.setChannelHidden`
 * / `setHiddenChannelIds` — no schema change, just a new entry point into the
 * existing visibility plumbing.
 *
 * Mirrors `HiddenCategoriesDialog` (M5) line-by-line with Channel in place of
 * Category. Channels are items (not containers) so the row has no count badge.
 */
@Composable
fun HiddenChannelsDialog(
    hiddenChannels: List<Channel>,
    onUnhide: (Channel) -> Unit,
    onUnhideAll: () -> Unit,
    onDismiss: () -> Unit
) {
    PremiumDialog(
        title = stringResource(R.string.hidden_channels_dialog_title),
        subtitle = stringResource(R.string.hidden_channels_dialog_subtitle),
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
                    enabled = hiddenChannels.isNotEmpty(),
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
                            text = stringResource(R.string.hidden_channels_dialog_unhide_all),
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
                    items(hiddenChannels, key = { it.id }) { channel ->
                        HiddenChannelRow(
                            channel = channel,
                            onUnhide = { onUnhide(channel) }
                        )
                    }
                }
            }
        },
        footer = {
            Spacer(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
            PremiumDialogFooterButton(
                label = stringResource(R.string.hidden_channels_dialog_close),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun HiddenChannelRow(
    channel: Channel,
    onUnhide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = channel.name,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
            modifier = Modifier.padding(end = 12.dp)
        )
        Switch(
            checked = true,
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
