package io.github.jhaago.sealdashboard.ui.legacy

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.jhaago.sealdashboard.R
import io.github.jhaago.sealdashboard.core.Wheel
import io.github.jhaago.sealdashboard.ui.DashboardDestination
import io.github.jhaago.sealdashboard.ui.DashboardUiState
import io.github.jhaago.sealdashboard.ui.theme.DashboardVisualStyle

private const val W = 1536f
private const val H = 1152f
private const val CYAN = 0xff44d8dc.toInt()
private const val GREEN = 0xff3bfa35.toInt()
private const val DARK = 0xff100408.toInt()

@Composable fun LegacyPixelNavigation(
    page: LegacyPixelPage, ui: DashboardUiState,
    onPage: (LegacyPixelPage) -> Unit,
    onDestination: (DashboardDestination) -> Unit,
    onStyle: (DashboardVisualStyle) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().background(Color(DARK)).testTag("legacy-hmi-top-nav"),
        verticalAlignment = Alignment.CenterVertically) {
        LegacyTab("POWER", page == LegacyPixelPage.POWER, "nav-OVERVIEW", Modifier.weight(1f)) { onPage(LegacyPixelPage.POWER) }
        LegacyTab("CHARGE", page == LegacyPixelPage.CHARGE, "legacy-page-CHARGE", Modifier.weight(1f)) { onPage(LegacyPixelPage.CHARGE) }
        LegacyTab("TYRES", page == LegacyPixelPage.TYRES, "legacy-page-TYRES", Modifier.weight(1f)) { onPage(LegacyPixelPage.TYRES) }
        LegacyTab("PROFILE", page == LegacyPixelPage.PROFILE, "legacy-page-PROFILE", Modifier.weight(1f)) { onPage(LegacyPixelPage.PROFILE) }
        LegacyTab("CLUSTER", false, "nav-DRIVE", Modifier.weight(1f)) { onDestination(DashboardDestination.DRIVE) }
        LegacyTab("ENERGY", false, "nav-ENERGY", Modifier.weight(1f)) { onDestination(DashboardDestination.ENERGY) }
        LegacyTab("CHAT", false, "nav-ASSISTANT", Modifier.weight(.8f)) { onDestination(DashboardDestination.ASSISTANT) }
        Box(Modifier.weight(.8f)) {
            LegacyTab("SET", false, "style-menu", Modifier.fillMaxWidth()) { menu = true }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DashboardVisualStyle.entries.forEach { style ->
                    DropdownMenuItem(text = { Text(style.label) }, onClick = { onStyle(style); menu = false },
                        modifier = Modifier.testTag("select-style-${style.name}"))
                }
                DropdownMenuItem(text = { Text("Chat") }, onClick = {
                    onDestination(DashboardDestination.ASSISTANT); menu = false
                })
                DropdownMenuItem(text = { Text("Development") }, onClick = {
                    onDestination(DashboardDestination.DEVELOPMENT); menu = false
                })
            }
        }
    }
    // The source and provider status are also accessible to UI automation and screen readers.
    Text(ui.sourceLabel, Modifier.testTag("source-label").background(Color(DARK)), color = Color(CYAN), fontSize = 10.sp)
    Text(ui.status.name, Modifier.testTag("provider-status").background(Color(DARK)), color = Color(CYAN), fontSize = 10.sp)
}

@Composable private fun LegacyTab(label: String, selected: Boolean, tag: String, modifier: Modifier, click: () -> Unit) {
    Box(modifier.heightIn(min = 48.dp).testTag(tag).semantics { contentDescription = label }
        .clickable(onClick = click).background(if (selected) Color(GREEN) else Color(DARK)),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) Color(DARK) else Color(CYAN), fontSize = 12.sp, maxLines = 1)
    }
}

