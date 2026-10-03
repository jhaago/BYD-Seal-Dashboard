package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockVehicleDataProviderTest {
    private fun TestScope.provider(): MockVehicleDataProvider {
        val clock = MonotonicClock { testScheduler.currentTime }
        return MockVehicleDataProvider(SimulationEngine(SimulationConfig(), clock), backgroundScope, clock)
    }
    private fun drive(p: MockVehicleDataProvider) {
        p.dispatch(MockCommand.Manual)
        p.dispatch(MockCommand.SetGear(Gear.DRIVE))
        p.dispatch(MockCommand.SetTargetSpeedKmh(60.0))
    }
    @Test fun repeatedStartCreatesOnlyOneTickPerHundredMilliseconds() = runTest {
        val p = provider(); drive(p)
        p.start(); p.start(); runCurrent()
        assertEquals(0.0, p.state.value.motion.speedKmh.value!!, 0.0)
        advanceTimeBy(100); runCurrent()
        assertEquals(0.648, p.state.value.motion.speedKmh.value!!, 1e-6)
        assertEquals(ProviderStatus.RUNNING, p.status.value)
        p.stop()
    }
    @Test fun backgroundHourDoesNotAccrueHiddenPhysicsOnResume() = runTest {
        val p = provider(); drive(p); p.start(); runCurrent()
        advanceTimeBy(100); runCurrent(); p.stop()
        val frozen = p.state.value
        advanceTimeBy(3_600_000); runCurrent()
        assertEquals(frozen, p.state.value)
        p.start(); runCurrent(); advanceTimeBy(100); runCurrent()
        assertEquals(1.296, p.state.value.motion.speedKmh.value!!, 1e-6)
        assertTrue(p.state.value.trip.distanceKm.value!! - frozen.trip.distanceKm.value!! < 0.001)
        p.stop()
    }
    @Test fun explicitPauseSurvivesStopStartAndResetClearsSession() = runTest {
        val p = provider(); drive(p); p.start(); runCurrent(); advanceTimeBy(1000); runCurrent()
        p.dispatch(MockCommand.SetPaused(true)); val frozen = p.state.value
        p.stop(); advanceTimeBy(5000); p.start(); runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(frozen, p.state.value)
        assertEquals(ProviderStatus.PAUSED, p.status.value)
        assertTrue(p.simulationStatus.value.paused)
        p.dispatch(MockCommand.Reset)
        val v = p.state.value
        assertEquals(80.0, v.battery.stateOfChargePercent.value!!, 0.0)
        assertEquals(0.0, v.motion.speedKmh.value!!, 0.0)
        assertEquals(Gear.PARK, v.motion.gear.value)
        assertEquals(DriveMode.NORMAL, v.powertrain.driveMode.value)
        assertTrue(v.openings.doorsOpen.values.all { it.value == false })
        assertEquals(false, v.openings.bootOpen.value)
        assertTrue(v.wheels.values.all { it.pressureKpa.value == 250.0 })
        assertEquals(0.0, v.trip.distanceKm.value!!, 0.0)
        assertEquals(0.0, v.trip.netEnergyKwh.value!!, 0.0)
        assertTrue(p.history.value.packPower.isEmpty())
        p.stop()
    }
    @Test fun diagnosticsAreBoundedAndClearingErrorsNeverInventsMeasuredData() = runTest {
        val p = provider(); p.start(); runCurrent()
        repeat(120) { p.dispatch(MockCommand.SetProviderError("integration error $it")) }
        assertEquals(100, p.diagnostics.value.size)
        assertEquals("integration error 119", p.diagnostics.value.last().message)
        assertEquals(ProviderStatus.ERROR, p.status.value)
        p.dispatch(MockCommand.SetFault(SignalKey.SOC, SignalQuality.UNAVAILABLE))
        assertNull(p.state.value.battery.stateOfChargePercent.value)
        p.dispatch(MockCommand.ClearFaults)
        assertEquals(ProviderStatus.RUNNING, p.status.value)
        assertEquals(SignalSource.SIMULATED, p.state.value.battery.stateOfChargePercent.source)
        assertEquals(SignalQuality.FRESH, p.state.value.battery.stateOfChargePercent.quality)
        p.stop()
    }
    @Test fun unsupportedCapabilitiesStayExplicit() = runTest {
        val p = provider()
        assertEquals(SignalCapability.UNSUPPORTED, p.capabilities.value[SignalKey.WINDOWS])
        assertEquals(SignalCapability.UNSUPPORTED, p.capabilities.value[SignalKey.GPS_SPEED])
        assertEquals(SignalCapability.SUPPORTED, p.capabilities.value[SignalKey.SPEED])
        assertTrue(p.state.value.powertrain.motorPowerKw.keys == setOf(Axle.REAR))
    }
}
