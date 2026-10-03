package io.github.jhaago.sealdashboard.preferences

import android.content.Context
import kotlinx.coroutines.flow.*
import io.github.jhaago.sealdashboard.ui.theme.*

enum class DriveLayout { COMPANION, FULL }
enum class NavigationSource { FOLLOW_LAYOUT, OWN, FACTORY_PREVIEW }
data class DisplaySettings(val layout: DriveLayout = DriveLayout.COMPANION, val mirrored: Boolean = false,
    val visualStyle: DashboardVisualStyle = DashboardVisualStyle.MODERN,
    val navigationSource: NavigationSource = NavigationSource.FOLLOW_LAYOUT) {
    val ownNavigation get() = navigationSource == NavigationSource.OWN ||
        (navigationSource == NavigationSource.FOLLOW_LAYOUT && layout == DriveLayout.FULL)
}

/** Only visual preferences survive process death; telemetry/trip values do not. */
class DisplayPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("display", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(DisplaySettings(
        DriveLayout.entries.find { it.name == preferences.getString("layout", null) } ?: DriveLayout.COMPANION,
        preferences.getBoolean("mirrored", false),
        parseVisualStyle(preferences.getString("style", null)),
        NavigationSource.entries.find { it.name == preferences.getString("navigationSource", null) } ?: NavigationSource.FOLLOW_LAYOUT,
    ))
    val state = mutableState.asStateFlow()
    fun update(settings: DisplaySettings) {
        preferences.edit().putString("layout", settings.layout.name).putBoolean("mirrored", settings.mirrored)
            .putString("style", settings.visualStyle.name).putString("navigationSource", settings.navigationSource.name).apply()
        mutableState.value = settings
    }
}
