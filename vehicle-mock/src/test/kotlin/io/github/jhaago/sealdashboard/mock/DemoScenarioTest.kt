package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import org.junit.Assert.*
import org.junit.Test

class DemoScenarioTest {
    @Test fun demoActuallyDrivesRegeneratesAndCharges() {
        val clock = FakeClock()
        val engine = SimulationEngine(SimulationConfig(), clock)
        var drove = false; var regenerated = false; var charged = false
        repeat(1200) {
            clock.millis += 100; engine.step(0.1)
            drove = drove || engine.state.motion.speedKmh.value!! > 20.0
            regenerated = regenerated || engine.state.powertrain.regenPowerKw.value!! > 1.0
            charged = charged || engine.state.charging.status.value == ChargeStatus.CHARGING
        }
        assertTrue(drove); assertTrue(regenerated); assertTrue(charged)
    }
    @Test fun manualModeCannotBeOverwrittenByDemoTimeline() {
        val clock = FakeClock()
        val engine = SimulationEngine(SimulationConfig(), clock)
        advance(engine, clock, 100); drive(engine, 20.0); advance(engine, clock, 1100)
        assertEquals(Gear.DRIVE, engine.state.motion.gear.value)
        assertEquals(20.0, engine.state.motion.speedKmh.value!!, 1e-6)
        assertEquals(ChargeStatus.UNPLUGGED, engine.state.charging.status.value)
    }
}
