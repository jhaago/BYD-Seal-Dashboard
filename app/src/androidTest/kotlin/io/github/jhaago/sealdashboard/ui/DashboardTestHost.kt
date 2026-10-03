package io.github.jhaago.sealdashboard.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*

/** Explicit clock/steps; no background ticker, sleeps, random values or vehicle access. */
class DashboardTestHost {
    var now = 0L
    val engine = SimulationEngine(SimulationConfig(), MonotonicClock { now })
    var ui by mutableStateOf(DashboardUiState(engine.state, ProviderStatus.RUNNING, now))
    var simulation by mutableStateOf(engine.simulationStatus)
    var display by mutableStateOf(DisplaySettings())
    fun command(command: MockCommand): CommandResult {
        val result = engine.apply(command); refresh(); return result
    }
    fun step(seconds: Double) { now += (seconds * 1000).toLong(); engine.step(seconds); refresh() }
    fun refresh() {
        ui = ui.copy(vehicle = engine.state, history = engine.history, nowMillis = now,
            status = if (engine.simulationStatus.paused) ProviderStatus.PAUSED else ProviderStatus.RUNNING)
        simulation = engine.simulationStatus
    }
    @Composable fun Content(modifier: Modifier = Modifier) {
        DashboardContent(ui, simulation, { ui = ui.copy(destination = it) }, ::command,
            display, { display = it }, modifier = modifier)
    }
}
