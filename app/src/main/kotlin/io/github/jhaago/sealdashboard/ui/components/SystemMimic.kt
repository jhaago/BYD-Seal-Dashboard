package io.github.jhaago.sealdashboard.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.*
import io.github.jhaago.sealdashboard.ui.theme.*

/** Original EV instrument mimic. Indicators mean sample quality, never plant readiness. */
@Composable fun SystemMimic(ui: DashboardUiState) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter
    val v = ui.vehicle
    Column(Modifier.fillMaxWidth().background(colors.surface).border(1.dp, colors.grid).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ENERGY / THERMAL MIMIC · SIMULATED", fontSize = 16.sp, color = colors.text)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val pack: @Composable (Modifier) -> Unit = { modifier ->
                MimicNode("HV PACK", "${f.number(v.battery.stateOfChargePercent)} %",
                    "${f.number(v.battery.voltageV, 1)} V · ${f.number(v.battery.currentA, 1)} A",
                    f.quality(v.battery.stateOfChargePercent), "systems-pack", modifier) {
                    val valid = f.quality(v.battery.stateOfChargePercent) == SignalQuality.FRESH
                    val fraction = if (valid) ((v.battery.stateOfChargePercent.value ?: 0.0) / 100).toFloat().coerceIn(0f, 1f) else 0f
                    Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                        drawRect(colors.grid, style = Stroke(1.dp.toPx()))
                        if (valid) drawRect(colors.accent.copy(alpha = .7f), size = Size(size.width * fraction, size.height))
                    }
                }
            }
            val conversion: @Composable (Modifier) -> Unit = { modifier ->
                MimicNode("CONVERSION", "—", "Conversion state unavailable", SignalQuality.UNAVAILABLE,
                    "systems-conversion", modifier)
            }
            val motor: @Composable (Modifier) -> Unit = { modifier ->
                val signal = v.powertrain.motorPowerKw[Axle.REAR] ?: Signal.unavailable()
                MimicNode("REAR MOTOR", "${f.number(signal, 1)} kW", "Single-motor RWD sample",
                    f.quality(signal), "systems-motor", modifier)
            }
            if (maxWidth >= 760.dp) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                pack(Modifier.weight(1f)); MimicLink(true); conversion(Modifier.weight(1f)); MimicLink(true); motor(Modifier.weight(1f))
            } else Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                pack(Modifier.fillMaxWidth()); MimicLink(false); conversion(Modifier.fillMaxWidth()); MimicLink(false); motor(Modifier.fillMaxWidth())
            }
        }
        val power = v.powertrain.packPowerKw
        Text("PACK FLOW · " + when {
            f.quality(power) != SignalQuality.FRESH -> "unavailable"
            (power.value ?: 0.0) < 0 -> "RECOVERY / CHARGING"
            (power.value ?: 0.0) > 0 -> "DISCHARGE"
            else -> "IDLE"
        } + " · ${f.number(power, 1)} kW", fontSize = 16.sp, color = colors.muted)
        MimicNode("THERMAL MONITOR", "${f.number(v.environment.batteryTemperatureC)} °C", "Battery temperature sample",
            f.quality(v.environment.batteryTemperatureC), "systems-thermal", Modifier.fillMaxWidth())
        Text("Coolant flow / pump state unavailable", fontSize = 16.sp, color = colors.muted)
        Text("Green = fresh sample. Amber = stale/error/unavailable. Original illustration; no equipment control.", fontSize = 16.sp, color = colors.muted)
    }
}

@Composable private fun MimicNode(title: String, value: String, detail: String, quality: SignalQuality,
    tag: String, modifier: Modifier, content: @Composable () -> Unit = {}) {
    val colors = LocalDashboardPalette.current
    val valid = quality == SignalQuality.FRESH
    Column(modifier.background(colors.background).border(1.dp, colors.grid).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(12.dp).background(if (valid) colors.accent else colors.warning))
            Text(title, fontSize = 16.sp)
        }
        Text(value, Modifier.testTag("$tag-value"), fontSize = 24.sp)
        Text(detail, fontSize = 16.sp, color = colors.muted)
        Text("${quality.name} SAMPLE", Modifier.testTag("$tag-quality"), fontSize = 16.sp,
            color = if (valid) colors.accent else colors.warning)
        content()
    }
}

@Composable private fun MimicLink(horizontal: Boolean) {
    val colors = LocalDashboardPalette.current
    Canvas(if (horizontal) Modifier.width(24.dp).height(8.dp) else Modifier.width(8.dp).height(24.dp)) {
        val start = if (horizontal) Offset(0f, size.height/2) else Offset(size.width/2, 0f)
        val end = if (horizontal) Offset(size.width, size.height/2) else Offset(size.width/2, size.height)
        drawLine(colors.grid, start, end, 2.dp.toPx())
    }
}
