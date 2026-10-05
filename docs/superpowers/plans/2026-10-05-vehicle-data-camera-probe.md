# Vehicle Data + Factory Camera Probe Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a diagnostic-only BYD integration path that can prove, on the user's actual Australian 2024 Seal Dynamic, which real telemetry signals and factory camera channels are accessible.

**Architecture:** Preserve the pure `vehicle-core` contracts and existing `vehicle-mock` provider. Add Android-specific `vehicle-byd` integration behind the same read-only provider boundary, plus a separate camera-probe interface. The app explicitly selects mock or BYD probe mode; missing/blocked real data stays missing and visible. Camera access is discovered incrementally and any shell-helper experiment is separately gated and used only when the owned vehicle exposes an authorized debugging/shell path.

**Tech Stack:** Kotlin, Android SDK, Jetpack Compose, coroutines/StateFlow, Android Camera2/service APIs, reflection only where needed for optional DiLink classes, JUnit/Android instrumentation tests. Existing JDK/Gradle/AGP versions remain authoritative unless the branch changes them for an independently justified reason.

**Spec:** `docs/superpowers/specs/2026-10-05-vehicle-data-camera-probe-design.md`.

## Global Constraints

- Read-only milestone: no vehicle actuator commands, CAN writes, rooting, firmware modification, signature spoofing, platform keys or security-control bypasses.
- Never silently substitute mock telemetry while BYD probe mode is active.
- `vehicle-core` remains Android/BYD-free.
- Real telemetry and cameras are unverified on this exact car until an in-car diagnostic report proves them.
- Preserve factory reverse/AVM behaviour; stop camera testing if the probe disrupts the native camera.
- No sentry recording, remote streaming, AI detection, camera automation or production camera overlays in this milestone.
- Channel labels are provisional until visually confirmed on the car.
- Third-party projects are evidence only; do not import proprietary/closed code, keys, signing material or binaries.
- Feature/test branch only. Do not merge to main automatically.

## Review Focus

1. A missing BYD class/service must produce `UNSUPPORTED`/`UNAVAILABLE`, not a crash: Task 1 unit/instrumentation tests.
2. Security/permission failures must be preserved as `PERMISSION_DENIED` with diagnostic detail, never collapsed to a zero/default value: Tasks 1 and 2 tests.
3. Camera channels may be reordered or return no first frame; probe UI must keep them as raw candidates until visual mapping is confirmed: Task 3 tests.
4. BYD provider failure must never fall back to mock telemetry invisibly: Task 2 app-wiring test.
5. Camera open/close and app background/resume must release resources so the factory AVM/reverse camera can recover: Task 3 lifecycle instrumentation test and in-car checklist.

---

## File and responsibility map

| Area | Files | Responsibility |
|---|---|---|
| Core | `vehicle-core/.../core/IntegrationProbe.kt` | Platform-neutral capability/result enums and diagnostic value records used by UI/export |
| BYD module | `vehicle-byd/build.gradle.kts` | Android library depending on `vehicle-core`; no dependency from core/mock back to BYD |
| BYD telemetry | `vehicle-byd/.../BydServiceDiscovery.kt`, `BydSignalReader.kt`, `BydVehicleDataProvider.kt` | Discover optional DiLink surfaces, read/normalize signals, expose provider state/status/capabilities/diagnostics |
| BYD camera | `vehicle-byd/.../camera/BydCameraProbe.kt`, `AndroidCameraProbe.kt`, `BydPanoramaProbe.kt` | Enumerate candidate cameras/services and attempt bounded read-only frame acquisition |
| App wiring | `app/.../AppContainer.kt`, `preferences/IntegrationPreferences.kt` | Explicit mock/BYD-probe mode selection and dependency wiring |
| Probe UI | `app/.../ui/development/VehicleProbeScreen.kt`, `ProbeViewModel.kt`, `DiagnosticReportWriter.kt` | Human-readable telemetry/service/camera diagnostics and local report export |
| Tests | matching unit/instrumentation paths | Absence/denial/failure handling, explicit provider selection, camera lifecycle and report serialization |

Use the existing root package `io.github.jhaago.sealdashboard`. Exact physical file splitting may follow current package conventions, but keep telemetry, camera probing and report/UI responsibilities separate.

## Task 1: Add diagnostic contracts and BYD discovery shell

