# Development and verification

Implementation uses `feature/mock-dashboard-foundation`; main is not a release branch yet. The first build step is toolchain verification, not the completed dashboard.

## Build environment

This workspace has Java 17 but no available Android SDK/emulator. Its direct Google SDK download probe timed out. The repository's normal GitHub Actions runner is the proposed build/test environment; no network restrictions are bypassed and no extra account credentials are extracted.

The initial workflow checks JDK 17 and Gradle 9.6.0, generates the official Gradle wrapper, verifies its published SHA-256, and installs Android platform API 37/build-tools 36.0.0. The Gradle distribution checksum is pinned as well. Action implementations are pinned by commit, workflow permissions are read-only, and checkout does not persist credentials.

Verification status: workflow prepared, not yet demonstrated successful. It currently produces wrapper files only, not an APK. Later tasks add module tests, Android lint, APK assembly and API 29/API 37 emulator evidence. A successful toolchain job alone does not establish Compose compatibility, app correctness, or car compatibility.

References: [Gradle 9.6.0 published checksums](https://github.com/gradle/gradle-distributions/releases/tag/v9.6.0), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [AGP built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).

No vehicle permissions, actuator commands, proprietary assets, platform keys or firmware changes are part of this workflow.
