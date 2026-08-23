package com.streamvault.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.R
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.rememberCrossfadeImageModel
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import com.streamvault.domain.model.Category

// بيانات مؤقتة لتمثيل الفئات الملونة بالصورة (استبدلها ببيانات من ViewModel إذا توفرت)
private data class LiveCategoryModel(
    val id: Long,
    val name: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color
)

private val liveCategories = listOf(
    LiveCategoryModel(1, "All Channels", 1250, Icons.Default.Tv, Color(0xFFE91E63)), // أحمر
    LiveCategoryModel(2, "Sports", 238, Icons.Default.SportsSoccer, Color(0xFF1E88E5)), // أزرق
    LiveCategoryModel(3, "News", 184, Icons.Default.Newspaper, Color(0xFF00ACC1)), // سماوي
    LiveCategoryModel(4, "Movies", 356, Icons.Default.Movie, Color(0xFFAB47BC)), // بنفسجي
    LiveCategoryModel(5, "Kids", 95, Icons.Default.Face, Color(0xFFFF7043)), // برتقالي
    LiveCategoryModel(6, "Documentary", 132, Icons.Default.Public, Color(0xFF26A69A)) // أخضر مائل للأزرق
)

@Composable
internal fun AlaaDashboard(
    uiState: DashboardUiState,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onNavigate: (String) -> Unit,
    onRecentChannelClick: (Channel, Long?) -> Unit,
    onFavoriteChannelClick: (Channel, Long?) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onPlaybackHistoryClick: (PlaybackHistory) -> Unit,
    onContinueWatchingItemClick: (PlaybackHistory) -> Unit
) {
    val isSelectedRoute = remember { mutableStateOf("Live TV") } // هنا تحدد الصفحة النشطة بناءً على الـ Navigation

    Row(modifier = Modifier.fillMaxSize()) {
        // 1. الشريط الجانبي (Sidebar)
        AlaaSidebar(
            currentRoute = isSelectedRoute.value,
            onRouteClick = { route -> 
                isSelectedRoute.value = route 
                onNavigate(route) 
            }
        )

        // 2. المحتوى الرئيسي
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section (مطابق للصورة)
            item(key = "hero") {
                AlaaHero(
                    title = "Live TV",
                    summary = "Watch 1000+ Live Channels",
                    artworkUrl = uiState.feature.artworkUrl,
                    onWatchNow = { onNavigate(Routes.LIVE_TV) }
                )
            }

            // Live Categories Section (الصف الملون)
            item(key = "categories") {
                AlaaLiveCategories(
                    title = "Live Categories",
                    categories = liveCategories, // هنا تمرر القائمة من الـ ViewModel
                    onCategoryClick = { onNavigate(Routes.LIVE_TV) }
                )
            }

            // Continue Watching Section
            if (uiState.continueWatching.isNotEmpty()) {
                item(key = "continue_watching") {
                    ContinueWatchingSection(
                        items = uiState.continueWatching,
                        onItemClick = onContinueWatchingItemClick
                    )
                }
            }

            // باقي الأقسام (الموجودة في الكود السابق مثل Recent Movies, Top Rated...)
            if (uiState.recentMovies.isNotEmpty()) {
                item(key = "recent_movies") {
                    // ... استخدم المكونات القديمة هنا مع تعديل الطابع البصري
                    Text(text = stringResource(R.string.dashboard_recent_movies), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    // MovieCard(...)
                }
            }
            // ...
        }
    }
}

// ------------------ مكونات التصميم الجديد ------------------

@Composable
private fun AlaaSidebar(currentRoute: String, onRouteClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color(0xFF0A0B10))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // اللوجو
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "ibo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = " TV", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF304A))
            }
            Spacer(modifier = Modifier.height(32.dp))

            // عناصر القائمة
            SidebarItem(icon = Icons.Default.Tv, label = "Live TV", isSelected = currentRoute == "Live TV") { onRouteClick(Routes.LIVE_TV) }
            SidebarItem(icon = Icons.Default.Movie, label = "Movies", isSelected = currentRoute == "Movies") { onRouteClick(Routes.MOVIES) }
            SidebarItem(icon = Icons.Default.List, label = "Series", isSelected = currentRoute == "Series") { onRouteClick(Routes.SERIES) }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SidebarItem(icon = Icons.Default.Favorite, label = "Favorites", isSelected = false) { onRouteClick(Routes.FAVORITES) }
            SidebarItem(icon = Icons.Default.History, label = "Recently Watched", isSelected = false) { onRouteClick(Routes.RECENT) }
            SidebarItem(icon = Icons.Default.Category, label = "Categories", isSelected = false) { onRouteClick(Routes.CATEGORIES) }
            
            Spacer(modifier = Modifier.height(24.dp))

            SidebarItem(icon = Icons.Default.Settings, label = "Settings", isSelected = false) { onRouteClick(Routes.SETTINGS) }
            SidebarItem(icon = Icons.Default.SwapHoriz, label = "Change Server", isSelected = false) { onRouteClick(Routes.SERVER) }
        }

        // الوقت والتاريخ في الأسفل
        val currentTime = remember { mutableStateOf(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))) }
        val currentDate = remember { mutableStateOf(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))) }
        
        Column {
            Text(text = currentTime.value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = currentDate.value, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SidebarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val background = if (isSelected) Color(0xFFFF304A).copy(alpha = 0.2f) else Color.Transparent
    val border = if (isSelected) Color(0xFFFF304A) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .then(if (isSelected) Modifier.border(1.dp, border, RoundedCornerShape(8.dp)) else Modifier)
            .focusable()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) Color(0xFFFF304A) else Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.titleMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AlaaHero(title: String, summary: String, artworkUrl: String?, onWatchNow: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF080B14), Color(0xFF151B2D))))
    ) {
        if (!artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = rememberCrossfadeImageModel(artworkUrl),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        // Overlay
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(0xFF06080F).copy(alpha = 0.95f), Color.Transparent)))) 
        
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 40.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(text = summary, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TvButton(
                onClick = onWatchNow,
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFFFF304A),
                    contentColor = Color.White,
                    focusedContainerColor = Color(0xFFFF5267)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text(text = "Watch Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AlaaLiveCategories(title: String, categories: List<LiveCategoryModel>, onCategoryClick: (LiveCategoryModel) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "View All", color = Color(0xFFFF304A), modifier = Modifier.focusable().clickable { })
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(categories) { category ->
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(category.color.copy(alpha = 0.2f))
                        .focusable()
                        .clickable { onCategoryClick(category) }
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Icon(category.icon, contentDescription = null, tint = category.color)
                        Column {
                            Text(text = category.name, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = category.count.toString(), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(items: List<PlaybackHistory>, onItemClick: (PlaybackHistory) -> Unit) {
    Column {
        Text(text = "Continue Watching", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .focusable()
                        .clickable { onItemClick(item) }
                ) {
                    AsyncImage(
                        model = rememberCrossfadeImageModel(item.posterUrl),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    // Gradient overlay
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
                    
                    // زر التشغيل
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 12.dp).size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                    }

                    // النص
                    Column(Modifier.align(Alignment.BottomStart).padding(start = 56.dp, bottom = 14.dp)) {
                        Text(text = item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val subtitle = if (item.seasonNumber != null) "S${item.seasonNumber} - Episode ${item.episodeNumber}" else (item.year ?: "Movie")
                        Text(text = subtitle, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    }

                    // شريط التقدم أسفل البطاقة
                    LinearProgressIndicator(
                        progress = { 0.5f }, // هنا توضع نسبة التقدم من الـ PlaybackHistory
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                        color = Color(0xFFFF304A),
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
