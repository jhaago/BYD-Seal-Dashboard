package io.github.jhaago.sealdashboard.ui.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.assistant.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun AssistantTranscript(turns: List<AssistantTurn>, drivingPreview: Boolean) {
    val colors = LocalDashboardPalette.current
    val visible = if (drivingPreview) turns.takeLast(2) else turns
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visible.forEach { turn ->
            Text(if (turn.fromUser) "You" else "SCRIPTED PREVIEW", fontSize=16.sp,
                color=if(turn.fromUser) colors.muted else colors.accent)
            Text(turn.text, fontSize=16.sp, maxLines=if(drivingPreview) 2 else Int.MAX_VALUE,
                overflow=TextOverflow.Ellipsis)
        }
    }
}
