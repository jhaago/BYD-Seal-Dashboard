package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import java.util.ArrayDeque
import java.util.Collections

/** Acquisition rate is independent of history rate; paused/frozen snapshots add nothing. */
class TelemetryHistoryBuffer {
    private val power = ArrayDeque<Signal<Double>>()
    private val soc = ArrayDeque<Signal<Double>>()
    private var lastSampleMillis: Long? = null

    fun record(state: VehicleState) {
        val last = lastSampleMillis
        if (last != null && (state.sampledAtMillis <= last || state.sampledAtMillis - last < 1000L)) return
        power.addLast(state.powertrain.packPowerKw)
        soc.addLast(state.battery.stateOfChargePercent)
        while (power.size > 60) power.removeFirst()
        while (soc.size > 900) soc.removeFirst()
        lastSampleMillis = state.sampledAtMillis
    }
    fun snapshot() = TelemetryHistory(
        Collections.unmodifiableList(power.toList()), Collections.unmodifiableList(soc.toList()),
    )
    fun reset() {
        power.clear(); soc.clear(); lastSampleMillis = null
    }
}
