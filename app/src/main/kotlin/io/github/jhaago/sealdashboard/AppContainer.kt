package io.github.jhaago.sealdashboard

import android.os.SystemClock
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.session.SimulationSession
import io.github.jhaago.sealdashboard.demo.*
import kotlinx.coroutines.*
import io.github.jhaago.sealdashboard.assistant.*

/** Process-owned composition root. Telemetry is deliberately not persisted. */
class AppContainer {
    val clock = MonotonicClock { SystemClock.elapsedRealtime() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mock = MockVehicleDataProvider(SimulationEngine(SimulationConfig(), clock), scope, clock)
    val vehicle: VehicleDataProvider = mock
    val simulation: MockSimulationController = mock
    val session = SimulationSession(vehicle)
    private val demoNavigation = DemoNavigationProvider()
    val navigation: NavigationProvider = demoNavigation
    val previewRoutes = PreviewRouteActions(demoNavigation)
    val assistant = TripAssistantController(ScriptedAssistantService(DemoChargerSearchProvider()), previewRoutes, scope)
    val media: MediaProvider = DemoMediaProvider()
    val projection: ProjectionProvider = DemoProjectionProvider()
}
