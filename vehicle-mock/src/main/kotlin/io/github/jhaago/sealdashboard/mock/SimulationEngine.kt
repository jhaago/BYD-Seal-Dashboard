package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import java.util.Collections
import kotlin.math.*

fun integrateSocPercent(socPercent: Double, capacityKwh: Double, packPowerKw: Double, elapsedSeconds: Double): Double {
    require(socPercent.isFinite() && socPercent in 0.0..100.0)
    require(capacityKwh.isFinite() && capacityKwh > 0)
    require(packPowerKw.isFinite() && elapsedSeconds.isFinite() && elapsedSeconds >= 0)
    val delta = packPowerKw * elapsedSeconds / 3600.0 / capacityKwh * 100.0
    require(delta.isFinite())
    return (socPercent - delta).coerceIn(0.0, 100.0)
}

/** Deterministic SI-unit model. Caller serializes access; Android and coroutine-free. */
class SimulationEngine(config: SimulationConfig, private val clock: MonotonicClock) {
    var config: SimulationConfig = config
        private set
    private var mode = SimulationMode.DEMO
    private var paused = false
    private var speedMps = 0.0
    private var socPercent = 80.0
    private var gear = Gear.PARK
    private var driveMode = DriveMode.NORMAL
    private var targetKmh = 0.0
    private var usePedals = false
    private var throttle = 0.0
    private var brake = 0.0
    private var charging = false
    private var climateEnabled = false
    private var climateTargetC = 22.0
    private var fanLevel = 2
    private var cabinC = 24.0
    private var motorC = 28.0
    private var batteryC = 28.0
    private var tyreC = 24.0
    private val pressures = Wheel.entries.associateWith { 250.0 }.toMutableMap()
    private val doors = Door.entries.associateWith { false }.toMutableMap()
    private var bootOpen = false
    private var distanceKm = 0.0
    private var grossKwh = 0.0
    private var recoveredKwh = 0.0
    private var elapsed = 0.0
    private var acceleration = 0.0
    private var motorKw = 0.0
    private var packKw = 0.0
    private var regenKw = 0.0
    private var chargeKw = 0.0
    private var providerError: String? = null
    private val faults = mutableMapOf<SignalKey, SignalQuality>()
    private val historyBuffer = TelemetryHistoryBuffer()
    var state: VehicleState = snapshot()
        private set
    val history get() = historyBuffer.snapshot()
    val simulationStatus get() = SimulationStatus(mode, paused, targetKmh, throttle, brake, config, providerError)

    fun step(elapsedSeconds: Double): VehicleState {
        require(elapsedSeconds.isFinite() && elapsedSeconds in 0.0..60.0) { "Simulation step must be finite, nonnegative, and at most 60 seconds" }
        if (elapsedSeconds == 0.0 || paused || providerError != null) return state
        val parts = ceil(elapsedSeconds / 0.05).toInt().coerceAtLeast(1)
        val dt = elapsedSeconds / parts
        repeat(parts) {
            if (mode == SimulationMode.DEMO) {
                val frame = DemoScenario.frame(elapsed)
                gear = frame.gear; targetKmh = frame.targetKmh; charging = frame.charging
                driveMode = frame.driveMode; usePedals = false
                if (gear == Gear.PARK) speedMps = 0.0
            }
            integrateStep(dt)
            elapsed += dt
        }
        state = snapshot()
        historyBuffer.record(state)
        return state
    }

