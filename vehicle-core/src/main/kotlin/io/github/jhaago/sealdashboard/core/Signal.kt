package io.github.jhaago.sealdashboard.core

enum class SignalSource { SIMULATED, MEASURED, DERIVED }
enum class SignalQuality { FRESH, STALE, UNAVAILABLE, ERROR }

/** Null means unavailable, never an implicit zero, closed door, or Park gear. */
data class Signal<T>(
    val value: T?,
    val observedAtMillis: Long,
    val source: SignalSource,
    val quality: SignalQuality,
) {
    companion object {
        fun <T> unavailable(source: SignalSource = SignalSource.MEASURED): Signal<T> =
            Signal(null, 0L, source, SignalQuality.UNAVAILABLE)
    }
}

/** Evaluate when displaying: a silent source can age without emitting state. */
fun <T> Signal<T>.effectiveQuality(nowMillis: Long, timeoutMillis: Long): SignalQuality {
    require(timeoutMillis >= 0) { "Freshness timeout must be nonnegative" }
    if (quality == SignalQuality.ERROR) return SignalQuality.ERROR
    if (value == null || quality == SignalQuality.UNAVAILABLE) return SignalQuality.UNAVAILABLE
    if (quality == SignalQuality.STALE) return SignalQuality.STALE
    // A changed clock origin/future sample must not extend the freshness window.
    if (nowMillis < observedAtMillis) return SignalQuality.STALE
    val age = nowMillis - observedAtMillis
    return if (age < 0 || age > timeoutMillis) SignalQuality.STALE else SignalQuality.FRESH
}
