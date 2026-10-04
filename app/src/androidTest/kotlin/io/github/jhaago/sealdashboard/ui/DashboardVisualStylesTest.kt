package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DashboardVisualStylesTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectingLegacyHmiOpensItsOverviewWithTopNavigation() {
        val host = DashboardTestHost()
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp, 720.dp)) }

        choose(DashboardVisualStyle.LEGACY_HMI)

        compose.onNodeWithTag("legacy-hmi-overview").assertIsDisplayed()
        compose.onNodeWithTag("legacy-hmi-top-nav").assertIsDisplayed()
    }

    @Test fun legacyHmiVehicleDiagramPlacesLiveTyrePressuresAtEachWheel() {
        val host = DashboardTestHost()
        host.command(MockCommand.SetTyrePressureKpa(Wheel.FRONT_LEFT, 241.0))
        host.command(MockCommand.SetTyrePressureKpa(Wheel.FRONT_RIGHT, 242.0))
        host.command(MockCommand.SetTyrePressureKpa(Wheel.REAR_LEFT, 238.0))
        host.command(MockCommand.SetTyrePressureKpa(Wheel.REAR_RIGHT, 239.0))
        host.display = host.display.copy(visualStyle = DashboardVisualStyle.LEGACY_HMI)
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp, 720.dp)) }

        val car = compose.onNodeWithTag("legacy-hmi-vehicle-schematic").fetchSemanticsNode().boundsInRoot
        val pressures = mapOf(
            Wheel.FRONT_LEFT to "241 kPa",
            Wheel.FRONT_RIGHT to "242 kPa",
            Wheel.REAR_LEFT to "238 kPa",
            Wheel.REAR_RIGHT to "239 kPa",
        ).mapValues { (wheel, expected) ->
            compose.onNodeWithTag("legacy-tyre-${wheel.name}").assertTextEquals(expected).fetchSemanticsNode().boundsInRoot
        }

        assertTrue(pressures.getValue(Wheel.FRONT_LEFT).center.x < car.center.x)
        assertTrue(pressures.getValue(Wheel.REAR_LEFT).center.x < car.center.x)
        assertTrue(pressures.getValue(Wheel.FRONT_RIGHT).center.x > car.center.x)
        assertTrue(pressures.getValue(Wheel.REAR_RIGHT).center.x > car.center.x)
        assertTrue(pressures.getValue(Wheel.REAR_LEFT).center.y > pressures.getValue(Wheel.FRONT_LEFT).center.y)
        assertTrue(pressures.getValue(Wheel.REAR_RIGHT).center.y > pressures.getValue(Wheel.FRONT_RIGHT).center.y)
        compose.onNodeWithTag("legacy-hmi-power-legend").assertIsDisplayed()
        compose.onNodeWithTag("legacy-hmi-battery-segments").assertIsDisplayed()
    }

    @Test fun legacyHmiDriveUsesRasterInstrumentCluster() {
        val host = DashboardTestHost()
        host.display = host.display.copy(visualStyle = DashboardVisualStyle.LEGACY_HMI)
        host.ui = host.ui.copy(destination = DashboardDestination.DRIVE)
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp, 720.dp)) }

        compose.onNodeWithTag("nav-DRIVE").performClick()
        compose.onNodeWithTag("legacy-hmi-drive").assertIsDisplayed()
        compose.onNodeWithTag("legacy-hmi-lane-view").assertIsDisplayed()
        compose.onNodeWithTag("legacy-hmi-power-flow").assertIsDisplayed()
        compose.onNodeWithTag("speed-value", true).assertTextEquals("0")
    }

    @Test fun selectingStylesPreservesScreenTelemetryAndDisplayChoices() {
        val host = DashboardTestHost()
        host.command(MockCommand.SetSocPercent(63.0))
        host.command(MockCommand.SetPaused(true))
        host.display = DisplaySettings(DriveLayout.FULL, true)
        host.ui = host.ui.copy(destination = DashboardDestination.ENERGY)
        val snapshot = host.engine.state
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp, 720.dp)) }
        for (style in DashboardVisualStyle.entries) {
            choose(style)
            if (style == DashboardVisualStyle.LEGACY_HMI)
                compose.onNodeWithTag("legacy-hmi-overview").assertExists()
            else compose.onNodeWithTag("screen-ENERGY").assertExists()
            compose.onNodeWithTag("source-label").assertTextEquals("SIMULATED")
            compose.onNodeWithTag("provider-status").assertTextEquals("PAUSED")
            compose.runOnIdle {
                assertEquals(snapshot, host.engine.state)
                assertEquals(63.0, host.engine.state.battery.stateOfChargePercent.value!!, 0.0)
                assertEquals(DisplaySettings(DriveLayout.FULL, true, style), host.display)
            }
        }
        compose.runOnIdle { host.ui = host.ui.copy(status = ProviderStatus.ERROR) }
        choose(DashboardVisualStyle.LEGACY_HMI)
        compose.onNodeWithTag("provider-status").assertTextEquals("ERROR")
        compose.onNodeWithTag("source-label").assertTextEquals("SIMULATED")
    }

    @Test fun everyStyleKeepsPrimaryReadoutsAndNavigationReachableWithoutOverlap() {
        val host = DashboardTestHost()
        var dimensions by mutableStateOf(Triple(1280, 720, 1f))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, dimensions.third)) {
                host.Content(Modifier.requiredSize(dimensions.first.dp, dimensions.second.dp))
            }
        }
        for (style in DashboardVisualStyle.entries) for ((w, h, scale) in listOf(Triple(1280,720,1f),Triple(400,720,1.3f),Triple(600,960,1.3f))) {
            compose.runOnIdle { host.display = host.display.copy(visualStyle = style); dimensions = Triple(w,h,scale) }
            compose.onNodeWithTag("style-menu").assertIsDisplayed()
            if (style == DashboardVisualStyle.LEGACY_HMI && compose.onAllNodesWithTag("legacy-hmi-overview").fetchSemanticsNodes().isNotEmpty())
                compose.onNodeWithTag("nav-DRIVE").performClick()
            val bounds = listOf("speed-value", "gear-value", "soc-value", "range-value", "power-value").map {
                compose.onNodeWithTag(it, true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            }
            for (a in bounds.indices) for (b in a+1 until bounds.size)
                assertFalse("$style at $w x $h / $scale", bounds[a].overlaps(bounds[b]))
            DashboardDestination.entries.forEach { compose.onNodeWithTag("nav-${it.name}").assertIsDisplayed() }
        }
    }

    @Test fun legacyHmiMimicDoesNotPresentUnknownConversionOrStaleSamplesAsActive() {
        val host = DashboardTestHost()
        host.display = host.display.copy(visualStyle = DashboardVisualStyle.LEGACY_HMI)
        host.ui = host.ui.copy(destination = DashboardDestination.ENERGY)
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp,720.dp)) }
        compose.onNodeWithTag("nav-ENERGY").performClick()
        compose.onNodeWithTag("systems-pack-quality").performScrollTo().assertTextEquals("FRESH SAMPLE")
        compose.onNodeWithText("Conversion state unavailable").assertExists()
        compose.runOnIdle { host.now += 3000; host.refresh() }
        compose.onNodeWithTag("systems-pack-quality").assertTextEquals("STALE SAMPLE")
        compose.onNodeWithTag("systems-pack-value").assertTextEquals("— %")
    }

    private fun choose(style: DashboardVisualStyle) {
        compose.onNodeWithTag("style-menu").performClick()
        compose.onNodeWithTag("select-style-${style.name}").performClick()
    }
}
