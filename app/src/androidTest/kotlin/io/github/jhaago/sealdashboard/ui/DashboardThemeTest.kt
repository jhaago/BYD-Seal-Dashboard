package io.github.jhaago.sealdashboard.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
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
}
