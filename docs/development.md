# Development and verification

Implementation uses `feature/mock-dashboard-foundation`; main is not a release branch yet. The first build step is toolchain verification, not the completed dashboard.

## Build environment

This workspace has Java 17 but no available Android SDK/emulator. Its direct Google SDK download probe timed out. The repository's normal GitHub Actions runner is the proposed build/test environment; no network restrictions are bypassed and no extra account credentials are extracted.

The initial workflow checks JDK 17 and Gradle 9.6.0, generates the official Gradle wrapper, verifies its published SHA-256, and installs Android platform API 37/build-tools 36.0.0. The Gradle distribution checksum is pinned as well. Action implementations are pinned by commit, workflow permissions are read-only, and checkout does not persist credentials.

Toolchain verification passed in [run 37117908912](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37117908912). The SDK package ID is `platforms;android-37.0`, while the application uses API level 37. A compile/lint check of the launchable Compose shell also passed in [run 37118429238](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37118429238); that run's domain tests failed as expected because their production interfaces did not yet exist.

The shell APK has minSdk 24 and targetSdk 37. Its merged permissions contain only the AndroidX application-scoped `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, not vehicle or network access. The shell is not the mock dashboard milestone: it has no simulator or screen navigation yet, and has not been run on an emulator or car.

## Building

Use JDK 17 and an Android SDK with `platforms;android-37.0`, `build-tools;36.0.0`, and platform-tools. Set `ANDROID_HOME` or an untracked `local.properties` pointing to the SDK. Gradle and all plugin/library versions are pinned in the repository.

```sh
./gradlew --version
./gradlew :vehicle-core:test :vehicle-mock:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On Windows, use `gradlew.bat`. Build output is `app/build/outputs/apk/debug/app-debug.apk`. CI retains build/lint/test reports and APK output separately from its toolchain bootstrap artifact. Test failures do not prevent the independent compatibility check from reporting its result.

The first shell required removal of an API 27-only style attribute from common resources; minimum OS was not raised to hide the lint error. All runtime claims remain pending API 29/API 37 emulator evidence. A successful build alone does not establish app usability or car compatibility.

References: [Gradle 9.6.0 published checksums](https://github.com/gradle/gradle-distributions/releases/tag/v9.6.0), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [AGP built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).

No vehicle permissions, actuator commands, proprietary assets, platform keys or firmware changes are part of this workflow.

## Simulator assumptions (not vehicle specifications)

The deterministic engine uses an illustrative 60 kWh pack, 2,000 kg mass, 140 kW rear mechanical-power limit, 45 kW recovered-power limit, 7 kW stored charging power and 0.15 kWh/km range estimate. These numbers are simulator settings, not verified Australian Seal Dynamic specifications or calibrated performance predictions.

The documented reset baseline is 80% SOC, 0 km/h, Park, Normal mode, four 250 kPa tyres at 24°C, closed doors/boot, climate off and no trip/history. Demo mode repeats a 93-second parked/accelerating/cruising/regen/stopped-charging journey. Manual commands cancel demo control; pause freezes physics. Climate and tyre temperature evolution are deliberately illustrative.

Physics integrates in steps no larger than 50 ms; explicit outer steps must be finite and within 0–60 seconds. The app provider will use 100 ms updates and must never feed background elapsed time into the model. Power and SOC histories sample at 1 Hz, bounded to 60 and 900 points respectively. History's shared read-only value type belongs to core so a later real provider can supply graph data without exposing mock commands.

Plug-in charging forces a stopped/Park simulation and never counts as trip regeneration. Current/power are signed positive discharge, negative stored charging/recovery. The engine is pure Kotlin, has an injected monotonic clock and contains no Android APIs or real vehicle commands. Runtime provider wiring and dashboard screens are still separate remaining tasks.

## Next work session: real vehicle data + factory camera probe

The next integration milestone is no longer just generic telemetry research. Build a diagnostic-only BYD probe that tests the actual Australian Seal for real vehicle signals and factory camera access before wiring either into the production dashboard.

Use `docs/superpowers/specs/2026-10-05-vehicle-data-camera-probe-design.md` and `docs/superpowers/plans/2026-10-05-vehicle-data-camera-probe.md` as the handoff. The work should probe speed, gear, SOC/range, HV electrical data, tyres, drive/charging/body/climate signals and Android-origin sensors, while separately discovering Camera2 and BYD/DiLink panoramic camera paths. Existing DiLink projects make factory camera access a credible planned capability, including front/rear/left/right/all-camera and sentry-style use, but nothing is considered confirmed on this exact car until an exported in-car diagnostic report proves it.

Keep this milestone read-only. Do not silently replace blocked real values with mock data, do not add vehicle commands, and do not implement sentry/remote camera/automatic camera overlays until basic telemetry and camera acquisition are verified and the factory reverse/AVM camera is shown to coexist safely.
