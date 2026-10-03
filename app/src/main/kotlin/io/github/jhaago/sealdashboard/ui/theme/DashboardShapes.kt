package io.github.jhaago.sealdashboard.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable fun dashboardPanelShape() = RoundedCornerShape(when (LocalDashboardStyle.current) {
    DashboardVisualStyle.SYSTEMS -> 0.dp
    DashboardVisualStyle.TRON -> 8.dp
    DashboardVisualStyle.MODERN -> 24.dp
})
