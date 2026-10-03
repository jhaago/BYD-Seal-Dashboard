package io.github.jhaago.sealdashboard.core

/** Historical values retain their timestamp/source/quality, including missing-value gaps. */
data class TelemetryHistory(
    val packPower: List<Signal<Double>> = emptyList(),
    val stateOfCharge: List<Signal<Double>> = emptyList(),
)
