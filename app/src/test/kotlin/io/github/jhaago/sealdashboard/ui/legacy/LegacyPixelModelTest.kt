package io.github.jhaago.sealdashboard.ui.legacy

import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.TelemetryFormatter
import org.junit.Assert.*
import org.junit.Test

class LegacyPixelModelTest {
    private val profile = VehicleProfile.AUSTRALIAN_SEAL_DYNAMIC_2024
    private fun <T> fresh(value: T) = Signal(value, 0L, SignalSource.SIMULATED, SignalQuality.FRESH)

    @Test fun unavailableSignalsDoNotBecomeReferenceNumbersOrActiveFlow() {
        val model = legacyPixelModel(VehicleState(profile), TelemetryFormatter(4000, ProviderStatus.RUNNING))
        assertEquals("—", model.soc)
        assertEquals("—", model.voltage)
        assertEquals("—", model.timeRemaining)
        assertFalse(model.charging)
        assertFalse(model.discharging)
        assertEquals("—", model.tyres.getValue(Wheel.FRONT_LEFT).pressure)
    }

    @Test fun chargingSeparatesIncomingPowerFromDrivingOutput() {
        val vehicle = VehicleState(profile,
            charging = ChargingState(fresh(ChargeStatus.CHARGING), fresh(11.0)),
            powertrain = PowertrainState(packPowerKw = fresh(24.0), regenPowerKw = fresh(8.0)),
            battery = BatteryState(stateOfChargePercent = fresh(78.0)))
        val model = legacyPixelModel(vehicle, TelemetryFormatter(1000, ProviderStatus.RUNNING))
        assertEquals("11", model.charge)
        assertEquals("—", model.discharge)
        assertEquals("—", model.regen)
        assertEquals("—", model.timeRemaining)
        assertTrue(model.charging)
        assertFalse(model.discharging)
        assertFalse(model.recovering)
    }

    @Test fun staleWheelDataIsUnavailableRatherThanNormal() {
        val wheel = WheelState(fresh(240.0), fresh(31.0))
        val vehicle = VehicleState(profile, wheels = Wheel.entries.associateWith { wheel })
        assertTrue(legacyPixelModel(vehicle, TelemetryFormatter(1000, ProviderStatus.RUNNING)).tyreDataComplete)
        val stale = legacyPixelModel(vehicle, TelemetryFormatter(3000, ProviderStatus.RUNNING))
        assertEquals("—", stale.tyres.getValue(Wheel.FRONT_LEFT).pressure)
        assertFalse(stale.tyreDataComplete)
    }
}
