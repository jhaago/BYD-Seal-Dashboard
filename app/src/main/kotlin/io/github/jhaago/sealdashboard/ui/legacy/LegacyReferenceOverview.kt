package io.github.jhaago.sealdashboard.ui.legacy

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import io.github.jhaago.sealdashboard.R
import io.github.jhaago.sealdashboard.core.*
import io.github.jhaago.sealdashboard.demo.DashboardDemoState
import io.github.jhaago.sealdashboard.preferences.DisplaySettings
import io.github.jhaago.sealdashboard.ui.DashboardDestination
import io.github.jhaago.sealdashboard.ui.DashboardUiState
import io.github.jhaago.sealdashboard.ui.theme.DashboardVisualStyle

/** The user's 1536×864 reference is the actual pixel artwork, not a smooth vector recreation. */
private const val REFERENCE_WIDTH = 1536f
private const val BODY_TOP = 77f
private const val BODY_HEIGHT = 787f
private val cyan = android.graphics.Color.rgb(67, 185, 242)
private val green = android.graphics.Color.rgb(9, 246, 46)
private val dark = android.graphics.Color.rgb(23, 6, 9)

@Composable fun LegacyReferenceNavigation(
    ui: DashboardUiState,
    onOverview: () -> Unit,
    onDestination: (DashboardDestination) -> Unit,
    display: DisplaySettings,
    onStyle: (DashboardVisualStyle) -> Unit,
) {
    val artwork = ImageBitmap.imageResource(R.drawable.legacy_hmi_reference)
    var settingsOpen by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(64.dp).testTag("legacy-hmi-top-nav")) {
        val sx = maxWidth / REFERENCE_WIDTH
        Canvas(Modifier.fillMaxSize()) {
            drawImage(artwork, srcSize = IntSize(1536, 77), dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.None)
        }
        ReferenceTouch("nav-OVERVIEW", "Drive overview", 75f, 26f, 148f, 42f, sx, 1.dp, onOverview)
        ReferenceTouch("nav-DRIVE", "Driving cluster", 231f, 25f, 132f, 44f, sx, 1.dp) { onDestination(DashboardDestination.DRIVE) }
        ReferenceTouch("nav-ENERGY", "Energy", 371f, 25f, 168f, 44f, sx, 1.dp) { onDestination(DashboardDestination.ENERGY) }
        ReferenceTouch("nav-VEHICLE", "Vehicle", 870f, 25f, 190f, 44f, sx, 1.dp) { onDestination(DashboardDestination.VEHICLE) }
        Box(Modifier.offset(x = 1065f * sx, y = 19.dp).width(270f * sx).height(48.dp)) {
            ReferenceTouch("style-menu", "Settings", 0f, 0f, 270f, 48f, sx, 1.dp) { settingsOpen = true }
            DropdownMenu(expanded = settingsOpen, onDismissRequest = { settingsOpen = false }) {
                DashboardVisualStyle.entries.forEach { style ->
                    DropdownMenuItem(text = { Text(style.label) }, onClick = { onStyle(style); settingsOpen = false },
                        modifier = Modifier.testTag("select-style-${style.name}"))
                }
                DropdownMenuItem(text = { Text("Chat") }, onClick = { onDestination(DashboardDestination.ASSISTANT); settingsOpen = false })
                DropdownMenuItem(text = { Text("Development") }, onClick = { onDestination(DashboardDestination.DEVELOPMENT); settingsOpen = false })
            }
        }
        // Semantics remain available without printing a second header over the reference artwork.
        Text(ui.sourceLabel, Modifier.offset(x = 1.dp, y = 1.dp).size(1.dp).testTag("source-label"), color = Color.Transparent)
        Text(ui.status.name, Modifier.offset(x = 1.dp, y = 1.dp).size(1.dp).testTag("provider-status"), color = Color.Transparent)
    }
}

@Composable private fun ReferenceTouch(tag: String, label: String, x: Float, y: Float, width: Float, height: Float,
    sx: Dp, sy: Dp, onClick: () -> Unit) {
    Box(Modifier.offset(x = x * sx, y = y * sy).size(width * sx, height * sy)
        .testTag(tag).semantics { contentDescription = label }.clickable(onClick = onClick))
}

