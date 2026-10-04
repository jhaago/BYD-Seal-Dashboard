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
        if (wide) LegacyReferenceOverview(ui, demos) else LegacyOverviewCompact(ui, demos)
    }
}

@Composable private fun LegacyOverviewWide(ui: DashboardUiState, demos: DashboardDemoState) {
    LegacyScanBackground(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LegacyNavigationPanel(demos.navigation, Modifier.weight(1.05f).fillMaxHeight())
            LegacyVehiclePanel(ui, Modifier.weight(1.45f).fillMaxHeight())
            LegacyDrivingData(ui, Modifier.weight(1f).fillMaxHeight())
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LegacyEnergyPanel(ui, Modifier.weight(1f).fillMaxHeight())
            LegacyClimatePanel(ui, Modifier.weight(1f).fillMaxHeight())
            LegacyAudioPanel(demos.media, Modifier.weight(1f).fillMaxHeight())
        }
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 850.dp && maxHeight >= 430.dp) LegacyReferenceDrive(ui)
        else LegacyHmiDriveCompact(ui)
    }
}

@Composable private fun LegacyHmiDriveCompact(ui: DashboardUiState) {
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
        Row(Modifier.fillMaxWidth().height(72.dp).border(1.dp, colors.grid), verticalAlignment = Alignment.CenterVertically) {
            Text("↰", Modifier.padding(horizontal = 10.dp), fontSize = 42.sp, color = colors.accent)
            Column(Modifier.weight(1f)) {
                Text("${state.distanceKm} KM", fontSize = 22.sp, color = colors.text, fontWeight = FontWeight.Bold)
                Text(state.nextTurn.uppercase(), fontSize = 12.sp, color = colors.muted, maxLines = 2)
            }
            Column(Modifier.width(92.dp).fillMaxHeight().border(1.dp, colors.grid).padding(5.dp),
                verticalArrangement = Arrangement.SpaceAround) {
                Text("↑ 2.4 KM", fontSize = 10.sp, color = colors.accent)
                Text("6.1 KM", fontSize = 10.sp, color = colors.muted)
                Text("${state.minutes} MIN", fontSize = 10.sp, color = colors.muted)
            }
        }
        LegacyMap(state.route, Modifier.fillMaxWidth().weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("N  ▲", fontSize = 11.sp, color = colors.muted)
            Text("SIMULATED ROUTE · 5 KM", fontSize = 10.sp, color = colors.muted)
        }
    }
}

@Composable private fun LegacyMap(route: List<RoutePoint>, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    Canvas(modifier.background(colors.background).border(1.dp, colors.grid)) {
        val streets = listOf(.08f,.22f,.38f,.55f,.74f,.9f)
        streets.forEachIndexed { index, fraction ->
            drawLine(colors.grid.copy(alpha = .6f), Offset(size.width*fraction,0f),
                Offset(size.width*(if(index%2==0) fraction+.16f else fraction-.12f),size.height), 2.dp.toPx())
            drawLine(colors.grid.copy(alpha = .45f), Offset(0f,size.height*fraction),
                Offset(size.width,size.height*(if(index%2==0) fraction-.12f else fraction+.08f)), 2.dp.toPx())
        }
        drawRect(colors.grid.copy(alpha=.25f), Offset(size.width*.7f,size.height*.08f), Size(size.width*.22f,size.height*.18f))
        if (route.size >= 2) route.zipWithNext().forEach { (a, b) ->
            drawLine(colors.accent, Offset(a.x * size.width, a.y * size.height), Offset(b.x * size.width, b.y * size.height), 5.dp.toPx())
        }
        val marker=Path().apply { moveTo(size.width*.1f,size.height*.76f); lineTo(size.width*.06f,size.height*.9f); lineTo(size.width*.14f,size.height*.9f); close() }
        drawPath(marker, colors.accent, style=Stroke(3.dp.toPx()))
    }
}

