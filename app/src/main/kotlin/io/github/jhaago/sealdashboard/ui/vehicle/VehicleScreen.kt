package io.github.jhaago.sealdashboard.ui.vehicle

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.*
import io.github.jhaago.sealdashboard.ui.components.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun VehicleScreen(ui: DashboardUiState) {
    val colors = LocalDashboardPalette.current
    val v = ui.vehicle; val f = ui.formatter
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SectionTitle("Vehicle", "Australian 2024 Seal Dynamic · single-motor RWD")
        if (LocalDashboardStyle.current == DashboardVisualStyle.SYSTEMS) SystemMimic(ui)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val tyreInfo: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Wheel.entries.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            pair.forEach { wheel ->
                                val state = v.wheels[wheel] ?: WheelState()
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TelemetryReadout(wheel.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, f.number(state.pressureKpa), "kPa")
                                    Text("${f.number(state.temperatureC)}°C · simulated", fontSize = 16.sp, color = colors.muted)
                                }
                            }
                        }
                    }
                    Door.entries.forEach { door ->
                        Text("${door.name.replace('_', ' ')} · ${f.text(v.openings.doorsOpen[door] ?: Signal.unavailable()) { if (it) "OPEN" else "closed" }}", fontSize = 16.sp)
                    }
                    Text("Boot · ${f.text(v.openings.bootOpen) { if (it) "OPEN" else "closed" }}", fontSize = 16.sp)
                }
            }
            if (maxWidth >= 900.dp) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                VehicleGraphic(v.wheels, v.openings, f, Modifier.weight(1f).height(420.dp))
                Box(Modifier.weight(1f)) { tyreInfo() }
            } else Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                VehicleGraphic(v.wheels, v.openings, f, Modifier.fillMaxWidth().height(340.dp)); tyreInfo()
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            TelemetryReadout("Battery", f.number(v.battery.stateOfChargePercent), "%", modifier = Modifier.weight(1f))
            TelemetryReadout("Range", f.number(v.battery.estimatedRangeKm), "km", modifier = Modifier.weight(1f))
        }
        Text("Charging · ${f.text(v.charging.status) { it.name.lowercase() }} · ${f.number(v.charging.chargePowerKw, 1)} kW", fontSize = 18.sp)
        Text("Battery ${f.number(v.environment.batteryTemperatureC)}°C · cabin ${f.number(v.environment.cabinTemperatureC)}°C", fontSize = 18.sp)
        Text("Climate · ${f.text(v.climate.enabled) { if (it) "on" else "off" }} · ${f.number(v.climate.targetTemperatureC)}°C setpoint", fontSize = 18.sp)
        Text("Window telemetry is unavailable in this mock build.", color = colors.muted, fontSize = 16.sp)
    }
}
