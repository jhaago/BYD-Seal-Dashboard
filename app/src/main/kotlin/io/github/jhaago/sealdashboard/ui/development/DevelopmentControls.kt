package io.github.jhaago.sealdashboard.ui.development

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.ui.theme.DashboardColors
import java.util.Locale

@Composable fun SimulationSlider(label: String, value: Double, range: ClosedFloatingPointRange<Float>, unit: String,
    tag: String, enabled: Boolean = true, onValue: (Double) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label · ${String.format(Locale.ROOT, "%.1f", value)} $unit", fontSize = 18.sp)
        Slider(value = value.toFloat().coerceIn(range.start, range.endInclusive), onValueChange = { onValue(it.toDouble()) },
            valueRange = range, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag))
    }
}
@Composable fun SimulationToggle(label: String, checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f), fontSize = 18.sp)
        Switch(checked, onChange, modifier = Modifier.testTag(tag).sizeIn(minWidth = 56.dp, minHeight = 56.dp))
    }
}
@Composable fun MockButton(label: String, tag: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick, enabled = enabled, modifier = Modifier.heightIn(min = 56.dp).testTag(tag)) { Text(label, fontSize = 16.sp) }
}
