package io.github.jhaago.sealdashboard.core

import kotlinx.coroutines.flow.StateFlow

/** Read-only telemetry. Lifecycle ownership is singular; start/stop are idempotent. */
interface VehicleDataProvider {
    val state: StateFlow<VehicleState>
    val status: StateFlow<ProviderStatus>
    val capabilities: StateFlow<Map<SignalKey, SignalCapability>>
    val diagnostics: StateFlow<List<DiagnosticEvent>>
    val history: StateFlow<TelemetryHistory>
    fun start()
    fun stop()
}
