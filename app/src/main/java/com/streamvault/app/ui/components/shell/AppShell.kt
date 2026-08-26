package com.streamvault.app.ui.components.shell

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.MainActivity
import com.streamvault.app.navigation.toAppRoute
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.design.AppMotion
import com.streamvault.app.ui.design.FocusSpec
import com.streamvault.app.ui.interaction.mouseClickable
import com.streamvault.app.ui.interaction.rememberTvInteractionSounds
import com.streamvault.app.ui.interaction.TvIconButton
import com.streamvault.app.ui.design.LocalAppShapes
import com.streamvault.app.ui.design.LocalAppSpacing
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import com.streamvault.app.ui.theme.LocalAppHomeTheme
import com.streamvault.app.ui.theme.LocalIsAlaaTheme
import com.streamvault.app.ui.themes.cinematic.CinematicCanvas
import com.streamvault.app.ui.themes.cinematic.CinematicGold
import com.streamvault.app.ui.themes.cinematic.CinematicMuted
import com.streamvault.app.ui.themes.cinematic.CinematicPanel
import com.streamvault.app.ui.themes.cinematic.CinematicPanelRaised
import com.streamvault.app.ui.themes.cinematic.CinematicText
import com.streamvault.app.ui.themes.cinematic.CinematicWine
import com.streamvault.app.ui.themes.neon.NeonCanvas
import com.streamvault.app.ui.themes.neon.NeonCyan
import com.streamvault.app.ui.themes.neon.NeonMuted
import com.streamvault.app.ui.themes.neon.NeonPanel
import com.streamvault.app.ui.themes.neon.NeonText
import com.streamvault.app.ui.themes.minimal.MinimalCanvas
import com.streamvault.app.ui.themes.minimal.MinimalFocus
import com.streamvault.app.ui.themes.minimal.MinimalMuted
import com.streamvault.app.ui.themes.minimal.MinimalPaper
import com.streamvault.app.ui.themes.minimal.MinimalRule
import com.streamvault.app.ui.themes.minimal.MinimalText
import com.streamvault.app.ui.themes.glass.GlassAccent
import com.streamvault.app.ui.themes.glass.GlassCanvas
import com.streamvault.app.ui.themes.glass.GlassCanvasDeep
import com.streamvault.app.ui.themes.glass.GlassFocus
import com.streamvault.app.ui.themes.glass.GlassFocusMotionMs
import com.streamvault.app.ui.themes.glass.GlassMuted
import com.streamvault.app.ui.themes.glass.GlassPane
import com.streamvault.app.ui.themes.glass.GlassPaneFocused
import com.streamvault.app.ui.themes.glass.GlassRule
import com.streamvault.app.ui.themes.glass.GlassText
import com.streamvault.app.ui.themes.streaming.StreamingCanvas
import com.streamvault.app.ui.themes.streaming.StreamingCanvasRaised
import com.streamvault.app.ui.themes.streaming.StreamingFocus
import com.streamvault.app.ui.themes.streaming.StreamingFocusMotionMs
import com.streamvault.app.ui.themes.streaming.StreamingMuted
import com.streamvault.app.ui.themes.streaming.StreamingPanel
import com.streamvault.app.ui.themes.streaming.StreamingPanelFocused
import com.streamvault.app.ui.themes.streaming.StreamingText
import com.streamvault.app.ui.themes.premium.PremiumCanvas
import com.streamvault.app.ui.themes.premium.PremiumCanvasRaised
import com.streamvault.app.ui.themes.premium.PremiumPanel
import com.streamvault.domain.model.AppHomeTheme
import com.streamvault.domain.model.AppTopLevelDestination
import com.streamvault.domain.model.CatalogLayout
import com.streamvault.domain.model.VirtualCategoryIds

enum class AppNavigationChrome {
    Rail,
    TopBar
}

