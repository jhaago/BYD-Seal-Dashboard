package io.github.jhaago.sealdashboard.core

import org.junit.Assert.*
import org.junit.Test

class EnergyMetricsTest {
    @Test fun dischargeHasPositivePackPower() {
        assertEquals(20.0, packPowerKw(400.0, 50.0), 1e-9)
    }
    @Test fun chargingAndRegenHaveNegativePackPower() {
        assertEquals(-10.0, packPowerKw(400.0, -25.0), 1e-9)
    }
    @Test fun noCurrentHasZeroPackPower() {
        assertEquals(0.0, packPowerKw(400.0, 0.0), 1e-9)
    }
    @Test fun efficiencyIsUnavailableBeforeMeaningfulDistance() {
        assertNull(efficiencyKwhPer100Km(0.0, 0.0))
        assertNull(efficiencyKwhPer100Km(1.0, 0.099))
    }
    @Test fun efficiencyUsesNetEnergyPerHundredKilometres() {
        assertEquals(15.0, efficiencyKwhPer100Km(1.5, 10.0)!!, 1e-9)
        assertEquals(10.0, efficiencyKwhPer100Km(0.01, 0.1)!!, 1e-9)
    }
    @Test fun downhillNetRecoveryIsNotRenamedDischarge() {
        assertEquals(-5.0, efficiencyKwhPer100Km(-0.5, 10.0)!!, 1e-9)
    }
    @Test fun invalidPowerInputsAreRejected() {
        for (voltage in listOf(0.0, -400.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { packPowerKw(voltage, 25.0) }
        }
        assertThrows(IllegalArgumentException::class.java) { packPowerKw(400.0, Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { packPowerKw(400.0, Double.NEGATIVE_INFINITY) }
    }
    @Test fun invalidEfficiencyInputsAreRejected() {
        for (distance in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { efficiencyKwhPer100Km(1.0, distance) }
        }
        assertThrows(IllegalArgumentException::class.java) { efficiencyKwhPer100Km(Double.NaN, 10.0) }
    }
}
