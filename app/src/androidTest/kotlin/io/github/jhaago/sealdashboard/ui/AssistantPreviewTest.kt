package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.assistant.*
import io.github.jhaago.sealdashboard.ui.theme.*
import kotlinx.coroutines.awaitCancellation
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssistantPreviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun typedFollowUpReviewsDismissesAndAppliesOneStopToOurMap() {
        val host = DashboardTestHost()
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp,720.dp)) }
        compose.onNodeWithTag("nav-ASSISTANT").performClick()
        send("Find a charger on my way")
        compose.onNodeWithTag("charger-option-demo-fast-2").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Availability: unknown").assertCountEquals(3)
        compose.onNodeWithTag("assistant-prompt-fast").performScrollTo().performClick()
        send("Add the second option to my trip")
        compose.onNodeWithTag("assistant-proposal").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("assistant-dismiss").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(host.navigation.state.value.waypoints.isEmpty()) }
        send("Add the second option to my trip")
        var proposalId = ""
        compose.runOnIdle { proposalId = host.assistant.state.value.proposal!!.id }
        compose.onNodeWithTag("assistant-apply").performScrollTo().performClick()
        compose.onNodeWithTag("screen-DRIVE").assertExists()
        compose.onNodeWithTag("street-map").assertExists()
        compose.runOnIdle {
            assertEquals("demo-fast-2", host.navigation.state.value.waypoints.single().chargerId)
            host.assistant.applyProposal(proposalId)
            assertEquals(1, host.navigation.state.value.waypoints.size)
            assertTrue(host.display.ownNavigation)
        }
    }

    @Test fun bothPresentationsKeepTelemetryAndNavigationVisibleInAllStyles() {
        val host = DashboardTestHost()
        var dimensions by mutableStateOf(Triple(400,720,1.3f))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, dimensions.third)) {
                host.Content(Modifier.requiredSize(dimensions.first.dp,dimensions.second.dp))
            }
        }
        compose.onNodeWithTag("nav-ASSISTANT").performClick()
        for (style in DashboardVisualStyle.entries) for (size in listOf(Triple(400,720,1.3f),Triple(1280,720,1f))) {
            compose.runOnIdle { host.display = host.display.copy(visualStyle = style); dimensions = size }
            if (style == DashboardVisualStyle.LEGACY_HMI && compose.onAllNodesWithTag("legacy-hmi-overview").fetchSemanticsNodes().isNotEmpty())
                compose.onNodeWithTag("nav-ASSISTANT").performClick()
            val bounds = listOf("assistant-speed","assistant-gear","assistant-soc","assistant-range","assistant-power").map {
                compose.onNodeWithTag(it,true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            }
            for (a in bounds.indices) for (b in a+1 until bounds.size) assertFalse(bounds[a].overlaps(bounds[b]))
            compose.onNodeWithTag("source-label").assertTextEquals("SIMULATED")
            DashboardDestination.entries.forEach { compose.onNodeWithTag("nav-${it.name}").assertIsDisplayed() }
            compose.onNodeWithTag("assistant-input").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("assistant-driving-toggle").performScrollTo().performClick()
            compose.onNodeWithTag("assistant-input").assertDoesNotExist()
            compose.onNodeWithText("Speech unavailable · scripted preview").assertExists()
            val prompt = compose.onNodeWithTag("assistant-prompt-find").performScrollTo().assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue(prompt.height >= with(compose.density) { 56.dp.toPx() })
            compose.onNodeWithTag("assistant-driving-toggle").performScrollTo().performClick()
        }
    }

    @Test fun clarificationFailureAndCancellationAreVisible() {
        val host = DashboardTestHost(AssistantService { request ->
            when(request.text) {
                "fail" -> throw IllegalStateException("fixture failure")
                "wait" -> awaitCancellation()
                else -> AssistantReply.Clarification("Which sample destination do you mean?")
            }
        })
        compose.setContent { host.Content(Modifier.requiredSize(1280.dp,720.dp)) }
        compose.onNodeWithTag("nav-ASSISTANT").performClick()
        send("ambiguous")
        compose.onNodeWithText("Which sample destination do you mean?").performScrollTo().assertIsDisplayed()
        send("fail")
        compose.onNodeWithTag("assistant-phase").performScrollTo().assertTextEquals("Failed")
        send("wait")
        compose.onNodeWithTag("assistant-phase").performScrollTo().assertTextEquals("Responding")
        compose.onNodeWithTag("assistant-cancel").performScrollTo().performClick()
        compose.onNodeWithTag("assistant-phase").assertTextEquals("Cancelled")
    }

    private fun send(text: String) {
        compose.onNodeWithTag("assistant-input").performScrollTo().performTextInput(text)
        compose.onNodeWithTag("assistant-send").performScrollTo().performClick()
        compose.waitForIdle()
    }
}