@Composable
fun AppScreenScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    navigationChrome: AppNavigationChrome = AppNavigationChrome.Rail,
    topBarVisible: Boolean = true,
    compactHeader: Boolean = false,
    showScreenHeader: Boolean = true,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalAppSpacing.current
    val isAlaaTheme = LocalIsAlaaTheme.current
    val isCinematicTheme = LocalAppHomeTheme.current == AppHomeTheme.CINEMATIC
    val isNeonFutureTheme = LocalAppHomeTheme.current == AppHomeTheme.NEON_FUTURE
    val isMinimalTheme = LocalAppHomeTheme.current == AppHomeTheme.MINIMAL
    val isGlassTheme = LocalAppHomeTheme.current == AppHomeTheme.GLASSMORPHISM
    val isStreamingPlatformTheme = LocalAppHomeTheme.current == AppHomeTheme.STREAMING_PLATFORM
    val isPremiumBlackTheme = LocalAppHomeTheme.current == AppHomeTheme.PREMIUM_BLACK
    // Minimal يفرض فهرس أوامر عمودياً خاصاً به. الثيمات الأخرى تبقى ملتزمة
    // بالـ chrome الذي طلبته الشاشة حتى لا تتغير مساراتها أو هويتها.
    val resolvedNavigationChrome = when {
        isStreamingPlatformTheme -> AppNavigationChrome.TopBar
        isMinimalTheme || isGlassTheme || isPremiumBlackTheme -> AppNavigationChrome.Rail
        else -> navigationChrome
    }
    val canvasBrush = if (isAlaaTheme) {
        Brush.verticalGradient(listOf(AlaaThemeColors.Canvas, AlaaThemeColors.CanvasRaised))
    } else if (isCinematicTheme) {
        Brush.verticalGradient(listOf(CinematicCanvas, CinematicPanel, CinematicCanvas))
    } else if (isNeonFutureTheme) {
        Brush.verticalGradient(listOf(NeonCanvas, NeonPanel, NeonCanvas))
    } else if (isMinimalTheme) {
        Brush.verticalGradient(listOf(MinimalCanvas, MinimalPaper, MinimalCanvas))
    } else if (isGlassTheme) {
        Brush.linearGradient(listOf(GlassCanvas, GlassCanvasDeep, GlassCanvas))
    } else if (isStreamingPlatformTheme) {
        Brush.verticalGradient(listOf(StreamingCanvas, StreamingCanvasRaised, StreamingPanel))
    } else if (isPremiumBlackTheme) {
        Brush.verticalGradient(listOf(PremiumCanvas, PremiumPanel, PremiumCanvasRaised, PremiumCanvas))
    } else {
        Brush.linearGradient(listOf(AppColors.Canvas, AppColors.CanvasElevated, AppColors.Surface))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBrush)
    ) {
        if (resolvedNavigationChrome == AppNavigationChrome.Rail) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isMinimalTheme) {
                    MinimalCommandRail(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(238.dp)
                    )
                } else if (isGlassTheme) {
                    GlassmorphismDestinationRail(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        modifier = Modifier
                            .fillMaxHeight()
                        .width(270.dp)
                    )
                } else if (isPremiumBlackTheme) {
                    PremiumBlackDestinationRail(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(106.dp)
                    )
                } else {
                    DestinationRail(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        isAlaaTheme = isAlaaTheme,
                        isCinematicTheme = isCinematicTheme,
                        isNeonFutureTheme = isNeonFutureTheme,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(
                                when {
                                    isAlaaTheme -> AlaaThemeDimensions.RailWidth
                                    isCinematicTheme -> 262.dp
                                    isNeonFutureTheme -> 246.dp
                                    else -> spacing.railWidth
                                }
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = if (isAlaaTheme) AlaaThemeDimensions.ContentPadding else if (isCinematicTheme || isNeonFutureTheme || isMinimalTheme || isGlassTheme || isPremiumBlackTheme) 24.dp else spacing.lg,
                            end = if (isAlaaTheme) AlaaThemeDimensions.ContentPadding else if (isCinematicTheme || isNeonFutureTheme || isMinimalTheme || isGlassTheme || isPremiumBlackTheme) 30.dp else spacing.screenGutter,
                            top = if (isAlaaTheme) AlaaThemeDimensions.ContentPadding else if (isCinematicTheme || isNeonFutureTheme || isMinimalTheme || isGlassTheme || isPremiumBlackTheme) 24.dp else spacing.safeTop,
                            bottom = if (isAlaaTheme) AlaaThemeDimensions.ContentPadding else if (isCinematicTheme || isNeonFutureTheme || isMinimalTheme || isGlassTheme || isPremiumBlackTheme) 24.dp else spacing.safeBottom
                        )
                ) {
                    if (isAlaaTheme || topBarActions != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAlaaTheme) {
                                AlaaTopAction(
                                    icon = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_title),
                                    onClick = { onNavigate(Routes.SEARCH) }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                AlaaTopAction(
                                    icon = Icons.Default.AccountCircle,
                                    contentDescription = stringResource(R.string.nav_settings),
                                    onClick = { onNavigate(Routes.SETTINGS) }
                                )
                                if (topBarActions != null) Spacer(modifier = Modifier.width(10.dp))
                            }
                            if (topBarActions != null) topBarActions()
                        }
                        Spacer(modifier = Modifier.height(if (isAlaaTheme) 16.dp else spacing.md))
                    }
                    if (showScreenHeader) {
                        AppScreenHeader(
                            title = title,
                            subtitle = subtitle,
                            modifier = Modifier.fillMaxWidth(),
                            compact = compactHeader
                        )
                        if (header != null) {
                            Spacer(modifier = Modifier.height(spacing.lg))
                            header()
                        }
                        Spacer(modifier = Modifier.height(spacing.lg))
                    } else if (header != null) {
                        header()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        content()
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {
                if (topBarVisible) {
                    TopNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        actions = topBarActions,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (showScreenHeader) {
                    AppScreenHeader(
                        title = title,
                        subtitle = subtitle,
                        modifier = Modifier.fillMaxWidth(),
                        compact = true
                    )
                    if (header != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        header()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (header != null) {
                    header()
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AppScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    compact: Boolean = false
) {
    val isAlaaTheme = LocalIsAlaaTheme.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!eyebrow.isNullOrBlank()) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelMedium,
                color = if (isAlaaTheme) AlaaThemeColors.Accent else AppColors.Brand
            )
        }
        Text(
            text = title,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.displaySmall,
            color = if (isAlaaTheme) AlaaThemeColors.TextPrimary else AppColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                color = if (isAlaaTheme) AlaaThemeColors.TextSecondary else AppColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * فهرس Minimal ليس إعادة تلوين للـ rail العام: هو قائمة أوامر تحريرية ثابتة
 * بلا تكبير عند التركيز، وتبقى كل وجهة مرتبطة بمسار التطبيق المشترك نفسه.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun MinimalCommandRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberDestinationItems()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    Surface(
        modifier = modifier
            .background(MinimalPaper)
            .focusProperties {
                val activeItem = findActiveDestinationItem(items, currentRoute)
                onEnter = { focusRequesters[activeItem?.route] ?: FocusRequester.Default }
            },
        shape = RoundedCornerShape(0.dp),
        colors = SurfaceDefaults.colors(containerColor = MinimalPaper),
        border = Border(
            border = BorderStroke(1.dp, MinimalRule),
            shape = RoundedCornerShape(0.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MinimalText
            )
            Text(
                text = "COMMAND INDEX",
                style = MaterialTheme.typography.labelSmall,
                color = MinimalMuted
            )
            Spacer(modifier = Modifier.height(24.dp))
            items.forEachIndexed { index, item ->
                val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
                MinimalCommandRailItem(
                    index = index + 1,
                    label = stringResource(item.labelRes),
                    selected = currentRoute.startsWith(item.route),
                    focusRequester = requester,
                    onClick = {
                        if (!currentRoute.startsWith(item.route)) {
                            onNavigate(item.route)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MinimalRule)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.label_tv),
                style = MaterialTheme.typography.labelSmall,
                color = MinimalMuted
            )
        }
    }
}

@Composable
private fun MinimalCommandRailItem(
    index: Int,
    label: String,
    selected: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sounds = rememberTvInteractionSounds()
    val shape = RoundedCornerShape(0.dp)

    Surface(
        onClick = {
            sounds.playSelect()
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = {
                    sounds.playSelect()
                    onClick()
                }
            )
            .onFocusChanged {
                if (it.isFocused && !isFocused) sounds.playNavigate()
                isFocused = it.isFocused
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MinimalCanvas else Color.Transparent,
            focusedContainerColor = MinimalCanvas,
            contentColor = MinimalText,
            focusedContentColor = MinimalText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(1.dp, MinimalFocus),
                shape = shape
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected || isFocused) MinimalText else MinimalMuted
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected || isFocused) MinimalText else MinimalMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun TopNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val items = rememberDestinationItems()
    val scrollState = rememberScrollState()
    val isAlaaTheme = LocalIsAlaaTheme.current
    val isStreamingPlatformTheme = LocalAppHomeTheme.current == AppHomeTheme.STREAMING_PLATFORM

    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    
    Surface(
        modifier = modifier.focusProperties {
            onEnter = {
                val activeItem = findActiveDestinationItem(items, currentRoute)
                focusRequesters[activeItem?.route] ?: FocusRequester.Default
            }
        },
        shape = RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else if (isStreamingPlatformTheme) 14.dp else 18.dp),
        colors = SurfaceDefaults.colors(
            containerColor = if (isAlaaTheme) AlaaThemeColors.Surface.copy(alpha = 0.96f) else if (isStreamingPlatformTheme) StreamingCanvasRaised.copy(alpha = 0.96f) else AppColors.Surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isStreamingPlatformTheme) 64.dp else 56.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall,
                color = if (isAlaaTheme) AlaaThemeColors.TextPrimary else if (isStreamingPlatformTheme) StreamingText else AppColors.TextPrimary,
                modifier = Modifier.wrapContentWidth(Alignment.Start)
            )
            Spacer(modifier = Modifier.width(32.dp)) // Increased spacing to prevent overlap
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { item ->
                    val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
                    TopNavigationButton(
                        label = stringResource(item.labelRes),
                        icon = item.icon,
                        selected = currentRoute.startsWith(item.route),
                        focusRequester = requester,
                        onClick = {
                            if (!currentRoute.startsWith(item.route)) {
                                onNavigate(item.route)
                            }
                        }
                    )
                }
            }
            if (actions != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

@Composable
private fun AlaaTopAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    TvIconButton(
        onClick = onClick,
        colors = androidx.tv.material3.IconButtonDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = AlaaThemeColors.TextSecondary,
            focusedContentColor = AlaaThemeColors.TextPrimary
        ),
        border = androidx.tv.material3.IconButtonDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent),
                shape = RoundedCornerShape(999.dp)
            )
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun AppTopBarCloseAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.settings_close_app)
) {
    TvIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.tv.material3.IconButtonDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = AppColors.SurfaceEmphasis,
            contentColor = AppColors.TextSecondary,
            focusedContentColor = AppColors.TextPrimary
        ),
        border = androidx.tv.material3.IconButtonDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(FocusSpec.BorderWidth, AppColors.Focus),
                shape = RoundedCornerShape(14.dp)
            )
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TopNavigationButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sounds = rememberTvInteractionSounds()
    val isAlaaTheme = LocalIsAlaaTheme.current
    val isStreamingPlatformTheme = LocalAppHomeTheme.current == AppHomeTheme.STREAMING_PLATFORM
    val navigationShape = RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else if (isStreamingPlatformTheme) 10.dp else 14.dp)
    val scale by animateFloatAsState(
        targetValue = if (isFocused) {
            if (isAlaaTheme) AlaaThemeFocus.FocusedScale else if (isStreamingPlatformTheme) 1.02f else FocusSpec.FocusedScale
        } else 1f,
        animationSpec = if (isAlaaTheme) {
            androidx.compose.animation.core.tween(durationMillis = AlaaThemeFocus.AnimationDurationMs)
        } else {
            AppMotion.FocusSpec
        },
        label = "topNavScale"
    )

    Surface(
        onClick = {
            sounds.playSelect()
            onClick()
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = {
                    sounds.playSelect()
                    onClick()
                }
            )
            .zIndex(if (isFocused) 1f else 0f) // Keep focused button on top
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged {
                if (it.isFocused && !isFocused) {
                    sounds.playNavigate()
                }
                isFocused = it.isFocused
            },
        shape = ClickableSurfaceDefaults.shape(navigationShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) {
                if (isAlaaTheme) AlaaThemeColors.AccentMuted else if (isStreamingPlatformTheme) StreamingPanelFocused else AppColors.BrandMuted
            } else Color.Transparent,
            focusedContainerColor = if (isAlaaTheme) AlaaThemeColors.SurfaceFocused else if (isStreamingPlatformTheme) StreamingPanelFocused else AppColors.SurfaceEmphasis
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    if (isAlaaTheme) AlaaThemeDimensions.FocusBorder else if (isStreamingPlatformTheme) 2.dp else FocusSpec.BorderWidth,
                    if (isAlaaTheme) AlaaThemeColors.Accent else if (isStreamingPlatformTheme) StreamingFocus else AppColors.Focus
                ),
                shape = navigationShape
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) {
                    if (isAlaaTheme) AlaaThemeColors.AccentStrong else if (isStreamingPlatformTheme) StreamingFocus else AppColors.Brand
                } else if (isAlaaTheme) AlaaThemeColors.TextSecondary else if (isStreamingPlatformTheme) StreamingMuted else AppColors.TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected || isFocused) {
                    if (isAlaaTheme) AlaaThemeColors.TextPrimary else if (isStreamingPlatformTheme) StreamingText else AppColors.TextPrimary
                } else if (isAlaaTheme) AlaaThemeColors.TextSecondary else if (isStreamingPlatformTheme) StreamingMuted else AppColors.TextSecondary
            )
        }
    }
}

