# Mock APK verification checkpoint

Status: API 29 evaluation checkpoint; original full milestone is incomplete until API 37 runtime validation succeeds. No car test or merge to main.

## Recovery and current implementation

A fresh dedicated clone recovered the saved feature branch without altering another checkout. All four Compose screens, deterministic simulator, foreground-owned provider and mock Development controls exist. Current APK content remains simulated; assistant/visual-theme additions are proposed documents only.

Seven corrected API29 screenshots from run 37125960577 were retrieved and visually inspected: companion/full Drive, Vehicle, Energy, Development, portrait/font 1.3 and compact/font 1.3. Primary values and navigation are readable. Lower Vehicle/Energy/Development sections scroll; runtime tests verify access to the lowest relevant content rather than calling offscreen content clipped.

## Verified API29 lifecycle checkpoint

At `5f8f5b1`, [run 37130411312](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37130411312) passed 50 module unit tests, Android lint/APK assembly, 13 API29 runtime tests, and both separate process-restart phases (1 test in each independent instrumentation process).

The real-Activity suite covers recreation preserving the application-owned provider and selected screen, repeated navigation/fault presentation, foreground/background behaviour and scroll reachability. Separate process tests seed 37% mock SOC plus Full/mirrored preferences, force-stop the target, and verify a different PID restores only display preferences: fresh telemetry at 80% SOC/0 km/h/Park, empty trip/history and unpaused simulation.

An initial test-only run at `07e82c9` exposed prohibited background-context relaunch and a reset assertion racing StateFlow presentation. These harness defects were corrected with external user-launch simulation, isolated fixture reset and a state-based wait. No production behaviour change was inferred from those failures.

## Independent whole-branch review and fix pass

A separate read-only reviewer inspected the foundation spec/plan and code from `2f487db` through `5f8f5b1`; no critical finding. Two Important findings were addressed in one fix pass:

1. Moving at empty SOC could not regenerate because mechanical and pack power were both forced to zero. Regression `movingAtEmptySocCanRecoverKineticEnergyWhenBraking` failed at the recovery assertion in [run 37130702351](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37130702351), with 25/26 mock tests passing. The minimal correction permits negative mechanical/pack power while retaining positive traction/energy limits.
2. The original runtime catch-up bound was too loose at the initial low speed. The revised test reaches at least 59 km/h before backgrounding, bounds resumed distance by measured foreground wall time plus two ticker steps, and checks that adding 1.5 seconds of hidden motion would be rejected by the observed run's bound. The tightened runtime test passed on `e327d75`; final verification also includes that bound check.

The production-fix checkpoint is `59eb681`; its build/runtime results are recorded below. No second reviewer cycle is claimed; the fix pass is verified by regression and full-suite results.

## Final code verification

At `59eb681`, [run 37131078425](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37131078425) passed all 51 module unit tests, Android lint/APK assembly, 13 API29 runtime tests with zero errors/skips, and both process-restart phases (one test each). Debug APK SHA-256: `f9c7cdfbc395ec4c7b6171ad051cbcc7e1c500e9265a94001ef43684e0a49066`. Minimum SDK 24, target 37; no network, vehicle, audio, overlay or camera permissions.

The test-only rotation run at `6231443`, [run 37131500874](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37131500874), completed 14 tests with one failure in the new screenshot-save helper after entering portrait. Its other 13 tests and both process phases passed. `UiAutomation.executeShellCommand` runs a command directly; the chained capture expression did not produce the expected output. The helper now uses `UiAutomation.takeScreenshot()` and the existing MediaStore pattern to retain public test images without storage permissions.

At `3e3b1d1`, [run 37131936898](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37131936898) passed 51 unit tests, lint/build, 14 API29 runtime tests and both process phases. Visual inspection of its native captures exposed a remaining harness gap: configuration changed before the surface relayout completed, so screenshots showed the previous orientation. Those images are not accepted as settled-rotation layout evidence.

