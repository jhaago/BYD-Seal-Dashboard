package io.github.jhaago.sealdashboard.mock

import org.junit.Assert.*
import org.junit.Test

class TelemetryHistoryTest {
    @Test fun historiesSampleAtOneHzAndRemainBoundedBeyondTwentyMinutes() {
        val clock = FakeClock()
        val engine = SimulationEngine(SimulationConfig(), clock)
        val buffer = TelemetryHistoryBuffer()
        repeat(12050) {
            clock.millis += 100; engine.step(0.1); buffer.record(engine.state)
        }
        assertEquals(60, buffer.snapshot().packPower.size)
        assertEquals(900, buffer.snapshot().stateOfCharge.size)
        assertTrue(buffer.snapshot().packPower.zipWithNext().all { (a, b) -> b.observedAtMillis - a.observedAtMillis >= 1000L })
    }
    @Test fun resetClearsHistoryAndPausedTimeAddsNoSamples() {
        val clock = FakeClock()
        val engine = SimulationEngine(SimulationConfig(), clock)
        val buffer = TelemetryHistoryBuffer()
        repeat(20) { clock.millis += 100; engine.step(0.1); buffer.record(engine.state) }
        val before = buffer.snapshot()
        engine.apply(MockCommand.SetPaused(true))
        repeat(20) { clock.millis += 100; engine.step(0.1); buffer.record(engine.state) }
        assertEquals(before, buffer.snapshot())
        buffer.reset()
        assertTrue(buffer.snapshot().packPower.isEmpty())
        assertTrue(buffer.snapshot().stateOfCharge.isEmpty())
    }
}
