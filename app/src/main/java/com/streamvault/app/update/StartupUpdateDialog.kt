package com.streamvault.app.update

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.streamvault.app.R

/** Root-level overlay, deliberately shared by every home theme. */
@Composable
fun StartupUpdateDialog(model: StartupUpdateViewModel) {
    val state by model.state.collectAsState()
    val download by model.downloadState.collectAsState()
    val release = state.release ?: return
    val updateFocus = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = model::later,
        title = { Text(stringResource(R.string.updater_available, release.versionName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.updater_system_confirm))
                if (state.error) Text(stringResource(R.string.updater_failed), color = MaterialTheme.colorScheme.error)
                if (state.accepted) {
                    if (state.waitingForPermission) {
                        Text(stringResource(R.string.updater_permission))
                    } else {
                        Text(stringResource(R.string.updater_downloading))
                        val progress = download.progress
                        if (progress == null) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        else LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            if (!state.accepted) Button(onClick = model::update, modifier = Modifier.focusRequester(updateFocus).heightIn(min = 56.dp)) {
                Text(stringResource(R.string.updater_update))
            }
        },
        dismissButton = {
            if (!state.accepted) OutlinedButton(onClick = model::later, modifier = Modifier.heightIn(min = 56.dp)) {
                Text(stringResource(R.string.updater_later))
            }
        }
    )
    LaunchedEffect(state.accepted) { if (!state.accepted) updateFocus.requestFocus() }
}
