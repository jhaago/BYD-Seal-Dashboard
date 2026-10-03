package io.github.jhaago.sealdashboard.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.ui.*
import io.github.jhaago.sealdashboard.ui.theme.*

/** Read-only values remain outside the conversation's scroll area. */
@Composable fun AssistantTelemetry(ui: DashboardUiState) {
    val colors=LocalDashboardPalette.current
    val v=ui.vehicle; val f=ui.formatter
    @Composable fun value(label: String, text: String, tag: String, modifier: Modifier) {
        Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(label,fontSize=16.sp,color=colors.muted)
            Text(text,Modifier.testTag(tag),fontSize=28.sp)
        }
    }
    Column(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal=16.dp,vertical=8.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            value("Speed km/h",f.number(v.motion.speedKmh),"assistant-speed",Modifier.weight(1f))
            value("Gear",f.gear(v.motion.gear),"assistant-gear",Modifier.weight(1f))
            value("SOC %",f.number(v.battery.stateOfChargePercent),"assistant-soc",Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            value("Range km",f.number(v.battery.estimatedRangeKm),"assistant-range",Modifier.weight(1f))
            value("Pack power kW",f.number(v.powertrain.packPowerKw),"assistant-power",Modifier.weight(1f))
        }
        Text("${f.quality(v.motion.speedKmh).name} speed · ${f.quality(v.battery.stateOfChargePercent).name} SOC",
            fontSize=16.sp,color=colors.muted)
    }
}