**Files:**
- Create `vehicle-core/src/main/kotlin/io/github/jhaago/sealdashboard/core/IntegrationProbe.kt`.
- Create `vehicle-byd/build.gradle.kts` and Android manifest.
- Create `vehicle-byd/src/main/kotlin/io/github/jhaago/sealdashboard/byd/BydServiceDiscovery.kt`.
- Add `vehicle-byd` to `settings.gradle.kts`.
- Test in `vehicle-core` and `vehicle-byd` unit/instrumentation test sources.

**Interfaces:**
- Produces `ProbeAvailability`, `ProbeValue<T>`, `ServiceProbeResult`, and a discovery API returning structured results instead of throwing for absent/blocked services.
- Later tasks consume these records for telemetry, cameras and report export.

- [ ] **Step 1: Write failing tests for capability/failure classification**

Assert that missing service/class maps to `UNAVAILABLE`/`UNSUPPORTED`, a `SecurityException` maps to `PERMISSION_DENIED`, and raw diagnostic text is retained without inventing a value.

- [ ] **Step 2: Run the focused tests and verify they fail before production types exist**

Run the relevant `vehicle-core` and `vehicle-byd` test targets. Expected: compile/test failure for missing probe types.

- [ ] **Step 3: Implement the minimal probe result types and BYD service discovery**

Keep discovery read-only. Enumerate Android-visible packages/services/classes needed by the next tasks, using reflection only for optional DiLink classes so ordinary Android/emulator runs remain supported.

- [ ] **Step 4: Add instrumentation coverage on a non-BYD Android environment**

Verify the module initializes and reports absent BYD services without crashing or requesting vehicle-control permissions.

- [ ] **Step 5: Run unit tests, instrumentation where available, lint and APK assembly**

Expected: all existing mock-dashboard tests still pass; BYD absence is a normal diagnostic state.

- [ ] **Step 6: Commit**

Suggested message: `feat: add read-only BYD integration discovery`.

## Task 2: Implement real telemetry probe provider and explicit app selection

**Files:**
- Create `vehicle-byd/.../BydSignalReader.kt`.
- Create `vehicle-byd/.../BydVehicleDataProvider.kt`.
- Modify `app/.../AppContainer.kt`.
- Create/modify `app/.../preferences/IntegrationPreferences.kt`.
- Add tests mirroring these files.

**Interfaces:**
- `BydVehicleDataProvider` implements the existing `VehicleDataProvider` interface (`state`, `status`, `capabilities`, `diagnostics`, `history`, `start()`, `stop()`).
- `BydSignalReader` reports candidate signal reads with raw value, normalized value/unit, timestamp and `ProbeAvailability`.
- App integration mode is explicit: `MOCK` or `BYD_PROBE`.

- [ ] **Step 1: Write failing tests for signal normalization and permission failures**

Cover speed, gear, SOC/range, voltage/current/power, tyre data, drive mode, charging, ambient temperature and representative body/climate fields using injected fake readers. Assert missing data remains null and permission failures remain diagnostic failures.

- [ ] **Step 2: Run focused tests and confirm RED**

Expected: failure because real provider/reader do not exist.

- [ ] **Step 3: Implement `BydSignalReader` adapters and normalization**

Start only with signals discoverable from the documented/publicly evidenced DiLink surfaces. Preserve raw values and source identifiers. Do not guess enum/unit mappings: mark unknown encodings as unknown until car validation.

- [ ] **Step 4: Implement `BydVehicleDataProvider` using existing core state/capability/diagnostic contracts**

Use idempotent lifecycle semantics matching the mock provider. Real provider failures must update provider status/capabilities rather than terminate the app.

- [ ] **Step 5: Wire explicit provider selection in `AppContainer`**

Default development behaviour may remain mock until the user deliberately selects BYD probe mode. Assert in tests that BYD probe failure does not instantiate/swap in mock data behind the UI.

- [ ] **Step 6: Run all domain/app tests, lint and APK assembly**

Expected: existing mock behaviour unchanged; BYD probe mode builds on ordinary Android even when every BYD signal is unavailable.

- [ ] **Step 7: Commit**

Suggested message: `feat: add BYD telemetry probe provider`.

## Task 3: Add factory camera discovery and bounded live-frame probe

**Files:**
- Create `vehicle-byd/.../camera/BydCameraProbe.kt`.
- Create `vehicle-byd/.../camera/AndroidCameraProbe.kt`.
- Create `vehicle-byd/.../camera/BydPanoramaProbe.kt`.
- Add camera probe tests/instrumentation.
- Do not add a shell daemon in the first commit of this task.

