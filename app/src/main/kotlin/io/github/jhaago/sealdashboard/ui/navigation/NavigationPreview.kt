package io.github.jhaago.sealdashboard.ui.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun NavigationPreview(state: NavigationState, style: DashboardVisualStyle, modifier: Modifier = Modifier, compact: Boolean = false) {
    val colors=LocalDashboardPalette.current
    val valid=state.route.size >= 2
    Column(modifier.background(colors.surface,dashboardPanelShape()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("SIMULATED MAP",fontSize=16.sp,color=colors.accent)
        Text(state.sourceLabel,fontSize=16.sp,color=colors.muted)
        Text(if(valid) state.nextTurn else "Route unavailable",fontSize=24.sp)
        if(valid) {
            Text(state.destination,fontSize=16.sp)
            Text("${state.distanceKm} km · ${state.minutes} min · ${state.estimateSource}",fontSize=16.sp,color=colors.muted)
        } else Text("No sample route geometry. No directions available.",fontSize=16.sp,color=colors.warning)
        // Style belongs to this composition; the renderer uses the same immutable tokens.
        DashboardTheme(style) {
            TronMapCanvas(DemoStreetMap.Sample,state.route,Modifier.fillMaxWidth().then(
                if (compact) Modifier.height(240.dp) else Modifier.weight(1f)
            ),state.chargerMarkers)
        }
        Text("Demo chargers · availability unknown",fontSize=16.sp,color=colors.muted)
        Text("Fictional local map · no live guidance",fontSize=16.sp,color=colors.muted)
    }
}

@Composable fun FactoryProjectionPreview(state: ProjectionState, media: MediaState, modifier: Modifier = Modifier) {
    val colors=LocalDashboardPalette.current
    Column(modifier.background(colors.surface,dashboardPanelShape()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(state.sourceLabel,fontSize=16.sp,color=colors.accent)
        Box(Modifier.fillMaxWidth().weight(1f).border(1.dp,colors.grid).padding(24.dp)) {
            Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("Factory projection",fontSize=28.sp)
                Text("Layout placeholder",fontSize=18.sp)
                Text("No Android Auto session is connected.",fontSize=16.sp,color=colors.muted)
            }
        }
        Text("${media.sourceLabel} · ${media.title}",fontSize=16.sp,color=colors.muted)
        Text(state.explanation,fontSize=16.sp,color=colors.muted)
    }
}
