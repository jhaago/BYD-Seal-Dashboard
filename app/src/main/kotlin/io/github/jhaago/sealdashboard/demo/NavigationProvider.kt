package io.github.jhaago.sealdashboard.demo

import kotlinx.coroutines.flow.*

/** Normalised fictional-map coordinates; never geographic latitude/longitude. */
data class RoutePoint(val x: Float, val y: Float) {
    init { require(x.isFinite() && y.isFinite() && x in 0f..1f && y in 0f..1f) }
}
data class MapChargerMarker(val id: String, val point: RoutePoint)
data class NavigationState(
    val sourceLabel: String = "SIMULATED ROUTE",
    val instruction: String = "Follow the coastal route",
    val distanceKm: Double = 8.4,
    val minutes: Int = 12,
    val destination: String = "Harbour lookout · demo",
    val nextTurn: String = "Turn right onto Harbour Way",
    val estimateSource: String = "DEMO ESTIMATE",
    val chargerMarkers: List<MapChargerMarker> = listOf(MapChargerMarker("demo-near-1", RoutePoint(.28f,.66f)),
        MapChargerMarker("demo-fast-2", RoutePoint(.52f,.35f)), MapChargerMarker("demo-fast-3", RoutePoint(.82f,.23f))),
    val route: List<RoutePoint> = listOf(RoutePoint(.12f, .85f), RoutePoint(.32f, .74f),
        RoutePoint(.35f, .4f), RoutePoint(.67f, .3f), RoutePoint(.84f, .12f)),
)
interface NavigationProvider { val state: StateFlow<NavigationState> }
class DemoNavigationProvider : NavigationProvider {
    override val state = MutableStateFlow(NavigationState()).asStateFlow()
}
