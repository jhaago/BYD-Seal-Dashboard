package io.github.jhaago.sealdashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.theme.*
import kotlin.math.*

@Composable fun HistoryChart(title: String, samples: List<Signal<Double>>, minimum: Double, maximum: Double, unit: String, modifier: Modifier = Modifier) {
    val colors = LocalDashboardPalette.current
    val validValues = samples.mapNotNull { it.value?.takeIf { v -> v.isFinite() && it.quality == SignalQuality.FRESH } }
    val lower = min(minimum, validValues.minOrNull() ?: minimum)
    val upper = max(maximum, validValues.maxOrNull() ?: maximum)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontSize = 18.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${lower.toInt()} $unit", fontSize = 16.sp, color = colors.muted)
            Text("${upper.toInt()} $unit", fontSize = 16.sp, color = colors.muted)
        }
        if (validValues.isEmpty()) Text("No valid samples yet", color = colors.muted, modifier = Modifier.height(120.dp))
        else Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            fun y(value: Double) = (size.height * (1 - (value - lower) / (upper - lower))).toFloat()
            repeat(4) { index ->
                val yy = size.height * index / 3
                drawLine(colors.grid, Offset(0f, yy), Offset(size.width, yy), 1.dp.toPx())
            }
            if (lower < 0 && upper > 0) drawLine(colors.muted, Offset(0f, y(0.0)), Offset(size.width, y(0.0)), 1.dp.toPx())
            val path = Path(); var connected = false
            samples.forEachIndexed { index, sample ->
                val value = sample.value
                if (value == null || !value.isFinite() || sample.quality != SignalQuality.FRESH) connected = false
                else {
                    val x = size.width * index / max(samples.lastIndex, 1)
                    if (connected) path.lineTo(x, y(value)) else path.moveTo(x, y(value))
                    connected = true
                    drawCircle(colors.accent, 2.dp.toPx(), Offset(x, y(value)))
                }
            }
            drawPath(path, colors.accent, style = Stroke(2.dp.toPx()))
        }
        Text("${samples.size} stored samples · historical, not a live reading", fontSize = 16.sp, color = colors.muted)
    }
}
