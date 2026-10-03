package io.github.jhaago.sealdashboard.core

/** Elapsed-time clock. Never supply wall-clock/calendar milliseconds. */
fun interface MonotonicClock {
    fun nowMillis(): Long
}
