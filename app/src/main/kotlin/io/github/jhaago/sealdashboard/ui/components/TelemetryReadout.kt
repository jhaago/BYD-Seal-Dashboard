package io.github.jhaago.sealdashboard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.ui.theme.DashboardColors

@Composable fun TelemetryReadout(label: String, value: String, unit: String = "", tag: String? = null,
    modifier: Modifier = Modifier, size: TextUnit = 28.sp) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = DashboardColors.Muted, fontSize = 16.sp)
        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, modifier = if (tag == null) Modifier else Modifier.testTag("$tag-value"),
                fontSize = size, lineHeight = size * 1.1f, fontWeight = FontWeight.Medium)
            if (unit.isNotEmpty()) Text(unit, color = DashboardColors.Muted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}

@Composable fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        subtitle?.let { Text(it, color = DashboardColors.Muted, fontSize = 16.sp) }
    }
}
