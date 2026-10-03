package io.github.jhaago.sealdashboard.ui

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.github.jhaago.sealdashboard.preferences.*
import io.github.jhaago.sealdashboard.ui.theme.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class DisplayPreferencesTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @After fun restore() { context.getSharedPreferences("display", Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun reconstructingPreferencesRestoresStyleWithoutLosingLayoutAndMirror() {
        DisplayPreferences(context).update(DisplaySettings(DriveLayout.FULL, true, DashboardVisualStyle.SYSTEMS))
        val restored = DisplayPreferences(context).state.value
        assertEquals(DashboardVisualStyle.SYSTEMS, restored.visualStyle)
        assertEquals(DriveLayout.FULL, restored.layout)
        assertTrue(restored.mirrored)
    }

    @Test fun corruptStoredStyleFallsBackToModern() {
        context.getSharedPreferences("display", Context.MODE_PRIVATE).edit().putString("style", "CORRUPT").commit()
        assertEquals(DashboardVisualStyle.MODERN, DisplayPreferences(context).state.value.visualStyle)
    }

    @Test fun wrongTypeStoredStyleFallsBackWithoutLosingOtherChoices() {
        DisplayPreferences(context).update(DisplaySettings(DriveLayout.FULL, true))
        context.getSharedPreferences("display", Context.MODE_PRIVATE).edit().putInt("style", 1).commit()
        val restored = DisplayPreferences(context).state.value
        assertEquals(DashboardVisualStyle.MODERN, restored.visualStyle)
        assertEquals(DriveLayout.FULL, restored.layout)
        assertTrue(restored.mirrored)
    }

    @Test fun switchingStylePreservesExistingDisplayChoices() {
        val preferences = DisplayPreferences(context)
        preferences.update(DisplaySettings(DriveLayout.FULL, true, DashboardVisualStyle.SYSTEMS))
        preferences.update(preferences.state.value.copy(visualStyle = DashboardVisualStyle.TRON))
        assertEquals(DisplaySettings(DriveLayout.FULL, true, DashboardVisualStyle.TRON), DisplayPreferences(context).state.value)
    }

    @Test fun previewSourcePersistsAndUnknownSourceUsesLayoutDefault() {
        DisplayPreferences(context).update(DisplaySettings(DriveLayout.FULL, true, DashboardVisualStyle.TRON, NavigationSource.OWN))
        assertEquals(NavigationSource.OWN, DisplayPreferences(context).state.value.navigationSource)
        context.getSharedPreferences("display", Context.MODE_PRIVATE).edit().putString("navigationSource", "FUTURE").commit()
        assertEquals(NavigationSource.FOLLOW_LAYOUT, DisplayPreferences(context).state.value.navigationSource)
    }
}
