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
import io.github.jhaago.sealdashboard.ui.theme.DashboardColors

@Composable fun DriveScreen(ui: DashboardUiState, display: DisplaySettings, onDisplayChange: (DisplaySettings) -> Unit, demos: DashboardDemoState) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
        val wide = maxWidth >= 1100.dp && maxHeight >= 480.dp && LocalDensity.current.fontScale <= 1.15f
        if (wide) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val projectionWeight = if (display.layout == DriveLayout.COMPANION) 2f else 1f
                val projection: @Composable () -> Unit = {
                    ProjectionPane(demos, display.layout, Modifier.weight(projectionWeight).fillMaxHeight().testTag("projection-pane"))
                }
                val telemetry: @Composable () -> Unit = {
                    CompanionPane(ui, display, onDisplayChange, false, Modifier.weight(1f).fillMaxHeight()
                        .testTag("telemetry-pane").background(DashboardColors.Surface, RoundedCornerShape(24.dp))
                        .verticalScroll(rememberScrollState()).padding(20.dp))
                }
                if (display.mirrored) { telemetry(); projection() } else { projection(); telemetry() }
            }
        } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CompanionPane(ui, display, onDisplayChange, true, Modifier.fillMaxWidth().testTag("telemetry-pane"))
            ProjectionPane(demos, display.layout, Modifier.fillMaxWidth().height(400.dp).testTag("projection-pane"))
        }
    }
}

@Composable private fun ProjectionPane(demos: DashboardDemoState, layout: DriveLayout, modifier: Modifier) {
    Column(modifier.background(DashboardColors.Surface, RoundedCornerShape(24.dp)).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (layout == DriveLayout.COMPANION) demos.projection.sourceLabel else demos.navigation.sourceLabel,
            color = DashboardColors.Accent, fontSize = 16.sp)
        Text(demos.navigation.instruction, fontSize = 28.sp)
        Text("${demos.navigation.distanceKm} km · ${demos.navigation.minutes} min · demonstration only", fontSize = 16.sp, color = DashboardColors.Muted)
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val step = 48.dp.toPx()
            var x = 0f
            while (x <= size.width) { drawLine(DashboardColors.Grid.copy(alpha = .5f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx()); x += step }
            var y = 0f
            while (y <= size.height) { drawLine(DashboardColors.Grid.copy(alpha = .5f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += step }
            val path = Path()
            demos.navigation.route.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x * size.width, point.y * size.height)
                else path.lineTo(point.x * size.width, point.y * size.height)
            }
            drawPath(path, DashboardColors.Accent.copy(alpha = .15f), style = Stroke(20.dp.toPx(), cap = StrokeCap.Round))
            drawPath(path, DashboardColors.Accent, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            demos.navigation.route.firstOrNull()?.let { drawCircle(DashboardColors.Text, 6.dp.toPx(), Offset(it.x * size.width, it.y * size.height)) }
        }
        Text(demos.media.sourceLabel, fontSize = 16.sp, color = DashboardColors.Muted)
        Text("${demos.media.title} · ${demos.media.artist}", fontSize = 18.sp)
        Text(demos.projection.explanation, fontSize = 16.sp, color = DashboardColors.Muted)
    }
}
