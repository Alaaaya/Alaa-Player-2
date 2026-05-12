package com.streamvault.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.shell.StatusPill
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _hasProviders = MutableStateFlow<Boolean?>(null)
    val hasProviders: StateFlow<Boolean?> = _hasProviders.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.getProviders()
                .map { it.isNotEmpty() }
                .collect { _hasProviders.value = it }
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

    LaunchedEffect(hasProviders) {
        when (hasProviders) {
            true -> onNavigateToHome()
            false -> Unit
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

        when (hasProviders) {
            false -> WelcomeStartCard(
                onNavigateToHome = onNavigateToHome,
                onNavigateToSetup = onNavigateToSetup,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )

            else -> WelcomeLoadingCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    }
}

@Composable
private fun WelcomeLoadingCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusPill(
                label = stringResource(R.string.app_name),
                containerColor = AppColors.BrandMuted
            )
            Spacer(modifier = Modifier.height(18.dp))
            CircularProgressIndicator(color = AppColors.Brand)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.welcome_loading_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.welcome_loading_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun WelcomeStartCard(
    onNavigateToHome: () -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StatusPill(
                label = stringResource(R.string.app_name),
                containerColor = AppColors.BrandMuted
            )
            Text(
                text = stringResource(R.string.welcome_tagline),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    onClick = onNavigateToSetup,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.welcome_setup_provider))
                }
                TvButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.colors(
                        containerColor = AppColors.SurfaceElevated,
                        focusedContainerColor = AppColors.BrandMuted,
                        contentColor = AppColors.TextPrimary
                    )
                ) {
                    Text(text = stringResource(R.string.welcome_setup_later))
                }
            }
        }
    }
}
