package io.github.jhaago.sealdashboard.ui.legacy

import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.ui.TelemetryFormatter
import org.junit.Assert.*
import org.junit.Test

class LegacyPixelModelTest {
    private val profile = VehicleProfile.AUSTRALIAN_SEAL_DYNAMIC_2024

    @Test fun unavailableSignalsDoNotBecomeReferenceNumbersOrActiveFlow() {
        val model = legacyPixelModel(VehicleState(profile), TelemetryFormatter(4000, ProviderStatus.RUNNING))
        assertEquals("—", model.soc)
        assertEquals("—", model.voltage)
        assertEquals("—", model.timeRemaining)
        assertFalse(model.charging)
        assertFalse(model.discharging)
        assertEquals("—", model.tyres.getValue(Wheel.FRONT_LEFT).pressure)
    }
}
