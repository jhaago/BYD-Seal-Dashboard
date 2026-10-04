package io.github.jhaago.sealdashboard.ui.theme

enum class DashboardVisualStyle(val label: String) {
    MODERN("Modern"),
    LEGACY_HMI("Legacy HMI"),
    FUTURISTIC("Futuristic"),
}

fun parseVisualStyle(raw: String?): DashboardVisualStyle = when (raw) {
    "SYSTEMS" -> DashboardVisualStyle.LEGACY_HMI
    "TRON" -> DashboardVisualStyle.FUTURISTIC
    else -> DashboardVisualStyle.entries.find { it.name == raw } ?: DashboardVisualStyle.MODERN
}
