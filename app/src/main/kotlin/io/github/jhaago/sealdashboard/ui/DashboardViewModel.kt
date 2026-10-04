package io.github.jhaago.sealdashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.jhaago.sealdashboard.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    provider: VehicleDataProvider,
    clock: MonotonicClock,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val selected = MutableStateFlow(DashboardDestination.DRIVE)
    private val presentationPulse = flow {
        while (currentCoroutineContext().isActive) { emit(Unit); delay(100) }
    }
    private val base = combine(provider.state, provider.status, provider.history, selected, presentationPulse) {
        vehicle, status, history, destination, _ ->
        // Sample after receiving telemetry so a snapshot cannot appear to come from the future.
        DashboardUiState(vehicle, status, clock.nowMillis(), destination, history)
    }
    val uiState = combine(base, provider.diagnostics, provider.capabilities) { ui, diagnostics, capabilities ->
        ui.copy(diagnostics = diagnostics, capabilities = capabilities)
    }.stateIn(scope ?: viewModelScope, SharingStarted.WhileSubscribed(5000),
        DashboardUiState(provider.state.value, provider.status.value, clock.nowMillis(),
            history = provider.history.value, diagnostics = provider.diagnostics.value, capabilities = provider.capabilities.value))
    fun selectDestination(destination: DashboardDestination) { selected.value = destination }

    class Factory(private val provider: VehicleDataProvider, private val clock: MonotonicClock) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == DashboardViewModel::class.java)
            return DashboardViewModel(provider, clock) as T
        }
    }
}
