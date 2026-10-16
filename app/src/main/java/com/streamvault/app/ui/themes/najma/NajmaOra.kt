package com.streamvault.app.ui.themes.najma

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Episode
import com.streamvault.player.PlayerSurfaceResizeMode
import kotlin.math.sin

/** A deterministic offline sky, no asset download or fabricated provider content. */
@Composable
internal fun NajmaSky(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "najmaSky")
    val phase by transition.animateFloat(0f, 6.283185f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "twinkle")
    Canvas(modifier) {
        drawRect(Brush.linearGradient(listOf(Color(0xFF32164E), IV.Bg, Color(0xFF08162D))))
        drawCircle(Brush.radialGradient(listOf(Color(0x444D307B), Color.Transparent), Offset(size.width*.28f,size.height*.3f),size.width*.6f),size.width*.6f,Offset(size.width*.28f,size.height*.3f))
        repeat(96) { i ->
            val x = ((i*173+37)%997)/997f*size.width
            val y = ((i*269+71)%991)/991f*size.height
            val alpha = .18f + .43f*((sin(phase+i*.71f)+1f)*.5f)
            drawCircle(Color.White.copy(alpha=alpha),if(i%7==0) 1.8f else .9f,Offset(x,y))
            if(i%17==0) {
                drawLine(Color.White.copy(alpha=alpha*.6f),Offset(x-4,y),Offset(x+4,y),.8f)
                drawLine(Color.White.copy(alpha=alpha*.6f),Offset(x,y-4),Offset(x,y+4),.8f)
            }
        }
    }
}

private fun Modifier.najmaGlass() = clip(IV.R).background(IV.Glass).border(1.dp,IV.Line,IV.R)

@Composable
internal fun NajmaShell(p: ShellParams) {
    Box(p.modifier.fillMaxSize()) {
        NajmaSky(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(p.contentPadding).padding(horizontal=28.dp)) {
            if(p.topBarVisible) CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxWidth().height(82.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    IvFocus({p.onNavigate(Routes.HOME)},Modifier.width(170.dp).height(48.dp),color=Color.Transparent) {
                        Row(Modifier.align(Alignment.Center),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(33.dp).clip(RoundedCornerShape(10.dp)).background(IV.Grad),contentAlignment=Alignment.Center) { IvIcon(Icons.Outlined.PlayArrow) }
                            Text("Najma",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    IvFocus({p.onNavigate(Routes.SEARCH)},Modifier.width(290.dp).height(44.dp),shape=CircleShape,color=IV.Glass) {
                        Row(Modifier.align(Alignment.Center),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            IvIcon(Icons.Outlined.Search,size=19.dp)
                            Text(tr("Search movies, series…","ابحث عن فيلم أو مسلسل…"),color=IV.Sub,fontSize=13.sp)
                        }
                    }
                    IvRound(Icons.Outlined.Home,40.dp) {p.onNavigate(Routes.HOME)}
                    IvRound(Icons.Outlined.FavoriteBorder,40.dp) {p.onNavigate(Routes.FAVORITES)}
                    IvRound(Icons.Outlined.PersonOutline,40.dp) {p.onNavigate(Routes.SETTINGS)}
                    IvRound(Icons.Outlined.Settings,40.dp) {p.onNavigate(Routes.SETTINGS)}
                    p.topBarActions?.let { Row(content=it) }
                }
            }
            p.header?.invoke(this)
            Column(Modifier.fillMaxSize(),content=p.content)
        }
    }
}

@Composable
private fun NajmaTile(title:String, subtitle:String, icon:ImageVector, artwork:String?, modifier:Modifier,onClick:()->Unit) {
    IvFocus(onClick,modifier,scale=1.035f,color=IV.Glass) { focused ->
        artwork?.let { AsyncImage(it,null,contentScale=ContentScale.Crop,alpha=.6f,modifier=Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if(focused) IV.Purple.copy(alpha=.6f) else IV.Panel.copy(alpha=.7f),Color.Transparent,IV.Bg.copy(alpha=.8f)))))
        Column(Modifier.align(Alignment.TopCenter).padding(top=28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
            IvIcon(icon,size=42.dp)
            Text(title,color=Color.White,fontSize=26.sp,fontWeight=FontWeight.Bold)
            Text(subtitle,color=IV.Sub,fontSize=12.sp,maxLines=1)
        }
    }
}

