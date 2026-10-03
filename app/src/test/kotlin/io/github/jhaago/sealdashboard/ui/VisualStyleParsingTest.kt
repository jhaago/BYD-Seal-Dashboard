package io.github.jhaago.sealdashboard.ui

import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

class VisualStyleParsingTest {
    @Test fun knownStylesRestoreAndMissingOrUnknownStylesUseModern() {
        assertEquals(DashboardVisualStyle.SYSTEMS, parseVisualStyle("SYSTEMS"))
        assertEquals(DashboardVisualStyle.TRON, parseVisualStyle("TRON"))
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle(null))
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle("future-style"))
        assertEquals(DashboardVisualStyle.MODERN, parseVisualStyle(""))
    }
}
