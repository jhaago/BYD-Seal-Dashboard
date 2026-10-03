package io.github.jhaago.sealdashboard.mock

import io.github.jhaago.sealdashboard.core.*

internal data class DemoFrame(val gear: Gear, val targetKmh: Double, val charging: Boolean, val driveMode: DriveMode)

/** A 93-second repeatable journey; no random numbers or real vehicle interactions. */
internal object DemoScenario {
    fun frame(elapsedSeconds: Double): DemoFrame {
        val t = elapsedSeconds % 93.0
        val cycle = (elapsedSeconds / 93.0).toLong() % 3
        val mode = listOf(DriveMode.NORMAL, DriveMode.ECO, DriveMode.SPORT)[cycle.toInt()]
        return when {
            t < 3.0 -> DemoFrame(Gear.PARK, 0.0, false, mode)
            t < 48.0 -> DemoFrame(Gear.DRIVE, 60.0, false, mode)
            t < 73.0 -> DemoFrame(Gear.DRIVE, 0.0, false, mode)
            else -> DemoFrame(Gear.PARK, 0.0, true, mode)
        }
    }
}
