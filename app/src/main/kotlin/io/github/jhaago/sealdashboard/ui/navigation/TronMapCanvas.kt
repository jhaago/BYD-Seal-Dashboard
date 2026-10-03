package io.github.jhaago.sealdashboard.ui.navigation

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import io.github.jhaago.sealdashboard.demo.*
import io.github.jhaago.sealdashboard.ui.theme.*
import kotlin.math.hypot

@Composable fun TronMapCanvas(map: DemoStreetMap, route: List<RoutePoint>, modifier: Modifier = Modifier,
    markers: List<MapChargerMarker> = emptyList()) {
    val colors = LocalDashboardPalette.current
    val neon = LocalDashboardStyle.current == DashboardVisualStyle.TRON
    Canvas(modifier.testTag("street-map").semantics {
        contentDescription = "Fictional streets and building blocks, sample route, vehicle marker and demo chargers. Not geographic navigation."
    }) {
        fun point(p: RoutePoint) = Offset(p.x*size.width, p.y*size.height)
        fun path(points: List<RoutePoint>, closed: Boolean = false) = Path().apply {
            points.forEachIndexed { i,p -> val q=point(p); if(i==0) moveTo(q.x,q.y) else lineTo(q.x,q.y) }
            if(closed && points.isNotEmpty()) close()
        }
        map.blocks.filter { it.size >= 3 }.forEach {
            drawPath(path(it,true),colors.elevated.copy(alpha=.7f))
            drawPath(path(it,true),colors.grid.copy(alpha=.65f),style=Stroke(1.dp.toPx()))
        }
        map.streets.filter { it.size >= 2 }.forEach {
            drawPath(path(it),colors.grid.copy(alpha=if(neon) .95f else .55f),style=Stroke(3.dp.toPx()))
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.muted.copy(alpha=.9f).toArgb(); textSize=16.sp.toPx()
            typeface = Typeface.create(Typeface.MONOSPACE,Typeface.NORMAL)
        }
        listOf(Triple("COAST RD",.36f,.46f),Triple("STATION AVE",.22f,.14f),Triple("DOCK ST",.76f,.68f)).forEach { (label,x,y) ->
            drawContext.canvas.nativeCanvas.drawText(label,(x*size.width).coerceAtMost((size.width-paint.measureText(label)-8.dp.toPx()).coerceAtLeast(0f)),y*size.height,paint)
        }
        if(route.size >= 2) {
            val p = path(route)
            drawPath(p,colors.accent.copy(alpha=if(neon) .14f else .08f),style=Stroke(if(neon) 20.dp.toPx() else 12.dp.toPx(),cap=StrokeCap.Round))
            drawPath(p,colors.accent,style=Stroke(4.dp.toPx(),cap=StrokeCap.Round))
            val start=point(route.first()); val next=point(route[1]); val length=hypot(next.x-start.x,next.y-start.y)
            drawCircle(colors.background,14.dp.toPx(),start)
            drawCircle(colors.accent,14.dp.toPx(),start,style=Stroke(2.dp.toPx()))
            if(length>0) {
                val ux=(next.x-start.x)/length; val uy=(next.y-start.y)/length
                val arrow=Path().apply {
                    moveTo(start.x+ux*11.dp.toPx(),start.y+uy*11.dp.toPx())
                    lineTo(start.x-ux*7.dp.toPx()-uy*7.dp.toPx(),start.y-uy*7.dp.toPx()+ux*7.dp.toPx())
                    lineTo(start.x-ux*7.dp.toPx()+uy*7.dp.toPx(),start.y-uy*7.dp.toPx()-ux*7.dp.toPx());close()
                };drawPath(arrow,colors.text)
            }
        }
        markers.forEachIndexed { index,marker ->
            val p=point(marker.point)
            drawCircle(colors.background,14.dp.toPx(),p)
            drawCircle(colors.accent,14.dp.toPx(),p,style=Stroke(2.dp.toPx()))
            paint.color=colors.text.toArgb(); val label=(index+1).toString()
            drawContext.canvas.nativeCanvas.drawText(label,p.x-paint.measureText(label)/2,p.y+paint.textSize*.35f,paint)
        }
    }
}
