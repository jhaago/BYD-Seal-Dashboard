package io.github.jhaago.sealdashboard.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable fun dashboardPanelShape() = RoundedCornerShape(when (LocalDashboardStyle.current) {
    DashboardVisualStyle.LEGACY_HMI -> 0.dp
    DashboardVisualStyle.FUTURISTIC -> 8.dp
    DashboardVisualStyle.MODERN -> 24.dp
})