@Composable
fun AppHeroHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.SurfaceElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.Canvas,
                            AppColors.SurfaceAccent,
                            AppColors.SurfaceEmphasis
                        )
                    )
                )
                .padding(32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                AppScreenHeader(
                    title = title,
                    subtitle = subtitle,
                    eyebrow = eyebrow
                )
                if (actions != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
                if (footer != null) {
                    footer()
                }
            }
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionContentColor: Color = AppColors.TextTertiary
) {
    val shapes = LocalAppShapes.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = if (onActionClick != null && !actionLabel.isNullOrBlank()) Modifier.weight(1f) else Modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextTertiary
                )
            }
        }

        if (onActionClick != null && !actionLabel.isNullOrBlank()) {
            val actionFocusRequester = remember { FocusRequester() }
            Surface(
                onClick = onActionClick,
                modifier = Modifier
                    .focusRequester(actionFocusRequester)
                    .mouseClickable(
                        focusRequester = actionFocusRequester,
                        onClick = onActionClick
                    ),
                shape = ClickableSurfaceDefaults.shape(shapes.pill),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = AppColors.Brand.copy(alpha = 0.12f),
                    focusedContainerColor = AppColors.Brand.copy(alpha = 0.22f),
                    contentColor = actionContentColor
                )
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.SurfaceEmphasis,
    contentColor: Color = AppColors.TextPrimary,
    cornerRadius: Dp = 999.dp,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
fun AppMessageState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    shape: RoundedCornerShape? = null,
    containerBrush: Brush? = null,
    borderColor: Color? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    titleColor: Color = AppColors.TextPrimary,
    subtitleColor: Color = AppColors.TextSecondary,
    titleTextAlign: TextAlign = TextAlign.Start,
    subtitleTextAlign: TextAlign = TextAlign.Start
) {
    val resolvedShape = shape ?: LocalAppShapes.current.large
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = resolvedShape,
        border = Border(
            border = BorderStroke(
                width = if (borderColor != null) 1.dp else 0.dp,
                color = borderColor ?: Color.Transparent
            ),
            shape = resolvedShape
        ),
        colors = SurfaceDefaults.colors(containerColor = AppColors.SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .then(if (containerBrush != null) Modifier.background(containerBrush) else Modifier)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor,
                textAlign = titleTextAlign,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = subtitle,
                style = subtitleStyle,
                color = subtitleColor,
                textAlign = subtitleTextAlign,
                modifier = Modifier.fillMaxWidth()
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(8.dp))
                action()
            }
        }
    }
}