Final code checkpoint `16c4b4d`, [run 37132448702](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37132448702), passed all 51 unit tests, lint/APK assembly, 14 API29 runtime tests with zero errors/skips, and both independent process phases. Debug APK SHA-256: `9d1dc7d340d15e10c7f05b24a8d83cc8e5ac5cd690065a78b8810cea6642f600`. API37 still failed emulator startup before app testing; the overall workflow is therefore not green.

The final rotation test waits for rendered dashboard bounds to match portrait/landscape, checks the Full-layout UI after both rotations and captures the native Compose root at device size, checking bitmap aspect ratio. No fixed-size fixture is injected. Both settled captures were visually inspected: 1080×1920 portrait and 1920×1080 landscape, Full mode with mirrored panes. Source/status, primary telemetry area, route pane and bottom navigation are readable without overlap. The paused snapshot aged to STALE in the later landscape capture; unknown placeholders and the stale label correctly replace old values, while the raw provider snapshot is retained. This incidental screenshot does not replace an explicit clock-aging UI assertion.

## API37 environment blocker

Emulator 37.2.12 with the API37 Google APIs x86_64 image repeatedly aborts SurfaceFlinger in `RegionSampling`, at `GoldfishMapper::readFromHost`, with `Assertion failed: !rcEnc->featureInfo()->hasReadColorBufferDma`. This happens before our APK is installed. A renderer change, disabling GLDirectMem and disabling HasSharedSlotsHostMemoryAllocator did not resolve the repeated assertion. Logs confirmed the feature overrides, but the guest still observed the DMA capability.

The host render-control source gates DMA advertising with both direct-memory and shared-slot capabilities; that explained the probes but is not proof the emulator forwards its overrides to the renderer. Further flag guessing stopped. Ineffective overrides were removed, and boot readiness now checks known guest crash evidence plus package/window services instead of treating `boot_completed` alone as a healthy emulator. API29 success is not substituted for API37 acceptance.

References: [gfxstream host render-control source](https://github.com/google/gfxstream/blob/main/host/render_control.cpp), [guest extension negotiation](https://github.com/google/gfxstream/blob/main/guest/renderControl_enc/ExtendedRenderControl.cpp). The exact build/guest incompatibility remains unresolved.

## Deferred minor findings

- Reverse target-speed mode caps speed at 15 km/h; pedal mode currently uses the global ceiling. This is a mock physics limitation, not a vehicle command.
- The Compose stale-data test injects STALE rather than aging a fresh unchanged snapshot. Clock-based silent aging is covered by the ViewModel unit test; the additional Compose path remains untested.

## Scope decisions and remaining gates

- Keep the existing feature branch and native execution preference; no merge/force update. Dedicated clone and authorized GitHub fast-forward writes preserve the user's repository workflow. Competing writers would still require reconciliation.
- Keep API37 incomplete while its system image cannot sustain core services. Cost: newer-Android app failures may remain undiscovered until this gate runs.
- Root inspection covers seven supplied fixtures, two settled native rotation captures and runtime scroll access. It is not independent visual review of every native-size/rotation combination; further layout defects may remain outside fixtures.
- Real telemetry, factory Android Auto control, cluster and camera/sentry remain outside this milestone. Vehicle compatibility must be proved later.
- New themes and assistant interaction are proposed offline preview slices. Live AI/speech/maps/chargers are separate integration decisions; if Jordan wants live services immediately, revise that sequencing before implementing.

Remaining original gate: API37 install/launch/runtime suite. Actual DiLink window/projection behaviour and car compatibility remain later hardware-integration work. The original mock milestone cannot be called complete while the API37 acceptance gate is blocked.

All telemetry/routes/media are simulated. No production vehicle, microphone, overlay, camera or network permissions are added. See [development and evaluation instructions](../development.md), [proposed assistant/theme design](../superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md), and its two implementation plans.
