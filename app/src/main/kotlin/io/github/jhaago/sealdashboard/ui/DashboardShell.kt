package io.github.jhaago.sealdashboard.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jhaago.sealdashboard.DashboardApplication
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.mock.*
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.development.DevelopmentScreen
import io.github.jhaago.sealdashboard.ui.drive.DriveScreen
import io.github.jhaago.sealdashboard.ui.energy.EnergyScreen
import io.github.jhaago.sealdashboard.ui.theme.*
import io.github.jhaago.sealdashboard.ui.vehicle.VehicleScreen

@Composable fun DashboardShell(viewModel: DashboardViewModel, simulation: MockSimulationController) {
    val context = LocalContext.current
    val container = (context.applicationContext as DashboardApplication).container
    val preferences = remember { DisplayPreferences(context) }
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val sim by simulation.simulationStatus.collectAsStateWithLifecycle()
    val display by preferences.state.collectAsStateWithLifecycle()
    val navigation by container.navigation.state.collectAsStateWithLifecycle()
    val media by container.media.state.collectAsStateWithLifecycle()
    val projection by container.projection.state.collectAsStateWithLifecycle()
    DashboardContent(ui, sim, viewModel::selectDestination, simulation::dispatch, display, preferences::update,
        demos = DashboardDemoState(navigation, media, projection))
}

/** Ordinary screens see read-only state; Development alone gets mock commands. */
@Composable fun DashboardContent(ui: DashboardUiState, simulation: SimulationStatus,
    onDestination: (DashboardDestination) -> Unit, onCommand: (MockCommand) -> CommandResult,
    display: DisplaySettings, onDisplayChange: (DisplaySettings) -> Unit,
    modifier: Modifier = Modifier, demos: DashboardDemoState = DashboardDemoState()) {
    DashboardTheme(display.visualStyle) {
        val colors = LocalDashboardPalette.current
        var rejection by remember { mutableStateOf<String?>(null) }
        val mockCommand: (MockCommand) -> CommandResult = { command ->
            val result = onCommand(command)
            rejection = (result as? CommandResult.Rejected)?.reason
            result
        }
        Column(modifier.fillMaxSize().testTag("dashboard-root").background(colors.background).safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SEAL", fontSize = 24.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
                    Text("Dashboard", fontSize = 16.sp, color = colors.muted)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(ui.sourceLabel, Modifier.testTag("source-label"), color = colors.accent, fontSize = 16.sp)
                    Text(ui.status.name, Modifier.testTag("provider-status"), fontSize = 16.sp,
                        color = if (ui.status == io.github.jhaago.sealdashboard.core.ProviderStatus.RUNNING) colors.muted else colors.warning)
                }
                ThemeAction(display.visualStyle) { onDisplayChange(display.copy(visualStyle = it)) }
            }
            HorizontalDivider(color = colors.grid)
            Box(Modifier.weight(1f).fillMaxWidth().testTag("screen-${ui.destination.name}")) {
                when (ui.destination) {
                    DashboardDestination.DRIVE -> DriveScreen(ui, display, onDisplayChange, demos)
                    DashboardDestination.VEHICLE -> VehicleScreen(ui)
                    DashboardDestination.ENERGY -> EnergyScreen(ui)
                    DashboardDestination.DEVELOPMENT -> DevelopmentScreen(ui, simulation, mockCommand, display, onDisplayChange)
                }
            }
            rejection?.let { Text(it, Modifier.fillMaxWidth().background(colors.elevated).padding(12.dp).testTag("command-result"),
                color = colors.warning, fontSize = 16.sp) }
            HorizontalDivider(color = colors.grid)
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DashboardDestination.entries.forEach { destination ->
                    val selected = ui.destination == destination
                    TextButton(onClick = { rejection = null; onDestination(destination) },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp).testTag("nav-${destination.name}")
                            .semantics { contentDescription = destination.label }
                            .background(if (selected) colors.elevated else colors.background, dashboardPanelShape()),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)) {
                        Text(if (destination == DashboardDestination.DEVELOPMENT) "Dev" else destination.label, fontSize = 16.sp,
                            color = if (selected) colors.accent else colors.muted)
                    }
                }
            }
        }
    }
}
