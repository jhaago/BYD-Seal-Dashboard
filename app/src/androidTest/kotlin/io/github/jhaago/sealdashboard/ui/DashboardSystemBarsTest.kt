package io.github.jhaago.sealdashboard.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import io.github.jhaago.sealdashboard.MainActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DashboardSystemBarsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun darkDashboardUsesReadableLightSystemBarIcons() {
        compose.runOnUiThread {
            val window = compose.activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            assertFalse(controller.isAppearanceLightStatusBars)
            assertFalse(controller.isAppearanceLightNavigationBars)
        }
    }
}
