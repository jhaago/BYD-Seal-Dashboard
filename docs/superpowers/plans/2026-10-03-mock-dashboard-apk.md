# Mock Dashboard APK Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver an installable, emulator-verified Android dashboard with coherent simulated Seal Dynamic telemetry and four usable screens.

**Architecture:** Three modules: Android/Compose `:app`, pure Kotlin contracts `:vehicle-core`, and pure Kotlin simulator `:vehicle-mock`. One application-owned provider session supplies immutable reactive state; development commands exist only in the mock module. Projection/navigation/media demos remain separate from vehicle telemetry.

**Tech Stack:** Kotlin, Jetpack Compose, coroutines/StateFlow, JUnit, Compose instrumentation tests, JDK 17. Initial toolchain candidate: AGP 9.4.0, Gradle 9.6.0, Kotlin/Compose compiler 2.4.10, Compose BOM 2026.09.00, compile/target API 37, minSdk 24. Resolve and verify these together in Task 1 before feature development; pin other library versions in the catalog, never use dynamic versions.

**Spec:** `docs/superpowers/specs/2026-10-03-mock-dashboard-design.md`, approved in conversation; execution was approved in conversation; resume the recovery checkpoint below.

## Recovery checkpoint — 3 October 2026

Tasks 1–3 and the Task 4 UI exist on the feature branch; do not recreate them from unchecked historical step boxes. Seven screenshot fixtures have now been inspected. At `5f8f5b1`, run 37130411312 passed module tests, lint/build, 13 API 29 runtime tests and both separate-process restart phases. The review tightened the no-catch-up runtime assertion and identified empty-SOC recovery as a boundary regression; its red/green evidence and final HEAD belong in `docs/testing/mock-apk-verification.md`. API 37 remains an emulator environment blocker, not a completed gate. Task 5 is incomplete until that runtime requirement is met.

Next-scope designs live in `../specs/2026-10-03-assistant-and-visual-themes-design.md`; proposed plans are `2026-10-03-dashboard-visual-themes.md` and `2026-10-03-trip-assistant-preview.md`. They describe offline visual/conversation previews; no new subsystem is implemented by these documents.

## Global Constraints

- Australian 2024 BYD Seal Dynamic, single-motor RWD; no front-motor display.
- `:vehicle-core` has no Android or BYD imports; `:vehicle-mock` depends only on core and JVM libraries.
- `VehicleDataProvider` is read-only; `MockSimulationController` is a separate mock-only command surface.
- Propose minSdk 24 (Android 7), subject to checking dependency manifests, and test Android 10/API 29 as a likely DiLink-era environment.
- Main touch targets should be at least 56 dp. Labels should generally be 16–18 sp or larger; speed should dominate at roughly 88–112 sp on a wide display.
- Target roughly 10 updates/second for speed/power and 1 history sample/second. Keep 60 seconds of power and up to 15 minutes of SOC history.
- Canonical battery current and pack power are positive for discharge, negative for charging/regen. `packPowerKw = voltageV * currentA / 1000`.
- Clamp SOC to 0–100; charging requires zero speed and Park. Plug-in charging does not count as trip regen.
- Unknown gear is not Park, and missing SOC is not zero. Never automatically substitute mock data into a real session.
- Milestone 1 requests no BYD permissions, writes no vehicle commands, uses no ADB daemon, platform key, CAN transport, firmware modification, overlays or cluster APIs.
- Android Auto preview is explicitly simulated; default preview has left 2/3 projection and right 1/3 dashboard. Real projection hosting is not part of this APK.
- No account/key/network dependency is needed to run the first build. No analytics or outbound telemetry.
- Feature branch only, logical verified commits, no automatic merge to main. Vehicle hardware compatibility remains unverified.

## Review Focus

1. NaN/infinite/out-of-range developer inputs must not poison telemetry or charts: Task 2 validation tests.
2. A stale sample with no subsequent emission must become visibly stale: Tasks 1 and 3 clock-based tests, Task 4 UI test.
3. Background/resume, repeated start, and rotation must not duplicate simulation or integrate hidden elapsed time: Tasks 3 and 5 lifecycle tests.
4. Charging at SOC boundaries and braking at 100% must preserve energy/interlock semantics: Task 2 boundary tests.
5. Compact windows and enlarged text must keep primary telemetry and navigation usable: Tasks 4 and 5 layout tests/screenshots.

---

## File and responsibility map

Use root package `io.github.jhaago.sealdashboard`. Paths below abbreviate the package prefix as `P`; expand it to `io/github/jhaago/sealdashboard` when creating files.

