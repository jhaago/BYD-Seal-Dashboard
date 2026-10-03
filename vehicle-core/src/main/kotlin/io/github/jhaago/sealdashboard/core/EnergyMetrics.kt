package io.github.jhaago.sealdashboard.core

/** Positive discharge; negative regenerative or plugged-in charging power. */
fun packPowerKw(voltageV: Double, currentA: Double): Double {
    require(voltageV.isFinite() && voltageV > 0) { "Pack voltage must be positive and finite" }
    require(currentA.isFinite()) { "Pack current must be finite" }
    val result = voltageV * currentA / 1000.0
    require(result.isFinite()) { "Pack power exceeds numeric range" }
    return result
}

/** Net efficiency is unavailable below 0.1 km; downhill recovery can be negative. */
fun efficiencyKwhPer100Km(netEnergyKwh: Double, distanceKm: Double): Double? {
    require(netEnergyKwh.isFinite()) { "Trip energy must be finite" }
    require(distanceKm.isFinite() && distanceKm >= 0) { "Trip distance must be nonnegative and finite" }
    if (distanceKm < 0.1) return null
    val result = netEnergyKwh / distanceKm * 100.0
    require(result.isFinite()) { "Efficiency exceeds numeric range" }
    return result
}