@Composable private fun LegacyVehiclePanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter
    val v = ui.vehicle
    LegacyPanel("VEHICLE STATUS", modifier.testTag("legacy-hmi-vehicle"), dense = true) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("FRONT MOTOR  — kW", Modifier.weight(1f), fontSize = 10.sp, color = colors.muted)
            Text("READY", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.accent)
            Text("  P  R  N  ", fontSize = 12.sp, color = colors.muted)
            Text(f.gear(v.motion.gear), Modifier.border(1.dp, colors.accent).padding(horizontal = 5.dp, vertical = 1.dp),
                fontSize = 18.sp, color = colors.accent)
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize().testTag("legacy-hmi-vehicle-schematic")) {
                val w=size.width; val h=size.height; val line=2.dp.toPx()
                val body=Path().apply {
                    moveTo(w*.43f,h*.06f); lineTo(w*.57f,h*.06f); lineTo(w*.65f,h*.15f)
                    lineTo(w*.68f,h*.31f); lineTo(w*.66f,h*.78f); lineTo(w*.59f,h*.93f)
                    lineTo(w*.41f,h*.93f); lineTo(w*.34f,h*.78f); lineTo(w*.32f,h*.31f)
                    lineTo(w*.35f,h*.15f); close()
                }
                drawPath(body,colors.grid,style=Stroke(line))
                drawLine(colors.grid,Offset(w*.36f,h*.18f),Offset(w*.64f,h*.18f),line)
                drawLine(colors.grid,Offset(w*.34f,h*.72f),Offset(w*.66f,h*.72f),line)
                drawRect(colors.grid,Offset(w*.4f,h*.22f),Size(w*.2f,h*.12f),style=Stroke(line))
                drawRect(colors.grid,Offset(w*.4f,h*.39f),Size(w*.2f,h*.2f),style=Stroke(line))
                drawRect(colors.grid,Offset(w*.43f,h*.76f),Size(w*.14f,h*.09f),style=Stroke(line))
                listOf(.21f,.68f).forEach { yy ->
                    drawRect(colors.accent,Offset(w*.29f,h*yy),Size(w*.045f,h*.15f))
                    drawRect(colors.accent,Offset(w*.665f,h*yy),Size(w*.045f,h*.15f))
                }
                drawLine(colors.accent,Offset(w*.5f,h*.34f),Offset(w*.5f,h*.76f),4.dp.toPx())
                drawLine(colors.accent,Offset(w*.335f,h*.755f),Offset(w*.665f,h*.755f),4.dp.toPx())
                drawLine(colors.accent,Offset(w*.5f,h*.18f),Offset(w*.5f,h*.22f),4.dp.toPx())
                repeat(5) { i ->
                    val x=w*(.42f+i*.032f)
                    drawRect(if(i<4) colors.accent else colors.grid,Offset(x,h*.44f),Size(w*.025f,h*.11f))
                }
            }
            LegacyTyrePressure(Wheel.FRONT_LEFT, ui, Modifier.align(Alignment.TopStart).padding(top=48.dp))
            LegacyTyrePressure(Wheel.FRONT_RIGHT, ui, Modifier.align(Alignment.TopEnd).padding(top=48.dp))
            LegacyTyrePressure(Wheel.REAR_LEFT, ui, Modifier.align(Alignment.BottomStart).padding(bottom=34.dp))
            LegacyTyrePressure(Wheel.REAR_RIGHT, ui, Modifier.align(Alignment.BottomEnd).padding(bottom=34.dp))
            Column(Modifier.align(Alignment.Center).width(90.dp).background(colors.background).border(1.dp,colors.grid)
                .padding(5.dp).testTag("legacy-hmi-battery-segments"), horizontalAlignment=Alignment.CenterHorizontally) {
                Text("BATTERY",fontSize=10.sp,color=colors.muted)
                Text("${f.number(v.battery.stateOfChargePercent)}%",fontSize=16.sp,color=colors.accent)
            }
            LegacyPowerLegend(Modifier.align(Alignment.CenterEnd).width(112.dp))
            Text("REAR MOTOR  ${f.number(v.powertrain.motorPowerKw[Axle.REAR] ?: Signal.unavailable(),1)} kW",
                Modifier.align(Alignment.BottomCenter),fontSize=10.sp,color=colors.muted)
        }
    }
}

