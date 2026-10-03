package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*
import org.junit.Assert.*
import org.junit.Test

internal class FakeClock : MonotonicClock {
    var millis = 0L
    override fun nowMillis() = millis
}
internal fun advance(engine: SimulationEngine, clock: FakeClock, ticks: Int) {
    repeat(ticks) { clock.millis += 100; engine.step(0.1) }
}
internal fun drive(engine: SimulationEngine, speedKmh: Double) {
    engine.apply(MockCommand.Manual)
    engine.apply(MockCommand.SetGear(Gear.DRIVE))
    engine.apply(MockCommand.SetTargetSpeedKmh(speedKmh))
}

class SimulationEngineTest {
    private val clock = FakeClock()
    private val engine = SimulationEngine(SimulationConfig(), clock)

    @Test fun accelerationRampsSpeedAndUsesRearMotorOnly() {
        drive(engine, 60.0)
        advance(engine, clock, 10)
        assertTrue(engine.state.motion.speedKmh.value!! in 0.1..10.0)
        assertTrue(engine.state.powertrain.motorPowerKw.getValue(Axle.REAR).value!! > 0)
        assertEquals(setOf(Axle.REAR), engine.state.powertrain.motorPowerKw.keys)
        assertTrue(engine.state.powertrain.packPowerKw.value!! > 0)
        assertTrue(engine.state.battery.stateOfChargePercent.value!! < 80.0)
        assertTrue(engine.state.battery.estimatedRangeKm.value!! < 320.0)
    }
    @Test fun brakingProducesRecoveryAndReducesSpeed() {
        drive(engine, 60.0)
        advance(engine, clock, 200)
        val before = engine.state.motion.speedKmh.value!!
        engine.apply(MockCommand.SetPedals(0.0, 1.0))
        advance(engine, clock, 10)
        assertTrue(engine.state.motion.speedKmh.value!! < before)
        assertTrue(engine.state.powertrain.regenPowerKw.value!! > 0)
        assertTrue(engine.state.powertrain.packPowerKw.value!! < 0)
        assertTrue(engine.state.trip.recoveredDrivingEnergyKwh.value!! > 0)
    }
    @Test fun identicalInputsAndClockProduceIdenticalTelemetry() {
        val otherClock = FakeClock()
        val other = SimulationEngine(SimulationConfig(), otherClock)
        drive(engine, 60.0); drive(other, 60.0)
        advance(engine, clock, 100); advance(other, otherClock, 100)
        assertEquals(engine.state, other.state)
    }
    @Test fun integrationIsIndependentOfOuterStepSize() {
        val otherClock = FakeClock()
        val other = SimulationEngine(SimulationConfig(), otherClock)
        drive(engine, 60.0); drive(other, 60.0)
        advance(engine, clock, 10)
        otherClock.millis = 1000L; other.step(1.0)
        assertEquals(engine.state.motion.speedKmh.value!!, other.state.motion.speedKmh.value!!, 1e-6)
        assertEquals(engine.state.battery.stateOfChargePercent.value!!, other.state.battery.stateOfChargePercent.value!!, 1e-6)
        assertEquals(engine.state.trip.distanceKm.value!!, other.state.trip.distanceKm.value!!, 1e-6)
    }
    @Test fun packVoltageAndCurrentAgreeWithSignedPackPower() {
        repeat(1200) {
            clock.millis += 100; engine.step(0.1)
            val s = engine.state
            assertEquals(s.powertrain.packPowerKw.value!!, s.battery.voltageV.value!! * s.battery.currentA.value!! / 1000.0, 1e-6)
            assertTrue(s.powertrain.regenPowerKw.value!! >= 0)
            assertTrue(s.battery.stateOfChargePercent.value!! in 0.0..100.0)
        }
    }
    @Test fun chargingInterlocksTractionAndDoesNotCountAsTripRegen() {
        drive(engine, 40.0); advance(engine, clock, 100)
        engine.apply(MockCommand.SetCharging(true))
        val soc = engine.state.battery.stateOfChargePercent.value!!
        val recovered = engine.state.trip.recoveredDrivingEnergyKwh.value!!
        val distance = engine.state.trip.distanceKm.value!!
        advance(engine, clock, 100)
        assertEquals(0.0, engine.state.motion.speedKmh.value!!, 0.0)
        assertEquals(Gear.PARK, engine.state.motion.gear.value)
        assertTrue(engine.state.battery.stateOfChargePercent.value!! > soc)
        assertEquals(0.0, engine.state.powertrain.motorPowerKw.getValue(Axle.REAR).value!!, 0.0)
        assertEquals(recovered, engine.state.trip.recoveredDrivingEnergyKwh.value!!, 0.0)
        assertEquals(distance, engine.state.trip.distanceKm.value!!, 0.0)
        assertTrue(engine.apply(MockCommand.SetGear(Gear.DRIVE)) is CommandResult.Rejected)
    }
    @Test fun emptyBatteryCannotGeneratePositiveTraction() {
        engine.apply(MockCommand.SetSocPercent(0.0)); drive(engine, 80.0)
        advance(engine, clock, 100)
        assertEquals(0.0, engine.state.motion.speedKmh.value!!, 0.0)
        assertEquals(0.0, engine.state.battery.stateOfChargePercent.value!!, 0.0)
        assertEquals(0.0, engine.state.powertrain.motorPowerKw.getValue(Axle.REAR).value!!, 0.0)
    }
    @Test fun fullBatteryStopsPlugInCharge() {
        engine.apply(MockCommand.SetSocPercent(100.0))
        engine.apply(MockCommand.SetCharging(true)); advance(engine, clock, 100)
        assertEquals(100.0, engine.state.battery.stateOfChargePercent.value!!, 0.0)
        assertEquals(0.0, engine.state.charging.chargePowerKw.value!!, 0.0)
        assertEquals(ChargeStatus.COMPLETE, engine.state.charging.status.value)
    }
    @Test fun fullBatteryHasNoRegenAcceptanceAtBrakingStart() {
        drive(engine, 60.0); advance(engine, clock, 200)
        engine.apply(MockCommand.SetSocPercent(100.0))
        engine.apply(MockCommand.SetPedals(0.0, 1.0)); clock.millis += 50; engine.step(0.05)
        assertEquals(0.0, engine.state.powertrain.regenPowerKw.value!!, 0.0)
        assertTrue(engine.state.battery.stateOfChargePercent.value!! <= 100.0)
    }
    @Test fun parkCannotRetainMovingSpeed() {
        drive(engine, 60.0); advance(engine, clock, 100)
        engine.apply(MockCommand.SetGear(Gear.PARK))
        assertEquals(0.0, engine.state.motion.speedKmh.value!!, 0.0)
    }
    @Test fun doorChangesRequireStationarySimulation() {
        drive(engine, 60.0); advance(engine, clock, 100)
        assertTrue(engine.apply(MockCommand.SetDoor(Door.FRONT_RIGHT, true)) is CommandResult.Rejected)
        engine.apply(MockCommand.SetGear(Gear.PARK))
        assertEquals(CommandResult.Accepted, engine.apply(MockCommand.SetDoor(Door.FRONT_RIGHT, true)))
        assertEquals(true, engine.state.openings.doorsOpen.getValue(Door.FRONT_RIGHT).value)
    }
    @Test fun invalidCommandsCannotPoisonState() {
        val before = engine.state
        for (speed in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, 1000.0)) {
            assertTrue(engine.apply(MockCommand.SetTargetSpeedKmh(speed)) is CommandResult.Rejected)
        }
        for (soc in listOf(-1.0, 101.0, Double.NaN)) {
            assertTrue(engine.apply(MockCommand.SetSocPercent(soc)) is CommandResult.Rejected)
        }
        assertTrue(engine.apply(MockCommand.SetTyrePressureKpa(Wheel.FRONT_LEFT, -10.0)) is CommandResult.Rejected)
        assertEquals(before, engine.state)
    }
    @Test fun invalidStepsDoNotMutateState() {
        val before = engine.state
        for (dt in listOf(-0.1, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { engine.step(dt) }
        }
        assertEquals(before, engine.step(0.0))
    }
    @Test fun explicitPauseFreezesPhysicsAndResetRestoresBaseline() {
        drive(engine, 60.0); advance(engine, clock, 100)
        engine.apply(MockCommand.SetPaused(true))
        val paused = engine.state
        advance(engine, clock, 100)
        assertEquals(paused, engine.state)
        engine.reset()
        assertEquals(80.0, engine.state.battery.stateOfChargePercent.value!!, 0.0)
        assertEquals(0.0, engine.state.motion.speedKmh.value!!, 0.0)
        assertEquals(0.0, engine.state.trip.distanceKm.value!!, 0.0)
        assertEquals(Gear.PARK, engine.state.motion.gear.value)
        assertEquals(DriveMode.NORMAL, engine.state.powertrain.driveMode.value)
        assertTrue(engine.state.wheels.values.all { it.pressureKpa.value == 250.0 })
        assertTrue(engine.state.openings.doorsOpen.values.all { it.value == false })
    }
    @Test fun signedBatteryAccountingIsExactAndClamped() {
        assertEquals(78.0, integrateSocPercent(80.0, 50.0, 10.0, 360.0), 1e-9)
        assertEquals(81.0, integrateSocPercent(80.0, 50.0, -5.0, 360.0), 1e-9)
        assertEquals(0.0, integrateSocPercent(1.0, 50.0, 100.0, 3600.0), 0.0)
        assertEquals(100.0, integrateSocPercent(99.0, 50.0, -100.0, 3600.0), 0.0)
    }
    @Test fun wheelClimateAndDriveModeAreMutableOnlyInMockState() {
        engine.apply(MockCommand.SetTyrePressureKpa(Wheel.REAR_RIGHT, 235.0))
        engine.apply(MockCommand.SetClimate(true, 20.0, 3))
        engine.apply(MockCommand.SetDriveMode(DriveMode.ECO))
        assertEquals(235.0, engine.state.wheels.getValue(Wheel.REAR_RIGHT).pressureKpa.value!!, 0.0)
        assertEquals(20.0, engine.state.climate.targetTemperatureC.value!!, 0.0)
        assertEquals(DriveMode.ECO, engine.state.powertrain.driveMode.value)
        assertEquals(SignalSource.SIMULATED, engine.state.climate.enabled.source)
    }
}
