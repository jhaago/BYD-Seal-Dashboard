package io.github.jhaago.sealdashboard.ui.development

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.ui.DashboardUiState
import io.github.jhaago.sealdashboard.ui.components.SectionTitle
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun SimulationAssumptions(ui: DashboardUiState, simulation: SimulationStatus, command: (MockCommand) -> CommandResult) {
    val colors = LocalDashboardPalette.current
    val config = simulation.config
    val enabled = simulation.paused && ui.vehicle.motion.speedKmh.value?.let { it < .01 } == true
    SectionTitle("Illustrative assumptions", "Not calibrated BYD specifications. Pause a stationary simulation to edit.")
    SimulationSlider("Pack capacity", config.capacityKwh, 10f..150f, "kWh", "config-capacity", enabled) { command(MockCommand.ReplaceConfig(config.copy(capacityKwh = it))) }
    SimulationSlider("Mass", config.massKg, 500f..5000f, "kg", "config-mass", enabled) { command(MockCommand.ReplaceConfig(config.copy(massKg = it))) }
    SimulationSlider("Rear motor limit", config.maxMotorPowerKw, 1f..500f, "kW", "config-motor", enabled) { command(MockCommand.ReplaceConfig(config.copy(maxMotorPowerKw = it))) }
    SimulationSlider("Recovery limit", config.maxRegenPowerKw, 0f..200f, "kW", "config-regen", enabled) { command(MockCommand.ReplaceConfig(config.copy(maxRegenPowerKw = it))) }
    SimulationSlider("Stored charging power", config.chargingPowerKw, .1f..250f, "kW", "config-charge", enabled) { command(MockCommand.ReplaceConfig(config.copy(chargingPowerKw = it))) }
    SimulationSlider("Range consumption", config.consumptionKwhPerKm, .05f.. .5f, "kWh/km", "config-consumption", enabled) { command(MockCommand.ReplaceConfig(config.copy(consumptionKwhPerKm = it))) }
    SimulationSlider("Speed limit", config.maxSpeedKmh, 10f..250f, "km/h", "config-speed", enabled) { command(MockCommand.ReplaceConfig(config.copy(maxSpeedKmh = it))) }
    if (!enabled) Text("Assumption editing is disabled until the mock is paused and stationary.", fontSize = 16.sp, color = colors.muted)
}
