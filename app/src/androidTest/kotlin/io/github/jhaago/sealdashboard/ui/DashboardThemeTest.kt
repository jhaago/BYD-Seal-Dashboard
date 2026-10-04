package io.github.jhaago.sealdashboard.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DashboardThemeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun primaryTextInheritsTheWarmWhiteDashboardColor() {
        var inherited = Color.Unspecified
        compose.setContent { DashboardTheme { inherited = LocalContentColor.current; Text("Primary telemetry") } }
        compose.runOnIdle { assertEquals(DashboardColors.Text, inherited) }
    }

    @Test fun switchingOneThemeDoesNotChangeAnotherCompositionPalette() {
        var firstStyle by mutableStateOf(DashboardVisualStyle.MODERN)
        var first: DashboardPalette? = null
        var second: DashboardPalette? = null
        var secondContent = Color.Unspecified
        compose.setContent {
            Row {
                DashboardTheme(firstStyle) { first = LocalDashboardPalette.current; Text("First") }
                DashboardTheme(DashboardVisualStyle.LEGACY_HMI) {
                    second = LocalDashboardPalette.current
                    secondContent = LocalContentColor.current
                    Text("Systems")
                }
            }
        }
        var originalSecond: DashboardPalette? = null
        var originalFirst: DashboardPalette? = null
        compose.runOnIdle {
            assertNotEquals(first!!.background, second!!.background)
            assertEquals(second!!.text, secondContent)
            originalSecond = second
            originalFirst = first
            firstStyle = DashboardVisualStyle.FUTURISTIC
        }
        compose.runOnIdle {
            assertNotEquals(originalFirst, first)
            assertEquals(originalSecond, second)
            assertEquals(second!!.text, secondContent)
        }
    }
}
