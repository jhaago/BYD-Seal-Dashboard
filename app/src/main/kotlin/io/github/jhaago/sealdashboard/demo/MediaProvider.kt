package io.github.jhaago.sealdashboard.demo

import kotlinx.coroutines.flow.*

data class MediaState(
    val sourceLabel: String = "SAMPLE MEDIA",
    val title: String = "Coastal drive",
    val artist: String = "Dashboard sample",
    val playbackControlAvailable: Boolean = false,
)
/** Metadata only in milestone one; no playback command surface or media permission. */
interface MediaProvider { val state: StateFlow<MediaState> }
class DemoMediaProvider : MediaProvider {
    override val state = MutableStateFlow(MediaState()).asStateFlow()
}
