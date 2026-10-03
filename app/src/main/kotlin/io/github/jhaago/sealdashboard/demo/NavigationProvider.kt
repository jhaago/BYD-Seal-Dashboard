package io.github.jhaago.sealdashboard.demo

import kotlinx.coroutines.flow.*

data class RoutePoint(val x: Float, val y: Float)
data class NavigationState(
    val sourceLabel: String = "SIMULATED ROUTE",
    val instruction: String = "Follow the coastal route",
    val distanceKm: Double = 8.4,
    val minutes: Int = 12,
    val route: List<RoutePoint> = listOf(RoutePoint(.12f, .85f), RoutePoint(.32f, .74f),
        RoutePoint(.35f, .4f), RoutePoint(.67f, .3f), RoutePoint(.84f, .12f)),
)
interface NavigationProvider { val state: StateFlow<NavigationState> }
class DemoNavigationProvider : NavigationProvider {
    override val state = MutableStateFlow(NavigationState()).asStateFlow()
}
