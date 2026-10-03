package io.github.jhaago.sealdashboard.demo

import org.junit.Assert.*
import org.junit.Test

class DemoProvidersTest {
    @Test fun demosAreLabelledAndDoNotHostProjectionOrPlayback() {
        val navigation = DemoNavigationProvider().state.value
        val media = DemoMediaProvider().state.value
        val projection = DemoProjectionProvider().state.value
        assertEquals("SIMULATED ROUTE", navigation.sourceLabel)
        assertEquals("SAMPLE MEDIA", media.sourceLabel)
        assertEquals("ANDROID AUTO · SIMULATED PREVIEW", projection.sourceLabel)
        assertFalse(projection.isRealSession)
        assertFalse(media.playbackControlAvailable)
        assertTrue(navigation.instruction.isNotBlank())
    }
}
