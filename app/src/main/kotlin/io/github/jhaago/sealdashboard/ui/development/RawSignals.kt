package io.github.jhaago.sealdashboard.ui.development

import io.github.jhaago.sealdashboard.core.*

data class RawSignal(val name: String, val unit: String, val signal: Signal<*>)
/** Domain values for development inspection, not inferred vendor signal IDs. */
fun rawSignals(v: VehicleState): List<RawSignal> = buildList {
    add(RawSignal("motion.speed", "km/h", v.motion.speedKmh))
    add(RawSignal("motion.gear", "", v.motion.gear))
    add(RawSignal("motion.acceleration", "m/s²", v.motion.accelerationMps2))
    add(RawSignal("motion.gpsSpeed", "km/h", v.motion.gpsSpeedKmh))
    add(RawSignal("battery.soc", "%", v.battery.stateOfChargePercent))
    add(RawSignal("battery.range", "km", v.battery.estimatedRangeKm))
    add(RawSignal("battery.voltage", "V", v.battery.voltageV))
    add(RawSignal("battery.current", "A", v.battery.currentA))
    v.powertrain.motorPowerKw.forEach { (axle, signal) -> add(RawSignal("motor.${axle.name.lowercase()}.output", "kW", signal)) }
    add(RawSignal("powertrain.packPower", "kW", v.powertrain.packPowerKw))
    add(RawSignal("powertrain.regen", "kW", v.powertrain.regenPowerKw))
    add(RawSignal("powertrain.driveMode", "", v.powertrain.driveMode))
    Wheel.entries.forEach { wheel ->
        val state = v.wheels[wheel] ?: WheelState()
        add(RawSignal("wheel.${wheel.name}.pressure", "kPa", state.pressureKpa))
        add(RawSignal("wheel.${wheel.name}.temperature", "°C", state.temperatureC))
    }
    Door.entries.forEach { door ->
        add(RawSignal("door.${door.name}.open", "", v.openings.doorsOpen[door] ?: Signal.unavailable<Boolean>()))
        add(RawSignal("window.${door.name}.open", "%", v.openings.windowOpenPercent[door] ?: Signal.unavailable<Double>()))
    }
    add(RawSignal("boot.open", "", v.openings.bootOpen))
    add(RawSignal("climate.enabled", "", v.climate.enabled))
    add(RawSignal("climate.target", "°C", v.climate.targetTemperatureC))
    add(RawSignal("climate.fan", "level", v.climate.fanLevel))
    add(RawSignal("charging.status", "", v.charging.status))
    add(RawSignal("charging.packPower", "kW", v.charging.chargePowerKw))
    add(RawSignal("temperature.outside", "°C", v.environment.outsideTemperatureC))
    add(RawSignal("temperature.cabin", "°C", v.environment.cabinTemperatureC))
    add(RawSignal("temperature.battery", "°C", v.environment.batteryTemperatureC))
    add(RawSignal("temperature.motor", "°C", v.environment.motorTemperatureC))
    add(RawSignal("trip.distance", "km", v.trip.distanceKm))
    add(RawSignal("trip.gross", "kWh", v.trip.grossEnergyUsedKwh))
    add(RawSignal("trip.recoveredDriving", "kWh", v.trip.recoveredDrivingEnergyKwh))
    add(RawSignal("trip.net", "kWh", v.trip.netEnergyKwh))
    add(RawSignal("trip.efficiency", "kWh/100 km", v.trip.efficiencyKwhPer100Km))
}
