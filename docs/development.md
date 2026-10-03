# Development and verification

Implementation uses `feature/mock-dashboard-foundation`; main is not a release branch yet. The changing simulator and four-screen dashboard now exist. Current evidence and remaining gates are in `testing/mock-apk-verification.md`.

## Build environment

This workspace has Java 17 but no available Android SDK/emulator. Its direct Google SDK download probe timed out. The repository's normal GitHub Actions runner is the proposed build/test environment; no network restrictions are bypassed and no extra account credentials are extracted.

The initial workflow checks JDK 17 and Gradle 9.6.0, generates the official Gradle wrapper, verifies its published SHA-256, and installs Android platform API 37/build-tools 36.0.0. The Gradle distribution checksum is pinned as well. Action implementations are pinned by commit, workflow permissions are read-only, and checkout does not persist credentials.

Toolchain verification passed in [run 37117908912](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37117908912). The SDK package ID is `platforms;android-37.0`, while the application uses API level 37. A compile/lint check of the launchable Compose shell also passed in [run 37118429238](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37118429238); that run's domain tests failed as expected because their production interfaces did not yet exist.

The shell APK has minSdk 24 and targetSdk 37. Its merged permissions contain only the AndroidX application-scoped `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, not vehicle or network access. That historic shell result was not the completed milestone. The current four-screen app has Android 10/API 29 launch, interaction and process-restart evidence; API 37 emulator compatibility and vehicle hardware testing remain unverified.

## Building

Use JDK 17 and an Android SDK with `platforms;android-37.0`, `build-tools;36.0.0`, and platform-tools. Set `ANDROID_HOME` or an untracked `local.properties` pointing to the SDK. Gradle and all plugin/library versions are pinned in the repository.

```sh
./gradlew --version
./gradlew :vehicle-core:test :vehicle-mock:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On Windows, use `gradlew.bat`. Build output is `app/build/outputs/apk/debug/app-debug.apk`. CI retains build/lint/test reports and APK output separately from its toolchain bootstrap artifact. Test failures do not prevent the independent compatibility check from reporting its result.

The first shell required removal of an API 27-only style attribute from common resources; minimum OS was not raised to hide the lint error. API 29 runtime evidence is recorded in the checkpoint; API 37 remains blocked by guest graphics-service crashes before APK installation. A successful build alone does not establish app usability or car compatibility.

References: [Gradle 9.6.0 published checksums](https://github.com/gradle/gradle-distributions/releases/tag/v9.6.0), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [AGP built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).

No vehicle permissions, actuator commands, proprietary assets, platform keys or firmware changes are part of this workflow.

## Simulator assumptions (not vehicle specifications)

The deterministic engine uses an illustrative 60 kWh pack, 2,000 kg mass, 140 kW rear mechanical-power limit, 45 kW recovered-power limit, 7 kW stored charging power and 0.15 kWh/km range estimate. These numbers are simulator settings, not verified Australian Seal Dynamic specifications or calibrated performance predictions.

The documented reset baseline is 80% SOC, 0 km/h, Park, Normal mode, four 250 kPa tyres at 24°C, closed doors/boot, climate off and no trip/history. Demo mode repeats a 93-second parked/accelerating/cruising/regen/stopped-charging journey. Manual commands cancel demo control; pause freezes physics. Climate and tyre temperature evolution are deliberately illustrative.

Physics integrates in steps no larger than 50 ms; explicit outer steps must be finite and within 0–60 seconds. The app provider will use 100 ms updates and must never feed background elapsed time into the model. Power and SOC histories sample at 1 Hz, bounded to 60 and 900 points respectively. History's shared read-only value type belongs to core so a later real provider can supply graph data without exposing mock commands.

Plug-in charging forces a stopped/Park simulation and never counts as trip regeneration. Current/power are signed positive discharge, negative stored charging/recovery. The engine is pure Kotlin, has an injected monotonic clock and contains no Android APIs or real vehicle commands. Runtime provider wiring and dashboard screens are implemented. The original final acceptance gate remains incomplete while API 37 cannot run.

## Evaluating the mock APK

Download `android-build-evidence` from a passing build job on the feature branch; its `app/build/outputs/apk/debug/app-debug.apk` is the debug preview. Match the commit/run and recorded SHA-256 in the checkpoint. The workflow can be red overall because a separate emulator job is blocked, so read each job rather than treating the overall result as a release approval.

On a phone/tablet, copy the APK, open it, and grant that file-opening app permission to install unknown apps when Android requests it. Android may reject an update signed by a different CI debug key; keep the existing installation/data until you intentionally choose how to replace it. Do not silently uninstall or clear user data to resolve a signature conflict.

For a connected emulator/development device:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n io.github.jhaago.sealdashboard/.MainActivity
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notAnnotation=io.github.jhaago.sealdashboard.ui.ProcessBoundaryTest
```

The separate-process fixture tests are orchestrated by CI: seed non-baseline mock SOC and visual preferences, force-stop the app, then run the verification phase in a different PID. They are excluded from the ordinary suite because their boundary is the external process restart. Do not run their verification phase without the seed phase.

Centre-screen/car sideload compatibility has not been established. All displayed telemetry/routes/media are simulated. Real Android Auto split control, telemetry, cluster and sentry remain outside this mock build.

## Proposed next preview slices

- [Assistant and visual-theme design](superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md)
- [Visual-theme implementation plan](superpowers/plans/2026-10-03-dashboard-visual-themes.md)
- [Trip-assistant preview implementation plan](superpowers/plans/2026-10-03-trip-assistant-preview.md)

The first assistant slice is explicitly scripted and offline. Live AI, speech and map/charger services require a later integration design; none is connected by the proposed documents.
