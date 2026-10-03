# BYD Seal Dashboard

Custom Android centre-screen dashboard for an Australian 2024 BYD Seal Dynamic (single-motor RWD).

Development branch: [feature/mock-dashboard-foundation](https://github.com/jhaago/BYD-Seal-Dashboard/tree/feature/mock-dashboard-foundation). Main remains the initial introduction, not a release.

Implemented: three-module Kotlin/Compose foundation, read-only vehicle model, deterministic changing mock telemetry, foreground-owned reactive session, Drive/Vehicle/Energy/Development screens, compact companion preview and mock-only development controls.

Verified at `16c4b4d`: 51 unit tests, Android lint/APK assembly, 14 API 29 runtime tests and two independent process-restart phases. Corrected fixtures and settled native portrait/landscape captures were visually inspected; a whole-branch review and its fix pass are recorded. Minimum Android API is 24. The full mock APK milestone remains incomplete: API 37 runtime validation is blocked by a guest graphics crash before APK installation. See the checkpoint for evidence and remaining limitations.

**All telemetry, route and media content is simulated. Do not rely on this build for real driving speed, range or navigation.** Real BYD integration, factory Android Auto split control, the instrument cluster and camera/sentry features are not implemented. No real vehicle commands or firmware changes are included.

- [Design and architecture](docs/superpowers/specs/2026-10-03-mock-dashboard-design.md)
- [Implementation plan](docs/superpowers/plans/2026-10-03-mock-dashboard-apk.md)
- [Verification checkpoint](docs/testing/mock-apk-verification.md)
- [Development/build instructions](docs/development.md)
- [DiLink feasibility research](docs/research/2026-10-03-dilink-feasibility.md)

Proposed next additions (not implemented):

- [Assistant and visual-theme design](docs/superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md)
- [Modern/Systems/Tron theme plan](docs/superpowers/plans/2026-10-03-dashboard-visual-themes.md)
- [Scripted trip-assistant preview plan](docs/superpowers/plans/2026-10-03-trip-assistant-preview.md)
