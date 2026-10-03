package io.github.jhaago.sealdashboard.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.assistant.*
import kotlinx.coroutines.*

/** Explicit clock/steps; no background ticker, sleeps, random values or vehicle access. */
class DashboardTestHost(service: AssistantService? = null) {
    private val assistantScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val navigation = DemoNavigationProvider()
    val assistant = TripAssistantController(service ?: ScriptedAssistantService(DemoChargerSearchProvider()), PreviewRouteActions(navigation), assistantScope)
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
        DisposableEffect(this@DashboardTestHost) { onDispose { assistantScope.cancel() } }
        val nav by navigation.state.collectAsState()
        val conversation by assistant.state.collectAsState()
        DashboardContent(ui, simulation, { ui = ui.copy(destination = it) }, ::command,
            display, { display = it }, modifier = modifier, demos = DashboardDemoState(navigation = nav), assistantState = conversation,
            onAssistantSubmit = assistant::submit, onAssistantCancel = assistant::cancel, onAssistantDismiss = assistant::dismissProposal,
            onAssistantApply = { id ->
                val pending = assistant.state.value.proposal
                assistant.applyProposal(id)
                val applied = assistant.state.value.appliedRouteId
                if (pending?.id == id && applied != null && applied == navigation.state.value.routeId) {
                    display = display.copy(navigationSource = NavigationSource.OWN)
                    ui = ui.copy(destination = DashboardDestination.DRIVE)
                }
            })
    }
}
