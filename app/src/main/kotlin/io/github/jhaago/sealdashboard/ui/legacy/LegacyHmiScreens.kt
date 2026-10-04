package io.github.jhaago.sealdashboard.ui.legacy

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.ui.DashboardUiState
import io.github.jhaago.sealdashboard.ui.theme.*

/** Coarse, square-edged EV HMI inspired by late-20th-century machinery displays. */
@Composable fun LegacyHmiOverview(ui: DashboardUiState, demos: DashboardDemoState) {
    BoxWithConstraints(Modifier.fillMaxSize().testTag("legacy-hmi-overview")) {
        val wide = maxWidth >= 900.dp && maxHeight >= 520.dp
        if (wide) LegacyOverviewWide(ui, demos) else LegacyOverviewCompact(ui, demos)
    }
}

@Composable private fun LegacyOverviewWide(ui: DashboardUiState, demos: DashboardDemoState) {
    Column(Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LegacyNavigationPanel(demos.navigation, Modifier.weight(1f).fillMaxHeight())
            LegacyVehiclePanel(ui, Modifier.weight(1.35f).fillMaxHeight())
            LegacyDrivingData(ui, Modifier.weight(1f).fillMaxHeight())
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LegacyEnergyPanel(ui, Modifier.weight(1f).fillMaxHeight())
            LegacyClimatePanel(ui, Modifier.weight(1f).fillMaxHeight())
            LegacyAudioPanel(demos.media, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable private fun LegacyOverviewCompact(ui: DashboardUiState, demos: DashboardDemoState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LegacyDrivingData(ui, Modifier.fillMaxWidth().heightIn(min = 260.dp))
        LegacyVehiclePanel(ui, Modifier.fillMaxWidth().height(330.dp))
        LegacyNavigationPanel(demos.navigation, Modifier.fillMaxWidth().height(320.dp))
        LegacyEnergyPanel(ui, Modifier.fillMaxWidth().height(220.dp))
        LegacyClimatePanel(ui, Modifier.fillMaxWidth().height(190.dp))
        LegacyAudioPanel(demos.media, Modifier.fillMaxWidth().height(180.dp))
    }
}

@Composable fun LegacyHmiDrive(ui: DashboardUiState) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter
    val v = ui.vehicle
    LegacyScanBackground(Modifier.fillMaxSize().testTag("legacy-hmi-drive")) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
            val wide = maxWidth >= 850.dp && maxHeight >= 430.dp
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().height(38.dp).border(1.dp, colors.grid).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SEAL · DYNAMIC RWD", fontSize = 14.sp, color = colors.muted)
                    Text("${f.text(v.powertrain.driveMode) { it.name }} · ${ui.status.name}", fontSize = 14.sp, color = colors.accent)
                    Text("${f.number(v.environment.outsideTemperatureC)}°C", fontSize = 14.sp, color = colors.muted)
                }
                if (wide) {
                    Row(Modifier.weight(1f).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LegacySpeedPanel(ui, Modifier.weight(.8f).fillMaxHeight())
                        LegacyLaneView(Modifier.weight(1.65f).fillMaxHeight())
                        LegacyGearPanel(ui, Modifier.weight(.6f).fillMaxHeight())
                        LegacyPowerFlow(ui, Modifier.weight(.9f).fillMaxHeight())
                    }
                    LegacyPrimaryStrip(ui, Modifier.fillMaxWidth().height(58.dp))
                } else {
                    Column(Modifier.weight(1f).padding(top = 6.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LegacySpeedPanel(ui, Modifier.weight(1f).fillMaxHeight())
                            LegacyGearPanel(ui, Modifier.width(100.dp).fillMaxHeight())
                        }
                        LegacyPrimaryStrip(ui)
                        LegacyLaneView(Modifier.fillMaxWidth().height(260.dp))
                        LegacyPowerFlow(ui, Modifier.fillMaxWidth().height(190.dp))
                    }
                }
            }
        }
    }
}

@Composable private fun LegacyNavigationPanel(state: NavigationState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    LegacyPanel("NAVIGATION", modifier.testTag("legacy-hmi-navigation")) {
        Text(state.nextTurn.uppercase(), fontSize = 15.sp, color = colors.accent, maxLines = 2)
        Text("${state.distanceKm} KM · ${state.minutes} MIN", fontSize = 13.sp, color = colors.muted)
        LegacyMap(state.route, Modifier.fillMaxWidth().weight(1f))
        Text("SIMULATED ROUTE · NO LIVE GUIDANCE", fontSize = 11.sp, color = colors.muted)
    }
}