| Area | Files under package prefix | Responsibility |
|---|---|---|
| Core | `core/VehicleProfile.kt`, `core/VehicleState.kt` | Variant/topology; typed motion, battery, powertrain, wheel, opening, climate, charging, environment, trip groups |
| Core | `core/Signal.kt`, `core/MonotonicClock.kt` | Nullable typed values, source/quality/time and freshness evaluation |
| Core | `core/VehicleDataProvider.kt`, `core/ProviderStatus.kt`, `core/EnergyMetrics.kt` | Read-only flows, capabilities/diagnostics; explicit pack/trip calculations |
| Mock | `mock/SimulationConfig.kt`, `mock/SimulationEngine.kt`, `mock/DemoScenario.kt` | Illustrative parameters, deterministic physics, repeatable scenario |
| Mock | `mock/MockSimulationController.kt`, `mock/MockVehicleDataProvider.kt`, `mock/TelemetryHistory.kt` | Validated commands, one coroutine ticker, bounded history |
| App | `DashboardApplication.kt`, `AppContainer.kt`, `MainActivity.kt`, `session/SimulationSession.kt` | Composition root and foreground-only session ownership |
| App | `ui/DashboardViewModel.kt`, `ui/DashboardUiState.kt`, `ui/DashboardShell.kt`, `ui/TelemetryFormatter.kt` | State-to-presentation mapping, selection/navigation, value/fault formatting |
| App | `ui/theme/DashboardTheme.kt`, `ui/components/TelemetryReadout.kt`, `ui/components/PowerBar.kt`, `ui/components/HistoryChart.kt` | Original visual tokens and reusable glanceable components |
| App | `ui/drive/DriveScreen.kt`, `ui/drive/CompanionPane.kt` | Full Drive and compact companion layouts |
| App | `ui/vehicle/VehicleScreen.kt`, `ui/vehicle/VehicleGraphic.kt`, `ui/energy/EnergyScreen.kt` | Original sedan status rendering and technical energy view |
| App | `ui/development/DevelopmentScreen.kt`, `ui/development/RawSignals.kt`, `preferences/DisplayPreferences.kt` | Mock controls/diagnostics; display preferences only |
| Demos | `demo/NavigationProvider.kt`, `demo/MediaProvider.kt`, `demo/ProjectionProvider.kt` | Typed separate demo state; no actual projection or playback commands |

Nested groups may be extracted from `VehicleState.kt` if it becomes difficult to review; do not create a module per screen. Each task's test files mirror production package paths.

## Task 1: Reproducible shell and read-only domain

**Files:** Create `settings.gradle.kts`, root/module `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, official wrapper files, `.gitignore`, `app/src/main/AndroidManifest.xml`, app resources and `MainActivity.kt`; all core files above; `vehicle-core/src/test/kotlin/P/core/SignalTest.kt` and `EnergyMetricsTest.kt`. Modify `README.md`. Add `.github/workflows/android.yml` if a permitted GitHub-hosted build is needed.

**Interfaces produced:**

```kotlin
fun interface MonotonicClock { fun nowMillis(): Long }
data class Signal<T>(val value: T?, val observedAtMillis: Long,
    val source: SignalSource, val quality: SignalQuality)
