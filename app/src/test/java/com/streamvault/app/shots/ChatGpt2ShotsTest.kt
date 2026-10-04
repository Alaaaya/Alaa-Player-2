package com.streamvault.app.shots

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.screens.dashboard.*
import com.streamvault.app.ui.screens.player.*
import com.streamvault.app.ui.screens.search.*
import com.streamvault.app.ui.screens.favorites.*
import com.streamvault.app.ui.screens.epg.GuideDensity
import com.streamvault.app.ui.themes.chatgpt2.Cg2Recent
import com.streamvault.app.ui.theme.StreamVaultTheme
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.*
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "ar-w960dp-h540dp-xhdpi", application = android.app.Application::class)
class ChatGpt2ShotsTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
    private val theme = AppHomeTheme.CHAT_GPT_2
    private val ui get() = bespokeThemeFor(theme)!!
    private val now = System.currentTimeMillis()
    private val art = "/home/kit/shots-art"
    private fun poster(i: Int) = "file://$art/p${i % 16 + 1}.jpg"
    private fun back(i: Int) = "file://$art/b${i % 5 + 1}.jpg"
    private fun logo(i: Int) = "file://$art/l${i % 12}.png"

    @Before fun images() {
        SingletonImageLoader.setUnsafe { ctx ->
            ImageLoader.Builder(ctx).coroutineContext(Dispatchers.Unconfined).build()
        }
    }

    private val cats = listOf("رياضة", "أخبار", "عربي", "أطفال", "أفلام", "وثائقي", "مسلسلات", "موسيقى")
        .mapIndexed { i, n -> Category(id = i + 1L, name = n, count = 24 + i * 9) }
    private fun prog(ch: String, t: String, s: Long, e: Long) = Program(channelId = ch, title = t, startTime = s, endTime = e, hasArchive = true)
    private val chNames = listOf(
        "MBC 1 HD" to "عرب أيدول", "beIN Sports 1 HD" to "الدوري الإنجليزي: آرسنال × تشيلسي", "الجزيرة" to "نشرة الأخبار", "SSC 1 4K" to "دوري روشن",
        "روتانا سينما" to "فيلم السهرة", "سبيستون" to "المحقق كونان", "العربية" to "بانوراما", "دبي ون" to "برنامج المساء",
        "ناشيونال جيوغرافيك" to "الجزيرة العربية البرية", "OSN Movies" to "فيلم أكشن", "CBC" to "باب الحارة", "LBC" to "توك شو")
    private val channels = chNames.mapIndexed { i, (n, p) ->
        val k = "ch$i"
        Channel(id = i + 1L, name = n, number = i + 1, logoUrl = logo(i), epgChannelId = k, categoryId = cats[i % 4].id, categoryName = cats[i % 4].name,
            catchUpSupported = i % 3 == 0, catchUpDays = if (i % 3 == 0) 7 else 0, isFavorite = i % 4 == 1,
            currentProgram = prog(k, p, now - 25 * 60000, now + 35 * 60000), nextProgram = prog(k, "التالي: استوديو التحليل", now + 35 * 60000, now + 95 * 60000))
    }
    private val movieNames = listOf("الكنز", "كيرة والجن", "ولاد رزق", "الفيل الأزرق", "الممر", "الخلية", "صندوق الدنيا", "الحريفة",
        "بيت الروبي", "السرب", "شقو", "أهل الكهف", "الهوى سلطان", "تراب الماس", "الجزيرة", "واحد صحيح")
    private val genres = listOf("أكشن", "دراما", "كوميدي", "خيال علمي", "رعب", "عائلي")
    private val movies = movieNames.mapIndexed { i, n -> Movie(id = i + 1L, name = n, posterUrl = poster(i), backdropUrl = back(i), year = "${2010 + i % 15}",
        rating = 6.5f + (i % 4) * 0.7f, genre = genres[i % genres.size], categoryName = genres[i % genres.size], categoryId = 100L + i % genres.size,
        duration = "2h ${10 + i}m", director = "مروان حامد", cast = "أحمد عز، كريم عبد العزيز، منى زكي",
        plot = "قصة ملحمية عن الشجاعة والعائلة والمصير، تدور أحداثها بين القاهرة والصحراء في مغامرة لا تُنسى.") }
    private val seriesNames = listOf("باب الحارة", "الهيبة", "الاختيار", "جعفر العمدة", "رسالة الإمام", "سفر برلك", "الحشاشين", "تاج", "العتاولة", "نسل الأغراب")
    private val episodes = (1..8).map { Episode(id = it.toLong(), title = "الحلقة $it", episodeNumber = it, seasonNumber = 1, coverUrl = "file://$art/e${(it - 1) % 6 + 1}.jpg",
        duration = "45:00", durationSeconds = 2700, plot = "تتصاعد الأحداث مع عودة جبل إلى الضيعة.", watchProgress = if (it < 3) 2700_000L else 0L) }
    private val seasons = (1..3).map { Season(seasonNumber = it, name = "الموسم $it", episodes = if (it == 1) episodes else emptyList(), episodeCount = 8) }
    private val series = seriesNames.mapIndexed { i, n -> Series(id = i + 1L, name = n, posterUrl = poster(i + 5), backdropUrl = back(i + 2), genre = genres[i % 6],
        categoryName = genres[i % 6], categoryId = 200L + i % 6, rating = 7.2f + (i % 3) * 0.5f, releaseDate = "${2015 + i % 9}",
        plot = "دراما عائلية مشوّقة عن الصراع على السلطة والنفوذ في قرية حدودية.", seasons = seasons) }
    private val history = movies.take(6).mapIndexed { i, m -> PlaybackHistory(id = i + 1L, contentId = m.id, contentType = ContentType.MOVIE, providerId = 1, title = m.name,
        posterUrl = m.posterUrl, streamUrl = "", resumePositionMs = (20 + i * 15) * 60000L, totalDurationMs = 140 * 60000L, lastWatchedAt = now - i * 3600000L) }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.setContent { StreamVaultTheme(appHomeTheme = theme) { Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0C))) { content() } } }
        repeat(6) { rule.mainClock.advanceTimeBy(500); Thread.sleep(150); rule.waitForIdle() }
        var bmp: Bitmap? = null
        rule.runOnUiThread {
            val v = rule.activity.window.decorView.rootView
            val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(android.graphics.Canvas(b)); bmp = b
        }
        val out = File("/home/kit/chatgpt2-shots/$name.png"); out.parentFile!!.mkdirs()
        out.outputStream().use { bmp!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Composable private fun shell(route: String, title: String, body: @Composable () -> Unit) {
        ui.Shell(ShellParams(route, {}, title, null, true, false, null, null, PaddingValues(0.dp0()), Modifier.fillMaxSize()) { body() })
    }
    private fun Int.dp0() = androidx.compose.ui.unit.Dp(this.toFloat())

    @Test fun a1_home() = shot("1_home") {
        shell(Routes.HOME, "الرئيسية") {
            ui.Dashboard(DashboardParams(
                DashboardUiState(homeTheme = theme, favoriteChannels = channels.filter { it.isFavorite }, recentChannels = channels.take(6),
                    continueWatching = history, continueWatchingMovies = history, recentMovies = movies, topRatedMovies = movies.reversed(),
                    recommendedMovies = movies.shuffled(java.util.Random(1)), recentSeries = series, liveCategories = cats, lastLiveCategory = cats[0],
                    feature = DashboardFeature("الكنز", "مغامرة ملحمية", back(0), "مشاهدة الآن", DashboardFeatureAction.MOVIES),
                    isLoading = false),
                emptySet(), emptySet(), {}, { _, _ -> }, { _, _ -> }, {}, {}, {}))
        }
    }

    @Test fun a2_live() = shot("2_live") {
        val fr = remember { mutableMapOf<Long, FocusRequester>() }; val fr2 = remember { mutableMapOf<Long, FocusRequester>() }
        shell(Routes.LIVE_TV, "مباشر") {
            ui.LiveTv(LiveTvParams("Alaa IPTV", cats, cats[0].id, "", "", channels, channels[0], null, false, null, { false }, { false },
                fr, fr2, remember { FocusRequester() }, {}, {}, {}, {}, {}, {}, {}, {}, { false }, { false }, { false }))
        }
    }

    private fun <T> lib(items: List<T>, genreOf: (T) -> String, base: Long, type: ContentType) = LibraryState(
        items.groupBy(genreOf), genres, genres.associateWith { g -> items.count { genreOf(it) == g } }, items.size,
        genres.mapIndexed { i, g -> Category(id = base + i, name = g, type = type, count = 3) }, null, emptyList(), false, false, false,
        "", emptyList(), LibraryFilterType.ALL, LibrarySortBy.LIBRARY, history)

    @Test fun a3_movies() = shot("3_movies") {
        shell(Routes.MOVIES, "أفلام") {
            ui.Movies(LibraryParams(lib(movies, { it.genre!! }, 100L, ContentType.MOVIE), remember { FocusRequester() }, { false }, { false }, {}, {}, {}, {}, {}, {}, {}, {}, {}))
        }
    }

    @Test fun a4_series() = shot("4_series") {
        shell(Routes.SERIES, "مسلسلات") {
            ui.Series(LibraryParams(lib(series, { it.genre!! }, 200L, ContentType.SERIES), remember { FocusRequester() }, { false }, { false }, {}, {}, {}, {}, {}, {}, {}, {}, {}))
        }
    }

    @Test fun a5_movie_details() = shot("5_movie_details") {
        ui.MovieDetail(MovieDetailParams(movies[0], true, 42 * 60000L, false, movies.drop(1).take(8), {}, {}, {}, {}, {}, {}, {}, {}, {}))
    }

    @Test fun a6_series_details() = shot("6_series_details") {
        ui.SeriesDetail(SeriesDetailParams(series[0], seasons[0], episodes[2], 6, false, ExternalRatings(), false, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}))
    }

    @Test fun a7_player() = shot("7_player") {
        val c = channels[1]
        Box(Modifier.fillMaxSize()) {
            AsyncImage(back(3), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            ui.PlayerOverlay(PlayerOverlayParams(
                true, c.name, "LIVE", false, true, c.currentProgram, c, c.name, 2, 25 * 60000L, 60 * 60000L, "ملء",
                2, false, 3, 4, null, false, 1f, c.currentProgram?.title, SleepTimerUiState(), PlayerTimeshiftUiState(),
                remember { FocusRequester() }, remember { FocusRequester() }, Modifier.fillMaxSize(),
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, false, false, {}, {}, {}, {}, false, {}, {}, {},
                {}, {}, SeekPreviewState(), {}, {}, nextProgram = c.nextProgram, resolutionBadgeLabel = "1080p"))
        }
    }

    @Test fun a8_player_list() = shot("8_player_channels") {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(back(3), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            ui.LiveChannelList(LiveChannelListParams(channels, emptyList(), 2, remember { FocusRequester() }, "رياضة", {}, {}, {}, {}, {}, {}))
        }
    }

    @Test fun a9_search() = shot("9_search") {
        shell(Routes.SEARCH, "بحث") {
            ui.Search(SearchParams("الك", SearchTab.ALL, listOf("الهيبة", "beIN"), SearchUiState(channels = channels.take(3), movies = movies.take(8),
                series = series.take(6), hasSearched = true, hasActiveProvider = true, queryLength = 3),
                emptySet(), emptySet(), remember { FocusRequester() }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, { false }, { false }, { false }))
        }
    }

    @Test fun b1_vod_player() = shot("10_vod_player") {
        val m = movies[1]
        Box(Modifier.fillMaxSize()) {
            AsyncImage(back(1), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            ui.PlayerOverlay(PlayerOverlayParams(
                true, m.name, "", false, false, null, null, "", 0, 52 * 60000L, 130 * 60000L, "ملء",
                2, false, 3, 4, null, false, 1f, null, SleepTimerUiState(), PlayerTimeshiftUiState(),
                remember { FocusRequester() }, remember { FocusRequester() }, Modifier.fillMaxSize(),
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, false, false, {}, {}, {}, {}, false, {}, {}, {},
                {}, {}, SeekPreviewState(), {}, {}, resolutionBadgeLabel = "4K"))
        }
    }

    private val favItems = channels.take(4).map { FavoriteUiModel(Favorite(providerId = 1, contentId = it.id, contentType = ContentType.LIVE), it.name, it.categoryName) } +
        movies.take(4).map { FavoriteUiModel(Favorite(providerId = 1, contentId = it.id, contentType = ContentType.MOVIE), it.name, it.genre) } +
        series.take(3).map { FavoriteUiModel(Favorite(providerId = 1, contentId = it.id, contentType = ContentType.SERIES), it.name, it.genre) }
    private fun favParams() = FavoritesParams(
        listOf(FavoriteSectionUiModel("live", "القنوات", "", favItems.take(4), true), FavoriteSectionUiModel("vod", "الأفلام والمسلسلات", "", favItems.drop(4), true)),
        history.map { SavedHistoryUiModel(it, it.title, "فيلم", 1) },
        channels.take(6).mapIndexed { i, c -> SavedHistoryUiModel(PlaybackHistory(id = 100L + i, contentId = c.id, contentType = ContentType.LIVE, providerId = 1, title = c.name, posterUrl = c.logoUrl, streamUrl = "", lastWatchedAt = now - i * 1800000L), c.name, c.categoryName ?: "", 1) },
        SavedLibraryPreset.ALL_SAVED, SavedLibraryFilter.ALL, SavedLibrarySort.entries.first(), {}, {}, {}, {}, {}, {})

    @Test fun b2_favorites() = shot("11_favorites") {
        Cg2Recent.active = false
        shell(Routes.FAVORITES, "المفضلة") { ui.Favorites(favParams()) }
    }

    @Test fun b3_recent() = shot("12_recent") {
        Cg2Recent.active = true
        shell(Routes.FAVORITES, "المشاهدة الأخيرة") { ui.Favorites(favParams()) }
    }

    @Test fun b4_settings() = shot("13_settings") {
        shell(Routes.SETTINGS, "الإعدادات") {
            ui.SettingsFrame({
                ui.SettingsNav(SettingsNavParams(listOf("قوائم التشغيل" to "إدارة المزودين", "المشغل" to "الترميز والتخزين المؤقت", "دليل البرامج EPG" to "المصادر والتحديث",
                    "الرقابة الأبوية" to "قفل الفئات", "المظهر" to "الثيمات", "اللغة" to "العربية", "التسجيل" to "المساحة والجدولة", "النسخ الاحتياطي" to "استيراد وتصدير",
                    "الجهاز / اللوحة" to "رمز الجهاز", "حول" to "الإصدار 1.7"), 0, remember { FocusRequester() }, {}))
            }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.0.dp0())) {
                    androidx.tv.material3.Text("قوائم التشغيل", color = Color.White, fontSize = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp))
                    listOf("Alaa IPTV  •  Xtream  •  نشط", "Backup M3U  •  M3U  •  آخر تحديث اليوم").forEach {
                        androidx.tv.material3.Text(it, color = Color(0xFFB8B8C0))
                    }
                }
            }
        }
    }

    @Test fun b5_guide() = shot("14_guide") {
        val ws = now - 30 * 60000L
        val pm = channels.associate { c -> c.epgChannelId!! to (0 until 6).map { j -> prog(c.epgChannelId!!, listOf("نشرة الأخبار", "مباراة مباشرة", "فيلم السهرة", "برنامج صباحي", "وثائقي", "مسلسل")[(j + c.id.toInt()) % 6], now - 25 * 60000L + (j - 0) * 50 * 60000L - 50 * 60000L, now - 25 * 60000L + j * 50 * 60000L) } }
        shell(Routes.EPG, "الدليل") {
            ui.Epg(EpgParams("رياضة", null, false, channels[1], channels[1].currentProgram, false, channels, setOf(2L), pm, ws, ws + 4 * 3600000L, GuideDensity.entries.first(),
                {}, {}, {}, {}, {}, {}, { _, _ -> }, { _, _ -> }, { _, _, _ -> }, { _, _, _ -> }, {}), Modifier.fillMaxSize())
        }
    }
}
