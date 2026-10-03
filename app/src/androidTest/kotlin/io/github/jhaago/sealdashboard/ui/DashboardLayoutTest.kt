package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DashboardLayoutTest {
    @get:Rule val compose = createComposeRule()
    @Test fun primaryTelemetryAndNavigationFitAtAllRequiredSizes() {
        val host = DashboardTestHost()
        var dimensions by mutableStateOf(Triple(1280, 720, 1f))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, dimensions.third)) {
                host.Content(Modifier.requiredSize(dimensions.first.dp, dimensions.second.dp))
            }
        }
        for ((width, height) in listOf(1280 to 720, 960 to 600, 600 to 960, 400 to 720)) {
            for (scale in listOf(1f, 1.3f)) {
                compose.runOnIdle { dimensions = Triple(width, height, scale) }
                val bounds = listOf("speed-value", "gear-value", "soc-value", "range-value", "power-value").map {
                    compose.onNodeWithTag(it, true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                }
                for (a in bounds.indices) for (b in a + 1 until bounds.size)
                    assertFalse("Telemetry overlaps at $width × $height / $scale", bounds[a].overlaps(bounds[b]))
                DashboardDestination.entries.forEach { compose.onNodeWithTag("nav-${it.name}").assertIsDisplayed() }
            }
        }
    }
    @Test fun wideCompanionPreviewUsesTwoToOnePaneWidths() {
        compose.setContent { DashboardTestHost().Content(Modifier.requiredSize(1280.dp, 720.dp)) }
        val projection = compose.onNodeWithTag("projection-pane").fetchSemanticsNode().boundsInRoot
        val telemetry = compose.onNodeWithTag("telemetry-pane").fetchSemanticsNode().boundsInRoot
        assertEquals(projection.width, telemetry.width * 2, 1f)
        assertTrue(projection.left < telemetry.left)
    }
}
