package io.github.jhaago.sealdashboard.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*

@Composable fun ThemeSelector(selected: DashboardVisualStyle, onSelect: (DashboardVisualStyle) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Visual style · ${selected.label}", fontSize = 16.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DashboardVisualStyle.entries.forEach { style ->
                OutlinedButton(onClick = { onSelect(style) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                    .testTag("theme-choice-${style.name}")) { Text(style.label, fontSize = 16.sp) }
            }
        }
    }
}

@Composable fun ThemeAction(selected: DashboardVisualStyle, onSelect: (DashboardVisualStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val legacy = LocalDashboardStyle.current == DashboardVisualStyle.LEGACY_HMI
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.heightIn(min = 56.dp).testTag("style-menu")) {
            Text(if (legacy) "SETTINGS" else "Style", fontSize = if (legacy) 12.sp else 16.sp)
        }
        DropdownMenu(expanded, { expanded = false }) {
            DashboardVisualStyle.entries.forEach { style ->
                DropdownMenuItem(text = { Text(if (style == selected) "${style.label} · selected" else style.label, fontSize = 16.sp) },
                    onClick = { onSelect(style); expanded = false }, modifier = Modifier.heightIn(min = 56.dp).testTag("select-style-${style.name}"))
            }
        }
    }
}
