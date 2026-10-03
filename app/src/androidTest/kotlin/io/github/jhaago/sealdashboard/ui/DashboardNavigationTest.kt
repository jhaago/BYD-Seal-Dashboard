package io.github.jhaago.sealdashboard.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import org.junit.Rule
import org.junit.Test

class DashboardNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val host = DashboardTestHost()
    @Test fun allDestinationsShareAnHonestSourceLabel() {
        compose.setContent { host.Content() }
        for (destination in DashboardDestination.entries) {
            compose.onNodeWithTag("nav-${destination.name}").performClick()
            compose.onNodeWithTag("screen-${destination.name}").assertExists()
            compose.onNodeWithTag("source-label").assertTextEquals("SIMULATED")
        }
    }
    @Test fun unknownSocAndStaleSpeedAreNotInventedValues() {
        host.command(MockCommand.SetFault(SignalKey.SOC, SignalQuality.UNAVAILABLE))
        host.command(MockCommand.SetFault(SignalKey.SPEED, SignalQuality.STALE))
        compose.setContent { host.Content() }
        compose.onNodeWithTag("soc-value", true).assertTextEquals("—")
        compose.onNodeWithTag("speed-value", true).assertTextEquals("—")
        compose.onNodeWithTag("speed-quality", true).assertTextContains("STALE", substring = true)
    }
}
