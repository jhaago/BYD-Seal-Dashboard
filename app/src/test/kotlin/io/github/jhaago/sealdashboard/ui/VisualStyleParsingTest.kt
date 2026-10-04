package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

class VisualStyleParsingTest {
    @Test fun knownStylesRestoreAndMissingOrUnknownStylesUseModern() {
        assertEquals("Legacy HMI", parseVisualStyle("LEGACY_HMI").label)
        assertEquals("Futuristic", parseVisualStyle("FUTURISTIC").label)
        assertEquals("Legacy HMI", parseVisualStyle("SYSTEMS").label)
        assertEquals("Futuristic", parseVisualStyle("TRON").label)
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle(null))
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle("future-style"))
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle(""))
    }
}
