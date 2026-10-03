package io.github.jhaago.sealdashboard.demo

import kotlinx.coroutines.flow.*

data class ProjectionState(
    val sourceLabel: String = "ANDROID AUTO · SIMULATED PREVIEW",
    val isRealSession: Boolean = false,
    val explanation: String = "Layout preview only. Factory Android Auto integration requires vehicle testing.",
)
interface ProjectionProvider { val state: StateFlow<ProjectionState> }
class DemoProjectionProvider : ProjectionProvider {
    override val state = MutableStateFlow(ProjectionState()).asStateFlow()
}
data class DashboardDemoState(
    val navigation: NavigationState = NavigationState(),
    val media: MediaState = MediaState(),
    val projection: ProjectionState = ProjectionState(),
)
