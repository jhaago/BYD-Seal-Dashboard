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

@Composable fun ChargerOptionCard(option: ChargerOption, index: Int, onSelect: () -> Unit) {
    val colors = LocalDashboardPalette.current
    Column(Modifier.fillMaxWidth().testTag("charger-option-${option.id}")
        .background(colors.elevated,dashboardPanelShape()).padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("${index+1}. ${option.name}", fontSize=20.sp,color=colors.accent)
        Text(option.connector.ifBlank { "Connector unknown" },fontSize=16.sp)
        Text(option.advertisedKw?.let { "Advertised: ${it.toInt()} kW" } ?: "Advertised power: unknown",fontSize=16.sp)
        Text(option.detourMinutes?.let { "Demo detour: $it min" } ?: "Detour: unknown",fontSize=16.sp)
        Text("Availability: ${option.availability.name.lowercase()}",fontSize=16.sp,color=colors.muted)
        Text(option.sourceLabel,fontSize=16.sp,color=colors.muted)
        Text("Arrival SOC: unknown",fontSize=16.sp,color=colors.muted)
        OutlinedButton(onClick=onSelect,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) {
            Text("Review this stop",fontSize=16.sp)
        }
    }
}
