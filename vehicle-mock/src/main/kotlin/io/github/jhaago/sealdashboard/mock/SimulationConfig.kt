package io.github.jhaago.sealdashboard.mock

/** Illustrative simulator assumptions, NOT calibrated BYD vehicle specifications. */
data class SimulationConfig(
    val capacityKwh: Double = 60.0,
    val massKg: Double = 2000.0,
    val maxMotorPowerKw: Double = 140.0,
    val maxRegenPowerKw: Double = 45.0,
    val chargingPowerKw: Double = 7.0,
    val consumptionKwhPerKm: Double = 0.15,
    val maxSpeedKmh: Double = 160.0,
) {
    init {
        require(capacityKwh.isFinite() && capacityKwh in 10.0..150.0)
        require(massKg.isFinite() && massKg in 500.0..5000.0)
        require(maxMotorPowerKw.isFinite() && maxMotorPowerKw in 1.0..500.0)
        require(maxRegenPowerKw.isFinite() && maxRegenPowerKw in 0.0..200.0)
        require(chargingPowerKw.isFinite() && chargingPowerKw in 0.1..250.0)
        require(consumptionKwhPerKm.isFinite() && consumptionKwhPerKm in 0.05..0.5)
        require(maxSpeedKmh.isFinite() && maxSpeedKmh in 10.0..250.0)
    }
}
