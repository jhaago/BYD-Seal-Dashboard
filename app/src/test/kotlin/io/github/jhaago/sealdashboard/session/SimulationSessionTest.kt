package io.github.jhaago.sealdashboard.session

import io.github.jhaago.sealdashboard.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class SimulationSessionTest {
    @Test fun singularForegroundOwnerIsIdempotent() {
        val p = CountingProvider(); val session = SimulationSession(p)
        session.onForegroundChanged(false)
        session.onForegroundChanged(true); session.onForegroundChanged(true)
        assertEquals(1, p.starts)
        session.onForegroundChanged(false); session.onForegroundChanged(false)
        assertEquals(1, p.stops)
        session.onForegroundChanged(true)
        assertEquals(2, p.starts)
    }
    private class CountingProvider : VehicleDataProvider {
        var starts = 0; var stops = 0
        override val state = MutableStateFlow(VehicleState(VehicleProfile.AUSTRALIAN_SEAL_DYNAMIC_2024))
        override val status = MutableStateFlow(ProviderStatus.IDLE)
        override val capabilities = MutableStateFlow(emptyMap<SignalKey, SignalCapability>())
        override val diagnostics = MutableStateFlow(emptyList<DiagnosticEvent>())
        override val history = MutableStateFlow(TelemetryHistory())
        override fun start() { starts++ }
        override fun stop() { stops++ }
    }
}
