# Dashboard Visual Themes Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. Preserve Jordan's native execution preference; do not merge to main.

**Goal:** Deliver Modern, Systems and Tron visual styles with an original offline navigation preview.

**Architecture:** Retain core/mock/provider modules; theme tokens and map rendering live in `:app`. Persist style in existing display preferences. The route preview consumes navigation state, without vehicle command access.

**Tech Stack:** Existing pinned Kotlin/Compose/JDK 17 toolchain; no new services or libraries.

**Spec:** `docs/superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md`, proposed extension; execution awaits design/plan review.

## Global Constraints

- Australian 2024 Seal Dynamic RWD; original artwork only.
- Minimum API 24, compile/target API 37, test API 29 and API 37; an unavailable emulator is a reported blocker, not a pass.
- Normal labels at least 16 sp, touch targets at least 56 dp, font scale 1.3 supported.
- Preserve source/freshness labels, core read-only contract and existing split/full/mirror preferences.
- `SYSTEMS` is an original inspired style; no defence assets or operational layouts.
- Tron offline map is fictional and labelled `SIMULATED MAP`; no Google tiles, attribution claims or real guidance.
- No additional permission, account, credential or network requirement.

## Review Focus

1. Unknown/corrupted persisted style should fall back to Modern — Task 1.
2. Global mutable colours should not leak one theme into another composition — Task 1.
3. Decorative map/theme elements should not obscure telemetry or warnings at 400 dp/font 1.3 — Task 2.
4. Switching map source must never imply control of factory Android Auto — Task 3.
5. Empty/malformed route geometry must render an honest unavailable preview — Task 3.

## Files and interfaces

Package prefix `P` below means `io/github/jhaago/sealdashboard`.

- Create `app/src/main/kotlin/P/ui/theme/DashboardVisualStyle.kt`, `DashboardPalette.kt`, `ThemeSelector.kt`.
- Modify `app/src/main/kotlin/P/ui/theme/DashboardTheme.kt`, `preferences/DisplayPreferences.kt`, `ui/DashboardShell.kt`, and existing screen/components that read `DashboardColors`.
- Create `app/src/main/kotlin/P/ui/navigation/NavigationPreview.kt`, `TronMapCanvas.kt`, `DemoStreetMap.kt`.
- Modify existing `demo/NavigationProvider.kt` and `ui/drive/DriveScreen.kt` to pass map state/renderers rather than keep route Canvas inline.

### Task 1: Immutable theme tokens and persistent selection

**Produces:** `enum class DashboardVisualStyle { MODERN, SYSTEMS, TRON }`; `fun parseVisualStyle(raw: String?): DashboardVisualStyle`; `data class DashboardPalette(val background: Color, val surface: Color, val elevated: Color, val text: Color, val muted: Color, val accent: Color, val warning: Color, val error: Color, val grid: Color)`; `@Composable fun DashboardTheme(style: DashboardVisualStyle, content: @Composable () -> Unit)`; `LocalDashboardPalette`; `DisplaySettings.visualStyle` default Modern.

- [ ] Write `DisplayPreferencesTest` instrumentation tests: save Systems, reconstruct preferences, receive Systems with existing Full/mirrored values; invalid stored `style` falls back to Modern; changing style preserves layout/mirror.
- [ ] Write `DashboardThemeTest` with two independent compositions/styles: Modern and Systems have different palettes, while switching one does not change the other's colours. Run `:app:connectedDebugAndroidTest`; expected meaningful missing-style/selection failure before implementation.
- [ ] Implement the types, immutable palette tokens and persisted style with parsing fallback. Keep existing palette as Modern; Systems squared blue/green surfaces and Tron cyan route accents. Do not introduce global mutable selected-theme state.
- [ ] Run `:app:testDebugUnitTest :app:connectedDebugAndroidTest :app:lintDebug`; expect all tests/lint pass. Commit `feat: add persistent dashboard visual styles`.

### Task 2: Theme selection and all-screen presentation

**Consumes:** Task 1 theme/preference interfaces. **Produces:** `@Composable fun ThemeSelector(selected: DashboardVisualStyle, onSelect: (DashboardVisualStyle) -> Unit)`; theme-aware existing screens/components; changing style updates preferences without touching provider state or selected destination.

- [ ] Add `DashboardVisualStylesTest`: select each style through UI, retain selected Energy screen, paused fixture SOC 63%, error/source labels and existing layout. At 1280×720, 400×720 and 600×960 dp/font 1.3 assert primary values and navigation visible/reachable and no readout overlap. Expected failure before selection/render support exists.
- [ ] Replace `DashboardColors` consumers with local immutable tokens. Add reachable selector in Development and a compact theme action for parked preview; use Systems frames/energy-flow graphic driven only by existing typed signals. Preserve readout units, warning text/icons and touch dimensions.
- [ ] Run full module tests, Android lint and runtime suite. Inspect screenshots for all four screens/all three styles, full/companion/portrait/large text; fix observed layout defects with regression tests. Commit `feat: apply visual styles across dashboard screens`.

### Task 3: Original street-map and Tron navigation preview

**Consumes:** `NavigationProvider.state`, current display settings and palette. **Produces:** `data class DemoStreetMap(val streets: List<List<RoutePoint>>, val blocks: List<List<RoutePoint>>)`; `@Composable fun NavigationPreview(state: NavigationState, style: DashboardVisualStyle, modifier: Modifier = Modifier)`; `@Composable fun TronMapCanvas(map: DemoStreetMap, route: List<RoutePoint>, modifier: Modifier = Modifier)`; labelled own-navigation/Android-Auto-preview selection, persisted as display preference with safe default.

- [ ] Add `NavigationPreviewTest`: fictional street geometry exists separately from route; route with zero points displays unavailable cue; non-finite points are rejected before drawing; next-turn/ETA/distance/sample markers render with simulation label. Switching own navigation vs factory preview changes label/content and never reports a real projection connection. Watch failures on emulator before implementation.
- [ ] Extract inline route drawing from DriveScreen. Implement original irregular street/block geometry, subdued street background, cyan route glow, vehicle/charger markers and instruction card. Map fixtures use local fictional coordinates, never latitude/longitude. Keep Modern and Systems maps restrained, with the same navigation state.
- [ ] Run `:vehicle-core:test :vehicle-mock:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and runtime tests on both configured APIs. Record command/commit/APK hash and inspect style screenshots. Commit `feat: add original Tron navigation preview`.

## Completion

Run a whole-branch review and document remaining environment gates. These tasks deliver visuals only; use the separate assistant-preview plan for conversation tools. Do not describe sample routes or simulated charging markers as live navigation.
