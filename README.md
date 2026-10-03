# BYD Seal Dashboard

Custom Android centre-screen dashboard for an Australian 2024 BYD Seal Dynamic (single-motor RWD).

Development branch: [feature/mock-dashboard-foundation](https://github.com/jhaago/BYD-Seal-Dashboard/tree/feature/mock-dashboard-foundation). Main remains the initial introduction, not a release.

Implemented: three-module Kotlin/Compose foundation, read-only vehicle model, deterministic changing mock telemetry, foreground-owned reactive session, Drive/Vehicle/Energy/Development screens, compact companion preview and mock-only development controls.

Verified at `1c2fc49`: 50 unit tests, Android lint, APK assembly and 9 API 29 runtime tests. Minimum Android API is 24. This is **not yet the completed APK milestone**: corrected screenshot inspection, lifecycle runtime regressions, API 37 emulator verification and whole-branch review remain outstanding. See the checkpoint below for exact evidence and blockers.

**All telemetry, route and media content is simulated. Do not rely on this build for real driving speed, range or navigation.** Real BYD integration, factory Android Auto split control, the instrument cluster and camera/sentry features are not implemented. No real vehicle commands or firmware changes are included.

- [Design and architecture](docs/superpowers/specs/2026-10-03-mock-dashboard-design.md)
- [Implementation plan](docs/superpowers/plans/2026-10-03-mock-dashboard-apk.md)
- [Verification checkpoint](docs/testing/mock-apk-verification.md)
- [Development/build instructions](docs/development.md)
- [DiLink feasibility research](docs/research/2026-10-03-dilink-feasibility.md)