@Composable
fun LoadMoreCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = LocalAppShapes.current
    val focusRequester = remember { FocusRequester() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = onClick
            ),
        shape = ClickableSurfaceDefaults.shape(shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AppColors.SurfaceElevated,
            focusedContainerColor = AppColors.SurfaceEmphasis
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(FocusSpec.BorderWidth, AppColors.Focus),
                shape = shapes.medium
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = label,
                tint = AppColors.Brand,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary
            )
        }
    }
}

@Composable
fun ContentMetadataStrip(
    values: List<String>,
    modifier: Modifier = Modifier
) {
    val filteredValues = values.filter { it.isNotBlank() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filteredValues.forEachIndexed { index, value ->
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary
            )
            if (index < filteredValues.lastIndex) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppColors.TextTertiary)
                )
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun DestinationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isAlaaTheme: Boolean = false,
    isCinematicTheme: Boolean = false,
    isNeonFutureTheme: Boolean = false
) {
    val spacing = LocalAppSpacing.current
    val items = rememberDestinationItems()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val favoritesFocusRequester = remember { FocusRequester() }
    val recentFocusRequester = remember { FocusRequester() }
    val providerFocusRequester = remember { FocusRequester() }
    val currentTime = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")) }

    Box(
        modifier = modifier
            .padding(
                start = if (isAlaaTheme || isCinematicTheme || isNeonFutureTheme) 0.dp else spacing.lg,
                top = if (isAlaaTheme || isCinematicTheme || isNeonFutureTheme) 0.dp else spacing.safeTop,
                bottom = if (isAlaaTheme || isCinematicTheme || isNeonFutureTheme) 0.dp else spacing.safeBottom
            )
            .clip(if (isAlaaTheme) RoundedCornerShape(0.dp) else if (isCinematicTheme) RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp) else if (isNeonFutureTheme) RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp) else RoundedCornerShape(28.dp))
            .background(
                if (isAlaaTheme) {
                    Brush.verticalGradient(listOf(AlaaThemeColors.Sidebar, AlaaThemeColors.Sidebar))
                } else if (isCinematicTheme) {
                    Brush.verticalGradient(listOf(CinematicPanel, CinematicCanvas))
                } else if (isNeonFutureTheme) {
                    Brush.verticalGradient(listOf(NeonPanel, NeonCanvas))
                } else {
                    Brush.verticalGradient(listOf(AppColors.SurfaceElevated, AppColors.Surface))
                }
            )
            .focusProperties {
                onEnter = {
                    when {
                        isAlaaTheme && currentRoute == Routes.liveTv(VirtualCategoryIds.FAVORITES) -> favoritesFocusRequester
                        isAlaaTheme && currentRoute == Routes.liveTv(VirtualCategoryIds.RECENT) -> recentFocusRequester
                        isAlaaTheme && currentRoute.startsWith(Routes.providerSetup()) -> providerFocusRequester
                        else -> {
                            val activeItem = findActiveDestinationItem(items, currentRoute)
                            focusRequesters[activeItem?.route] ?: FocusRequester.Default
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                                .padding(
                    horizontal = if (isAlaaTheme) AlaaThemeDimensions.RailPadding else if (isCinematicTheme) 18.dp else if (isNeonFutureTheme) 14.dp else 12.dp,
                    vertical = if (isAlaaTheme) 28.dp else if (isCinematicTheme) 24.dp else if (isNeonFutureTheme) 20.dp else 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (isAlaaTheme) 12.dp else if (isCinematicTheme) 11.dp else if (isNeonFutureTheme) 8.dp else 10.dp)

        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = if (isAlaaTheme || isCinematicTheme || isNeonFutureTheme) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                color = if (isAlaaTheme) AlaaThemeColors.TextPrimary else if (isCinematicTheme) CinematicText else if (isNeonFutureTheme) NeonText else AppColors.TextPrimary
            )
            Text(
                text = stringResource(R.string.label_tv),
                style = MaterialTheme.typography.labelSmall,
                color = if (isAlaaTheme) AlaaThemeColors.Accent else if (isCinematicTheme) CinematicGold else if (isNeonFutureTheme) NeonCyan else AppColors.TextTertiary
            )
            Spacer(modifier = Modifier.height(10.dp))
            items.forEach { item ->
                val requester = focusRequesters.getOrPut(item.route) { FocusRequester() }
                RailButton(
                    label = stringResource(item.labelRes),
                    icon = item.icon,
                    selected = currentRoute.startsWith(item.route),
                    isAlaaTheme = isAlaaTheme,
                    isCinematicTheme = isCinematicTheme,
                    isNeonFutureTheme = isNeonFutureTheme,
                    modifier = Modifier.focusRequester(requester),
                    onClick = {
                        if (!currentRoute.startsWith(item.route)) {
                            onNavigate(item.route)
                        }
                    }
                )
            }
            if (isAlaaTheme) {
                Spacer(modifier = Modifier.height(8.dp))
                val favoritesRoute = Routes.liveTv(VirtualCategoryIds.FAVORITES)
                val recentRoute = Routes.liveTv(VirtualCategoryIds.RECENT)
                RailButton(
                    label = stringResource(R.string.dashboard_favorite_channels),
                    icon = Icons.Default.Favorite,
                    selected = currentRoute == favoritesRoute,
                    isAlaaTheme = true,
                    modifier = Modifier.focusRequester(favoritesFocusRequester),
                    onClick = { onNavigate(favoritesRoute) }
                )
                RailButton(
                    label = stringResource(R.string.dashboard_recent_channels),
                    icon = Icons.Default.History,
                    selected = currentRoute == recentRoute,
                    isAlaaTheme = true,
                    modifier = Modifier.focusRequester(recentFocusRequester),
                    onClick = { onNavigate(recentRoute) }
                )
                RailButton(
                    label = stringResource(R.string.settings_providers),
                    icon = Icons.Default.SwapHoriz,
                    selected = currentRoute.startsWith(Routes.providerSetup()),
                    isAlaaTheme = true,
                    modifier = Modifier.focusRequester(providerFocusRequester),
                    onClick = { onNavigate(Routes.providerSetup()) }
                )
            }
            if (isAlaaTheme) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlaaThemeColors.TextPrimary
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = AlaaThemeColors.TextTertiary
                )
            }
        }
    }
}

