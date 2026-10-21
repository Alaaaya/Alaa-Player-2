package com.streamvault.app.shots
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.streamvault.app.ui.theme.StreamVaultTheme
import com.streamvault.app.ui.themes.thalj.ThaljUi
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.app.ui.screens.dashboard.*
import com.streamvault.app.ui.screens.player.*
import com.streamvault.app.ui.screens.settings.*
import com.streamvault.domain.model.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.mockito.kotlin.mock
import java.io.File
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34], qualifiers="ar-rSA-ldrtl-w960dp-h540dp-xhdpi", application=android.app.Application::class)
class ThaljShotsTest {
 @get:Rule val rule=createAndroidComposeRule<androidx.activity.ComponentActivity>()
 private val art: String get() = File("src/test/resources/thalj-art.jpg").absoluteFile.toURI().toString()
 private val films = listOf("رحلة إلى الشمال 4K", "المدينة الأخيرة", "خلف الأفق", "الطريق إلى البحر", "ليلة في برلين", "الحلم البعيد", "عالم جديد", "بين النجوم").mapIndexed { i,n -> Movie(id=i+1L,name=n,posterUrl=art,backdropUrl=art,year="2026",rating=8.4f,genre="مغامرة، دراما",duration="2h 14m",plot="رحلة استثنائية عبر الجبال والبحار بحثاً عن بداية جديدة. بيانات تجريبية لعرض واجهة التطبيق.",director="مخرج تجريبي",cast="فريق العرض التجريبي") }
 private val shows = films.map { Series(id=it.id,name=it.name,posterUrl=art,backdropUrl=art,rating=8.7f,releaseDate="2026",plot=it.plot,genre="دراما") }
 private val channels = listOf("الأخبار HD", "الرياضة FHD", "الوثائقية 4K", "السينما HD", "العائلة HD", "الموسيقى", "السفر", "الثقافة").mapIndexed {i,n -> Channel(id=i+1L,name=n,number=i+1,categoryId=1,groupTitle="قنوات عربية",isFavorite=i<3) }
 private val cats = listOf("قنوات عربية", "رياضة", "أخبار", "أفلام", "وثائقيات", "أطفال").mapIndexed { i,n -> Category(id=i+1L,name=n,count=12) }
 private fun <T> library(items:List<T>):LibraryParams<T> = LibraryParams(LibraryState(mapOf("الكل" to items),listOf("الكل"),mapOf("الكل" to items.size),items.size,cats,null,items,false,false,false,"",items,LibraryFilterType.ALL,LibrarySortBy.LIBRARY,emptyList()),FocusRequester(),{false},{false},{},{},{},{},{},{},{},{},{})
 private fun capture(name:String, route:String="home", screen:@Composable ()->Unit) {
  rule.setContent { StreamVaultTheme(AppHomeTheme.THALJ) {
   if(route=="player") Box(Modifier.fillMaxSize().background(Color(0xFF112A3A))) { screen() }
   else ThaljUi.Shell(ShellParams(route,{},"",null,true,false,null,null,PaddingValues(0.dp),Modifier) { screen() })
  } }
  rule.waitForIdle(); rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()
  var bitmap:Bitmap?=null
  rule.runOnUiThread { val v=rule.activity.window.decorView; check(v.width==1920 && v.height==1080) { "Wrong size ${v.width}x${v.height}" }; bitmap=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(bitmap!!)) }
  val out=File("build/outputs/thalj-shots/$name.png");out.parentFile!!.mkdirs();out.outputStream().use {bitmap!!.compress(Bitmap.CompressFormat.PNG,100,it)}
 }
 @Test fun home() = capture("01-home") { ThaljUi.Dashboard(DashboardParams(
uiState = DashboardUiState(isLoading=false,recommendedMovies=films,recentMovies=films,recentSeries=shows,favoriteChannels=channels, recentChannels=channels),
recordingChannelIds = emptySet(),
scheduledChannelIds = emptySet(),
onNavigate = {},
onRecentChannelClick = {},
onFavoriteChannelClick = {},
onMovieClick = {},
onSeriesClick = {},
onContinueWatchingItemClick = {}
)) }
 @Test fun live() = capture("02-live", "live_tv") { ThaljUi.LiveTv(LiveTvParams(
sourceTitle = "قائمة تجريبية",
categories = cats,
selectedCategoryId = 1L,
categorySearchQuery = "",
channelSearchQuery = "",
channels = channels,
previewChannel = channels.first(),
previewPlayerEngine = null,
isPreviewLoading = false,
previewErrorMessage = null,
isCategoryLocked = { false },
isChannelLocked = { false },
categoryFocusRequesters = mutableMapOf(),
channelFocusRequesters = mutableMapOf(),
previewFocusRequester = FocusRequester(),
onCategorySearchChange = {},
onChannelSearchChange = {},
onCategoryClick = {},
onCategoryLongClick = {},
onChannelClick = {},
onChannelLongClick = {},
onCategoryFocused = {},
onChannelFocused = {},
onRequestChannelsFromCategory = { false },
onRequestPreviewFromChannel = { false },
onRequestChannelsFromPreview = { false }
)) }
 @Test fun movies() = capture("03-movies", "movies") { ThaljUi.Movies(library(films)) }
 @Test fun series() = capture("04-series", "series") { ThaljUi.Series(library(shows)) }
 @Test fun details() = capture("05-details", "movie/1") { ThaljUi.MovieDetail(MovieDetailParams(
movie = films.first(),
hasResume = false,
resumePositionMs = 0L,
isCasting = false,
relatedContent = films.drop(1),
onPlay = {},
onCopyUrl = {},
onDownload = {},
onCast = {},
onToggleFavorite = {},
onSelectVariant = {},
onRelatedClick = {},
onBack = {},
onPlayTrailer = {}
)) }
 @Test fun player() = capture("06-player", "player") { ThaljUi.PlayerOverlay(PlayerOverlayParams(
visible = true,
title = "رحلة إلى الشمال",
contentType = "MOVIE",
isCatchUpPlayback = false,
isPlaying = true,
currentProgram = null,
currentChannel = null,
currentChannelName = null,
displayChannelNumber = 0,
currentPosition = 1240000L,
duration = 8040000L,
aspectRatioLabel = "16:9",
subtitleTrackCount = 3,
liveTranslationAvailable = false,
audioTrackCount = 2,
videoQualityCount = 3,
currentRecordingStatus = null,
isMuted = false,
playbackSpeed = 1f,
mediaTitle = "رحلة إلى الشمال",
sleepTimerUiState = SleepTimerUiState(),
timeshiftUiState = PlayerTimeshiftUiState(),
playButtonFocusRequester = FocusRequester(),
quickActionsFocusRequester = FocusRequester(),
modifier = Modifier,
onClose = {},
onTogglePlayPause = {},
onSeekBackward = {},
onSeekForward = {},
onRestartProgram = {},
onOpenArchive = {},
onStartRecording = {},
onStopRecording = {},
onScheduleRecording = {},
onScheduleDailyRecording = {},
onScheduleWeeklyRecording = {},
onToggleAspectRatio = {},
onOpenSubtitleTracks = {},
onOpenAudioTracks = {},
onOpenVideoTracks = {},
onOpenPlaybackSpeed = {},
onOpenStopPlaybackTimer = {},
onOpenIdleStandbyTimer = {},
onOpenAudioVideoSync = {},
audioVideoSyncEnabled = false,
showEpisodesAction = false,
onOpenEpisodes = {},
onOpenSplitScreen = {},
onEnterPictureInPicture = {},
onToggleMute = {},
isCastConnected = false,
onCast = {},
onStopCasting = {},
onSeekToLiveEdge = {},
onSeekToPosition = {},
onSetScrubbingMode = {},
seekPreview = SeekPreviewState(),
onSeekPreviewPositionChanged = {},
onUserInteraction = {},
resolutionBadgeLabel = "4K HDR"
)) }
 @Test fun settings() = capture("07-settings", "settings") { ThaljUi.SettingsFrame(navigation={ThaljUi.SettingsNav(SettingsNavParams(
entries = listOf("قوائم التشغيل" to "المزودون والحسابات", "التشغيل" to "الفيديو والصوت", "المظهر" to "الثيمات", "اللغة" to "العربية", "حول التطبيق" to "Alaa Player 2.1"),
selectedCategory = 2,
focusRequester = FocusRequester(),
onCategorySelected = {}
))},content={ LazyColumn { settingsThemesSection(SettingsUiState(appHomeTheme=AppHomeTheme.THALJ), mock<SettingsViewModel>()) } }) }
}
