package io.github.jhaago.sealdashboard.ui.development

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.*
import io.github.jhaago.sealdashboard.ui.components.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun DevelopmentScreen(ui: DashboardUiState, simulation: SimulationStatus, command: (MockCommand) -> CommandResult,
    display: DisplaySettings, onDisplayChange: (DisplaySettings) -> Unit) {
    val colors = LocalDashboardPalette.current
    val v = ui.vehicle; val f = ui.formatter
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Development", "All controls below affect simulation only.")
        ThemeSelector(display.visualStyle) { onDisplayChange(display.copy(visualStyle = it)) }
        Text("${simulation.mode} · ${if (simulation.paused) "PAUSED" else "foreground simulation"}", color = colors.accent, fontSize = 18.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MockButton("Demo", "demo-mode") { command(MockCommand.Demo) }
            MockButton("Manual", "manual-mode") { command(MockCommand.Manual) }
            MockButton(if (simulation.paused) "Resume" else "Pause", "pause-simulation") { command(MockCommand.SetPaused(!simulation.paused)) }
            MockButton("Reset", "reset-simulation") { command(MockCommand.Reset) }
        }
        Text("Mock gear", fontSize = 18.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Gear.entries.forEach { gear -> MockButton(gear.name, "gear-${gear.name}") { command(MockCommand.SetGear(gear)) } }
        }
        SimulationSlider("Target speed", simulation.targetSpeedKmh, 0f..simulation.config.maxSpeedKmh.toFloat(), "km/h", "target-speed") { command(MockCommand.SetTargetSpeedKmh(it)) }
        SimulationSlider("Accelerator input", simulation.throttle, 0f..1f, "fraction", "throttle") { command(MockCommand.SetPedals(it, simulation.brake)) }
        SimulationSlider("Brake input", simulation.brake, 0f..1f, "fraction", "brake") { command(MockCommand.SetPedals(simulation.throttle, it)) }
        Text("Target-speed and pedal controls are alternative simulation inputs; editing either selects that method.", fontSize = 16.sp, color = colors.muted)
        v.battery.stateOfChargePercent.value?.let { value ->
            SimulationSlider("Mock SOC", value, 0f..100f, "%", "soc-control") { command(MockCommand.SetSocPercent(it)) }
        } ?: Text("SOC unavailable — clear its injected fault to edit.", fontSize = 18.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DriveMode.entries.forEach { mode -> MockButton(mode.name, "mode-${mode.name}") { command(MockCommand.SetDriveMode(mode)) } }
        }
        SimulationToggle("Mock charging (forces Park / stopped)", v.charging.status.value == ChargeStatus.CHARGING || v.charging.status.value == ChargeStatus.COMPLETE,
            "charging-control") { command(MockCommand.SetCharging(it)) }
        SectionTitle("Openings and tyres", "Opening edits require a stationary mock. Tyre values are reference pressures at 24°C.")
        Door.entries.forEach { door ->
            v.openings.doorsOpen[door]?.value?.let { open ->
                SimulationToggle("Mock ${door.name.replace('_', ' ').lowercase()} open", open, "door-${door.name}") { command(MockCommand.SetDoor(door, it)) }
            } ?: Text("${door.name}: state unavailable", fontSize = 18.sp)
        }
        v.openings.bootOpen.value?.let { SimulationToggle("Mock boot open", it, "boot-control") { open -> command(MockCommand.SetBoot(open)) } }
        Wheel.entries.forEach { wheel ->
            val state = v.wheels[wheel]
            val pressure = state?.pressureKpa?.value; val temperature = state?.temperatureC?.value
            if (pressure != null && temperature != null) {
                val reference = pressure * (297.15 / (temperature + 273.15))
                SimulationSlider(wheel.name.replace('_', ' '), reference, 0f..500f, "kPa at 24°C", "pressure-${wheel.name}") { command(MockCommand.SetTyrePressureKpa(wheel, it)) }
            } else Text("${wheel.name}: pressure unavailable", fontSize = 18.sp)
        }
        SectionTitle("Mock climate")
        val enabled = v.climate.enabled.value ?: false
        val target = v.climate.targetTemperatureC.value ?: 22.0
        val fan = v.climate.fanLevel.value ?: 2
        SimulationToggle("Climate edit target", enabled, "climate-control") { command(MockCommand.SetClimate(it, target, fan)) }
        SimulationSlider("Climate edit setpoint", target, 16f..30f, "°C", "climate-target") { command(MockCommand.SetClimate(enabled, it, fan)) }
        SimulationSlider("Fan edit target", fan.toDouble(), 0f..7f, "level", "climate-fan") { command(MockCommand.SetClimate(enabled, target, it.toInt())) }
        SectionTitle("Display")
        SimulationToggle("Mirror projection / driver panes", display.mirrored, "mirror-panes") { onDisplayChange(display.copy(mirrored = it)) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DriveLayout.entries.forEach { layout -> MockButton(layout.name, "layout-${layout.name}") { onDisplayChange(display.copy(layout = layout)) } }
        }
        SimulationAssumptions(ui, simulation, command)
        SectionTitle("Data source")
        MockButton("BYD real provider — unavailable", "real-provider", false) {}
        Text("Real telemetry is not integrated. Mock controls never command the vehicle.", fontSize = 18.sp, color = colors.warning)
        SectionTitle("Injected faults")
        var signal by remember { mutableStateOf(SignalKey.SPEED) }
        var menu by remember { mutableStateOf(false) }
        Box {
            MockButton("Signal: ${signal.name}", "fault-signal") { menu = true }
            DropdownMenu(menu, { menu = false }) { SignalKey.entries.forEach { key -> DropdownMenuItem(
                text = { Text(key.name) }, onClick = { signal = key; menu = false }, modifier = Modifier.heightIn(min = 56.dp)) } }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(SignalQuality.UNAVAILABLE, SignalQuality.STALE, SignalQuality.ERROR).forEach { quality ->
                MockButton(quality.name, "fault-${quality.name}") { command(MockCommand.SetFault(signal, quality)) }
            }
            MockButton("Clear signal", "clear-signal") { command(MockCommand.SetFault(signal, null)) }
        }
        MockButton("Simulate integration error", "provider-error") { command(MockCommand.SetProviderError("Simulated integration error · development scenario")) }
        MockButton("Clear integration error", "clear-provider-error") { command(MockCommand.SetProviderError(null)) }
        MockButton("Clear all faults", "clear-faults") { command(MockCommand.ClearFaults) }
        SectionTitle("Diagnostics", "Last 100 events; mock-only error scenarios.")
        if (ui.diagnostics.isEmpty()) Text("No integration errors.", fontSize = 18.sp)
        ui.diagnostics.asReversed().forEach { Text("${it.level} · ${it.message}", fontSize = 16.sp) }
        SectionTitle("Raw domain values", "Names/units are app fields, not evidence of undocumented BYD APIs.")
        rawSignals(v).forEach { raw ->
            Text("${raw.name} [${raw.unit}] · ${raw.signal.value ?: "—"}", fontSize = 16.sp)
            Text("${if (raw.signal.value == null) "NO SAMPLE" else raw.signal.source.name} · ${f.quality(raw.signal)} · t=${raw.signal.observedAtMillis} ms",
                fontSize = 16.sp, color = colors.muted)
        }
    }
}
