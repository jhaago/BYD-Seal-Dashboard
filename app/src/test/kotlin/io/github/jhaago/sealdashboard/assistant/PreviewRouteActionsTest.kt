package io.github.jhaago.sealdashboard.assistant

import io.github.jhaago.sealdashboard.demo.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PreviewRouteActionsTest {
    @Test fun appliesOnceChangesRouteIdentityAndAddsOneOrderedStop() {
        val navigation = DemoNavigationProvider()
        val actions = PreviewRouteActions(navigation)
        val proposal = RouteProposal("proposal-1", "demo-1", "demo-fast-2")
        assertTrue(actions.apply(proposal) is ProposalResult.Applied)
        val applied = navigation.state.value
        assertNotEquals("demo-1", applied.routeId)
        assertEquals(1, applied.waypoints.size)
        assertEquals("demo-fast-2", applied.waypoints.single().chargerId)
        assertTrue(applied.route.contains(RoutePoint(.52f,.35f)))
        assertEquals(ProposalResult.AlreadyApplied, actions.apply(proposal))
        assertEquals(1, navigation.state.value.waypoints.size)
        assertEquals(applied, navigation.state.value)
    }

    @Test fun staleAndUnknownStopsLeaveTheRouteUntouched() {
        val navigation = DemoNavigationProvider()
        val actions = PreviewRouteActions(navigation)
        val original = navigation.state.value
        assertEquals(ProposalResult.Stale, actions.apply(RouteProposal("old", "old-route", "demo-fast-2")))
        assertEquals(ProposalResult.UnknownCharger, actions.apply(RouteProposal("bad", "demo-1", "unknown")))
        assertEquals(original, navigation.state.value)
    }

    @Test fun missingMetadataRemainsUnknownAndInvalidNumbersAreRejected() {
        val option = ChargerOption("missing", "Unknown charger", "Unknown", null, null,
            ChargerAvailability.UNKNOWN, "DEMO FIXTURE")
        assertNull(option.advertisedKw)
        assertNull(option.detourMinutes)
        for (power in listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0, 0.0)) {
            try { option.copy(advertisedKw = power); fail("Must reject invalid power $power") }
            catch (_: IllegalArgumentException) { }
        }
        try { option.copy(detourMinutes = -1); fail("Must reject negative detour") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun fixtureFactsAndUnknownAvailabilityComeFromSearchProvider() = runTest {
        val options = DemoChargerSearchProvider().search("demo-1")
        assertEquals(listOf("demo-near-1", "demo-fast-2", "demo-fast-3"), options.map { it.id })
        assertEquals(listOf(22.0,150.0,250.0), options.map { it.advertisedKw })
        assertEquals(listOf(2,5,9), options.map { it.detourMinutes })
        assertTrue(options.all { it.availability == ChargerAvailability.UNKNOWN && it.sourceLabel == "DEMO FIXTURE" })
    }
}
