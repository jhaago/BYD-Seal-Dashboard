package io.github.jhaago.sealdashboard.ui.drive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.*
import io.github.jhaago.sealdashboard.ui.components.*
import io.github.jhaago.sealdashboard.ui.theme.DashboardColors

@Composable fun CompanionPane(ui: DashboardUiState, display: DisplaySettings, onDisplayChange: (DisplaySettings) -> Unit,
    compact: Boolean, modifier: Modifier = Modifier) {
    val v = ui.vehicle; val f = ui.formatter
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DYNAMIC · RWD", color = DashboardColors.Muted, fontSize = 16.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(f.number(v.motion.speedKmh), Modifier.testTag("speed-value"), fontSize = if (compact) 80.sp else 96.sp,
                    lineHeight = if (compact) 88.sp else 104.sp, fontWeight = FontWeight.Light)
                Text("km/h", fontSize = 18.sp, color = DashboardColors.Muted)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(f.gear(v.motion.gear), Modifier.testTag("gear-value"), fontSize = 42.sp, color = DashboardColors.Accent)
                Text("Gear", fontSize = 16.sp, color = DashboardColors.Muted)
            }
        }
        val quality = f.quality(v.motion.speedKmh)
        if (quality != SignalQuality.FRESH)
            Text("Speed · ${quality.name}", Modifier.testTag("speed-quality"), color = DashboardColors.Warning, fontSize = 16.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TelemetryReadout("Battery", f.number(v.battery.stateOfChargePercent), "%", "soc", Modifier.weight(1f))
            TelemetryReadout("Estimated range", f.number(v.battery.estimatedRangeKm), "km", "range", Modifier.weight(1f))
        }
        PowerBar(v.powertrain.packPowerKw, f)
        Text(f.text(v.powertrain.driveMode) { "${it.name} MODE" }, color = DashboardColors.Accent, fontSize = 16.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Trip ${f.number(v.trip.distanceKm, 1)} km", fontSize = 16.sp, color = DashboardColors.Muted)
            Text("Outside ${f.number(v.environment.outsideTemperatureC)}°C", fontSize = 16.sp, color = DashboardColors.Muted)
        }
        Text(f.text(v.climate.enabled) { if (it) "Climate ${f.number(v.climate.targetTemperatureC)}°C · fan ${f.text(v.climate.fanLevel) { level -> level.toString() }}" else "Climate off" },
            fontSize = 16.sp, color = DashboardColors.Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onDisplayChange(display.copy(layout = if (display.layout == DriveLayout.COMPANION) DriveLayout.FULL else DriveLayout.COMPANION)) },
                modifier = Modifier.heightIn(min = 56.dp).testTag("layout-toggle")) {
                Text(if (display.layout == DriveLayout.COMPANION) "Full dashboard" else "Companion preview")
            }
        }
    }
}
