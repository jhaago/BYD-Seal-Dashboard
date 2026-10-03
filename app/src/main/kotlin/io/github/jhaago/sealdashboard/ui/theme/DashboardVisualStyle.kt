package io.github.jhaago.sealdashboard.ui.theme

enum class DashboardVisualStyle(val label: String) { MODERN("Modern"), SYSTEMS("Systems"), TRON("Tron") }
fun parseVisualStyle(raw: String?): DashboardVisualStyle =
    DashboardVisualStyle.entries.find { it.name == raw } ?: DashboardVisualStyle.MODERN
