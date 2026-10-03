package io.github.jhaago.sealdashboard.assistant

data class AssistantRequest(val text: String, val routeId: String, val optionIds: List<String>)
enum class ChargerAvailability { UNKNOWN, AVAILABLE, UNAVAILABLE }
data class ChargerOption(val id: String, val name: String, val connector: String,
    val advertisedKw: Double?, val detourMinutes: Int?, val availability: ChargerAvailability,
    val sourceLabel: String) {
    init {
        require(id.isNotBlank() && name.isNotBlank() && sourceLabel.isNotBlank())
        require(advertisedKw == null || (advertisedKw.isFinite() && advertisedKw > 0))
        require(detourMinutes == null || detourMinutes >= 0)
    }
}
data class RouteProposal(val id: String, val baseRouteId: String, val chargerId: String) {
    init { require(id.isNotBlank() && baseRouteId.isNotBlank() && chargerId.isNotBlank()) }
}
sealed interface AssistantReply {
    data class Message(val text: String) : AssistantReply
    data class ChargerOptions(val options: List<ChargerOption>, val reason: String) : AssistantReply
    data class Clarification(val text: String) : AssistantReply
    data class RouteProposal(val proposal: io.github.jhaago.sealdashboard.assistant.RouteProposal) : AssistantReply
}
