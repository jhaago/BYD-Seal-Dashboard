package io.github.jhaago.sealdashboard.core

enum class ProviderStatus { IDLE, RUNNING, PAUSED, DISCONNECTED, ERROR }
enum class SignalCapability { SUPPORTED, UNSUPPORTED }
enum class SignalKey {
    SPEED, GEAR, ACCELERATION, GPS_SPEED, SOC, RANGE, PACK_VOLTAGE, PACK_CURRENT,
    MOTOR_POWER, PACK_POWER, REGEN_POWER, DRIVE_MODE, TYRE_PRESSURES, TYRE_TEMPERATURES,
    DOORS, BOOT, WINDOWS, CLIMATE, CHARGING, OUTSIDE_TEMPERATURE, CABIN_TEMPERATURE,
    BATTERY_TEMPERATURE, MOTOR_TEMPERATURE, TRIP,
}
enum class DiagnosticLevel { INFO, WARNING, ERROR }

/** Bounded provider diagnostic metadata, never vendor SDK objects or credentials. */
data class DiagnosticEvent(
    val id: Long,
    val observedAtMillis: Long,
    val level: DiagnosticLevel,
    val message: String,
    val signal: SignalKey? = null,
)
