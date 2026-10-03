package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Collections

/** One serialized mock session. Fixed foreground steps never catch up background time. */
class MockVehicleDataProvider(
    private val engine: SimulationEngine,
    private val scope: CoroutineScope,
    private val clock: MonotonicClock,
) : VehicleDataProvider, MockSimulationController {
    private val lock = Any()
    private var ticker: Job? = null
    private var running = false
    private var started = false
    private var generation = 0L
    private var eventId = 0L
    private val mutableState = MutableStateFlow(engine.state)
    private val mutableStatus = MutableStateFlow(ProviderStatus.IDLE)
    private val mutableSimulationStatus = MutableStateFlow(engine.simulationStatus)
    private val mutableHistory = MutableStateFlow(engine.history)
    private val mutableDiagnostics = MutableStateFlow<List<DiagnosticEvent>>(emptyList())
    private val mutableCapabilities = MutableStateFlow(Collections.unmodifiableMap(
        SignalKey.entries.associateWith {
            if (it == SignalKey.WINDOWS || it == SignalKey.GPS_SPEED) SignalCapability.UNSUPPORTED
            else SignalCapability.SUPPORTED
        },
    ))
    override val state = mutableState.asStateFlow()
    override val status = mutableStatus.asStateFlow()
    override val simulationStatus = mutableSimulationStatus.asStateFlow()
    override val history = mutableHistory.asStateFlow()
    override val diagnostics = mutableDiagnostics.asStateFlow()
    override val capabilities = mutableCapabilities.asStateFlow()

    override fun start() = synchronized(lock) {
        if (running) return@synchronized
        running = true; started = true
        val token = ++generation
        publish()
        ticker = scope.launch {
            while (isActive) {
                delay(100)
                synchronized(lock) {
                    // A cancelled old job cannot tick a newly resumed session.
                    if (running && generation == token) {
                        engine.step(0.1)
                        publish()
                    }
                }
            }
        }
    }

    override fun stop() = synchronized(lock) {
        if (!running) return@synchronized
        running = false; generation++
        ticker?.cancel(); ticker = null
        publish()
    }

    override fun dispatch(command: MockCommand): CommandResult = synchronized(lock) {
        val result = engine.apply(command)
        if (result is CommandResult.Rejected) {
            diagnostic(DiagnosticLevel.WARNING, result.reason)
        } else when (command) {
            MockCommand.Reset -> mutableDiagnostics.value = emptyList()
            is MockCommand.SetProviderError -> diagnostic(
                if (command.message == null) DiagnosticLevel.INFO else DiagnosticLevel.ERROR,
                command.message?.take(500) ?: "Simulated integration error cleared",
            )
            is MockCommand.SetFault -> diagnostic(
                if (command.quality == null) DiagnosticLevel.INFO else DiagnosticLevel.WARNING,
                "${command.signal}: ${command.quality ?: "fault cleared"}", command.signal,
            )
            MockCommand.ClearFaults -> diagnostic(DiagnosticLevel.INFO, "Simulated faults cleared")
            else -> Unit
        }
        publish()
        result
    }

    private fun publish() {
        mutableState.value = engine.state
        mutableHistory.value = engine.history
        mutableSimulationStatus.value = engine.simulationStatus
        mutableStatus.value = when {
            engine.simulationStatus.integrationError != null -> ProviderStatus.ERROR
            !started -> ProviderStatus.IDLE
            !running || engine.simulationStatus.paused -> ProviderStatus.PAUSED
            else -> ProviderStatus.RUNNING
        }
    }
    private fun diagnostic(level: DiagnosticLevel, message: String, signal: SignalKey? = null) {
        val event = DiagnosticEvent(++eventId, clock.nowMillis(), level, message, signal)
        mutableDiagnostics.value = Collections.unmodifiableList((mutableDiagnostics.value + event).takeLast(100))
    }
}