@Composable private fun LegacyMap(route: List<RoutePoint>, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    Canvas(modifier.background(colors.background).border(1.dp, colors.grid)) {
        val step = 24.dp.toPx()
        var x = 0f
        while (x <= size.width) { drawLine(colors.grid.copy(alpha = .32f), Offset(x, 0f), Offset(x, size.height), 1f); x += step }
        var y = 0f
        while (y <= size.height) { drawLine(colors.grid.copy(alpha = .32f), Offset(0f, y), Offset(size.width, y), 1f); y += step }
        if (route.size >= 2) route.zipWithNext().forEach { (a, b) ->
            drawLine(colors.accent, Offset(a.x * size.width, a.y * size.height), Offset(b.x * size.width, b.y * size.height), 4.dp.toPx())
        }
        drawRect(colors.accent, Offset(size.width * .1f, size.height * .8f), Size(10.dp.toPx(), 10.dp.toPx()))
    }
}

@Composable private fun LegacyVehiclePanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter
    val v = ui.vehicle
    LegacyPanel("VEHICLE STATUS · RWD", modifier.testTag("legacy-hmi-vehicle")) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize().testTag("legacy-hmi-vehicle-schematic")) {
                val w = size.width; val h = size.height
                val left = w * .28f; val right = w * .72f; val top = h * .05f; val bottom = h * .93f
                val body = Path().apply {
                    moveTo(w*.4f, top); lineTo(w*.6f, top); lineTo(right, h*.18f); lineTo(right, h*.8f)
                    lineTo(w*.62f, bottom); lineTo(w*.38f, bottom); lineTo(left, h*.8f); lineTo(left, h*.18f); close()
                }
                drawPath(body, colors.grid, style = Stroke(2.dp.toPx()))
                drawRect(colors.grid, Offset(w*.38f,h*.2f), Size(w*.24f,h*.18f), style = Stroke(2.dp.toPx()))
                drawRect(colors.grid, Offset(w*.38f,h*.62f), Size(w*.24f,h*.14f), style = Stroke(2.dp.toPx()))
                listOf(.23f,.73f).forEach { yy ->
                    drawRect(colors.accent, Offset(w*.23f,h*yy), Size(w*.06f,h*.14f))
                    drawRect(colors.accent, Offset(w*.71f,h*yy), Size(w*.06f,h*.14f))
                }
                drawLine(colors.accent, Offset(w*.5f,h*.37f), Offset(w*.5f,h*.69f), 4.dp.toPx())
                drawLine(colors.accent, Offset(w*.29f,h*.73f), Offset(w*.71f,h*.73f), 4.dp.toPx())
                drawRect(colors.accent, Offset(w*.46f,h*.67f), Size(w*.08f,h*.12f), style = Stroke(3.dp.toPx()))
            }
            Text("BATTERY\n${f.number(v.battery.stateOfChargePercent)}%", Modifier.align(Alignment.Center)
                .background(colors.background).border(1.dp, colors.grid).padding(6.dp), fontSize = 14.sp, color = colors.accent)
            Text("REAR MOTOR  ${f.number(v.powertrain.motorPowerKw[Axle.REAR] ?: Signal.unavailable(), 1)} kW",
                Modifier.align(Alignment.BottomEnd), fontSize = 12.sp, color = colors.muted)
        }
    }
}

@Composable private fun LegacyDrivingData(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val v = ui.vehicle
    LegacyPanel("DRIVING DATA", modifier.testTag("legacy-hmi-driving-data")) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("SPEED", fontSize = 12.sp, color = colors.muted)
                Text(f.number(v.motion.speedKmh), Modifier.testTag("speed-value"), fontSize = 48.sp, lineHeight = 50.sp,
                    fontWeight = FontWeight.Bold, color = colors.text)
            }
            Text(f.gear(v.motion.gear), Modifier.testTag("gear-value"), fontSize = 30.sp, color = colors.accent)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LegacyDatum("RANGE", f.number(v.battery.estimatedRangeKm), "km", "range", Modifier.weight(1f))
            LegacyDatum("BATTERY", f.number(v.battery.stateOfChargePercent), "%", "soc", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LegacyDatum("PACK", f.number(v.powertrain.packPowerKw, 1), "kW", "power", Modifier.weight(1f))
            LegacyDatum("OUTSIDE", f.number(v.environment.outsideTemperatureC), "°C", null, Modifier.weight(1f))
        }
        Text(f.text(v.powertrain.driveMode) { "${it.name} MODE" }, color = colors.accent, fontSize = 14.sp)
    }
}