@Composable fun LegacyReferenceOverview(ui: DashboardUiState, demos: DashboardDemoState) {
    val artwork = ImageBitmap.imageResource(R.drawable.legacy_hmi_reference)
    val f = ui.formatter
    val v = ui.vehicle
    val soc = f.number(v.battery.stateOfChargePercent)
    val pressures = Wheel.entries.associateWith { wheel ->
        f.number(v.wheels[wheel]?.pressureKpa ?: Signal.unavailable())
    }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("legacy-hmi-overview")) {
        val sx = maxWidth / REFERENCE_WIDTH
        val sy = maxHeight / BODY_HEIGHT
        Canvas(Modifier.fillMaxSize()) {
            drawImage(artwork, srcOffset = IntOffset(0, 77), srcSize = IntSize(1536, 787),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.None)
            drawIntoCanvas { composeCanvas ->
                val canvas = composeCanvas.nativeCanvas
                canvas.save()
                canvas.scale(size.width / REFERENCE_WIDTH, size.height / BODY_HEIGHT)
                canvas.translate(0f, -BODY_TOP)
                val cover = Paint().apply { color = dark; style = Paint.Style.FILL }
                val type = Paint().apply { isAntiAlias = false; typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }
                fun value(x: Float, y: Float, width: Float, height: Float, text: String, size: Float,
                    ink: Int = cyan) {
                    canvas.drawRect(x, y, x + width, y + height, cover)
                    type.color = ink
                    type.textSize = size
                    canvas.drawText(text, x + 2f, y + height - (height - size) / 2f - 4f, type)
                }
                // Cover the example readings in the image. A disconnected/stale source prints dashes.
                value(111f, 129f, 180f, 81f, "${demos.navigation.distanceKm} km", 37f)
                value(112f, 171f, 176f, 40f, demos.navigation.nextTurn.uppercase().take(17), 15f)
                value(310f, 132f, 131f, 45f, "${demos.navigation.minutes} MIN", 17f)
                value(689f, 151f, 125f, 31f, "${f.number(v.powertrain.motorPowerKw[Axle.FRONT] ?: Signal.unavailable())} kW", 22f)
                value(860f, 517f, 129f, 31f, "${f.number(v.powertrain.motorPowerKw[Axle.REAR] ?: Signal.unavailable())} kW", 22f)
                value(677f, 364f, 58f, 28f, "$soc%", 23f)
                val charge = v.battery.stateOfChargePercent.takeIf { f.quality(it) == SignalQuality.FRESH }?.value
                canvas.drawRect(640f, 345f, 661f, 390f, cover)
                if (charge != null) {
                    type.color = green
                    canvas.drawRect(640f, 390f - (charge.coerceIn(0.0, 100.0).toFloat() * .45f), 661f, 390f, type)
                }
                value(1038f, 129f, 30f, 24f, f.gear(v.motion.gear), 24f, green)
                value(1155f, 154f, 225f, 90f, f.number(v.motion.speedKmh), 100f)
                value(1285f, 207f, 94f, 36f, "km/h", 27f)
                value(1428f, 169f, 67f, 34f, "—", 31f)
                value(1114f, 298f, 164f, 56f, "${f.number(v.battery.estimatedRangeKm)} km", 47f)
                value(1301f, 290f, 118f, 36f, "$soc%", 30f)
                canvas.drawRect(1303f, 326f, 1495f, 354f, cover)
                val segmentCount = ((charge ?: 0.0) / 10.0).toInt().coerceIn(0, 10)
                repeat(10) { index ->
                    type.color = if (index < segmentCount) green else cyan
                    type.style = if (index < segmentCount) Paint.Style.FILL else Paint.Style.STROKE
                    type.strokeWidth = 2f
                    val left = 1304f + index * 19f
                    canvas.drawRect(left, 327f, left + 16f, 350f, type)
                }
                type.style = Paint.Style.FILL
                value(1112f, 402f, 174f, 44f, f.text(v.powertrain.driveMode) { it.name }, 26f, green)
                value(1334f, 403f, 165f, 46f, "${f.number(v.environment.outsideTemperatureC)}°C", 29f)
                value(1114f, 491f, 181f, 39f, "${f.number(v.environment.motorTemperatureC)}°C / —", 25f)
                value(1335f, 499f, 55f, 27f, pressures.getValue(Wheel.FRONT_LEFT), 20f)
                value(1459f, 499f, 50f, 27f, pressures.getValue(Wheel.FRONT_RIGHT), 20f)
                value(1335f, 526f, 55f, 28f, pressures.getValue(Wheel.REAR_LEFT), 20f)
                value(1459f, 526f, 50f, 28f, pressures.getValue(Wheel.REAR_RIGHT), 20f)
                value(45f, 801f, 237f, 28f, "${f.number(v.trip.efficiencyKwhPer100Km, 1)} kWh/100km", 22f)
                value(315f, 801f, 172f, 28f, "${f.number(v.trip.distanceKm, 1)} km", 24f)
                value(552f, 647f, 112f, 27f, "${f.number(v.climate.targetTemperatureC)}°C", 22f)
                value(892f, 647f, 109f, 27f, "${f.number(v.climate.targetTemperatureC)}°C", 22f)
                value(1308f, 660f, 195f, 24f, demos.media.artist.uppercase().take(17), 19f)
                value(1308f, 685f, 195f, 26f, demos.media.title.uppercase().take(17), 19f)
                canvas.restore()
            }
        }
        // The image is decorative; these real values and regions expose the UI to accessibility and tests.
        ReferenceRegion("legacy-hmi-navigation", 14f, 79f, 447f, 495f, sx, sy)
        ReferenceRegion("legacy-hmi-vehicle-schematic", 469f, 111f, 609f, 465f, sx, sy)
        ReferenceRegion("legacy-hmi-driving-data", 1092f, 79f, 429f, 496f, sx, sy)
        ReferenceRegion("legacy-hmi-power-legend", 891f, 222f, 175f, 160f, sx, sy)
        ReferenceRegion("legacy-hmi-battery-segments", 627f, 335f, 121f, 66f, sx, sy)
        ReferenceReading("speed-value", f.number(v.motion.speedKmh), 1154f, 158f, 225f, 90f, sx, sy)
        ReferenceReading("gear-value", f.gear(v.motion.gear), 1035f, 124f, 38f, 38f, sx, sy)
        ReferenceReading("range-value", f.number(v.battery.estimatedRangeKm), 1113f, 298f, 164f, 55f, sx, sy)
        ReferenceReading("soc-value", "$soc%", 1300f, 289f, 119f, 40f, sx, sy)
        ReferenceReading("power-value", f.number(v.powertrain.packPowerKw, 1), 893f, 223f, 1f, 1f, sx, sy)
        listOf(Wheel.FRONT_LEFT to (1333f to 499f), Wheel.FRONT_RIGHT to (1458f to 499f),
            Wheel.REAR_LEFT to (1333f to 526f), Wheel.REAR_RIGHT to (1458f to 526f)).forEach { (wheel, point) ->
            ReferenceReading("legacy-tyre-${wheel.name}", "${pressures.getValue(wheel)} kPa", point.first, point.second,
                61f, 25f, sx, sy)
        }
    }
}

@Composable private fun ReferenceRegion(tag: String, x: Float, y: Float, width: Float, height: Float, sx: Dp, sy: Dp) {
    Box(Modifier.offset(x = x * sx, y = (y - BODY_TOP) * sy).size(width * sx, height * sy).testTag(tag))
}

@Composable private fun ReferenceReading(tag: String, value: String, x: Float, y: Float, width: Float, height: Float,
    sx: Dp, sy: Dp) {
    Text(value, Modifier.offset(x = x * sx, y = (y - BODY_TOP) * sy).size(width * sx, height * sy)
        .testTag(tag), color = Color.Transparent, fontSize = 1.sp)
}
