package io.github.jhaago.sealdashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.TelemetryFormatter
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun PowerBar(signal: Signal<Double>, formatter: TelemetryFormatter) {
    val colors = LocalDashboardPalette.current
    val valid = formatter.quality(signal) == SignalQuality.FRESH
    val power = if (valid) signal.value ?: 0.0 else 0.0
    val proportion by animateFloatAsState((power / if (power < 0) 60 else 180).toFloat().coerceIn(-1f, 1f), tween(120), label = "pack-power")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text("Pack power", color = colors.muted, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                Text(formatter.number(signal, 1), Modifier.testTag("power-value"), fontSize = 26.sp)
                Text("kW", fontSize = 16.sp, color = colors.muted)
            }
        }
        Canvas(Modifier.fillMaxWidth().height(12.dp)) {
            val mid = size.width * .3f
            val extent = if (proportion < 0) mid else size.width - mid
            drawLine(colors.grid, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 6.dp.toPx(), StrokeCap.Round)
            drawLine(colors.muted, Offset(mid, 0f), Offset(mid, size.height), 1.dp.toPx())
            if (valid) drawLine(colors.accent, Offset(mid, size.height / 2),
                Offset(mid + proportion * extent, size.height / 2), 6.dp.toPx(), StrokeCap.Round)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recovery", color = colors.muted, fontSize = 16.sp)
            Text("Discharge", color = colors.muted, fontSize = 16.sp)
        }
    }
}
