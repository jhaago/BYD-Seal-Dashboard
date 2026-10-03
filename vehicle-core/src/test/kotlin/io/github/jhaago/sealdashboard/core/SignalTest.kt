package io.github.jhaago.sealdashboard.core

import org.junit.Assert.*
import org.junit.Test

class SignalTest {
    private val speed = Signal(50.0, 1000L, SignalSource.SIMULATED, SignalQuality.FRESH)
    @Test fun sampleRemainsFreshWithinTimeout() {
        assertEquals(SignalQuality.FRESH, speed.effectiveQuality(2999L, 2000L))
    }
    @Test fun silentSampleAgesWithoutAnEmission() {
        assertEquals(SignalQuality.STALE, speed.effectiveQuality(3001L, 2000L))
    }
    @Test fun timeoutBoundaryIsStillFresh() {
        assertEquals(SignalQuality.FRESH, speed.effectiveQuality(3000L, 2000L))
    }
    @Test fun explicitStaleIsNotRevivedByRecentTimestamp() {
        assertEquals(SignalQuality.STALE, speed.copy(quality = SignalQuality.STALE).effectiveQuality(1001L, 2000L))
    }
    @Test fun errorDoesNotBecomeOrdinaryStale() {
        assertEquals(SignalQuality.ERROR, speed.copy(quality = SignalQuality.ERROR).effectiveQuality(9999L, 2000L))
    }
    @Test fun missingValueNeverAppearsFresh() {
        val missing = Signal<Double>(null, 1000L, SignalSource.MEASURED, SignalQuality.FRESH)
        assertEquals(SignalQuality.UNAVAILABLE, missing.effectiveQuality(1001L, 2000L))
    }
    @Test fun unsupportedOrUnavailableRemainsUnavailable() {
        val missing = Signal<Double>(null, 1000L, SignalSource.MEASURED, SignalQuality.UNAVAILABLE)
        assertEquals(SignalQuality.UNAVAILABLE, missing.effectiveQuality(9999L, 2000L))
    }
    @Test fun futureTimestampDoesNotGrantIndefiniteFreshness() {
        assertEquals(SignalQuality.STALE, speed.effectiveQuality(999L, 2000L))
    }
    @Test fun invalidFreshnessTimeoutIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { speed.effectiveQuality(1000L, -1L) }
    }
}
