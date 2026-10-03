package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NavigationPreviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun anEmptyRouteShowsUnavailableInsteadOfDirections() {
        val host = DashboardTestHost()
        compose.setContent {
            DashboardContent(host.ui, host.simulation, {}, host::command,
                DisplaySettings(DriveLayout.FULL, visualStyle = DashboardVisualStyle.TRON), {},
                Modifier.requiredSize(1280.dp,720.dp), DashboardDemoState(navigation = NavigationState(route = emptyList())))
        }
        compose.onNodeWithText("Route unavailable").assertIsDisplayed()
        compose.onNodeWithText("Follow the coastal route").assertDoesNotExist()
    }

    @Test fun switchingPreviewSourceShowsStreetMapTurnAndSampleMarkersWithoutARealSession() {
        val host = DashboardTestHost()
        host.display = host.display.copy(visualStyle = DashboardVisualStyle.TRON)
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp,720.dp)) }
        compose.onNodeWithText("ANDROID AUTO · SIMULATED PREVIEW").assertExists()
        compose.onNodeWithTag("navigation-source-toggle").performScrollTo().performClick()
        compose.onNodeWithText("SIMULATED MAP").assertExists()
        compose.onNodeWithTag("street-map").assertExists()
        compose.onNodeWithText("Turn right onto Harbour Way").assertExists()
        compose.onNodeWithText("8.4 km · 12 min · DEMO ESTIMATE").assertExists()
        compose.onNodeWithText("Demo chargers · availability unknown").assertExists()
        assertFalse(DemoProjectionProvider().state.value.isRealSession)
        compose.onNodeWithTag("navigation-source-toggle").performScrollTo().performClick()
        compose.onNodeWithText("ANDROID AUTO · SIMULATED PREVIEW").assertExists()
        compose.onNodeWithTag("street-map").assertDoesNotExist()
    }
}