/** A 4:3 logical pixel canvas; all borders and live glyphs share one coordinate grid. */
@Composable fun LegacyPixelScreen(ui: DashboardUiState, page: LegacyPixelPage) {
    val top = ImageBitmap.imageResource(R.drawable.legacy_seal_top)
    val side = ImageBitmap.imageResource(R.drawable.legacy_seal_side)
    val model = legacyPixelModel(ui.vehicle, ui.formatter)
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(DARK)).testTag("legacy-hmi-overview")) {
        val width = minOf(maxWidth, maxHeight * (W / H))
        val height = width * (H / W)
        Box(Modifier.width(width).height(height).align(Alignment.Center)
            .testTag("legacy-canvas-${page.name}")) {
            Canvas(Modifier.fillMaxSize()) {
                val sx = size.width / W
                val sy = size.height / H
                if (page == LegacyPixelPage.PROFILE) {
                    drawImage(side, srcOffset = IntOffset(70, 340), srcSize = IntSize(1390, 510),
                        dstOffset = IntOffset((80*sx).toInt(), (250*sy).toInt()),
                        dstSize = IntSize((1376*sx).toInt(), (510*sy).toInt()), filterQuality = FilterQuality.None)
                } else {
                    drawImage(top, srcOffset = IntOffset(490, 190), srcSize = IntSize(550, 1160),
                        dstOffset = IntOffset((516*sx).toInt(), (155*sy).toInt()),
                        dstSize = IntSize((505*sx).toInt(), (880*sy).toInt()), filterQuality = FilterQuality.None)
                }
                drawIntoCanvas { target ->
                    val c = target.nativeCanvas
                    c.save()
                    c.scale(sx, sy)
                    val pen = Paint().apply { color = CYAN; isAntiAlias = false; style = Paint.Style.STROKE; strokeWidth = 4f }
                    fun line(x: Float, y: Float, x2: Float, y2: Float, green: Boolean = false) {
                        pen.color = if (green) GREEN else CYAN; pen.style = Paint.Style.STROKE
                        c.drawLine(x, y, x2, y2, pen)
                    }
                    fun box(x: Float, y: Float, width: Float, height: Float, green: Boolean = false) {
                        pen.color = if (green) GREEN else CYAN; pen.style = Paint.Style.STROKE
                        c.drawRect(x, y, x + width, y + height, pen)
                    }
                    fun text(s: String, x: Float, y: Float, cell: Float = 5f, green: Boolean = false) {
                        pen.style = Paint.Style.FILL; pen.color = if (green) GREEN else CYAN
                        pixelText(c, s, x, y, cell, pen)
                    }
                    fun panel(label: String, value: String, unit: String, x: Float, y: Float, width: Float = 410f, height: Float = 270f) {
                        box(x,y,width,height)
                        text(label,x+28,y+30,6f)
                        text(value,x+45,y+99,if (value.length > 2) 14f else 17f,true)
                        text(unit,x+width-105,y+height-66,5f)
                    }
                    fun gauge(x: Float,y: Float, fraction: Float?) {
                        repeat(10) { i ->
                            box(x+i*30,y,25f,27f, green = fraction != null && i < (fraction*10).toInt())
                            if (fraction != null && i < (fraction*10).toInt()) {
                                pen.style = Paint.Style.FILL; pen.color = GREEN
                                c.drawRect(x+i*30+4,y+4,x+i*30+21,y+23,pen)
                            }
                        }
                    }
                    val socFraction = model.soc.toFloatOrNull()?.div(100f)?.coerceIn(0f,1f)
                    if (page == LegacyPixelPage.POWER || page == LegacyPixelPage.CHARGE) {
                        // A separate live circuit layer leaves the supplied body outline untouched.
                        box(724f,325f,90f,94f)
                        box(675f,525f,190f,245f)
                        box(724f,845f,90f,94f)
                        line(769f,420f,769f,525f,model.discharging || model.charging)
                        line(769f,770f,769f,845f,model.discharging || model.charging)
                        line(650f,373f,724f,373f,model.discharging)
                        line(814f,373f,885f,373f,model.discharging)
                        line(650f,891f,724f,891f,model.discharging)
                        line(814f,891f,885f,891f,model.discharging)
                        repeat(6) { i ->
                            val on = socFraction != null && i < (socFraction * 6).toInt()
                            box(700f,550f+i*32f,140f,27f,on)
                        }
                    }
                    when(page) {
                        LegacyPixelPage.POWER -> {
                            text("BATTERY / POWER FLOW",40f,38f,9f)
                            text("BYD SEAL",1260f,51f,5f)
                            line(35f,120f,1500f,120f)
                            panel("BAT",model.soc,"%",38f,180f)
                            gauge(88f,399f,socFraction)
                            panel("PACK",model.voltage,"V",38f,500f,410f,250f)
                            panel("RANGE",model.range,"KM",38f,790f,410f,270f)
                            panel("DISCHARGE",model.discharge,"KW",1088f,180f)
                            text(if (model.discharging) ">>>>>" else "-----",1140f,400f,12f,model.discharging)
                            panel("REGEN",model.regen,"KW",1088f,490f,410f,245f)
                            text(if (model.recovering) "<<<<<" else "-----",1140f,690f,12f,model.recovering)
                            box(1088f,780f,410f,280f)
                            text("POWER FLOW",1120f,807f,5f)
                            text("BATTERY PACK",1120f,870f,5f)
                            text("FRONT MOTOR  —",1120f,930f,4f)
                            text("REAR MOTOR",1120f,990f,5f)
                        }
                        LegacyPixelPage.CHARGE -> {
                            box(22f,60f,1492f,1050f)
                            text("CHARGING / DISCHARGE",55f,95f,8f)
                            line(43f,157f,1490f,157f)
                            panel("SOC",model.soc,"%",52f,190f,400f,298f)
                            gauge(95f,420f,socFraction)
                            panel("CHARGE",model.charge,"KW",52f,525f,400f,270f)
                            text(if (model.charging) ">>>" else "---",110f,736f,9f,model.charging)
                            panel("TIME REM",model.timeRemaining,"",52f,830f,400f,250f)
                            panel("PACK TEMP",model.packTemperature,"°C",1090f,190f,400f,298f)
                            panel("OUTPUT",model.discharge,"KW",1090f,525f,400f,270f)
                            text(if (model.discharging) ">>>" else "---",1150f,736f,9f,model.discharging)
                            text(if (model.charging) "CHARGING" else "CHARGE INACTIVE",1050f,915f,5f,true)
                        }
                        LegacyPixelPage.TYRES -> {
                            text("TYRE PRESSURE",445f,98f,11f)
                            line(50f,165f,460f,165f); line(1070f,165f,1480f,165f)
                            val locations = listOf(
                                Triple(Wheel.FRONT_LEFT,95f,245f), Triple(Wheel.FRONT_RIGHT,1118f,245f),
                                Triple(Wheel.REAR_LEFT,95f,710f), Triple(Wheel.REAR_RIGHT,1118f,710f))
                            locations.forEach { (wheel,x,y) ->
                                val reading = model.tyres.getValue(wheel)
                                box(x,y,326f,270f,true)
                                text(wheel.name.replace('_',' '),x+24f,y+30f,4f)
                                line(x+12f,y+78f,x+310f,y+78f,true)
                                text(reading.pressure,x+28f,y+105f,11f,true)
                                text("KPA",x+228f,y+150f,4f,true)
                                line(x+12f,y+213f,x+310f,y+213f,true)
                                text("${reading.temperature}°C",x+110f,y+225f,5f,true)
                            }
                            line(55f,1050f,1480f,1050f)
                            box(432f,1068f,680f,64f,true)
                            text(if (model.tyreDataComplete) "TYRE DATA AVAILABLE" else "TYRE DATA UNAVAILABLE",475f,1085f,6f,true)
                        }
                        LegacyPixelPage.PROFILE -> {
                            text("BYD SEAL / VEHICLE PROFILE",60f,65f,9f)
                            line(50f,140f,1480f,140f)
                            box(80f,805f,1376f,240f)
                            text("SPEED ${model.speed} KM/H",125f,850f,6f)
                            text("GEAR ${model.gear}",875f,850f,6f,true)
                            text("RANGE ${model.range} KM",125f,945f,6f)
                            text("MODE ${model.mode}",875f,945f,6f,true)
                        }
                    }
                    c.restore()
                }
            }
            // Native semantics accompany the pixel canvas, including each tyre's actual reading.
            Text(model.soc + "%", Modifier.testTag("soc-value"), color = Color.Transparent, fontSize = 1.sp)
            Text(model.speed, Modifier.testTag("speed-value"), color = Color.Transparent, fontSize = 1.sp)
            if (page == LegacyPixelPage.TYRES) Wheel.entries.forEach { wheel ->
                Text("${model.tyres.getValue(wheel).pressure} kPa", Modifier.testTag("legacy-tyre-${wheel.name}"),
                    color = Color.Transparent, fontSize = 1.sp)
            }
            Box(Modifier.align(Alignment.Center).size(width * .32f, height * .75f).testTag("legacy-hmi-vehicle-schematic"))
        }
    }
}
