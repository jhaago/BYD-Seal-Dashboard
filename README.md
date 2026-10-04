# BYD Seal Dashboard

Custom Android centre-screen dashboard for an Australian 2024 BYD Seal Dynamic (single-motor RWD).

Development branch: [feature/mock-dashboard-foundation](https://github.com/jhaago/BYD-Seal-Dashboard/tree/feature/mock-dashboard-foundation). Main remains the initial introduction, not a release.

Implemented on the development branch: three-module Kotlin/Compose foundation; read-only changing mock telemetry; Drive, Vehicle, Energy, Development and Assistant screens; selectable Modern, Systems and Tron visuals; original fictional navigation geometry; compact companion layouts; and an offline scripted trip-assistant preview with reviewable sample charging stops.

Verified at `00453a3`: 67 unit tests, Android lint/APK assembly, 29 API 29 runtime tests and two independent process-restart phases. Native captures of all themes, the compact map and parked/driving Assistant layouts were inspected. Independent reviews found and verified fixes for compact map collapse, corrupt saved-style handling and stale charger-card selection. Minimum Android API is 24. API 37 runtime validation remains blocked by a guest graphics crash before APK installation.

**All telemetry, route, charger, Assistant and media content is simulated. Do not rely on this build for real driving speed, range, charger availability or navigation.** Speech and live AI are unavailable in this preview. Real BYD integration, factory Android Auto split control, the instrument cluster and camera/sentry features are not implemented. No real vehicle commands or firmware changes are included.

- [Design and architecture](docs/superpowers/specs/2026-10-03-mock-dashboard-design.md)
- [Implementation plan](docs/superpowers/plans/2026-10-03-mock-dashboard-apk.md)
- [Verification checkpoint](docs/testing/mock-apk-verification.md)
- [Visual-theme verification](docs/testing/visual-themes-verification.md)
- [Assistant-preview verification](docs/testing/assistant-preview-progress.md)
- [Development/build instructions](docs/development.md)
- [DiLink feasibility research](docs/research/2026-10-03-dilink-feasibility.md)

Implemented extension records:

- [Assistant and visual-theme design](docs/superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md)
- [Modern/Systems/Tron theme plan](docs/superpowers/plans/2026-10-03-dashboard-visual-themes.md)
- [Scripted trip-assistant preview plan](docs/superpowers/plans/2026-10-03-trip-assistant-preview.md)
