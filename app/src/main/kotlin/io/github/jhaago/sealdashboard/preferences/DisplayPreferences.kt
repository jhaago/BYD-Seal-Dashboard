package io.github.jhaago.sealdashboard.preferences

import android.content.Context
import kotlinx.coroutines.flow.*

enum class DriveLayout { COMPANION, FULL }
data class DisplaySettings(val layout: DriveLayout = DriveLayout.COMPANION, val mirrored: Boolean = false)

/** Only visual preferences survive process death; telemetry/trip values do not. */
class DisplayPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("display", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(DisplaySettings(
        DriveLayout.entries.find { it.name == preferences.getString("layout", null) } ?: DriveLayout.COMPANION,
        preferences.getBoolean("mirrored", false),
    ))
    val state = mutableState.asStateFlow()
    fun update(settings: DisplaySettings) {
        preferences.edit().putString("layout", settings.layout.name).putBoolean("mirrored", settings.mirrored).apply()
        mutableState.value = settings
    }
}