fun <T> Signal<T>.effectiveQuality(nowMillis: Long, timeoutMillis: Long): SignalQuality
interface VehicleDataProvider {
    val state: StateFlow<VehicleState>
    val status: StateFlow<ProviderStatus>
    val capabilities: StateFlow<Map<SignalKey, SignalCapability>>
    val diagnostics: StateFlow<List<DiagnosticEvent>>
    fun start()
    fun stop()
}
fun packPowerKw(voltageV: Double, currentA: Double): Double
fun efficiencyKwhPer100Km(netEnergyKwh: Double, distanceKm: Double): Double?
```

`VehicleState` contains `profile`, `motion`, `battery`, `powertrain`, `wheels`, `openings`, `climate`, `charging`, `environment`, `trip`, `sampledAtMillis`; readings are `Signal<T>`. Define enum-backed gear/mode/wheel/door keys and one rear driven axle. Unsupported capability is distinct from missing reading. `ProviderStatus` distinguishes idle/running/paused/disconnected/error.

- [ ] Establish permitted build execution before implementation. This workspace has JDK 17 but no discovered SDK/emulator; the Google SDK repository probe timed out on 3 October. Use an already configured/local Android environment or normal GitHub Actions if available within repository permissions; stop and report if access/approval blocks either. Do not bypass network restrictions. Verify package versions against official release notes, including AGP 9 built-in Kotlin configuration, and fetch/generate the official wrapper with distribution SHA-256.
- [ ] Create the three-module build plus a launchable minimal Compose shell displaying `SIMULATED`. Run `./gradlew --version` and `./gradlew :app:assembleDebug`; require Gradle/JVM versions reported and `BUILD SUCCESSFUL`. Resolve toolchain mismatches here and record any justified version change.
- [ ] Write domain tests: `packPowerKw(400.0, 50.0) == 20.0`; `packPowerKw(400.0, -25.0) == -10.0`; efficiency at zero or <0.1 km is null; at 10 km and 1.5 kWh it equals 15.0. A signal observed at 1000 is fresh at 2999 and stale at 3001 with timeout 2000; null/unavailable and error retain their distinct status.
- [ ] Run `./gradlew :vehicle-core:test`; require failure caused by absent domain implementation, not dependency/setup failure.
- [ ] Implement the listed core interfaces/model and pure calculations. No actuator contract or Android dependency. Reject invalid calculation inputs instead of emitting non-finite values; document the 0.1 km efficiency threshold.
- [ ] Run `./gradlew :vehicle-core:test :app:assembleDebug :app:lintDebug`; require all pass. Inspect merged manifest/APK: package `io.github.jhaago.sealdashboard`, min 24/target 37, normal launcher entry, resizable, no forced orientation, no BYD/actuator/network permissions. Confirm no dependency raised minSdk.
- [ ] Commit task-owned files: `build: establish Android shell and read-only vehicle model`. Record exact toolchain pins and build location in README; do not describe the shell as the completed milestone.

## Task 2: Deterministic vehicle simulation

**Files:** Create mock config/engine/scenario/history/controller files in `vehicle-mock/src/main/kotlin/P/mock/`; tests `SimulationEngineTest.kt`, `DemoScenarioTest.kt`, `TelemetryHistoryTest.kt` in the matching test package.

**Consumes:** Task 1 model/clock/metrics. **Produces:**

```kotlin
class SimulationEngine(config: SimulationConfig, clock: MonotonicClock) {
    val state: VehicleState
    fun step(elapsedSeconds: Double): VehicleState
    fun apply(command: MockCommand): CommandResult
    fun reset(): VehicleState
}
fun integrateSocPercent(socPercent: Double, capacityKwh: Double,
    packPowerKw: Double, elapsedSeconds: Double): Double
