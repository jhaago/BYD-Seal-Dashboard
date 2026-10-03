package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.core.*
import java.util.Locale

/** Evaluate against current presentation time, not only on source emissions. */
class TelemetryFormatter(private val nowMillis: Long, private val status: ProviderStatus) {
    fun <T> quality(signal: Signal<T>): SignalQuality {
        val quality = signal.effectiveQuality(nowMillis, 2000)
        if (quality != SignalQuality.FRESH) return quality
        if (signal.value is Double && !signal.value.isFinite()) return SignalQuality.ERROR
        return if (status == ProviderStatus.DISCONNECTED || status == ProviderStatus.ERROR) SignalQuality.STALE else quality
    }
    fun <T> text(signal: Signal<T>, render: (T) -> String): String =
        if (quality(signal) == SignalQuality.FRESH) signal.value?.let(render) ?: "—" else "—"
    fun number(signal: Signal<Double>, decimals: Int = 0): String {
        require(decimals in 0..3)
        return text(signal) { String.format(Locale.ROOT, "%.${decimals}f", it).replace('-', '−') }
    }
    fun gear(signal: Signal<Gear>): String = text(signal) {
        when (it) { Gear.PARK -> "P"; Gear.REVERSE -> "R"; Gear.NEUTRAL -> "N"; Gear.DRIVE -> "D" }
    }
}