**Interfaces:**
- `BydCameraProbe` exposes a list/flow of `CameraCandidate` records containing raw identifier, provisional role, source path, format/resolution, availability, first-frame state, frame count/FPS and last error.
- `open(candidateId)` and `close()` must be bounded/idempotent and release acquired resources.

- [ ] **Step 1: Write failing tests for candidate mapping and lifecycle**

Assert unknown/reordered channels remain `UNKNOWN_n` until explicitly mapped; no-first-frame timeout is diagnostic rather than success; repeated close is safe.

- [ ] **Step 2: Implement ordinary Android Camera2 enumeration first**

Record camera IDs and characteristics without assuming any correspond to BYD exterior cameras.

- [ ] **Step 3: Implement DiLink/BYD panorama service discovery and app-accessible frame attempt**

Use only interfaces visible to the sideloaded app. Catch and classify service absence, permission denial and first-frame timeout. Do not interfere with native reverse/AVM ownership beyond a bounded test open/close.

- [ ] **Step 4: Add lifecycle instrumentation**

Verify open/close/background/resume releases Android-side resources on a normal test environment. Add an in-car checklist for factory reverse-camera recovery because that property cannot be proven in CI.

- [ ] **Step 5: Record but do not implement the optional shell-helper branch unless the actual car test proves app access is denied and an authorized shell/debugging path is available**

If required later, create a separate follow-up plan for the helper process. Do not bundle security-bypass work into this milestone.

- [ ] **Step 6: Run tests, lint and APK assembly**

- [ ] **Step 7: Commit**

Suggested message: `feat: add BYD factory camera probe`.

## Task 4: Build the in-car probe screen, export report and execute car checklist

**Files:**
- Create `app/.../ui/development/VehicleProbeScreen.kt`.
- Create `app/.../ui/development/ProbeViewModel.kt`.
- Create `app/.../ui/development/DiagnosticReportWriter.kt`.
- Modify the existing Development screen/navigation to expose `Vehicle Probe`.
- Update `docs/testing/` with the actual-car checklist and later the captured results.

**Interfaces:**
- The screen presents telemetry, service and camera probe state without converting failures to friendly fake numbers.
- `DiagnosticReportWriter` serializes app/build/device metadata, provider status, signal probe records, discovered services, camera candidates, errors and timestamps to a local JSON/text file.

- [ ] **Step 1: Write failing ViewModel/report tests**

Assert raw + normalized values, capability/error states and camera candidate metadata survive serialization; mock values are never present in a BYD-probe report unless explicitly marked as a separate mock session.

- [ ] **Step 2: Implement the probe UI and report writer**

Keep the UI diagnostic and glanceable: group Powertrain, Battery/Energy, Tyres, Body, Climate, Android Sensors, Services and Cameras. A camera preview appears only after a real first frame.

- [ ] **Step 3: Add UI/runtime tests for compact/full screen and unavailable services**

The screen must remain usable when every BYD capability is unavailable.

- [ ] **Step 4: Build a fresh installable APK from committed HEAD and record hash/manifest permissions**

Confirm no new actuator/control permissions were introduced unintentionally.

- [ ] **Step 5: Run the actual Seal validation checklist**

On the user's owned car: record firmware/API/display metadata; select BYD probe mode; compare speed/SOC/range/gear/tyres against factory displays; open/close doors and change climate one item at a time; test charging if convenient; enumerate camera candidates; visually map any working feeds; verify first-frame timing; close the probe and confirm factory reverse/AVM still works; test one background/resume cycle. Export the diagnostic report.

- [ ] **Step 6: Update `docs/testing/` with only observed results**

Promote each signal/channel from candidate to confirmed only when captured evidence supports it. Record unavailable/denied signals just as explicitly.

- [ ] **Step 7: Commit**

Suggested message: `test: record Seal telemetry and camera probe results`.

## Follow-up only after successful probe

Do not start these until Task 4 establishes the actual capabilities of this car:

- production real-data provider for the Legacy HMI and modern dashboard;
- front/rear/left/right/360 camera page;
- reverse/indicator/parking-speed camera overlays;
- dashcam recording and retention;
- parked sentry mode;
- phone/remote live viewing and its security model.