@Composable
private fun RailButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAlaaTheme: Boolean = false,
    isCinematicTheme: Boolean = false,
    isNeonFutureTheme: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) {
            if (isAlaaTheme) AlaaThemeFocus.FocusedScale else if (isCinematicTheme) 1.025f else if (isNeonFutureTheme) 1.018f else FocusSpec.FocusedScale
        } else {
            1f
        },
        animationSpec = AppMotion.FocusSpec,
        label = "railButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = onClick
            )
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(
            RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else if (isCinematicTheme) 14.dp else if (isNeonFutureTheme) 8.dp else 18.dp)
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = when {
                isAlaaTheme && selected -> AlaaThemeColors.AccentMuted
                isCinematicTheme && selected -> CinematicWine.copy(alpha = 0.50f)
                isNeonFutureTheme && selected -> NeonCyan.copy(alpha = 0.16f)
                selected -> AppColors.BrandMuted
                else -> Color.Transparent
            },
            focusedContainerColor = if (isAlaaTheme) AlaaThemeColors.SurfaceFocused else if (isCinematicTheme) CinematicPanelRaised else if (isNeonFutureTheme) NeonCyan.copy(alpha = 0.10f) else AppColors.SurfaceEmphasis
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    if (isAlaaTheme) AlaaThemeDimensions.FocusBorder else if (isCinematicTheme) 2.dp else if (isNeonFutureTheme) 2.dp else FocusSpec.BorderWidth,
                    if (isAlaaTheme) AlaaThemeColors.Accent else if (isCinematicTheme) CinematicGold else if (isNeonFutureTheme) NeonCyan else AppColors.Focus
                ),
                shape = RoundedCornerShape(if (isAlaaTheme) AlaaThemeDimensions.CornerMedium else if (isCinematicTheme) 14.dp else if (isNeonFutureTheme) 8.dp else 18.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    isAlaaTheme && selected -> AlaaThemeColors.Accent
                    isAlaaTheme -> AlaaThemeColors.TextSecondary
                    isCinematicTheme && selected -> CinematicGold
                    isCinematicTheme -> CinematicMuted
                    isNeonFutureTheme && selected -> NeonCyan
                    isNeonFutureTheme -> NeonMuted
                    selected -> AppColors.Brand
                    else -> AppColors.TextSecondary
                },
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = when {
                    isAlaaTheme && selected -> AlaaThemeColors.TextPrimary
                    isAlaaTheme -> AlaaThemeColors.TextSecondary
                    isCinematicTheme && selected -> CinematicText
                    isCinematicTheme -> CinematicMuted
                    isNeonFutureTheme -> NeonText
                    selected -> AppColors.TextPrimary
                    else -> AppColors.TextSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal data class DestinationItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
)