@Composable
internal fun NajmaHome(p:DashboardParams) {
    val s=p.uiState
    val first=remember {FocusRequester()}
    LaunchedEffect(Unit) {runCatching {first.requestFocus()}}
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(Modifier.fillMaxSize().padding(top=12.dp,bottom=24.dp),horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            Column(Modifier.weight(2.4f),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    NajmaTile(tr("Live TV","البث المباشر"),"${s.stats.liveChannelCount} "+tr("channels","قناة"),Icons.Outlined.LiveTv,s.recentChannels.firstOrNull()?.logoUrl,Modifier.weight(1f).fillMaxHeight().focusRequester(first)) {p.onNavigate(Routes.LIVE_TV)}
                    NajmaTile(tr("Movies","الأفلام"),tr("Action · Drama · Comedy","أكشن · دراما · كوميديا"),Icons.Outlined.Movie,s.recentMovies.firstOrNull()?.posterUrl,Modifier.weight(1f).fillMaxHeight()) {p.onNavigate(Routes.MOVIES)}
                    NajmaTile(tr("Series","المسلسلات"),tr("Your next story","قصتك القادمة"),Icons.Outlined.VideoLibrary,s.recentSeries.firstOrNull()?.posterUrl,Modifier.weight(1f).fillMaxHeight()) {p.onNavigate(Routes.SERIES)}
                }
                Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    IvButton(tr("Favorites","المفضلة"),Icons.Outlined.FavoriteBorder,{p.onNavigate(Routes.FAVORITES)},Modifier.weight(1f),primary=false)
                    IvButton(tr("Playlists","قوائم التشغيل"),Icons.Outlined.PlaylistAdd,{p.onNavigate(Routes.SETTINGS)},Modifier.weight(1f),primary=false)
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().najmaGlass().padding(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text(tr("Continue watching","متابعة المشاهدة"),color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
                if(s.continueWatching.isEmpty()) {
                    Spacer(Modifier.weight(1f))
                    IvIcon(Icons.Outlined.History,IV.Sub,36.dp)
                    Text(tr("Your next chapter starts here","مشاهداتك القادمة تظهر هون"),color=IV.Sub,fontSize=15.sp)
                    Spacer(Modifier.weight(1f))
                }
                LazyColumn(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    items(s.continueWatching.take(12),key={it.id}) {h ->
                        IvFocus({p.onContinueWatchingItemClick(h)},Modifier.fillMaxWidth().height(88.dp),color=Color.Transparent,scale=1.02f) {
                            Row(Modifier.fillMaxSize().padding(5.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                AsyncImage(h.posterUrl,null,contentScale=ContentScale.Crop,modifier=Modifier.width(90.dp).fillMaxHeight().clip(IV.RS))
                                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                    Text(h.title,color=Color.White,fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                                    Text(h.seasonNumber?.let {"S$it · E${h.episodeNumber ?: 1}"} ?: cgDuration(h.resumePositionMs),color=IV.Sub,fontSize=11.sp)
                                    CgProgress(if(h.totalDurationMs>0) h.resumePositionMs.toFloat()/h.totalDurationMs else 0f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NajmaLive(p:LiveTvParams) {
    var focused by remember {mutableStateOf<Channel?>(null)}
    val channel=focused ?: p.previewChannel ?: p.channels.firstOrNull()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(Modifier.fillMaxSize().padding(bottom=20.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            LazyColumn(Modifier.weight(.8f).fillMaxHeight().najmaGlass().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                item {Text(tr("Categories","الأقسام"),color=Color.White,fontSize=21.sp,modifier=Modifier.padding(10.dp))}
                items(p.categories,key={it.id}) {c ->
                    IvFocus({p.onCategoryClick(c)},Modifier.fillMaxWidth().height(54.dp).focusRequester(p.categoryRequester(c.id)).cgRight(p.onRequestChannelsFromCategory),color=if(c.id==p.selectedCategoryId) IV.Purple else Color.Transparent,onLongClick={p.onCategoryLongClick(c)},onFocus={if(it)p.onCategoryFocused(c)},scale=1.02f) {
                        Row(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
                            Text(c.name,color=Color.White,fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                            Text("${c.count}",color=IV.Sub,fontSize=12.sp)
                            if(p.isCategoryLocked(c)) IvIcon(Icons.Outlined.Lock,size=15.dp)
                        }
                    }
                }
            }
            LazyColumn(Modifier.weight(1.15f).fillMaxHeight().najmaGlass().padding(12.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                item {CgSearchField(p.channelSearchQuery,p.onChannelSearchChange,tr("Search channels","بحث القنوات"))}
                if(p.channels.isEmpty()) item {Text(tr("No channels in this category","لا توجد قنوات بهذا القسم"),color=IV.Sub,modifier=Modifier.padding(12.dp))}
                items(p.channels,key={it.id}) {c ->
                    IvFocus({p.onChannelClick(c)},Modifier.fillMaxWidth().height(65.dp).focusRequester(p.channelRequester(c.id)).cgLeft {p.categoryFocusRequesters[p.selectedCategoryId]?.requestFocus();true}.cgRight(p.onRequestPreviewFromChannel),color=if(p.previewChannel?.id==c.id) IV.PurpleSoft else Color.Transparent,onLongClick={p.onChannelLongClick(c)},onFocus={if(it){focused=c;p.onChannelFocused(c)}},scale=1.02f) {
                        Row(Modifier.fillMaxSize().padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            CgLogo(c.name,if(p.isChannelLocked(c)) null else c.logoUrl,40.dp)
                            Text(c.name,color=Color.White,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                            if(c.isFavorite) IvIcon(Icons.Outlined.Favorite,IV.Purple,16.dp)
                        }
                    }
                }
            }
            Column(Modifier.weight(1.8f).fillMaxHeight().najmaGlass().padding(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                IvFocus({channel?.let(p.onChannelClick)},Modifier.fillMaxWidth().aspectRatio(16f/9f).focusRequester(p.previewFocusRequester).cgLeft(p.onRequestChannelsFromPreview),color=Color.Black,focusedColor=Color.Black,scale=1.02f) {
                    if(p.previewPlayerEngine!=null && p.previewChannel!=null) PlayerRenderView(p.previewPlayerEngine,PlayerSurfaceResizeMode.FIT,Modifier.fillMaxSize())
                    else channel?.logoUrl?.let {AsyncImage(it,null,contentScale=ContentScale.Fit,modifier=Modifier.fillMaxSize().padding(32.dp))}
                    Box(Modifier.align(Alignment.TopStart).padding(12.dp)) {IvTag("LIVE")}
                    if(p.isPreviewLoading) Text("…",color=Color.White,modifier=Modifier.align(Alignment.Center))
                }
                Text(channel?.name ?: tr("Choose a channel","اختر قناة"),color=Color.White,fontSize=26.sp,fontWeight=FontWeight.Bold,maxLines=2)
                p.previewErrorMessage?.let {Text(it,color=IV.Purple,fontSize=13.sp,maxLines=3)}
                Spacer(Modifier.weight(1f))
                IvButton(tr("Watch live","شاهد الآن"),Icons.Outlined.PlayArrow,{channel?.let(p.onChannelClick)},Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
internal fun NajmaSeries(p:SeriesDetailParams) {
    var focused by remember(p.series.id,p.selectedSeason?.seasonNumber) {mutableStateOf<Episode?>(null)}
    val episode=focused ?: p.resumeEpisode ?: p.selectedSeason?.episodes?.firstOrNull()
    Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            IvRound(Icons.Outlined.ArrowBack,40.dp,onClick=p.onBack)
            Text(p.series.name,color=Color.White,fontSize=27.sp,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
            IvRound(Icons.Outlined.FavoriteBorder,40.dp,active=p.series.isFavorite,onClick=p.onToggleFavorite)
        }
        Row(Modifier.weight(1f).padding(bottom=20.dp),horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            LazyColumn(Modifier.weight(.7f).fillMaxHeight().najmaGlass().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
                items(p.series.seasons,key={it.seasonNumber}) {season ->
                    IvChip(season.name.ifBlank {tr("Season","الموسم")+" ${season.seasonNumber}"},season.seasonNumber==p.selectedSeason?.seasonNumber,Modifier.fillMaxWidth()) {p.onSeasonSelected(season)}
                }
            }
            LazyColumn(Modifier.weight(1.2f).fillMaxHeight().najmaGlass().padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                if(p.selectedSeason?.episodes.isNullOrEmpty()) item {Text(tr("No episodes","لا توجد حلقات"),color=IV.Sub)}
                items(p.selectedSeason?.episodes.orEmpty(),key={it.id}) {e ->
                    IvFocus({p.onEpisodeClick(e)},Modifier.fillMaxWidth().height(75.dp),onLongClick={p.onCopyEpisodeUrl(e)},onFocus={if(it)focused=e},color=if(episode?.id==e.id) IV.PurpleSoft else Color.Transparent,scale=1.02f) {
                        Row(Modifier.align(Alignment.Center).fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                            Text("${e.episodeNumber}",color=IV.Sub,fontSize=18.sp)
                            Text(e.title,color=Color.White,fontSize=16.sp,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                        }
                    }
                }
            }
            Column(Modifier.weight(1.5f).fillMaxHeight().najmaGlass().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                AsyncImage(episode?.coverUrl ?: p.series.backdropUrl ?: p.series.posterUrl,null,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(IV.RS))
                Text(episode?.title ?: p.series.name,color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold,maxLines=2)
                Text(episode?.plot ?: p.series.plot.orEmpty(),color=IV.Sub,fontSize=15.sp,maxLines=4,overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.weight(1f))
                IvButton(tr("Play","تشغيل"),Icons.Outlined.PlayArrow,{episode?.let(p.onEpisodeClick)},Modifier.fillMaxWidth())
            }
        }
    }
}