    fun apply(command: MockCommand): CommandResult {
        fun rejected(reason: String) = CommandResult.Rejected(reason)
        // Validation completes before mode, values, or timestamps are mutated.
        when (command) {
            is MockCommand.SetTargetSpeedKmh -> if (!command.value.isFinite() || command.value !in 0.0..config.maxSpeedKmh) return rejected("Speed is outside the simulated range")
            is MockCommand.SetPedals -> if (!command.throttle.isFinite() || !command.brake.isFinite() || command.throttle !in 0.0..1.0 || command.brake !in 0.0..1.0) return rejected("Pedal input must be within 0–1")
            is MockCommand.SetSocPercent -> if (!command.value.isFinite() || command.value !in 0.0..100.0) return rejected("SOC must be within 0–100%")
            is MockCommand.SetTyrePressureKpa -> if (!command.value.isFinite() || command.value !in 0.0..500.0) return rejected("Pressure must be within 0–500 kPa")
            is MockCommand.SetDoor, is MockCommand.SetBoot -> if (speedMps > 0.001) return rejected("Opening changes require a stationary simulation")
            is MockCommand.SetGear -> {
                if (charging && command.value != Gear.PARK) return rejected("Disconnect simulated charging first")
                if (command.value in setOf(Gear.DRIVE, Gear.REVERSE) && (doors.values.any { it } || bootOpen)) return rejected("Close simulated openings before driving")
                if (speedMps > 0.001 && command.value in setOf(Gear.DRIVE, Gear.REVERSE) && command.value != gear) return rejected("Stop before changing direction")
            }
            is MockCommand.SetClimate -> if (!command.targetC.isFinite() || command.targetC !in 16.0..30.0 || command.fanLevel !in 0..7) return rejected("Climate setpoint or fan level is outside the simulated range")
            is MockCommand.ReplaceConfig -> if (!paused || speedMps > 0.001) return rejected("Pause a stationary simulation before editing assumptions")
            else -> Unit
        }
        if (charging && ((command is MockCommand.SetTargetSpeedKmh && command.value > 0) || (command is MockCommand.SetPedals && command.throttle > 0))) return rejected("Disconnect simulated charging first")
        when (command) {
            MockCommand.Demo -> { mode = SimulationMode.DEMO; elapsed = 0.0; usePedals = false }
            MockCommand.Manual -> mode = SimulationMode.MANUAL
            MockCommand.Reset -> { reset(); return CommandResult.Accepted }
            is MockCommand.SetPaused -> paused = command.paused
            is MockCommand.SetProviderError -> providerError = command.message?.take(500)
            is MockCommand.SetFault -> if (command.quality == null) faults.remove(command.signal) else faults[command.signal] = command.quality
            MockCommand.ClearFaults -> { faults.clear(); providerError = null }
            else -> {
                mode = SimulationMode.MANUAL
                when (command) {
                    is MockCommand.SetTargetSpeedKmh -> { targetKmh = command.value; usePedals = false }
                    is MockCommand.SetPedals -> { throttle = command.throttle; brake = command.brake; usePedals = true }
                    is MockCommand.SetSocPercent -> { socPercent = command.value; idlePower() }
                    is MockCommand.SetGear -> { gear = command.value; if (gear == Gear.PARK) { speedMps = 0.0; idlePower() } }
                    is MockCommand.SetDriveMode -> driveMode = command.value
                    is MockCommand.SetTyrePressureKpa -> pressures[command.wheel] = command.value
                    is MockCommand.SetDoor -> doors[command.door] = command.open
                    is MockCommand.SetBoot -> bootOpen = command.open
                    is MockCommand.SetCharging -> {
                        charging = command.enabled
                        if (charging) { speedMps = 0.0; gear = Gear.PARK; targetKmh = 0.0; throttle = 0.0; brake = 0.0 }
                        idlePower()
                    }
                    is MockCommand.SetClimate -> { climateEnabled = command.enabled; climateTargetC = command.targetC; fanLevel = command.fanLevel }
                    is MockCommand.ReplaceConfig -> { config = command.value; targetKmh = targetKmh.coerceAtMost(config.maxSpeedKmh) }
                    else -> Unit
                }
            }
        }
        state = snapshot()
        return CommandResult.Accepted
    }

    fun reset(): VehicleState {
        mode = SimulationMode.DEMO; paused = false; speedMps = 0.0; socPercent = 80.0
        gear = Gear.PARK; driveMode = DriveMode.NORMAL; targetKmh = 0.0
        throttle = 0.0; brake = 0.0; usePedals = false; charging = false
        climateEnabled = false; climateTargetC = 22.0; fanLevel = 2
        cabinC = 24.0; motorC = 28.0; batteryC = 28.0; tyreC = 24.0
        Wheel.entries.forEach { pressures[it] = 250.0 }; Door.entries.forEach { doors[it] = false }; bootOpen = false
        distanceKm = 0.0; grossKwh = 0.0; recoveredKwh = 0.0; elapsed = 0.0
        acceleration = 0.0; motorKw = 0.0; packKw = 0.0; regenKw = 0.0; chargeKw = 0.0
        providerError = null; faults.clear(); historyBuffer.reset()
        state = snapshot()
        return state
    }

