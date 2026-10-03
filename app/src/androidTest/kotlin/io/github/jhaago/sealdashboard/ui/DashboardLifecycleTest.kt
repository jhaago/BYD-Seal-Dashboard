package io.github.jhaago.sealdashboard.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.SystemClock
import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import io.github.jhaago.sealdashboard.DashboardApplication
import io.github.jhaago.sealdashboard.MainActivity
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import io.github.jhaago.sealdashboard.preferences.*

/** Exercise the launched Activity, application-owned provider and actual foreground lifecycle. */
class DashboardLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val container get() = (compose.activity.application as DashboardApplication).container

    private fun command(command: MockCommand) {
        assertEquals(CommandResult.Accepted, container.simulation.dispatch(command))
    }
    private fun waitUntil(predicate: () -> Boolean) = compose.waitUntil(10_000, predicate)

    @Before fun resetPriorActivityFixture() {
        command(MockCommand.Reset)
        command(MockCommand.Manual)
        waitUntil { container.vehicle.status.value == ProviderStatus.RUNNING }
    }

    @Test fun recreationPreservesSessionAndSelectedScreen() {
        command(MockCommand.Manual)
        command(MockCommand.SetSocPercent(63.0))
        command(MockCommand.SetPaused(true))
        compose.onNodeWithTag("nav-ENERGY").performClick()
        val provider = container.vehicle
        val before = provider.state.value
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("screen-ENERGY").assertExists()
        assertSame(provider, container.vehicle)
        assertEquals(before, provider.state.value)
        compose.onNodeWithTag("provider-status").assertTextEquals("PAUSED")
    }

    @Test fun repeatedNavigationRetainsOneSessionAndFaults() {
        command(MockCommand.Manual)
        command(MockCommand.SetPaused(true))
        command(MockCommand.SetFault(SignalKey.SOC, SignalQuality.UNAVAILABLE))
        command(MockCommand.SetFault(SignalKey.SPEED, SignalQuality.STALE))
        command(MockCommand.SetProviderError("Lifecycle fixture integration failure"))
        val provider = container.vehicle
        val before = provider.state.value
        repeat(3) {
            DashboardDestination.entries.forEach { destination ->
                compose.onNodeWithTag("nav-${destination.name}").performClick()
                compose.onNodeWithTag("screen-${destination.name}").assertExists()
                assertSame(provider, container.vehicle)
            }
        }
        compose.onNodeWithTag("nav-DRIVE").performClick()
        compose.onNodeWithTag("speed-value", true).assertTextEquals("—")
        compose.onNodeWithTag("soc-value", true).assertTextEquals("—")
        compose.onNodeWithTag("provider-status").assertTextEquals("ERROR")
        assertEquals(before, provider.state.value)
        command(MockCommand.Reset)
        command(MockCommand.SetPaused(true))
        waitUntil { compose.activity.dashboardViewModel.uiState.value.formatter.number(
            compose.activity.dashboardViewModel.uiState.value.vehicle.battery.stateOfChargePercent) == "80" }
        compose.onNodeWithTag("speed-value", true).assertTextEquals("0")
        compose.onNodeWithTag("soc-value", true).assertTextEquals("80")
        assertTrue(container.simulation.history.value.packPower.isEmpty())
    }

    @Test fun backgroundFreezesPhysicsAndResumeDoesNotCatchUp() {
        command(MockCommand.Manual)
        command(MockCommand.SetGear(Gear.DRIVE))
        command(MockCommand.SetTargetSpeedKmh(60.0))
        waitUntil { (container.vehicle.state.value.motion.speedKmh.value ?: 0.0) >= 59.0 }
        val context = compose.activity.applicationContext
        context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        waitUntil { container.vehicle.status.value == ProviderStatus.PAUSED }
        val stopped = container.vehicle.state.value
        val start = SystemClock.elapsedRealtime()
        waitUntil { SystemClock.elapsedRealtime() - start >= 1500 }
        assertEquals(stopped, container.vehicle.state.value)
        // Simulate the user launching the app from outside it. A background app
        // context cannot bring itself forward on Android 10 (and should not).
        val resumeRequestedAt = SystemClock.elapsedRealtime()
        val launch = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "am start -W -n io.github.jhaago.sealdashboard/.MainActivity")
        ParcelFileDescriptor.AutoCloseInputStream(launch).bufferedReader().use { it.readText() }
        waitUntil { container.vehicle.status.value == ProviderStatus.RUNNING }
        waitUntil { container.vehicle.state.value.sampledAtMillis > stopped.sampledAtMillis }
        val resumed = container.vehicle.state.value
        // Bound by foreground wall time plus two ticker steps for scheduling.
        // At >=59 km/h, integrating the hidden 1.5 s exceeds that margin.
        val activeSeconds = (SystemClock.elapsedRealtime() - resumeRequestedAt + 200) / 1000.0
        val distance = resumed.trip.distanceKm.value!! - stopped.trip.distanceKm.value!!
        val permittedDistance = activeSeconds * 60.0 / 3600.0
        assertTrue(distance <= permittedDistance)
        // Check that this observed run's bound rejects a deliberate hidden-time
        // contribution; this fails if the oracle is loosened until it is useless.
        val hiddenDistance = stopped.motion.speedKmh.value!! * 1.5 / 3600.0
        assertFalse("Resume oracle must reject 1.5 seconds of hidden motion", distance + hiddenDistance <= permittedDistance)
        compose.onNodeWithTag("source-label").assertTextEquals("SIMULATED")
    }

    @Test fun lowerVehicleAndEnergyContentIsReachable() {
        command(MockCommand.SetPaused(true))
        compose.onNodeWithTag("nav-VEHICLE").performClick()
        compose.onNodeWithText("Window telemetry is unavailable in this mock build.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("nav-ENERGY").performClick()
        compose.onNodeWithText("Net efficiency").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("nav-DEVELOPMENT").performClick()
        compose.onNodeWithTag("real-provider").performScrollTo().assertIsDisplayed()
    }

    @SdkSuppress(minSdkVersion = 29)
    @Test fun physicalDisplayRotationKeepsTelemetryPreferencesAndDestination() {
        command(MockCommand.SetSocPercent(63.0))
        command(MockCommand.SetPaused(true))
        DisplayPreferences(compose.activity).update(DisplaySettings(DriveLayout.FULL, mirrored = true))
        compose.onNodeWithTag("nav-ENERGY").performClick()
        val provider = container.vehicle
        val snapshot = provider.state.value
        val autoRotation = shell("settings get system accelerometer_rotation").trim()
        val rotation = shell("settings get system user_rotation").trim()
        try {
            shell("settings put system accelerometer_rotation 0")
            shell("settings put system user_rotation 1")
            waitUntil { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
            compose.onNodeWithTag("screen-ENERGY").assertExists()
            compose.onNodeWithTag("nav-DRIVE").performClick()
            compose.onNodeWithTag("speed-value", true).assertIsDisplayed()
            compose.onNodeWithTag("soc-value", true).assertIsDisplayed()
            captureDisplay("drive-rotated-portrait", portrait = true)
            assertSame(provider, container.vehicle)
            assertEquals(snapshot, provider.state.value)
            assertEquals(DisplaySettings(DriveLayout.FULL, mirrored = true), DisplayPreferences(compose.activity).state.value)
            shell("settings put system user_rotation 0")
            waitUntil { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            compose.onNodeWithTag("screen-DRIVE").assertExists()
            captureDisplay("drive-rotated-landscape", portrait = false)
            assertSame(provider, container.vehicle)
            assertEquals(snapshot, provider.state.value)
        } finally {
            shell("settings put system user_rotation $rotation")
            shell("settings put system accelerometer_rotation $autoRotation")
            DisplayPreferences(compose.activity).update(DisplaySettings())
        }
    }

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }

    private fun captureDisplay(name: String, portrait: Boolean) {
        // Configuration changes precede the rendered window's resize transaction.
        compose.waitUntil(10_000) {
            val bounds = compose.onNodeWithTag("dashboard-root").fetchSemanticsNode().boundsInRoot
            if (portrait) bounds.height > bounds.width else bounds.width > bounds.height
        }
        compose.onNodeWithText("Companion preview").assertIsDisplayed()
        compose.onNodeWithText("SIMULATED ROUTE").assertExists()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = compose.onNodeWithTag("dashboard-root").captureToImage().asAndroidBitmap()
        assertEquals(portrait, bitmap.height > bitmap.width)
        val resolver = instrumentation.targetContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SealDashboardEvaluation")
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            requireNotNull(resolver.openOutputStream(uri)).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            bitmap.recycle()
        }
    }
}