@Composable private fun LegacyTyrePressure(wheel: Wheel, ui: DashboardUiState, modifier: Modifier) {
    val colors=LocalDashboardPalette.current
    val value=ui.formatter.number(ui.vehicle.wheels[wheel]?.pressureKpa ?: Signal.unavailable())
    Text("$value kPa",modifier.background(colors.background).padding(2.dp).testTag("legacy-tyre-${wheel.name}"),
        fontSize=10.sp,color=colors.text)
}

@Composable private fun LegacyPowerLegend(modifier: Modifier) {
    val colors=LocalDashboardPalette.current
    Column(modifier.background(colors.background).border(1.dp,colors.grid).padding(5.dp)
        .testTag("legacy-hmi-power-legend"),verticalArrangement=Arrangement.spacedBy(3.dp)) {
        Text("POWER FLOW",fontSize=10.sp,color=colors.muted)
        Text("━━ BATTERY",fontSize=9.sp,color=colors.accent)
        Text("┈┈ MOTOR (R)",fontSize=9.sp,color=colors.muted)
        Text("┈┈ REGEN",fontSize=9.sp,color=colors.muted)
    }
}

@Composable private fun LegacyDrivingData(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val v = ui.vehicle
    LegacyPanel("DRIVING DATA", modifier.testTag("legacy-hmi-driving-data"), dense = true) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("SPEED", fontSize = 12.sp, color = colors.muted)
                Row(verticalAlignment=Alignment.Bottom) {
                    Text(f.number(v.motion.speedKmh), Modifier.testTag("speed-value"), fontSize = 58.sp, lineHeight = 58.sp,
                        fontWeight = FontWeight.Bold, color = colors.text)
                    Text("km/h",fontSize=12.sp,color=colors.muted,modifier=Modifier.padding(bottom=8.dp))
                }
            }
            Column(horizontalAlignment=Alignment.End) {
                Text("SPEED LIMIT",fontSize=9.sp,color=colors.muted)
                Text("—",Modifier.border(1.dp,colors.grid).padding(7.dp),fontSize=20.sp,color=colors.muted)
                Text(f.gear(v.motion.gear), Modifier.testTag("gear-value"), fontSize = 30.sp, color = colors.accent)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LegacyDatum("RANGE", f.number(v.battery.estimatedRangeKm), "km", "range", Modifier.weight(1f))
            LegacyDatum("BATTERY", f.number(v.battery.stateOfChargePercent), "%", "soc", Modifier.weight(1f))
        }
        LegacySegmentBar(v.battery.stateOfChargePercent.value, Modifier.fillMaxWidth().height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LegacyDatum("PACK", f.number(v.powertrain.packPowerKw, 1), "kW", "power", Modifier.weight(1f))
            LegacyDatum("OUTSIDE", f.number(v.environment.outsideTemperatureC), "°C", null, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(f.text(v.powertrain.driveMode) { "${it.name} MODE" }, color = colors.accent, fontSize = 13.sp)
            Text("MOTOR ${f.number(v.environment.motorTemperatureC)}°C",color=colors.muted,fontSize=10.sp)
        }
    }
}

@Composable private fun LegacySegmentBar(percent: Double?, modifier: Modifier) {
    val colors=LocalDashboardPalette.current
    Canvas(modifier.border(1.dp,colors.grid)) {
        val count=10; val active=((percent ?: 0.0)/10.0).toInt().coerceIn(0,count)
        val gap=2.dp.toPx(); val width=(size.width-gap*(count+1))/count
        repeat(count) { i -> drawRect(if(i<active) colors.accent else colors.grid.copy(alpha=.35f),
            Offset(gap+i*(width+gap),gap),Size(width,size.height-gap*2)) }
    }
}

