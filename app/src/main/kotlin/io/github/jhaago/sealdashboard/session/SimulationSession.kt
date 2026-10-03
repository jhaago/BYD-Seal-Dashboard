package io.github.jhaago.sealdashboard.session

import io.github.jhaago.sealdashboard.core.VehicleDataProvider

/** Application foreground owner, never an Activity or screen-owned polling job. */
class SimulationSession(private val provider: VehicleDataProvider) {
    private var foreground = false
    @Synchronized fun onForegroundChanged(foreground: Boolean) {
        if (this.foreground == foreground) return
        this.foreground = foreground
        if (foreground) provider.start() else provider.stop()
    }
}