@Composable private fun LegacyEnergyPanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val power = ui.vehicle.powertrain.packPowerKw
    LegacyPanel("ENERGY", modifier.testTag("legacy-hmi-energy")) {
        Canvas(Modifier.fillMaxWidth().weight(1f).border(1.dp, colors.grid)) {
            val bars = listOf(.22f,.36f,.3f,.48f,.62f,.34f,.25f,.44f,.57f,.38f,.28f,.46f,.31f,.2f)
            val gap = size.width / bars.size
            bars.forEachIndexed { index, value -> drawRect(colors.accent, Offset(index*gap+2f,size.height*(1-value)), Size(gap-4f,size.height*value)) }
        }
        Text("PACK FLOW  ${f.number(power, 1)} kW", Modifier.testTag("overview-pack-power"), fontSize = 13.sp, color = colors.muted)
    }
}

@Composable private fun LegacyClimatePanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val climate = ui.vehicle.climate
    LegacyPanel("CLIMATE", modifier.testTag("legacy-hmi-climate")) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DRIVER\n${f.number(climate.targetTemperatureC)}°C", fontSize = 16.sp)
            Text(if (f.text(climate.enabled) { it.toString() } == "true") "A/C ON" else "A/C OFF", fontSize = 16.sp, color = colors.accent)
            Text("FAN\n${f.text(climate.fanLevel) { it.toString() }}", fontSize = 16.sp)
        }
        Text("CABIN ${f.number(ui.vehicle.environment.cabinTemperatureC)}°C", fontSize = 13.sp, color = colors.muted)
    }
}

@Composable private fun LegacyAudioPanel(media: MediaState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    LegacyPanel("AUDIO · ${media.sourceLabel}", modifier.testTag("legacy-hmi-audio")) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.width(130.dp).height(56.dp).border(1.dp, colors.grid)) {
                val bars = listOf(.35f,.62f,.8f,.48f,.7f,.42f,.66f,.28f)
                val gap = size.width / bars.size
                bars.forEachIndexed { i, value -> drawRect(colors.accent, Offset(i*gap+3f,size.height*(1-value)), Size(gap-6f,size.height*value)) }
            }
            Column { Text(media.title.uppercase(), fontSize = 14.sp); Text(media.artist.uppercase(), fontSize = 12.sp, color = colors.muted) }
        }
        Text("METADATA PREVIEW · PLAYBACK CONTROL UNAVAILABLE", fontSize = 10.sp, color = colors.muted)
    }
}

@Composable private fun LegacySpeedPanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current; val f = ui.formatter
    LegacyPanel("SPEED", modifier) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(f.number(ui.vehicle.motion.speedKmh), Modifier.testTag("speed-value"), fontSize = 76.sp,
                    lineHeight = 80.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                Text("km/h", fontSize = 18.sp, color = colors.accent)
            }
        }
    }
}

@Composable private fun LegacyGearPanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current; val f = ui.formatter
    LegacyPanel("GEAR", modifier) {
        Text(f.gear(ui.vehicle.motion.gear), Modifier.testTag("gear-value"), fontSize = 54.sp, color = colors.accent)
        Text("ECU", fontSize = 13.sp, color = colors.muted)
        Text(f.text(ui.vehicle.powertrain.driveMode) { it.name }, Modifier.fillMaxWidth().background(colors.accent)
            .padding(4.dp), fontSize = 14.sp, color = colors.background)
    }
}

@Composable private fun LegacyLaneView(modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    Canvas(modifier.border(1.dp, colors.grid).testTag("legacy-hmi-lane-view")) {
        val stroke = 2.dp.toPx()
        drawLine(colors.grid, Offset(size.width*.18f,size.height), Offset(size.width*.43f,0f), stroke)
        drawLine(colors.grid, Offset(size.width*.82f,size.height), Offset(size.width*.57f,0f), stroke)
        repeat(5) { i ->
            val y1=size.height*(.92f-i*.18f); val y2=y1-size.height*.08f
            drawLine(colors.muted, Offset(size.width*.45f,y1), Offset(size.width*.47f,y2), stroke)
            drawLine(colors.muted, Offset(size.width*.55f,y1), Offset(size.width*.53f,y2), stroke)
        }
        val carLeft=size.width*.42f; val carTop=size.height*.54f
        drawRect(colors.text, Offset(carLeft,carTop), Size(size.width*.16f,size.height*.27f), style=Stroke(stroke))
        drawRect(colors.accent, Offset(size.width*.445f,size.height*.71f), Size(size.width*.11f,stroke*2))
        drawRect(colors.muted, Offset(size.width*.28f,size.height*.38f), Size(size.width*.11f,size.height*.18f), style=Stroke(stroke))
    }
}

