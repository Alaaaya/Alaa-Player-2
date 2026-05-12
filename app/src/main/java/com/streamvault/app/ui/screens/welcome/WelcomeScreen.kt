package com.streamvault.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.shell.StatusPill
import com.streamvault.app.ui.design.AppColors
import com.streamvault.data.sync.SyncProgressBus
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.sync.Section
import com.streamvault.domain.sync.SyncProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    syncProgressBus: SyncProgressBus
) : ViewModel() {

    private val _hasProviders = MutableStateFlow<Boolean?>(null)
    val hasProviders: StateFlow<Boolean?> = _hasProviders.asStateFlow()

    // D8 — garde anti-rebond : une fois `hasProviders` connu (true ou false),
    // on cesse définitivement de relayer le bus pour ne pas afficher la
    // progression d'un sync ultérieur (settings refresh, retry) sur Welcome.
    private val acceptingProgress = MutableStateFlow(true)

    val syncProgress: StateFlow<SyncProgress?> =
        combine(syncProgressBus.flow, acceptingProgress) { progress, accept ->
            if (accept) progress else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            providerRepository.getProviders()
                .map { it.isNotEmpty() }
                .collect { _hasProviders.value = it }
        }
        viewModelScope.launch {
            _hasProviders
                .filterNotNull()
                .first()
            acceptingProgress.value = false
        }
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val hasProviders by viewModel.hasProviders.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()

    LaunchedEffect(hasProviders) {
        when (hasProviders) {
            true -> onNavigateToHome()
            false -> onNavigateToSetup()
            null -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f),
                            AppColors.HeroTop,
                            AppColors.HeroBottom
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = syncProgress
                val pillLabel = if (progress != null) {
                    stringResource(sectionLabelRes(progress.section))
                } else {
                    stringResource(R.string.app_name)
                }
                val pillColor = if (progress != null) {
                    sectionColor(progress.section)
                } else {
                    AppColors.BrandMuted
                }
                StatusPill(
                    label = pillLabel,
                    containerColor = pillColor
                )
                Spacer(modifier = Modifier.height(18.dp))
                // D13 — spinner masqué dès qu'on a une progression structurée.
                if (progress == null) {
                    CircularProgressIndicator(color = AppColors.Brand)
                    Spacer(modifier = Modifier.height(18.dp))
                }
                Text(
                    text = stringResource(R.string.welcome_loading_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val subtitle = if (progress != null && progress.currentLabel.isNotBlank()) {
                    progress.currentLabel
                } else {
                    stringResource(R.string.welcome_loading_subtitle)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    if (progress.total > 0) {
                        LinearProgressIndicator(
                            progress = { progress.current.toFloat() / progress.total.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .width(260.dp),
                            color = AppColors.Brand,
                            trackColor = AppColors.BrandMuted
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .width(260.dp),
                            color = AppColors.Brand,
                            trackColor = AppColors.BrandMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(
                            R.string.sync_items_indexed_format,
                            progress.itemsIndexed
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun sectionColor(section: Section): Color = when (section) {
    Section.LIVE -> AppColors.Brand
    Section.VOD -> AppColors.Success
    Section.SERIES -> AppColors.Warning
}

private fun sectionLabelRes(section: Section): Int = when (section) {
    Section.LIVE -> R.string.sync_section_live
    Section.VOD -> R.string.sync_section_vod
    Section.SERIES -> R.string.sync_section_series
}
