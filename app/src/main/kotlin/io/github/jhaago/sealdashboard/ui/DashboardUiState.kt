package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.core.*

enum class DashboardDestination(val label: String) { DRIVE("Drive"), VEHICLE("Vehicle"), ENERGY("Energy"), DEVELOPMENT("Development"), ASSISTANT("Assistant") }
data class DashboardUiState(
    val vehicle: VehicleState,
    val status: ProviderStatus,
    val nowMillis: Long,
    val destination: DashboardDestination = DashboardDestination.DRIVE,
    val history: TelemetryHistory = TelemetryHistory(),
    val diagnostics: List<DiagnosticEvent> = emptyList(),
    val capabilities: Map<SignalKey, SignalCapability> = emptyMap(),
) {
    val formatter get() = TelemetryFormatter(nowMillis, status)
    val sourceLabel get() = "SIMULATED"
}
