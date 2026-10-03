package io.github.jhaago.sealdashboard.core

enum class Axle { FRONT, REAR }

/** Vehicle identity/topology, deliberately independent of telemetry and model assumptions. */
data class VehicleProfile(
    val id: String,
    val model: String,
    val modelYear: Int,
    val variant: String,
    val market: String,
    val drivenAxles: Set<Axle>,
) {
    companion object {
        val AUSTRALIAN_SEAL_DYNAMIC_2024 = VehicleProfile(
            id = "au-seal-dynamic-2024", model = "BYD Seal", modelYear = 2024,
            variant = "Dynamic", market = "Australia", drivenAxles = setOf(Axle.REAR),
        )
    }
}
