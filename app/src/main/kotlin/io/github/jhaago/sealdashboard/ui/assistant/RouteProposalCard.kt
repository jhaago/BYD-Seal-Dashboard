package io.github.jhaago.sealdashboard.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.assistant.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun RouteProposalCard(proposal: RouteProposal, option: ChargerOption?, onApply: (String) -> Unit,
    onDismiss: (String) -> Unit) {
    val colors = LocalDashboardPalette.current
    Column(Modifier.fillMaxWidth().testTag("assistant-proposal").background(colors.elevated,dashboardPanelShape())
        .padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Review sample stop",fontSize=24.sp,color=colors.accent)
        Text(option?.name ?: "Sample charger",fontSize=20.sp)
        Text("Apply updates our simulated route. Factory Android Auto is unchanged.",fontSize=16.sp,color=colors.muted)
        Button(onClick={ onApply(proposal.id) },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-apply")) {
            Text("Apply to sample trip",fontSize=16.sp)
        }
        OutlinedButton(onClick={ onDismiss(proposal.id) },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-dismiss")) {
            Text("Dismiss",fontSize=16.sp)
        }
    }
}
