package io.github.jhaago.sealdashboard.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.jhaago.sealdashboard.core.Gear
import io.github.jhaago.sealdashboard.mock.MockCommand
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DevelopmentControlsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun manualSpeedControlsChangeTheSharedDriveReadout() {
        val host = DashboardTestHost()
        compose.setContent { host.Content() }
        compose.onNodeWithTag("nav-DEVELOPMENT").performClick()
        compose.onNodeWithTag("manual-mode").performClick()
        compose.onNodeWithTag("gear-DRIVE").performClick()
        compose.onNodeWithTag("target-speed").performScrollTo().performSemanticsAction(
            androidx.compose.ui.semantics.SemanticsActions.SetProgress) { assertTrue(it(60f)) }
        compose.runOnIdle { host.step(10.0) }
        assertEquals(Gear.DRIVE, host.ui.vehicle.motion.gear.value)
        assertTrue(host.ui.vehicle.motion.speedKmh.value!! > 50)
        compose.onNodeWithTag("nav-DRIVE").performClick()
        compose.onNodeWithTag("speed-value", true).assertTextEquals("60")
    }
    @Test fun realProviderIsExplicitlyDisabledAndRejectedCommandsExplainWhy() {
        val host = DashboardTestHost()
        host.command(MockCommand.Manual)
        host.command(MockCommand.SetGear(Gear.DRIVE))
        host.command(MockCommand.SetTargetSpeedKmh(30.0)); host.step(5.0)
        compose.setContent { host.Content() }
        compose.onNodeWithTag("nav-DEVELOPMENT").performClick()
        compose.onNodeWithTag("real-provider").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Real telemetry is not integrated. Mock controls never command the vehicle.").assertExists()
        compose.onNodeWithTag("door-FRONT_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("command-result").assertTextContains("stationary")
    }
}