@Composable private fun LegacyEnergyPanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val power = ui.vehicle.powertrain.packPowerKw
    LegacyPanel("ENERGY", modifier.testTag("legacy-hmi-energy"), dense = true) {
        Canvas(Modifier.fillMaxWidth().weight(1f).border(1.dp, colors.grid)) {
            repeat(4) { i -> drawLine(colors.grid.copy(alpha=.35f),Offset(0f,size.height*i/3f),Offset(size.width,size.height*i/3f),1f) }
            val bars = listOf(.22f,.36f,.3f,.48f,.62f,.34f,.25f,.44f,.57f,.38f,.28f,.46f,.31f,.2f)
            val gap = size.width / bars.size
            bars.forEachIndexed { index, value -> drawRect(colors.accent, Offset(index*gap+2f,size.height*(1-value)), Size(gap-4f,size.height*value)) }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text("AVG ${f.number(ui.vehicle.trip.efficiencyKwhPer100Km,1)} kWh/100km",fontSize=10.sp,color=colors.muted)
            Text("TRIP ${f.number(ui.vehicle.trip.distanceKm,1)} km",fontSize=10.sp,color=colors.muted)
        }
        Text("PACK FLOW  ${f.number(power, 1)} kW", Modifier.testTag("overview-pack-power"), fontSize = 11.sp, color = colors.muted)
    }
}

@Composable private fun LegacyClimatePanel(ui: DashboardUiState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    val f = ui.formatter; val climate = ui.vehicle.climate
    LegacyPanel("CLIMATE", modifier.testTag("legacy-hmi-climate"), dense = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DRIVER\n${f.number(climate.targetTemperatureC)}°C", fontSize = 14.sp)
            Text("↗  ◉  ↖\n  SEAT",fontSize=14.sp,color=colors.muted)
            Text("PASSENGER\n${f.number(climate.targetTemperatureC)}°C", fontSize = 14.sp)
        }
        LegacySegmentBar((climate.fanLevel.value ?: 0)*20.0,Modifier.fillMaxWidth().height(14.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            listOf(if(f.text(climate.enabled){it.toString()}=="true") "A/C ON" else "A/C OFF","AUTO","RECIRC","SYNC").forEachIndexed { i,label ->
                Text(label,Modifier.weight(1f).border(1.dp,if(i==0) colors.accent else colors.grid).padding(5.dp),
                    fontSize=10.sp,color=if(i==0) colors.accent else colors.muted)
            }
        }
        Text("CABIN ${f.number(ui.vehicle.environment.cabinTemperatureC)}°C", fontSize = 10.sp, color = colors.muted)
    }
}

@Composable private fun LegacyAudioPanel(media: MediaState, modifier: Modifier) {
    val colors = LocalDashboardPalette.current
    LegacyPanel("AUDIO · ${media.sourceLabel}", modifier.testTag("legacy-hmi-audio"), dense = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.width(130.dp).height(62.dp).border(1.dp, colors.grid)) {
                val bars = listOf(.35f,.62f,.8f,.48f,.7f,.42f,.66f,.28f)
                val gap = size.width / bars.size
                bars.forEachIndexed { i, value -> drawRect(colors.accent, Offset(i*gap+3f,size.height*(1-value)), Size(gap-6f,size.height*value)) }
            }
            Column { Text("TRACK  5 / 12",fontSize=10.sp,color=colors.muted); Text(media.artist.uppercase(),fontSize=11.sp,color=colors.muted); Text(media.title.uppercase(),fontSize=13.sp) }
        }
        LegacySegmentBar(58.0,Modifier.fillMaxWidth().height(10.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("◀","Ⅱ","▶","•••").forEachIndexed { i,label -> Text(label,Modifier.weight(1f).border(1.dp,if(i==1) colors.accent else colors.grid)
                .padding(4.dp),fontSize=13.sp,color=if(i==1) colors.accent else colors.muted) }
        }
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

@Composable private fun LegacyPanel(title: String, modifier: Modifier = Modifier, dense: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val colors=LocalDashboardPalette.current
    Column(modifier.background(colors.background).border(2.dp,colors.grid)) {
        Text(title,Modifier.fillMaxWidth().border(1.dp,colors.grid).padding(horizontal=6.dp,vertical=2.dp),
            fontSize=12.sp,color=colors.text,letterSpacing=1.sp,fontWeight=FontWeight.Bold)
        Column(Modifier.fillMaxSize().padding(if(dense) 5.dp else 7.dp),
            verticalArrangement=Arrangement.spacedBy(if(dense) 4.dp else 6.dp),content=content)
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
