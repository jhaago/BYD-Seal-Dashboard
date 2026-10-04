package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.mock.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @Test fun telemetryArrivingBetweenPresentationTicksRemainsFresh() = runTest {
        val clock = MonotonicClock { testScheduler.currentTime }
        val provider = MockVehicleDataProvider(SimulationEngine(SimulationConfig(), clock), backgroundScope, clock)
        val vm = DashboardViewModel(provider, clock, backgroundScope)
        val collector = backgroundScope.launch { vm.uiState.collect {} }; runCurrent()

        advanceTimeBy(50)
        provider.dispatch(MockCommand.SetSocPercent(79.0))
        runCurrent()

        assertEquals("79", vm.uiState.value.formatter.number(vm.uiState.value.vehicle.battery.socPercent))
        assertEquals(SignalQuality.FRESH, vm.uiState.value.formatter.quality(vm.uiState.value.vehicle.battery.socPercent))
        collector.cancel()
    }

    @Test fun presentationAgesSilentSamplesAndKeepsNavigationIndependent() = runTest {
        val clock = MonotonicClock { testScheduler.currentTime }
        val provider = MockVehicleDataProvider(SimulationEngine(SimulationConfig(), clock), backgroundScope, clock)
        val vm = DashboardViewModel(provider, clock, backgroundScope)
        val collector = backgroundScope.launch { vm.uiState.collect {} }; runCurrent()
        assertEquals("0", vm.uiState.value.formatter.number(vm.uiState.value.vehicle.motion.speedKmh))
        vm.selectDestination(DashboardDestination.ENERGY); runCurrent()
        assertEquals(DashboardDestination.ENERGY, vm.uiState.value.destination)
        advanceTimeBy(2100); runCurrent()
        assertEquals("—", vm.uiState.value.formatter.number(vm.uiState.value.vehicle.motion.speedKmh))
        assertEquals(0L, vm.uiState.value.vehicle.sampledAtMillis)
        collector.cancel()
    }
}