@Composable private fun LegacyPowerFlow(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current; val f = ui.formatter; val v = ui.vehicle
    LegacyPanel("POWER FLOW", modifier.testTag("legacy-hmi-power-flow")) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val stroke=3.dp.toPx()
            drawRect(colors.grid, Offset(size.width*.08f,size.height*.2f), Size(size.width*.2f,size.height*.5f), style=Stroke(stroke))
            repeat(5) { i -> drawRect(colors.accent, Offset(size.width*.11f,size.height*(.62f-i*.08f)), Size(size.width*.14f,size.height*.05f)) }
            drawRect(colors.grid, Offset(size.width*.66f,size.height*.35f), Size(size.width*.16f,size.height*.2f), style=Stroke(stroke))
            drawLine(colors.accent, Offset(size.width*.28f,size.height*.45f), Offset(size.width*.66f,size.height*.45f), stroke)
            drawLine(colors.accent, Offset(size.width*.74f,size.height*.55f), Offset(size.width*.74f,size.height*.82f), stroke)
            drawLine(colors.grid, Offset(size.width*.5f,size.height*.82f), Offset(size.width*.9f,size.height*.82f), stroke)
        }
        Text("BATTERY ${f.number(v.battery.stateOfChargePercent)}% · REAR MOTOR",
            fontSize = 12.sp, color = colors.muted)
    }
}

@Composable private fun LegacyPrimaryStrip(ui: DashboardUiState, modifier: Modifier = Modifier, showPower: Boolean = true) {
    val colors=LocalDashboardPalette.current; val f=ui.formatter; val v=ui.vehicle
    Row(modifier.border(1.dp, colors.grid), verticalAlignment=Alignment.CenterVertically) {
        LegacyStripDatum("RANGE", f.number(v.battery.estimatedRangeKm), "km", "range", Modifier.weight(1f))
        LegacyStripDatum("BATTERY", f.number(v.battery.stateOfChargePercent), "%", "soc", Modifier.weight(1f))
        if (showPower) LegacyStripDatum("PACK", f.number(v.powertrain.packPowerKw,1), "kW", "power", Modifier.weight(1f))
        LegacyStripDatum("TRIP", f.number(v.trip.distanceKm,1), "km", null, Modifier.weight(1f))
    }
}

@Composable private fun LegacyStripDatum(label:String,value:String,unit:String,tag:String?,modifier:Modifier) {
    val colors=LocalDashboardPalette.current
    Row(modifier.fillMaxHeight().border(.5.dp,colors.grid).padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.SpaceBetween) {
        Text(label,fontSize=12.sp,color=colors.muted)
        Text("$value $unit",Modifier.then(if(tag!=null) Modifier.testTag("$tag-value") else Modifier),fontSize=16.sp)
    }
}

@Composable private fun LegacyDatum(label:String,value:String,unit:String,tag:String?,modifier:Modifier) {
    val colors=LocalDashboardPalette.current
    Column(modifier.border(1.dp,colors.grid).padding(6.dp)) {
        Text(label,fontSize=11.sp,color=colors.muted)
        Text("$value $unit",Modifier.then(if(tag!=null) Modifier.testTag("$tag-value") else Modifier),fontSize=18.sp)
    }
}

@Composable private fun LegacyPanel(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors=LocalDashboardPalette.current
    Column(modifier.background(colors.surface).border(1.dp,colors.grid)) {
        Text(title,Modifier.fillMaxWidth().border(1.dp,colors.grid).padding(horizontal=8.dp,vertical=3.dp),
            fontSize=13.sp,color=colors.muted,letterSpacing=1.sp)
        Column(Modifier.fillMaxSize().padding(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp),content=content)
    }
}

@Composable private fun LegacyScanBackground(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors=LocalDashboardPalette.current
    Box(modifier.drawBehind {
        drawRect(colors.background)
        val gap=4.dp.toPx(); var y=0f
        while(y<size.height){ drawLine(colors.grid.copy(alpha=.045f),Offset(0f,y),Offset(size.width,y),1f); y+=gap }
    },content=content)
}
