# Legacy Pixel HMI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace flattened Legacy HMI screenshot overlays with a coherent pixel display using the supplied car art and live mock telemetry.

**Architecture:** A single logical 1536×1152 pixel canvas draws panel frames, glyphs, bars, arrows, and values. The supplied top and side car art is reused as static imagery; changing values never come from baked-in screenshot samples. A small presentation model derives honest display states from `DashboardUiState` before drawing.

**Tech Stack:** Kotlin, Jetpack Compose Canvas, Android bitmap resources, JUnit and Compose instrumentation.

**Spec:** The five supplied 2026-10-05 reference images and the approved in-chat direction: exact cyan/green pixel style; top and side BYD Seal outline; power, charge/discharge, tyre pages; missing values show dashes; no fictitious live data.

## Global Constraints

- Remain on `feature/mock-dashboard-foundation`; do not merge to main.
- Keep `SIMULATED` visible and never imply physical BYD telemetry exists.
- Preserve current Chat, Development, Drive, Energy, and Vehicle navigation.
- Keep missing and stale readings unavailable, never zero by default.
- Preserve car art aspect and pixel-cell consistency on landscape and compact screens.

## Review Focus

- Charging and driving power must not display as simultaneously active when charging.
- A missing front motor must not be depicted as powered on this RWD profile.
- Stale tyre readings must not retain a normal status.
- Nil charging time remaining must show an unavailable marker.
- Long or unavailable readouts must remain inside their panels.

---

### Task 1: Presentation model

**Files:** Create `app/src/main/kotlin/io/github/jhaago/sealdashboard/ui/legacy/LegacyPixelModel.kt`; test `app/src/test/kotlin/io/github/jhaago/sealdashboard/ui/legacy/LegacyPixelModelTest.kt`.

**Interface:** `fun legacyPixelSnapshot(ui: DashboardUiState): LegacyPixelSnapshot` supplies formatted SOC, range, voltage, rear motor, discharge, regen, charge, battery temperature, tyre pressure and temperature maps, plus booleans for active charge/discharge/regen and available tyre data.

- [ ] Write tests for RWD, charging, stale signals, and unavailable time remaining.
- [ ] Run unit tests and observe model-not-found failure.
- [ ] Implement the snapshot using `TelemetryFormatter` quality checks and mutually exclusive flow states.
- [ ] Run unit tests to green.

### Task 2: Reusable pixel screen and four reference pages

**Files:** Create `app/src/main/kotlin/io/github/jhaago/sealdashboard/ui/legacy/LegacyPixelScreens.kt`; add top and side art resources; modify `LegacyHmiScreens.kt` and `DashboardShell.kt`; update `DashboardVisualStylesTest.kt`.

**Interface:** `LegacyPixelScreens(ui, page)` renders `LegacyPixelPage.POWER`, `CHARGING`, `TYRES`, or `PROFILE`; `LegacyPixelNavigation` selects those pages and preserves access to cluster, Chat, Dev, and style selection.

- [ ] Write runtime tests for page reachability and signal text changes.
- [ ] Run them against the previous implementation to confirm failure.
- [ ] Draw logical pixel frames and live values, with the supplied car artwork as a separate static layer.
- [ ] Run unit and Android tests, inspect screenshots at 1280×720 and compact size.
- [ ] Build APK, review visible failures, and commit the feature branch.
