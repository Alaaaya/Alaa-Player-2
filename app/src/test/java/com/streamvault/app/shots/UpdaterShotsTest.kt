package com.streamvault.app.shots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.streamvault.app.ui.theme.StreamVaultTheme
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.update.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Calls the unchanged production composable; only its network/download state is supplied. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "ar-rSA-ldrtl-w960dp-h540dp-xhdpi", application = android.app.Application::class)
class UpdaterShotsTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private fun shot(name: String, downloading: Boolean) {
        val model = mock<StartupUpdateViewModel>()
        val release = GitHubReleaseInfo("2.1", 2100, "https://example.org", null, null, "", null)
        whenever(model.state).thenReturn(MutableStateFlow(StartupUpdateState(release, accepted = downloading)))
        whenever(model.downloadState).thenReturn(MutableStateFlow(
            AppUpdateDownloadState(
                status = if (downloading) AppUpdateDownloadStatus.Downloading else AppUpdateDownloadStatus.Idle,
                versionName = "2.1",
                progress = if (downloading) 0.42f else null
            )
        ))
        rule.setContent {
            StreamVaultTheme {
                Box(Modifier.fillMaxSize().background(AppColors.Canvas)) {
                    StartupUpdateDialog(model)
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1000)
        rule.waitForIdle()
        rule.onNodeWithText("تحديث جديد 2.1 متوفر").assertExists()
        if (downloading) rule.onNodeWithText("جارٍ تنزيل التحديث…").assertExists()
        else rule.onNodeWithText("تحديث", useUnmergedTree = true).assertExists()

        // Compose dialogs are separate Android windows. Capture the real dialog window
        // and place it at its real centered position over the activity window and dim layer.
        val dialog = rule.onNode(isDialog()).captureToImage().asAndroidBitmap()
        var background: Bitmap? = null
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            check(view.width == 1920 && view.height == 1080) { "Unexpected TV size ${view.width}x${view.height}" }
            background = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(background!!))
        }
        val result = background!!
        Canvas(result).apply {
            drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(),
                Paint().apply { color = android.graphics.Color.argb(82, 0, 0, 0) })
            drawBitmap(dialog, (result.width - dialog.width) / 2f, (result.height - dialog.height) / 2f, null)
        }
        val out = File("build/outputs/updater-shots/updater_shot_${name}.png")
        check(out.parentFile!!.isDirectory || out.parentFile!!.mkdirs())
        out.outputStream().use { result.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun available() = shot("available", false)
    @Test fun downloading() = shot("downloading", true)
}
