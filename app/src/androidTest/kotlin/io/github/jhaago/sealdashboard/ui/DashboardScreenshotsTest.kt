package io.github.jhaago.sealdashboard.ui

import android.graphics.Bitmap
import android.content.ContentValues
import android.provider.MediaStore
import androidx.test.filters.SdkSuppress
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Deterministic screenshot fixtures; they complement, not replace, APK launch evidence. */
@SdkSuppress(minSdkVersion = 29)
class DashboardScreenshotsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun captureBothDriveModesAllScreensAndLargeTextPortrait() {
        var host by mutableStateOf(DashboardTestHost())
        var size by mutableStateOf(Triple(1280, 720, 1f))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, size.third)) {
                host.Content(Modifier.requiredSize(size.first.dp, size.second.dp))
            }
        }
        for (style in DashboardVisualStyle.entries) {
            compose.runOnIdle {
                host = DashboardTestHost()
                repeat(400) { host.step(.1) }
                host.display = DisplaySettings(visualStyle = style)
                size = Triple(1280, 720, 1f)
            }
            val prefix = style.name.lowercase()
            if (style == DashboardVisualStyle.LEGACY_HMI) {
                capture("$prefix-overview")
                compose.onNodeWithTag("legacy-page-CHARGE").performClick()
                capture("$prefix-charge")
                compose.onNodeWithTag("legacy-page-TYRES").performClick()
                capture("$prefix-tyres")
                compose.onNodeWithTag("legacy-page-PROFILE").performClick()
                capture("$prefix-profile")
                compose.onNodeWithTag("nav-DRIVE").performClick()
            }
            capture("$prefix-drive-companion")
            compose.runOnIdle { host.display = host.display.copy(layout = DriveLayout.FULL) }
            capture("$prefix-drive-full")
            compose.runOnIdle {
                host.command(MockCommand.SetGear(Gear.PARK))
                host.command(MockCommand.SetDoor(Door.FRONT_LEFT, true))
                host.command(MockCommand.SetTyrePressureKpa(Wheel.REAR_RIGHT, 235.0))
                host.ui = host.ui.copy(destination = DashboardDestination.VEHICLE)
            }
            capture("$prefix-vehicle")
            compose.runOnIdle {
                host.command(MockCommand.SetDoor(Door.FRONT_LEFT, false))
                host.command(MockCommand.SetGear(Gear.DRIVE))
                host.command(MockCommand.SetTargetSpeedKmh(60.0))
                repeat(100) { host.step(.1) }
                host.command(MockCommand.SetTargetSpeedKmh(0.0))
                repeat(30) { host.step(.1) }
                host.ui = host.ui.copy(destination = DashboardDestination.ENERGY)
            }
            capture("$prefix-energy")
            compose.runOnIdle { host.ui = host.ui.copy(destination = DashboardDestination.DEVELOPMENT) }
            capture("$prefix-development")
            compose.runOnIdle { host.ui = host.ui.copy(destination = DashboardDestination.DRIVE); size = Triple(600, 960, 1.3f) }
            capture("$prefix-drive-portrait-large-text")
            compose.runOnIdle { size = Triple(400, 720, 1.3f) }
            capture("$prefix-drive-compact-large-text")
            if (style != DashboardVisualStyle.LEGACY_HMI) {
                compose.onNodeWithTag("street-map").performScrollTo()
                capture("$prefix-map-compact-large-text")
            }
            compose.runOnIdle {
                host.ui = host.ui.copy(destination = DashboardDestination.ASSISTANT)
                size = Triple(1280,720,1f)
                host.assistant.submit("Find a charger on my way")
            }
            if (compose.onAllNodesWithTag("assistant-input").fetchSemanticsNodes().isEmpty())
                compose.onNodeWithTag("assistant-driving-toggle").performScrollTo().performClick()
            capture("$prefix-assistant-parked")
            compose.onNodeWithTag("assistant-driving-toggle").performScrollTo().performClick()
            capture("$prefix-assistant-driving")
            compose.runOnIdle { size = Triple(400,720,1.3f) }
            capture("$prefix-assistant-compact-driving")
        }
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("dashboard-root").captureToImage().asAndroidBitmap()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "screenshots")
        assertTrue(directory.isDirectory || directory.mkdirs())
        File(directory, "$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        // Public test media survives AGP's uninstall cleanup; no storage permission is requested.
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SealDashboardEvaluation")
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        requireNotNull(resolver.openOutputStream(uri)).use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
}
