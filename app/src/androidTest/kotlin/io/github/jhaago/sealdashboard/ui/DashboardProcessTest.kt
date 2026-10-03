package io.github.jhaago.sealdashboard.ui

import android.os.Process
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.github.jhaago.sealdashboard.DashboardApplication
import io.github.jhaago.sealdashboard.MainActivity
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.theme.DashboardVisualStyle
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** These two tests run in separate instrumentation processes, with force-stop between them. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ProcessBoundaryTest

@ProcessBoundaryTest
class DashboardProcessTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val container get() = (context.applicationContext as DashboardApplication).container
    private val marker get() = File(context.filesDir, "process-restart-fixture.txt")

    @Test fun prepareForProcessRestart() {
        ActivityScenario.launch(MainActivity::class.java).use {
            assertEquals(CommandResult.Accepted, container.simulation.dispatch(MockCommand.Manual))
            assertEquals(CommandResult.Accepted, container.simulation.dispatch(MockCommand.SetSocPercent(37.0)))
            assertEquals(CommandResult.Accepted, container.simulation.dispatch(MockCommand.SetPaused(true)))
            DisplayPreferences(context).update(DisplaySettings(DriveLayout.FULL, mirrored = true, visualStyle = DashboardVisualStyle.SYSTEMS))
            // Flush the real preference file before the external process termination.
            assertTrue(context.getSharedPreferences("display", 0).edit().commit())
            assertEquals(37.0, container.vehicle.state.value.battery.stateOfChargePercent.value!!, 0.0)
            marker.writeText(Process.myPid().toString())
        }
    }

    @Test fun freshProcessRestoresOnlyDisplayPreferences() {
        assertTrue("CI must seed the prior process before this test", marker.isFile)
        assertNotEquals("This is a process boundary, not Activity recreation", marker.readText().toInt(), Process.myPid())
        assertEquals(DisplaySettings(DriveLayout.FULL, mirrored = true, visualStyle = DashboardVisualStyle.SYSTEMS), DisplayPreferences(context).state.value)
        assertEquals(80.0, container.vehicle.state.value.battery.stateOfChargePercent.value!!, 0.0)
        assertEquals(0.0, container.vehicle.state.value.motion.speedKmh.value!!, 0.0)
        assertEquals(0.0, container.vehicle.state.value.trip.distanceKm.value!!, 0.0)
        assertEquals(Gear.PARK, container.vehicle.state.value.motion.gear.value)
        assertFalse(container.simulation.simulationStatus.value.paused)
        assertTrue(container.simulation.history.value.packPower.isEmpty())
        marker.delete()
        DisplayPreferences(context).update(DisplaySettings())
    }
}
