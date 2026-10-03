package io.github.jhaago.sealdashboard.assistant

import io.github.jhaago.sealdashboard.demo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripAssistantControllerTest {
    @Test fun followUpsResolveAgainstTheCurrentOrderedOptionsAndApplyOnlyOnce() = runTest {
        val navigation = DemoNavigationProvider()
        val controller = TripAssistantController(ScriptedAssistantService(DemoChargerSearchProvider()), PreviewRouteActions(navigation), this)
        controller.submit("Find a charger on my way"); runCurrent()
        assertEquals(AssistantPhase.ChoiceRequired, controller.state.value.phase)
        assertEquals(listOf("demo-near-1","demo-fast-2","demo-fast-3"), controller.state.value.options.map { it.id })
        controller.submit("Show the smallest detour"); runCurrent()
        assertEquals(listOf(2,5,9), controller.state.value.options.map { it.detourMinutes })
        controller.submit("I need a faster charger"); runCurrent()
        assertEquals(listOf("demo-fast-3","demo-fast-2","demo-near-1"), controller.state.value.options.map { it.id })
        controller.submit("Add the second option to my trip"); runCurrent()
        val proposal = controller.state.value.proposal!!
        assertEquals("demo-fast-2", proposal.chargerId)
        assertEquals(AssistantPhase.ProposalReady, controller.state.value.phase)
        assertTrue(navigation.state.value.waypoints.isEmpty())
        controller.applyProposal(proposal.id); controller.applyProposal(proposal.id)
        assertEquals(1, navigation.state.value.waypoints.size)
        assertEquals(navigation.state.value.routeId, controller.state.value.appliedRouteId)
    }

    @Test fun unsupportedAndAmbiguousRequestsAskInsteadOfGuessing() = runTest {
        val navigation = DemoNavigationProvider()
        val controller = TripAssistantController(ScriptedAssistantService(DemoChargerSearchProvider()), PreviewRouteActions(navigation), this)
        controller.submit("Take me there"); runCurrent()
        assertEquals(AssistantPhase.ChoiceRequired, controller.state.value.phase)
        assertTrue(controller.state.value.transcript.last().text.contains("sample"))
        assertNull(controller.state.value.proposal)
        controller.submit("Add the second option to my trip"); runCurrent()
        assertNull(controller.state.value.proposal)
        assertTrue(navigation.state.value.waypoints.isEmpty())
    }

    @Test fun aNonCooperativeOldResponseCannotReplaceTheNewerConversation() = runTest {
        val service = AssistantService { request ->
            if (request.text == "old") withContext(NonCancellable) { delay(1000); AssistantReply.Message("late old response") }
            else AssistantReply.Message("new response")
        }
        val controller = TripAssistantController(service, PreviewRouteActions(DemoNavigationProvider()), this)
        controller.submit("old"); runCurrent()
        controller.submit("new"); runCurrent()
        advanceTimeBy(1001); runCurrent()
        assertEquals("new response", controller.state.value.transcript.last().text)
        assertFalse(controller.state.value.transcript.any { it.text == "late old response" })
    }

    @Test fun cancellationPreventsLateCompletion() = runTest {
        val service = AssistantService { withContext(NonCancellable) { delay(1000); AssistantReply.Message("late") } }
        val controller = TripAssistantController(service, PreviewRouteActions(DemoNavigationProvider()), this)
        controller.submit("wait"); runCurrent(); controller.cancel()
        advanceTimeBy(1001); runCurrent()
        assertEquals(AssistantPhase.Cancelled, controller.state.value.phase)
        assertFalse(controller.state.value.transcript.any { it.text == "late" })
    }

    @Test fun providerFailureIsVisibleAndDoesNotChangeTheRoute() = runTest {
        val navigation = DemoNavigationProvider()
        val service = ScriptedAssistantService(ChargerSearchProvider { throw IllegalStateException("test failure") })
        val controller = TripAssistantController(service, PreviewRouteActions(navigation), this)
        controller.submit("Find a charger on my way"); runCurrent()
        assertEquals(AssistantPhase.Failed, controller.state.value.phase)
        assertTrue(controller.state.value.transcript.last().text.contains("unavailable"))
        assertTrue(navigation.state.value.waypoints.isEmpty())
    }

    @Test fun staleProposalAndStaleOrdinalCannotAddTheWrongStop() = runTest {
        val navigation = DemoNavigationProvider()
        val actions = PreviewRouteActions(navigation)
        val controller = TripAssistantController(ScriptedAssistantService(DemoChargerSearchProvider()), actions, this)
        controller.submit("Find a charger on my way"); runCurrent()
        controller.submit("Add the second option to my trip"); runCurrent()
        val stale = controller.state.value.proposal!!
        actions.apply(RouteProposal("other", "demo-1", "demo-near-1"))
        val updated = navigation.state.value
        controller.applyProposal(stale.id)
        assertEquals(updated, navigation.state.value)
        assertNull(controller.state.value.appliedRouteId)
        controller.submit("Add the second option to my trip"); runCurrent()
        assertNull(controller.state.value.proposal)
        assertTrue(controller.state.value.options.isEmpty())
        assertEquals(updated, navigation.state.value)
    }

    @Test fun dismissedProposalCannotBeAppliedAndAReplacementHasANewIdentity() = runTest {
        val navigation = DemoNavigationProvider()
        val controller = TripAssistantController(ScriptedAssistantService(DemoChargerSearchProvider()), PreviewRouteActions(navigation), this)
        controller.submit("Find a charger on my way"); runCurrent()
        controller.submit("Add the second option to my trip"); runCurrent()
        val old = controller.state.value.proposal!!.id
        controller.dismissProposal(old); controller.applyProposal(old)
        assertTrue(navigation.state.value.waypoints.isEmpty())
        controller.submit("Add the second option to my trip"); runCurrent()
        assertNotEquals(old, controller.state.value.proposal!!.id)
        controller.applyProposal(old)
        assertTrue(navigation.state.value.waypoints.isEmpty())
    }

    @Test fun routeChangeDuringAResponseClearsItsOptions() = runTest {
        val navigation = DemoNavigationProvider()
        val actions = PreviewRouteActions(navigation)
        val service = AssistantService { delay(1000); AssistantReply.ChargerOptions(DemoChargerSearchProvider.options, "sample") }
        val controller = TripAssistantController(service, actions, this)
        controller.submit("search"); runCurrent()
        actions.apply(RouteProposal("other", "demo-1", "demo-fast-3"))
        advanceTimeBy(1001); runCurrent()
        assertTrue(controller.state.value.options.isEmpty())
        assertEquals(navigation.state.value.routeId, controller.state.value.routeId)
        assertNull(controller.state.value.proposal)
    }
}