interface MockSimulationController {
    val simulationStatus: StateFlow<SimulationStatus>
    val history: StateFlow<TelemetryHistory>
    fun dispatch(command: MockCommand): CommandResult
}
```

Define sealed mock commands for demo/manual, pause, reset, target speed, throttle/brake, SOC, gear, drive mode, per-wheel pressure, per-door/boot, charging, climate, fault injection. `CommandResult` is accepted or rejected with a reason. Config is an immutable value; replace-config command validates it. UI edits assumptions only in paused/stationary simulation.

- [ ] Write engine tests with an injected fake clock: same commands and elapsed time produce equal snapshots; acceleration raises speed/positive rear output; braking lowers speed/produces nonnegative regen and negative pack power when recovery exceeds auxiliaries. Ten 0.1 s steps and one 1 s interval agree within a documented integration tolerance using bounded physics substeps.
- [ ] Add boundary/input tests: charging forces zero speed/P and raises SOC; SOC stays within 0–100; empty battery blocks positive traction; full battery has no charge/regen acceptance; Park stops motion; moving door edits reject; charging changes no trip recovered energy. NaN/infinity, negative dt, negative pressure and SOC outside 0–100 reject without changing state. Zero dt leaves state unchanged.
- [ ] Add exact accounting tests: 10 kW discharge for 360 s uses 1 kWh and drops a configured 50 kWh pack by 2 percentage points; 5 kW charging for 360 s adds 0.5 kWh and 1 point, without trip recovery. These test a pure accounting helper used by the engine, not a forced constant physical driving trace. At every engine sample assert pack power matches V×A/1000 within 1e-6.
- [ ] Run `./gradlew :vehicle-mock:test`; require meaningful red tests before implementing.
- [ ] Implement `SimulationConfig`, engine, `integrateSocPercent` in `SimulationEngine.kt`, command validation and `DemoScenario`: parked → accelerate → cruise → regen → stopped/P → charging → repeat. Use SI internally, integrate climate auxiliary load, bounded target-speed ramps and modeled losses; label all configuration assumptions illustrative, not calibrated BYD specs. Scenario cannot overwrite manual commands after switching mode.
- [ ] Implement `TelemetryHistory` with 1 Hz samples, maximum 60 power and 900 SOC points. Add tests stepping beyond 20 minutes asserting those caps, monotonic times, reset empties buffers and pause inserts no samples. Do not add a database.
- [ ] Run `./gradlew :vehicle-core:test :vehicle-mock:test`; require all pass and deterministic repeat runs. Commit `feat: add deterministic mock vehicle simulator`.

## Task 3: One reactive provider and lifecycle owner

**Files:** Create `MockVehicleDataProvider.kt`; app application/container/session/ViewModel/UI-state/formatter files above; tests `MockVehicleDataProviderTest.kt`, `SimulationSessionTest.kt`, `TelemetryFormatterTest.kt`; configure Android lifecycle dependencies in catalog/app build.

**Consumes:** Tasks 1–2 interfaces. **Produces:** `MockVehicleDataProvider` implements both provider/controller with injected engine, scope, clock; application container exposes it as separate typed dependencies. `SimulationSession.onForegroundChanged(foreground: Boolean): Unit` is the sole start/stop owner. `DashboardViewModel.uiState: StateFlow<DashboardUiState>` exposes immutable display state and `selectDestination(destination: DashboardDestination): Unit`.

- [ ] Write coroutine virtual-time tests: repeated start creates one ticker, 100 ms changes telemetry once, stop halts physics, resume after 1 hour advances only the next 100 ms. Explicit pause persists across background/resume. Reset restores baseline (80% SOC, 0 km/h, P, normal mode, all openings closed, four 250 kPa tyres) and clears trip/history.
- [ ] Add failure tests: stopping sample emission makes speed stale after 2000 ms even without a new vehicle snapshot; unavailable SOC formats `—` not `0%`; unknown gear formats `—` not P; disconnected samples are immediately stale. Fault diagnostics cap at 100 entries and preserve error text; clearing fault resumes simulated source, never claims measured data.
- [ ] Run `./gradlew :vehicle-mock:test :app:testDebugUnitTest`; require red assertions/missing implementation.
- [ ] Implement a single 100 ms ticker and serialized command handling. Freshness presentation also ticks while foreground so a silent source can age; background stop does not accrue elapsed physics. Retain state in application-owned session across Activity rotation, not per-screen instances. Use ProcessLifecycleOwner for foreground ownership and lifecycle-aware Compose collection later.
- [ ] Implement formatter and ViewModel mapping with clock-based freshness, unit labels, unknown/fault copy and status. Freshness does not imply the simulation is running: visibly display `PAUSED` when paused. No persisted telemetry/trip after process death.
- [ ] Run `./gradlew :vehicle-core:test :vehicle-mock:test :app:testDebugUnitTest :app:lintDebug`; require all pass. Commit `feat: connect reactive telemetry and foreground simulation lifecycle`.

## Task 4: Four-screen automotive UI and simulator controls

**Files:** Create all listed UI/theme/component/demo/preferences files, `app/src/androidTest/kotlin/P/ui/DashboardNavigationTest.kt`, `DashboardLayoutTest.kt`, `DevelopmentControlsTest.kt`; unit tests `demo/DemoProvidersTest.kt`. Update Activity to host `DashboardShell`.

**Consumes:** Provider/ViewModel/controller from Task 3. **Produces:** `@Composable DashboardShell(viewModel: DashboardViewModel, simulation: MockSimulationController): Unit`; each screen receives immutable presentation state, not a mock provider. `DriveLayout` is full/companion, pane mirroring persisted in `DisplayPreferences`; default companion preview uses left 2/3 demo projection/right 1/3 telemetry.

- [ ] Write Compose tests: navigation selects all four destinations; source label always says `SIMULATED`; manual target speed command from Development updates Drive after engine steps; SOC null renders dash; stale speed renders dash plus stale message. Real provider selector is disabled and explains why. Tests use a controllable fake session, not sleeps/random values.
- [ ] Write layout tests at 1280×720 dp, 960×600 dp, portrait 600×960 dp and compact 400×720 dp with font scales 1.0 and 1.3. Assert speed, gear, SOC, range and power semantics exist, navigation is reachable and primary telemetry nodes do not overlap. In a full-width companion preview at normal font scale assert pane widths are 2:1 (rounding tolerance 1 dp). Narrow/large-text layouts may stack instead of preserving that ratio.
- [ ] Run `./gradlew :app:connectedDebugAndroidTest` on a configured emulator; require expected red tests. If no emulator can run, report the blocker and leave this gate unchecked; do not substitute compile success for runtime evidence.
- [ ] Implement theme (near-black/graphite/warm-white/cyan), 56 dp targets, 16–18 sp labels, approximately 96 sp wide speed. Drive speed updates directly, with restrained short transitions elsewhere. Use original Canvas route geometry and explicitly labeled sample media/projection; demo providers have `state: StateFlow<...State>`, no Google assets, keys or network. Unit test demo labels/source and absence of playback/hosting behavior.
- [ ] Implement Drive/CompanionPane with primary telemetry hierarchy, horizontal signed pack-power bar, ambient/climate/trip and full mode toggle. Support a real narrow app window as well as internal preview; do not hardcode a whole-screen aspect ratio or implement hidden split APIs.
- [ ] Implement Vehicle with original top-down sedan renderer receiving typed wheel/opening state, four pressures/optional temperatures, charge/SOC/range. Implement Energy with rear output distinct from pack power, regen/V/A, bounded power/SOC graphs and gross/recovered/net trip figures. Unknown values and empty charts show explicit placeholders, not fabricated zeroes.
- [ ] Implement scrollable Development grouped controls for all Task 2 commands, per-wheel/door, mock climate/charging, config assumptions, bounded diagnostics and raw named values/unit/source/quality. Rejected commands show reason. Preferences store layout/mirror only. Avoid low-speed timers, camera recording, cluster work or real controls in this task.
- [ ] Run `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:lintDebug`; require all pass. Review screenshots in both Drive modes and portrait before committing `feat: build automotive dashboard screens and mock development controls`.

## Task 5: APK verification and evaluation handoff

**Files:** Create `app/src/androidTest/kotlin/P/ui/DashboardLifecycleTest.kt`, `docs/testing/mock-apk-verification.md`, `docs/development.md`; update README and CI artifact configuration if used.

**Consumes:** Complete Tasks 1–4. **Produces:** verified debug APK and reproducible evidence, not a car-compatibility claim.

- [ ] Add runtime regression tests: rotation preserves telemetry and selected screen; repeated four-screen navigation never starts extra ticker; background/foreground resumes without hidden energy/distance jump; pause/reset and error/availability scenarios remain visible after navigation. Process recreation restores display preference but initializes a fresh mock session.
- [ ] Run `./gradlew :vehicle-core:test :vehicle-mock:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`; record command, commit, versions, totals and actual exit status. Run APK manifest inspection and `sha256sum app/build/outputs/apk/debug/app-debug.apk`; record no unintended permissions and minSdk 24. Never mark unexecuted checks passed.
- [ ] Install and launch via `adb install -r app/build/outputs/apk/debug/app-debug.apk` and `adb shell am start -n io.github.jhaago.sealdashboard/.MainActivity` on API 29 and API 37 emulators; run `./gradlew :app:connectedDebugAndroidTest` separately on each and capture logcat crashes/test reports. If either device is unavailable, report milestone incomplete with exact missing runtime gate.
- [ ] Capture and inspect screenshots for full/companion Drive, Vehicle, Energy, Development; 1920×1080 tablet landscape plus narrow and portrait windows, font scale 1.3 and system insets. Verify no overlap/clipping, usable targets, clear units/fault labels and no scenario state reset on rotation. Correct defects and rerun their tests before recording evidence.
- [ ] Document Android Studio/JDK/SDK setup, wrapper build commands, emulator run, sideload instructions, simulator baseline/modes and limitations. Note debug signing may change between independent build machines; do not instruct uninstall/data deletion silently. Explicitly distinguish mock/emulator-tested from car-tested and explain real Android Auto, cluster and camera/sentry work are deferred feasibility investigations.
- [ ] Commit `test: verify mock dashboard APK and document evaluation workflow`, then run fresh verification at committed HEAD and record APK hash. Review whole branch before presenting APK; keep branch unmerged unless Jordan chooses integration.

## Toolchain evidence and execution boundary

Official sources checked 3 October 2026: [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes) specifies Gradle 9.6.0/JDK 17/API 37 support; [Compose BOM guide](https://developer.android.com/develop/ui/compose/bom) demonstrates stable BOM 2026.09.00; [Kotlin releases](https://kotlinlang.org/docs/releases.html) lists 2.4.10; [Android API tool minimums](https://developer.android.com/build/releases/about-agp) distinguishes compile/target from minSdk. Dependency resolution and runtime testing remain to be performed.

Self-review: all design sections map to Tasks 1–5; five review-focus cases have tests in their owning tasks. Real telemetry, factory projection, cluster and camera/sentry integration are intentionally excluded, not silently assumed available. Begin with Task 1 only, verify its build path and interfaces, then progress through dependent tasks; do not generate the entire application before obtaining build evidence.
