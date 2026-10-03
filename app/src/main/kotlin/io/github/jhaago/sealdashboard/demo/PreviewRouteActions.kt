package io.github.jhaago.sealdashboard.demo

import io.github.jhaago.sealdashboard.assistant.*

sealed interface ProposalResult {
    data class Applied(val routeId: String) : ProposalResult
    data object Stale : ProposalResult
    data object AlreadyApplied : ProposalResult
    data object UnknownCharger : ProposalResult
}

/** Commands own only fictional navigation state; no projection or vehicle provider access. */
class PreviewRouteActions(private val navigation: DemoNavigationProvider) {
    private val appliedIds = mutableSetOf<String>()
    val currentState get() = navigation.state.value
    @Synchronized fun apply(proposal: RouteProposal): ProposalResult {
        if (proposal.id in appliedIds) return ProposalResult.AlreadyApplied
        val current = currentState
        if (proposal.baseRouteId != current.routeId) return ProposalResult.Stale
        val charger = DemoChargerSearchProvider.options.find { it.id == proposal.chargerId }
            ?: return ProposalResult.UnknownCharger
        val marker = current.chargerMarkers.find { it.id == charger.id } ?: return ProposalResult.UnknownCharger
        val nextId = "${current.routeId}-stop-${proposal.id}"
        val geometry = if (current.route.isEmpty()) listOf(marker.point) else
            current.route.dropLast(1) + marker.point + current.route.last()
        navigation.updatePreview(current.copy(routeId = nextId,
            waypoints = current.waypoints + RouteWaypoint(charger.id, charger.name, marker.point),
            route = geometry, minutes = current.minutes + (charger.detourMinutes ?: 0),
            nextTurn = "Continue to ${charger.name}"))
        appliedIds += proposal.id
        return ProposalResult.Applied(nextId)
    }
}
