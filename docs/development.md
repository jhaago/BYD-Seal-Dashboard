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
