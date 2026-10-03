package io.github.jhaago.sealdashboard.core

enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE }
enum class DriveMode { ECO, NORMAL, SPORT }
enum class Wheel { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }
enum class Door { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }
enum class ChargeStatus { UNPLUGGED, CONNECTED, CHARGING, COMPLETE }

data class MotionState(
    val speedKmh: Signal<Double> = Signal.unavailable(),
    val gear: Signal<Gear> = Signal.unavailable(),
    val accelerationMps2: Signal<Double> = Signal.unavailable(),
    val gpsSpeedKmh: Signal<Double> = Signal.unavailable(),
)
data class BatteryState(
    val stateOfChargePercent: Signal<Double> = Signal.unavailable(),
    val estimatedRangeKm: Signal<Double> = Signal.unavailable(),
    val voltageV: Signal<Double> = Signal.unavailable(),
    val currentA: Signal<Double> = Signal.unavailable(),
)
data class PowertrainState(
    val motorPowerKw: Map<Axle, Signal<Double>> = emptyMap(),
    val packPowerKw: Signal<Double> = Signal.unavailable(),
    val regenPowerKw: Signal<Double> = Signal.unavailable(),
    val driveMode: Signal<DriveMode> = Signal.unavailable(),
)
data class WheelState(
    val pressureKpa: Signal<Double> = Signal.unavailable(),
    val temperatureC: Signal<Double> = Signal.unavailable(),
)
data class OpeningsState(
    val doorsOpen: Map<Door, Signal<Boolean>> = emptyMap(),
    val bootOpen: Signal<Boolean> = Signal.unavailable(),
    val windowOpenPercent: Map<Door, Signal<Double>> = emptyMap(),
)
data class ClimateState(
    val enabled: Signal<Boolean> = Signal.unavailable(),
    val targetTemperatureC: Signal<Double> = Signal.unavailable(),
    val fanLevel: Signal<Int> = Signal.unavailable(),
)
data class ChargingState(
    val status: Signal<ChargeStatus> = Signal.unavailable(),
    val chargePowerKw: Signal<Double> = Signal.unavailable(),
)
data class EnvironmentState(
    val outsideTemperatureC: Signal<Double> = Signal.unavailable(),
    val cabinTemperatureC: Signal<Double> = Signal.unavailable(),
    val batteryTemperatureC: Signal<Double> = Signal.unavailable(),
    val motorTemperatureC: Signal<Double> = Signal.unavailable(),
)
data class TripData(
    val distanceKm: Signal<Double> = Signal.unavailable(),
    val grossEnergyUsedKwh: Signal<Double> = Signal.unavailable(),
    val recoveredDrivingEnergyKwh: Signal<Double> = Signal.unavailable(),
    val netEnergyKwh: Signal<Double> = Signal.unavailable(),
    val efficiencyKwhPer100Km: Signal<Double> = Signal.unavailable(),
)

/** Published collections must be immutable snapshots; providers must copy before publishing. */
data class VehicleState(
    val profile: VehicleProfile,
    val motion: MotionState = MotionState(),
    val battery: BatteryState = BatteryState(),
    val powertrain: PowertrainState = PowertrainState(),
    val wheels: Map<Wheel, WheelState> = emptyMap(),
    val openings: OpeningsState = OpeningsState(),
    val climate: ClimateState = ClimateState(),
    val charging: ChargingState = ChargingState(),
    val environment: EnvironmentState = EnvironmentState(),
    val trip: TripData = TripData(),
    val sampledAtMillis: Long = 0L,
)
