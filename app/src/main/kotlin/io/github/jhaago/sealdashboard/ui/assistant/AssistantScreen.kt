package io.github.jhaago.sealdashboard.ui.assistant

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.assistant.*
import io.github.jhaago.sealdashboard.ui.theme.*

@Composable fun AssistantScreen(state: AssistantState, onSubmit: (String) -> Unit, onCancel: () -> Unit,
    onSelectOption: (ChargerSelection) -> Unit, onApply: (String) -> Unit, onDismiss: (String) -> Unit, drivingPreview: Boolean,
    onDrivingPreviewChange: (Boolean) -> Unit = {}) {
    val colors=LocalDashboardPalette.current
    var input by remember { mutableStateOf("") }
    @Composable fun conversation() {
        Text("SCRIPTED PREVIEW",fontSize=20.sp,color=colors.accent)
        Text("Offline sample conversation · no live AI",fontSize=16.sp,color=colors.muted)
        OutlinedButton(onClick={ onDrivingPreviewChange(!drivingPreview) },
            modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-driving-toggle")) {
            Text(if(drivingPreview) "Switch to parked typing" else "Switch to driving preview",fontSize=16.sp)
        }
        Text("Speech unavailable · scripted preview",fontSize=16.sp,color=colors.warning)
        Text(state.phase.name,Modifier.testTag("assistant-phase"),fontSize=16.sp,color=colors.accent)
        if(state.phase==AssistantPhase.Responding) {
            OutlinedButton(onClick=onCancel,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-cancel")) {
                Text("Cancel request",fontSize=16.sp)
            }
        }
        if(!drivingPreview) {
            OutlinedTextField(input,{input=it},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-input"),
                label={ Text("Type a sample request",fontSize=16.sp) },textStyle=MaterialTheme.typography.bodyLarge,
                maxLines=3)
            Button(onClick={ val request=input; input=""; onSubmit(request) },enabled=input.isNotBlank(),
                modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-send")) { Text("Send",fontSize=16.sp) }
        }
        listOf(Triple("find","Find a charger on my way","Find a demo charger"),
            Triple("detour","Show the smallest detour","Smallest demo detour"),
            Triple("fast","I need a faster charger","Higher advertised power")).forEach { (id,request,label) ->
            OutlinedButton(onClick={onSubmit(request)},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("assistant-prompt-$id")) {
                Text(if(drivingPreview) label else request,fontSize=16.sp)
            }
        }
        AssistantTranscript(state.transcript,drivingPreview)
    }
    @Composable fun choices() {
        if(state.options.isEmpty() && state.proposal==null) Text("Use a sample prompt to see fictional chargers.",fontSize=16.sp,color=colors.muted)
        if(state.rankingReason.isNotBlank()) Text(state.rankingReason,fontSize=16.sp,color=colors.muted)
        state.proposal?.let { proposal ->
            RouteProposalCard(proposal,state.options.find { it.id==proposal.chargerId },onApply,onDismiss)
        }
        state.options.forEachIndexed { index,option ->
            val selection = state.selectionFor(option.id)
            ChargerOptionCard(option,index) { onSelectOption(selection) }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
        if(maxWidth>=900.dp) Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement=Arrangement.spacedBy(12.dp)) { conversation() }
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement=Arrangement.spacedBy(12.dp)) { choices() }
        } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement=Arrangement.spacedBy(12.dp)) { conversation(); choices() }
    }
}
