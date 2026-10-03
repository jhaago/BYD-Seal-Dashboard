package io.github.jhaago.sealdashboard.ui.vehicle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.TelemetryFormatter
import io.github.jhaago.sealdashboard.ui.theme.*

/** Original generic sedan schematic, not OEM Seal artwork. Replaceable typed renderer. */
@Composable fun VehicleGraphic(wheels: Map<Wheel, WheelState>, openings: OpeningsState, formatter: TelemetryFormatter, modifier: Modifier = Modifier) {
    val colors = LocalDashboardPalette.current
    Column(modifier, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text("FRONT", fontSize = 16.sp, color = colors.muted)
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width; val h = size.height
            val path = Path().apply {
                moveTo(w * .36f, h * .05f); cubicTo(w * .26f, h * .05f, w * .22f, h * .13f, w * .22f, h * .28f)
                lineTo(w * .22f, h * .8f); quadraticTo(w * .22f, h * .95f, w * .36f, h * .95f)
                lineTo(w * .64f, h * .95f); quadraticTo(w * .78f, h * .95f, w * .78f, h * .8f)
                lineTo(w * .78f, h * .28f); cubicTo(w * .78f, h * .13f, w * .74f, h * .05f, w * .64f, h * .05f); close()
            }
            drawPath(path, colors.elevated)
            drawPath(path, colors.muted, style = Stroke(2.dp.toPx()))
            drawRoundRect(colors.background, Offset(w * .3f, h * .29f), Size(w * .4f, h * .38f), CornerRadius(w * .05f))
            drawLine(colors.accent.copy(alpha = .6f), Offset(w * .31f, h * .26f), Offset(w * .69f, h * .26f), 2.dp.toPx())
            drawLine(colors.muted, Offset(w * .32f, h * .71f), Offset(w * .68f, h * .71f), 2.dp.toPx())
            Wheel.entries.forEach { wheel ->
                val left = wheel == Wheel.FRONT_LEFT || wheel == Wheel.REAR_LEFT
                val front = wheel == Wheel.FRONT_LEFT || wheel == Wheel.FRONT_RIGHT
                val signal = wheels[wheel]?.pressureKpa ?: Signal.unavailable()
                val color = if (formatter.quality(signal) == SignalQuality.FRESH) colors.muted else colors.warning
                drawRoundRect(color, Offset(w * if (left) .12f else .8f, h * if (front) .23f else .7f),
                    Size(w * .08f, h * .13f), CornerRadius(4.dp.toPx()))
            }
            Door.entries.forEach { door ->
                val left = door == Door.FRONT_LEFT || door == Door.REAR_LEFT
                val front = door == Door.FRONT_LEFT || door == Door.FRONT_RIGHT
                val signal = openings.doorsOpen[door] ?: Signal.unavailable()
                val valid = formatter.quality(signal) == SignalQuality.FRESH
                val x = w * if (left) .22f else .78f
                val y = h * if (front) .39f else .59f
                val open = valid && signal.value == true
                drawLine(if (!valid) colors.warning else if (open) colors.accent else colors.grid,
                    Offset(x, y), Offset(x + if (open) w * if (left) -.18f else .18f else 0f, y + h * .14f), 3.dp.toPx())
            }
            if (formatter.quality(openings.bootOpen) == SignalQuality.FRESH && openings.bootOpen.value == true)
                drawLine(colors.accent, Offset(w * .32f, h * .98f), Offset(w * .68f, h * .98f), 4.dp.toPx())
        }
        Text("Original schematic · not a live camera", fontSize = 16.sp, color = colors.muted)
    }
}