    private fun idlePower() {
        acceleration = 0.0; motorKw = 0.0; regenKw = 0.0; chargeKw = 0.0
        packKw = if (socPercent > 0) auxiliaryPowerKw() else 0.0
        if (charging) {
            chargeKw = if (socPercent < 100) config.chargingPowerKw else 0.0
            packKw = -chargeKw
        }
    }
    private fun auxiliaryPowerKw() = 0.3 + if (climateEnabled) 0.8 + fanLevel * 0.1 else 0.0

    private fun integrateStep(dt: Double) {
        val aux = auxiliaryPowerKw()
        if (charging) {
            speedMps = 0.0; gear = Gear.PARK; acceleration = 0.0; motorKw = 0.0; regenKw = 0.0
            chargeKw = min(config.chargingPowerKw, (100.0 - socPercent) / 100.0 * config.capacityKwh * 3600.0 / dt).coerceAtLeast(0.0)
            packKw = -chargeKw // Grid covers auxiliaries; pack power is stored charging power.
        } else {
            chargeKw = 0.0
            val oldSpeed = speedMps
            val resistanceN = 200.0 + 0.34 * oldSpeed * oldSpeed
            val accelerationLimit = when (driveMode) { DriveMode.ECO -> 1.3; DriveMode.NORMAL -> 1.8; DriveMode.SPORT -> 2.2 }
            val driven = gear == Gear.DRIVE || gear == Gear.REVERSE
            val target = min(targetKmh, if (gear == Gear.REVERSE) 15.0 else config.maxSpeedKmh) / 3.6
            var requestedA = when {
                gear == Gear.PARK -> 0.0
                !driven || socPercent <= 0.0 -> -resistanceN / config.massKg - brake * 2.6
                usePedals -> if (brake > 0) -brake * 2.6 else throttle * accelerationLimit - if (throttle == 0.0) resistanceN / config.massKg else 0.0
                else -> ((target - oldSpeed) / dt).coerceIn(-2.6, accelerationLimit)
            }
            if (requestedA > 0) requestedA = min(requestedA, (config.maxMotorPowerKw * 1000.0 / max(oldSpeed, 0.5) - resistanceN) / config.massKg).coerceAtLeast(0.0)
            speedMps = if (gear == Gear.PARK) 0.0 else (oldSpeed + requestedA * dt).coerceIn(0.0, config.maxSpeedKmh / 3.6)
            acceleration = (speedMps - oldSpeed) / dt
            val meanSpeed = (oldSpeed + speedMps) / 2.0
            val mechanicalKw = if (driven && socPercent > 0) (config.massKg * acceleration + resistanceN) * meanSpeed / 1000.0 else 0.0
            regenKw = if (driven && socPercent < 100.0 && mechanicalKw < 0) min(-mechanicalKw * 0.7, config.maxRegenPowerKw) else 0.0
            // Limit recovery/discharge by actual remaining energy in this physics substep.
            regenKw = min(regenKw, (100.0 - socPercent) / 100.0 * config.capacityKwh * 3600.0 / dt + aux)
            motorKw = if (mechanicalKw >= 0) mechanicalKw.coerceAtMost(config.maxMotorPowerKw) else -regenKw / 0.7
            packKw = if (socPercent <= 0) 0.0 else if (motorKw >= 0) motorKw / 0.92 + aux else aux - regenKw
            if (packKw > 0) packKw = min(packKw, socPercent / 100.0 * config.capacityKwh * 3600.0 / dt)
            distanceKm += meanSpeed * dt / 1000.0
            grossKwh += max(packKw, 0.0) * dt / 3600.0
            recoveredKwh += max(-packKw, 0.0) * dt / 3600.0
        }
        socPercent = integrateSocPercent(socPercent, config.capacityKwh, packKw, dt)
        cabinC += ((if (climateEnabled) climateTargetC else 22.0) - cabinC) * dt / 120.0
        motorC += ((28.0 + abs(motorKw) * 0.4) - motorC) * dt / 180.0
        batteryC += ((28.0 + abs(packKw) * 0.08) - batteryC) * dt / 300.0
        tyreC += ((24.0 + speedMps * 0.15) - tyreC) * dt / 180.0
    }

