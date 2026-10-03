package io.github.jhaago.sealdashboard.assistant

/** A small documented conversation fixture, not a model or general language parser. */
class ScriptedAssistantService(private val search: ChargerSearchProvider) : AssistantService {
    override suspend fun respond(request: AssistantRequest): AssistantReply {
        val text = request.text.trim().lowercase().replace(Regex("\\s+"), " ")
        return when (text) {
            "find a charger on my way" -> AssistantReply.ChargerOptions(search.search(request.routeId), "Sample order · availability unknown")
            "show the smallest detour" -> AssistantReply.ChargerOptions(
                search.search(request.routeId).sortedBy { it.detourMinutes ?: Int.MAX_VALUE }, "Smallest demo detour first")
            "i need a faster charger" -> AssistantReply.ChargerOptions(
                search.search(request.routeId).sortedByDescending { it.advertisedKw ?: -1.0 }, "Highest advertised power first · actual rate unknown")
            else -> {
                val ordinal = when (text) {
                    "add the first option to my trip", "add option 1 to my trip" -> 0
                    "add the second option to my trip", "add option 2 to my trip" -> 1
                    "add the third option to my trip", "add option 3 to my trip" -> 2
                    else -> null
                }
                val id = ordinal?.let { request.optionIds.getOrNull(it) }
                if (id != null) AssistantReply.RouteProposal(RouteProposal("sample", request.routeId, id))
                else AssistantReply.Clarification("Please use a sample prompt and choose from the current sample options. No destination was guessed.")
            }
        }
    }
}
