package io.github.jhaago.sealdashboard.ui.energy

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

@Composable fun EnergyScreen(ui: DashboardUiState) {
    val colors = LocalDashboardPalette.current
    val v = ui.vehicle; val f = ui.formatter
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SectionTitle("Energy", "Rear motor and HV pack are separate signals. Positive = discharge; negative = recovery / charging.")
        if (LocalDashboardStyle.current == DashboardVisualStyle.SYSTEMS) SystemMimic(ui)
        val metrics = listOf(
            Triple("Rear motor output", f.number(v.powertrain.motorPowerKw[Axle.REAR] ?: Signal.unavailable(), 1), "kW"),
            Triple("HV pack power", f.number(v.powertrain.packPowerKw, 1), "kW"),
            Triple("Driving regen", f.number(v.powertrain.regenPowerKw, 1), "kW"),
            Triple("HV voltage", f.number(v.battery.voltageV, 1), "V"),
            Triple("HV current", f.number(v.battery.currentA, 1), "A"),
            Triple("Battery temperature", f.number(v.environment.batteryTemperatureC), "°C"),
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 900.dp) 3 else 2
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                metrics.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { (label, value, unit) -> TelemetryReadout(label, value, unit, modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 900.dp) Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                HistoryChart("Signed pack power · up to 60 samples", ui.history.packPower, -60.0, 180.0, "kW", Modifier.weight(1f))
                HistoryChart("State of charge · up to 900 samples", ui.history.stateOfCharge, 0.0, 100.0, "%", Modifier.weight(1f))
            } else Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                HistoryChart("Signed pack power · up to 60 samples", ui.history.packPower, -60.0, 180.0, "kW")
                HistoryChart("State of charge · up to 900 samples", ui.history.stateOfCharge, 0.0, 100.0, "%")
            }
        }
        SectionTitle("This simulated trip")
        val trip = listOf(
            Triple("Distance", f.number(v.trip.distanceKm, 2), "km"),
            Triple("Gross used", f.number(v.trip.grossEnergyUsedKwh, 3), "kWh"),
            Triple("Recovered driving", f.number(v.trip.recoveredDrivingEnergyKwh, 3), "kWh"),
            Triple("Net energy", f.number(v.trip.netEnergyKwh, 3), "kWh"),
            Triple("Net efficiency", f.number(v.trip.efficiencyKwhPer100Km, 1), "kWh/100 km"),
        )
        trip.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { (label, value, unit) -> TelemetryReadout(label, value, unit, modifier = Modifier.weight(1f), size = 24.sp) }
            }
        }
        Text("Efficiency appears after 0.1 km. Plug-in charging is not trip regeneration. Simulator parameters are illustrative, not calibrated BYD specifications.",
            fontSize = 16.sp, color = colors.muted)
    }
}
