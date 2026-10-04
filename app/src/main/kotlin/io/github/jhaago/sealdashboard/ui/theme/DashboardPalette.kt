package io.github.jhaago.sealdashboard.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DashboardPalette(val background: Color, val surface: Color, val elevated: Color,
    val text: Color, val muted: Color, val accent: Color, val warning: Color, val error: Color, val grid: Color)

fun paletteFor(style: DashboardVisualStyle): DashboardPalette = when (style) {
    DashboardVisualStyle.MODERN -> DashboardPalette(Color(0xFF0B1015), Color(0xFF151D24), Color(0xFF1D2932),
        Color(0xFFF1F2EE), Color(0xFFABB9C3), Color(0xFF72DED8), Color(0xFFFFCB78), Color(0xFFFF8894), Color(0xFF2B3943))
    DashboardVisualStyle.LEGACY_HMI -> DashboardPalette(Color(0xFF090506), Color(0xFF12080B), Color(0xFF1D0D13),
        Color(0xFF77D9FF), Color(0xFF62BFE9), Color(0xFF20F565), Color(0xFFFFD75E), Color(0xFFFF6878), Color(0xFF2BC7FF))
    DashboardVisualStyle.FUTURISTIC -> DashboardPalette(Color(0xFF050C16), Color(0xFF0B1827), Color(0xFF14283B),
        Color(0xFFE6F5FF), Color(0xFFA4BFCE), Color(0xFF52EAFF), Color(0xFFFFD077), Color(0xFFFF89AB), Color(0xFF244862))
}
val LocalDashboardPalette = staticCompositionLocalOf { paletteFor(DashboardVisualStyle.MODERN) }
val LocalDashboardStyle = staticCompositionLocalOf { DashboardVisualStyle.MODERN }
