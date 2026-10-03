package io.github.jhaago.sealdashboard

import android.os.SystemClock
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.session.SimulationSession
import kotlinx.coroutines.*

/** Process-owned composition root. Telemetry is deliberately not persisted. */
class AppContainer {
    val clock = MonotonicClock { SystemClock.elapsedRealtime() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mock = MockVehicleDataProvider(SimulationEngine(SimulationConfig(), clock), scope, clock)
    val vehicle: VehicleDataProvider = mock
    val simulation: MockSimulationController = mock
    val session = SimulationSession(vehicle)
}
