package io.github.jhaago.sealdashboard.assistant

import io.github.jhaago.sealdashboard.demo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class AssistantPhase { Ready, Responding, ChoiceRequired, ProposalReady, Failed, Cancelled }
data class AssistantTurn(val fromUser: Boolean, val text: String)
data class ChargerSelection(val routeId: String, val optionsRevision: Long, val chargerId: String)
data class AssistantState(val routeId: String = "demo-1", val phase: AssistantPhase = AssistantPhase.Ready,
    val transcript: List<AssistantTurn> = emptyList(), val options: List<ChargerOption> = emptyList(),
    val optionsRevision: Long = 0, val rankingReason: String = "", val proposal: RouteProposal? = null,
    val appliedRouteId: String? = null) {
    fun selectionFor(chargerId: String): ChargerSelection {
        require(options.any { it.id == chargerId })
        return ChargerSelection(routeId, optionsRevision, chargerId)
    }
}

/** Serializes proposals and request generations. Never accepts vehicle command dependencies. */
class TripAssistantController(private val service: AssistantService, private val routes: PreviewRouteActions,
    private val scope: CoroutineScope) {
    private val lock = Any()
    private var generation = 0L
    private var job: Job? = null
    private val mutableState = MutableStateFlow(AssistantState(routeId = routes.currentState.routeId))
    val state = mutableState.asStateFlow()

    fun submit(text: String) = synchronized(lock) {
        if (text.isBlank()) return@synchronized
        val ticket = ++generation
        job?.cancel()
        val old = mutableState.value
        val routeId = routes.currentState.routeId
        val options = if (old.routeId == routeId) old.options else emptyList()
        val request = AssistantRequest(text.trim(), routeId, options.map { it.id })
        mutableState.value = old.copy(routeId = routeId, phase = AssistantPhase.Responding,
            transcript = (old.transcript + AssistantTurn(true, request.text)).takeLast(30), options = options,
            rankingReason = if (options.isEmpty()) "" else old.rankingReason, proposal = null, appliedRouteId = null)
        job = scope.launch {
            try {
                val reply = service.respond(request)
                synchronized(lock) {
                    if (ticket != generation) return@synchronized
                    if (routes.currentState.routeId != request.routeId) {
                        message("The sample route changed. Search again before choosing a stop.", AssistantPhase.ChoiceRequired,
                            clearOptions = true, routeId = routes.currentState.routeId)
                    } else when (reply) {
                        is AssistantReply.Message -> message(reply.text, AssistantPhase.Ready)
                        is AssistantReply.Clarification -> message(reply.text, AssistantPhase.ChoiceRequired)
                        is AssistantReply.ChargerOptions -> {
                            val current = mutableState.value
                            mutableState.value = current.copy(phase = AssistantPhase.ChoiceRequired,
                                options = reply.options.toList(), optionsRevision = ticket, rankingReason = reply.reason,
                                transcript = current.transcript + AssistantTurn(false, reply.reason))
                        }
                        is AssistantReply.RouteProposal -> {
                            val current = mutableState.value
                            // A dismissed proposal can never acquire the identity of a later proposal.
                            val proposal = reply.proposal.copy(id = "${reply.proposal.id}-$ticket")
                            if (proposal.baseRouteId != current.routeId || proposal.chargerId !in current.options.map { it.id })
                                message("Search again and choose a current sample option.", AssistantPhase.ChoiceRequired, true)
                            else mutableState.value = current.copy(phase = AssistantPhase.ProposalReady, proposal = proposal,
                                transcript = current.transcript + AssistantTurn(false, "Review the sample stop, then Apply or Dismiss."))
                        }
                    }
                }
            } catch (_: CancellationException) {
                // Cancel/newer request already owns the visible state.
            } catch (_: Exception) {
                synchronized(lock) {
                    if (ticket == generation) message("The preview service is unavailable. Try the sample request again.", AssistantPhase.Failed, true)
                }
            }
        }
    }

    fun selectOption(selection: ChargerSelection) = synchronized(lock) {
        val current = mutableState.value
        val routeId = routes.currentState.routeId
        val option = current.options.find { it.id == selection.chargerId }
        if (selection.routeId != routeId || selection.routeId != current.routeId ||
            selection.optionsRevision != current.optionsRevision || option == null) {
            message("The charger options changed. Choose a current sample stop.", AssistantPhase.ChoiceRequired,
                clearOptions = selection.routeId != routeId, routeId = routeId)
            return@synchronized
        }
        val ticket = ++generation
        job?.cancel()
        val proposal = RouteProposal("selection-$ticket", routeId, option.id)
        mutableState.value = current.copy(phase = AssistantPhase.ProposalReady, proposal = proposal,
            appliedRouteId = null, transcript = current.transcript +
                AssistantTurn(true, "Review ${option.name}") +
                AssistantTurn(false, "Review the sample stop, then Apply or Dismiss."))
    }

    fun cancel() = synchronized(lock) {
        ++generation; job?.cancel()
        message("Request cancelled.", AssistantPhase.Cancelled, true)
    }

    fun applyProposal(id: String) = synchronized(lock) {
        val proposal = mutableState.value.proposal?.takeIf { it.id == id } ?: return@synchronized
        ++generation; job?.cancel()
        when (val result = routes.apply(proposal)) {
            is ProposalResult.Applied -> {
                message("Sample stop added. Our preview route was updated.", AssistantPhase.Ready, true, result.routeId)
                mutableState.value = mutableState.value.copy(appliedRouteId = result.routeId)
            }
            ProposalResult.Stale -> message("The sample route changed. Search again before applying a stop.", AssistantPhase.ChoiceRequired, true, routes.currentState.routeId)
            ProposalResult.UnknownCharger -> message("That sample charger is unavailable to this preview.", AssistantPhase.Failed, true)
            ProposalResult.AlreadyApplied -> message("That stop was already applied.", AssistantPhase.Ready, true, routes.currentState.routeId)
        }
    }

    fun dismissProposal(id: String) = synchronized(lock) {
        if (mutableState.value.proposal?.id != id) return@synchronized
        ++generation; job?.cancel()
        message("Proposal dismissed. The sample route was not changed.", AssistantPhase.Ready)
    }

    private fun message(text: String, phase: AssistantPhase, clearOptions: Boolean = false,
        routeId: String = mutableState.value.routeId) {
        val current = mutableState.value
        mutableState.value = current.copy(routeId = routeId, phase = phase, proposal = null, appliedRouteId = null,
            transcript = (current.transcript + AssistantTurn(false, text)).takeLast(30),
            options = if (clearOptions) emptyList() else current.options,
            rankingReason = if (clearOptions) "" else current.rankingReason)
    }
}