private fun findActiveDestinationItem(
    items: List<DestinationItem>,
    currentRoute: String
): DestinationItem? =
    items
        .filter { currentRoute.startsWith(it.route) }
        .maxByOrNull { it.route.length }
        ?: items.firstOrNull { it.route == currentRoute }

private fun buildDestinationItems(): List<DestinationItem> =
    AppTopLevelDestination.defaultOrder.map { it.toDestinationItem() }

private fun buildDestinationItems(
    configured: List<AppTopLevelDestination>,
    layout: CatalogLayout
): List<DestinationItem> {
    if (layout == CatalogLayout.SPLIT) return configured.map { it.toDestinationItem() }
    var insertedVod = false
    return buildList {
        configured.forEach { destination ->
            when (destination) {
                AppTopLevelDestination.MOVIES,
                AppTopLevelDestination.SERIES -> if (!insertedVod) {
                    add(DestinationItem(Routes.VOD, R.string.nav_vod, Icons.Default.Star))
                    insertedVod = true
                }
                else -> add(destination.toDestinationItem())
            }
        }
    }
}

@Composable
internal fun rememberDestinationItems(): List<DestinationItem> {
    val context = LocalContext.current
    val mainActivity = remember(context) { context.findMainActivity() }
    val configuredDestinations = mainActivity?.preferencesRepository?.appTopLevelDestinations
        ?.collectAsStateWithLifecycle(initialValue = AppTopLevelDestination.defaultOrder)
        ?.value
        ?: AppTopLevelDestination.defaultOrder
    val catalogLayout = mainActivity?.providerRepository?.getActiveProvider()
        ?.collectAsStateWithLifecycle(initialValue = null)
        ?.value
        ?.catalogLayout
        ?: CatalogLayout.SPLIT
    return remember(configuredDestinations, catalogLayout) {
        buildDestinationItems(configuredDestinations, catalogLayout)
    }
}

