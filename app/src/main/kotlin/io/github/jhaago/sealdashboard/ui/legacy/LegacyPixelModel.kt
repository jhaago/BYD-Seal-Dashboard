package io.github.jhaago.sealdashboard.ui.legacy

import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.TelemetryFormatter

enum class LegacyPixelPage { POWER, CHARGE, TYRES, PROFILE }

data class TyreReading(val pressure: String, val temperature: String)

data class LegacyPixelModel(
    val soc: String, val range: String, val voltage: String,
    val discharge: String, val regen: String, val charge: String,
    val packTemperature: String, val timeRemaining: String,
    val speed: String, val gear: String, val mode: String,
    val charging: Boolean, val discharging: Boolean, val recovering: Boolean,
    val tyres: Map<Wheel, TyreReading>, val tyreDataComplete: Boolean,
)

fun legacyPixelModel(v: VehicleState, f: TelemetryFormatter): LegacyPixelModel {
    fun positive(signal: Signal<Double>): Boolean = f.quality(signal) == SignalQuality.FRESH && (signal.value ?: 0.0) > 0
    val charging = f.text(v.charging.status) { it.name } == ChargeStatus.CHARGING.name && positive(v.charging.chargePowerKw)
    val pressures = Wheel.entries.associateWith { wheel ->
        val state = v.wheels[wheel] ?: WheelState()
        TyreReading(f.number(state.pressureKpa), f.number(state.temperatureC))
    }
    return LegacyPixelModel(
        soc = f.number(v.battery.stateOfChargePercent),
        range = f.number(v.battery.estimatedRangeKm),
        voltage = f.number(v.battery.voltageV),
        discharge = if (charging) "—" else f.number(v.powertrain.packPowerKw),
        regen = if (charging) "—" else f.number(v.powertrain.regenPowerKw),
        charge = if (charging) f.number(v.charging.chargePowerKw) else "—",
        packTemperature = f.number(v.environment.batteryTemperatureC),
        timeRemaining = "—", // There is no estimated charging time signal in VehicleState.
        speed = f.number(v.motion.speedKmh), gear = f.gear(v.motion.gear),
        mode = f.text(v.powertrain.driveMode) { it.name },
        charging = charging,
        discharging = !charging && positive(v.powertrain.packPowerKw),
        recovering = !charging && positive(v.powertrain.regenPowerKw),
        tyres = pressures,
        tyreDataComplete = pressures.values.all { it.pressure != "—" && it.temperature != "—" },
    )
}
