package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.core.*
import org.junit.Assert.*
import org.junit.Test

class TelemetryFormatterTest {
    private fun <T> fresh(value: T?) = Signal(value, 0, SignalSource.SIMULATED, SignalQuality.FRESH)
    @Test fun missingSocAndGearAreNeverZeroOrPark() {
        val f = TelemetryFormatter(0, ProviderStatus.RUNNING)
        assertEquals("—", f.number(fresh<Double>(null)))
        assertEquals("—", f.gear(fresh<Gear>(null)))
    }
    @Test fun aSilentSampleAgesWithoutANewEmission() {
        val sample = fresh(42.0)
        assertEquals("42", TelemetryFormatter(2000, ProviderStatus.RUNNING).number(sample))
        val f = TelemetryFormatter(2001, ProviderStatus.RUNNING)
        assertEquals("—", f.number(sample))
        assertEquals(SignalQuality.STALE, f.quality(sample))
    }
    @Test fun disconnectAndErrorImmediatelyInvalidateLiveReadings() {
        for (status in listOf(ProviderStatus.DISCONNECTED, ProviderStatus.ERROR)) {
            val f = TelemetryFormatter(0, status)
            assertEquals("—", f.number(fresh(42.0)))
            assertEquals(SignalQuality.STALE, f.quality(fresh(42.0)))
        }
    }
    @Test fun nonFiniteValueCannotRenderAsTelemetry() {
        val f = TelemetryFormatter(0, ProviderStatus.RUNNING)
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY))
            assertEquals("—", f.number(fresh(value)))
    }
    @Test fun signedPowerAndRealGearArePreserved() {
        val f = TelemetryFormatter(0, ProviderStatus.RUNNING)
        assertEquals("−12.5", f.number(fresh(-12.5), 1))
        assertEquals("D", f.gear(fresh(Gear.DRIVE)))
        assertEquals("R", f.gear(fresh(Gear.REVERSE)))
    }
}
