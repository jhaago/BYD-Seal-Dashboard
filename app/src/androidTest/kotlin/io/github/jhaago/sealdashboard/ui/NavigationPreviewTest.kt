package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
                DisplaySettings(DriveLayout.FULL, visualStyle = DashboardVisualStyle.FUTURISTIC), {},
                Modifier.requiredSize(1280.dp,720.dp), DashboardDemoState(navigation = NavigationState(route = emptyList())))
        }
        compose.onNodeWithText("Route unavailable").assertIsDisplayed()
        compose.onNodeWithText("Follow the coastal route").assertDoesNotExist()
    }

    @Test fun compactLargeTextKeepsUsableMapAndCompleteFooter() {
        val host = DashboardTestHost()
        host.display = DisplaySettings(DriveLayout.FULL, visualStyle = DashboardVisualStyle.LEGACY_HMI)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.3f)) {
                host.Content(Modifier.requiredSize(400.dp,720.dp))
            }
        }
        val map = compose.onNodeWithTag("street-map").performScrollTo().fetchSemanticsNode().boundsInRoot
        val minimumHeight = with(compose.density) { 200.dp.toPx() }
        assertTrue("Map must retain 200dp usable height; got $map", map.height >= minimumHeight)
        compose.onNodeWithText("Fictional local map · no live guidance").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("speed-value", true).performScrollTo().assertIsDisplayed()
        DashboardDestination.entries.forEach { compose.onNodeWithTag("nav-${it.name}").assertIsDisplayed() }
    }

    @Test fun switchingPreviewSourceShowsStreetMapTurnAndSampleMarkersWithoutARealSession() {
        val host = DashboardTestHost()
        host.display = host.display.copy(visualStyle = DashboardVisualStyle.FUTURISTIC)
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
