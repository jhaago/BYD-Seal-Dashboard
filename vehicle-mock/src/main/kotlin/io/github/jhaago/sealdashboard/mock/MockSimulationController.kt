package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import kotlinx.coroutines.flow.StateFlow

enum class SimulationMode { DEMO, MANUAL }
data class SimulationStatus(
    val mode: SimulationMode,
    val paused: Boolean,
    val targetSpeedKmh: Double,
    val throttle: Double,
    val brake: Double,
    val config: SimulationConfig,
    val integrationError: String?,
)
sealed interface CommandResult {
    data object Accepted : CommandResult
    data class Rejected(val reason: String) : CommandResult
}

/** Commands operate on simulation only. No real provider implements this interface. */
sealed interface MockCommand {
    data object Demo : MockCommand
    data object Manual : MockCommand
    data object Reset : MockCommand
    data class SetPaused(val paused: Boolean) : MockCommand
    data class SetTargetSpeedKmh(val value: Double) : MockCommand
    data class SetPedals(val throttle: Double, val brake: Double) : MockCommand
    data class SetSocPercent(val value: Double) : MockCommand
    data class SetGear(val value: Gear) : MockCommand
    data class SetDriveMode(val value: DriveMode) : MockCommand
    data class SetTyrePressureKpa(val wheel: Wheel, val value: Double) : MockCommand
    data class SetDoor(val door: Door, val open: Boolean) : MockCommand
    data class SetBoot(val open: Boolean) : MockCommand
    data class SetCharging(val enabled: Boolean) : MockCommand
    data class SetClimate(val enabled: Boolean, val targetC: Double, val fanLevel: Int) : MockCommand
    data class ReplaceConfig(val value: SimulationConfig) : MockCommand
    data class SetFault(val signal: SignalKey, val quality: SignalQuality?) : MockCommand
    data class SetProviderError(val message: String?) : MockCommand
    data object ClearFaults : MockCommand
}
interface MockSimulationController {
    val simulationStatus: StateFlow<SimulationStatus>
    val history: StateFlow<TelemetryHistory>
    fun dispatch(command: MockCommand): CommandResult
}