    private fun <K, V> immutableMap(values: Map<K, V>): Map<K, V> = Collections.unmodifiableMap(LinkedHashMap(values))
    private fun snapshot(): VehicleState {
        val now = clock.nowMillis()
        fun <T> signal(value: T?, key: SignalKey, source: SignalSource = SignalSource.SIMULATED): Signal<T> {
            val quality = faults[key] ?: if (value == null) SignalQuality.UNAVAILABLE else SignalQuality.FRESH
            return Signal(if (quality == SignalQuality.UNAVAILABLE) null else value, now, source, quality)
        }
        val voltage = 360.0 + socPercent * 0.45
        val netKwh = grossKwh - recoveredKwh
        return VehicleState(
            profile = VehicleProfile.AUSTRALIAN_SEAL_DYNAMIC_2024,
            motion = MotionState(signal(speedMps * 3.6, SignalKey.SPEED), signal(gear, SignalKey.GEAR), signal(acceleration, SignalKey.ACCELERATION)),
            battery = BatteryState(
                signal(socPercent, SignalKey.SOC), signal(socPercent / 100.0 * config.capacityKwh / config.consumptionKwhPerKm, SignalKey.RANGE, SignalSource.DERIVED),
                signal(voltage, SignalKey.PACK_VOLTAGE), signal(packKw * 1000.0 / voltage, SignalKey.PACK_CURRENT),
            ),
            powertrain = PowertrainState(
                immutableMap(mapOf(Axle.REAR to signal(motorKw, SignalKey.MOTOR_POWER))), signal(packKw, SignalKey.PACK_POWER),
                signal(regenKw, SignalKey.REGEN_POWER), signal(driveMode, SignalKey.DRIVE_MODE),
            ),
            wheels = immutableMap(pressures.mapValues { (_, pressure) -> WheelState(
                signal(pressure * (tyreC + 273.15) / 297.15, SignalKey.TYRE_PRESSURES), signal(tyreC, SignalKey.TYRE_TEMPERATURES),
            ) }),
            openings = OpeningsState(immutableMap(doors.mapValues { signal(it.value, SignalKey.DOORS) }), signal(bootOpen, SignalKey.BOOT)),
            climate = ClimateState(signal(climateEnabled, SignalKey.CLIMATE), signal(climateTargetC, SignalKey.CLIMATE), signal(fanLevel, SignalKey.CLIMATE)),
            charging = ChargingState(signal(if (!charging) ChargeStatus.UNPLUGGED else if (socPercent >= 100.0) ChargeStatus.COMPLETE else ChargeStatus.CHARGING, SignalKey.CHARGING), signal(chargeKw, SignalKey.CHARGING)),
            environment = EnvironmentState(signal(22.0, SignalKey.OUTSIDE_TEMPERATURE), signal(cabinC, SignalKey.CABIN_TEMPERATURE), signal(batteryC, SignalKey.BATTERY_TEMPERATURE), signal(motorC, SignalKey.MOTOR_TEMPERATURE)),
            trip = TripData(signal(distanceKm, SignalKey.TRIP, SignalSource.DERIVED), signal(grossKwh, SignalKey.TRIP, SignalSource.DERIVED), signal(recoveredKwh, SignalKey.TRIP, SignalSource.DERIVED), signal(netKwh, SignalKey.TRIP, SignalSource.DERIVED), signal(efficiencyKwhPer100Km(netKwh, distanceKm), SignalKey.TRIP, SignalSource.DERIVED)),
            sampledAtMillis = now,
        )
    }
}