private fun AppTopLevelDestination.toDestinationItem(): DestinationItem = when (this) {
    AppTopLevelDestination.HOME -> DestinationItem(Routes.HOME, R.string.nav_home, Icons.Default.Home)
    AppTopLevelDestination.LIVE_TV -> DestinationItem(Routes.LIVE_TV, R.string.nav_live_tv, Icons.Default.PlayArrow)
    AppTopLevelDestination.MOVIES -> DestinationItem(Routes.MOVIES, R.string.nav_movies, Icons.Default.Star)
    AppTopLevelDestination.SERIES -> DestinationItem(Routes.SERIES, R.string.nav_series, Icons.Default.Menu)
    AppTopLevelDestination.DOWNLOADS -> DestinationItem(Routes.DOWNLOADS, R.string.nav_downloads, Icons.Default.Download)
    AppTopLevelDestination.GUIDE -> DestinationItem(Routes.EPG, R.string.nav_epg, Icons.Default.Info)
    AppTopLevelDestination.SEARCH -> DestinationItem(Routes.SEARCH, R.string.search_title, Icons.Default.Search)
    AppTopLevelDestination.PLUGINS -> DestinationItem(Routes.PLUGINS, R.string.nav_plugins, PluginBlocksIcon)
    AppTopLevelDestination.SETTINGS -> DestinationItem(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
}

private fun Context.findMainActivity(): MainActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is MainActivity) return current
        current = current.baseContext
    }
    return null
}

private val PluginBlocksIcon: ImageVector
    get() {
        if (_pluginBlocksIcon != null) return _pluginBlocksIcon!!
        _pluginBlocksIcon = ImageVector.Builder(
            name = "PluginBlocks",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 4f)
                horizontalLineTo(10f)
                verticalLineTo(11f)
                horizontalLineTo(3f)
                close()
                moveTo(14f, 4f)
                horizontalLineTo(21f)
                verticalLineTo(11f)
                horizontalLineTo(14f)
                close()
                moveTo(8.5f, 13f)
                horizontalLineTo(15.5f)
                verticalLineTo(20f)
                horizontalLineTo(8.5f)
                close()
            }
        }.build()
        return _pluginBlocksIcon!!
    }

private var _pluginBlocksIcon: ImageVector? = null
