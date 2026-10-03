package io.github.jhaago.sealdashboard.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

object DashboardColors {
    val Background = Color(0xFF0B1015)
    val Surface = Color(0xFF151D24)
    val Elevated = Color(0xFF1D2932)
    val Text = Color(0xFFF1F2EE)
    val Muted = Color(0xFFABB9C3)
    val Accent = Color(0xFF72DED8)
    val Warning = Color(0xFFFFCB78)
    val Grid = Color(0xFF2B3943)
}
@Composable fun DashboardTheme(style: DashboardVisualStyle = DashboardVisualStyle.MODERN, content: @Composable () -> Unit) {
    val palette = paletteFor(style)
    val family = if (style == DashboardVisualStyle.SYSTEMS) FontFamily.Monospace else FontFamily.SansSerif
    MaterialTheme(
        colorScheme = darkColorScheme(primary = palette.accent, onPrimary = palette.background,
            background = palette.background, onBackground = palette.text,
            surface = palette.surface, onSurface = palette.text,
            secondary = palette.muted, outline = palette.grid, error = palette.error),
        typography = Typography(bodyLarge = TextStyle(fontFamily = family, fontSize = 18.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 22.sp),
            labelLarge = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 22.sp)),
        content = { CompositionLocalProvider(LocalContentColor provides palette.text,
            LocalDashboardPalette provides palette, LocalDashboardStyle provides style, content = content) },
    )
}
