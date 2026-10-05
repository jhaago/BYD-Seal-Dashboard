# Vehicle Data + Factory Camera Probe Design

**Status:** Next integration milestone after the mock-dashboard/car-install checkpoint. This document records the intended direction for the next work session; it does not claim that any real vehicle signal or camera channel has yet been verified on the user's Australian 2024 BYD Seal Dynamic.

## Goal

Build a diagnostic-only integration layer and in-car probe screen that establishes, on the actual Seal, which real vehicle telemetry signals and factory camera channels are accessible to a sideloaded app. Preserve the existing mock provider as a safe fallback for development, but never silently substitute mock values when the app is in a real-vehicle probe session.

## Why this is now the priority

Public DiLink projects provide strong feasibility evidence for real telemetry such as speed, gear, SOC, remaining range, high-voltage electrical data, tyre data, drive mode, charging state and ambient temperature. Existing DiLink camera/sentry projects also demonstrate use of BYD panoramic/factory camera feeds on DiLink 3 vehicles, including a global BYD Seal. Some of those implementations use a separate helper/daemon launched with shell-level privileges rather than relying on ordinary Android Camera2 access.

This evidence upgrades factory cameras from a speculative future feature to a planned capability, but the exact access path, channel mapping, permissions, lifecycle and firmware behaviour remain unverified on this specific Australian car.

Reference evidence for the next session:

- Existing project research: `docs/research/2026-10-03-dilink-feasibility.md`.
- OverDrive: https://github.com/yash-srivastava/Overdrive-release — reports DiLink 3/global Seal support, front/rear/left/right/all-four camera views, telemetry, camera mapping and sentry/dashcam work.
- BladeWatch: https://bladewatch.net/bladewatch-byd-dashcam-sentry/ — reports BYD DiLink 3 panoramic recording and parked surveillance.
- Strike: https://github.com/UnrealSalty/Strike — documents a separate `app_process` daemon running as shell UID 2000 for capture/recording/surveillance and a read-only vehicle telemetry layer.

Treat those sources as feasibility evidence only. Do not copy proprietary code, closed components, keys, signing material or undocumented binaries into this project.

## Milestone scope

### Vehicle telemetry probe

Probe and report availability, raw value, normalized value, source, unit/encoding assumptions, refresh rate and errors for at least:

- speed and gear/READY state;
- battery SOC and remaining range;
- HV voltage, HV current and signed pack power if exposed;
- four tyre pressures and temperatures if exposed;
- drive mode;
- charging state and charging power if exposed;
- ambient temperature;
- door, boot and window states;
- climate setpoint/fan/status;
- cabin, battery and motor temperatures only if actually exposed;
- Android GPS speed/location and motion sensors as separate Android-origin sources, never mislabeled as BYD vehicle data.

Every candidate must be classed as `AVAILABLE`, `UNAVAILABLE`, `PERMISSION_DENIED`, `UNSUPPORTED`, `ERROR` or `UNKNOWN`. Missing values remain missing.

### Factory camera probe

Probe in this order:

1. Enumerate normal Android Camera2 devices and their characteristics.
2. Discover known BYD/DiLink camera/panorama services, packages, classes and accessible binder/service endpoints without requiring writes to the car.
3. Attempt read-only/live frame acquisition through an app-accessible path where available.
4. If ordinary app access is denied, record the exact failure and whether a user-authorized shell/debugging path is available on the car.
5. Only in a separately gated experiment, and only if the user's own vehicle exposes a legitimate shell/debugging path, test an isolated helper process for frame acquisition. Do not attempt to bypass SELinux, signature checks, locked debugging or firmware security controls.

The probe should identify possible channels as `FRONT`, `REAR`, `LEFT`, `RIGHT`, `PANORAMA/AVM` or `UNKNOWN_n`, because physical/channel ordering can vary by model and firmware. Channel naming is not considered verified until the user confirms it visually on the car.

### Diagnostic UI

Add a dedicated development/probe screen, not a production driving UI. It should show:

- integration mode and provider status;
- each telemetry signal with raw + normalized value, timestamp/age, units, capability state and last error;
- discovered Android/BYD services and relevant permission results;
- camera candidates with channel identifier, resolution/format, first-frame state, frame counter/FPS and error state;
- an explicit `Export diagnostic report` action that writes a local text/JSON report suitable for attaching to the next work session;
- no automatic mock substitution while real probing is active.

A preview tile may display a live frame only after acquisition succeeds. Do not build sentry recording, remote streaming, AI detection or automatic indicator/reverse overlays until basic channel acquisition is proven on this car.

## Architecture

Keep `vehicle-core` platform-neutral and read-only. Add an Android-specific `vehicle-byd` module depending on `vehicle-core`. The module owns service discovery, reflection/binder adapters, BYD-specific normalization and camera probing. The app chooses between `MockVehicleDataProvider` and a real `BydVehicleDataProvider` explicitly in development settings; real-provider failure must remain visible.

Camera discovery/capture uses a separate `BydCameraProbe` surface so telemetry availability does not depend on camera privileges. Any later shell helper must be a separate process with a narrow read/capture interface and clear lifecycle; the normal UI process should not inherit broad shell responsibilities.

## Safety and non-goals

- Read-only milestone: no vehicle actuator commands, CAN writes, firmware modification, rooting, signature spoofing or platform-key work.
- Do not interfere with the factory reverse/AVM camera. If opening a test stream causes the native camera to lose signal, stop the probe and record the conflict.
- No background/parked recording in this milestone.
- No remote camera access or cloud upload in this milestone.
- No copying third-party proprietary/closed camera libraries.
- All tests happen only on a vehicle/head unit the user owns or is authorized to manage.

## Success criteria for the in-car session

The milestone is successful when a diagnostic report from the actual Seal answers, with evidence:

1. Which telemetry signals are readable, their units/encodings and update rates.
2. Which signals are blocked or absent and the exact failure mode.
3. Whether any factory camera channel can produce frames to the sideloaded app or an explicitly authorized shell helper.
4. How discovered camera channels map to front/rear/left/right/panorama.
5. Whether camera access coexists safely with the factory reverse/AVM function and survives basic stop/start/resume testing.

Only after those answers should production dashboard pages consume real data or the project implement dashcam/sentry/automatic camera overlays.
