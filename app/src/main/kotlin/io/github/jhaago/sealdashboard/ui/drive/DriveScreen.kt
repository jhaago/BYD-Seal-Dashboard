package io.github.jhaago.sealdashboard.ui.drive

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.DashboardUiState
import io.github.jhaago.sealdashboard.ui.theme.*
import io.github.jhaago.sealdashboard.ui.navigation.*

@Composable fun DriveScreen(ui: DashboardUiState, display: DisplaySettings, onDisplayChange: (DisplaySettings) -> Unit, demos: DashboardDemoState) {
    val colors = LocalDashboardPalette.current
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
        val wide = maxWidth >= 1100.dp && maxHeight >= 480.dp && LocalDensity.current.fontScale <= 1.15f
        if (wide) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val projectionWeight = if (display.layout == DriveLayout.COMPANION) 2f else 1f
                val projection: @Composable () -> Unit = {
                    ProjectionPane(demos, display, Modifier.weight(projectionWeight).fillMaxHeight().testTag("projection-pane"))
                }
                val telemetry: @Composable () -> Unit = {
                    CompanionPane(ui, display, onDisplayChange, false, Modifier.weight(1f).fillMaxHeight()
                        .testTag("telemetry-pane").background(colors.surface, dashboardPanelShape())
                        .verticalScroll(rememberScrollState()).padding(20.dp))
                }
                if (display.mirrored) { telemetry(); projection() } else { projection(); telemetry() }
            }
        } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CompanionPane(ui, display, onDisplayChange, true, Modifier.fillMaxWidth().testTag("telemetry-pane"))
            ProjectionPane(demos, display, Modifier.fillMaxWidth().then(
                if (display.ownNavigation) Modifier else Modifier.height(400.dp)
            ).testTag("projection-pane"), compact = true)
        }
    }
}

@Composable private fun ProjectionPane(demos: DashboardDemoState, display: DisplaySettings, modifier: Modifier, compact: Boolean = false) {
    if (display.ownNavigation) NavigationPreview(demos.navigation, display.visualStyle, modifier, compact)
    else FactoryProjectionPreview(demos.projection, demos.media, modifier)
}
