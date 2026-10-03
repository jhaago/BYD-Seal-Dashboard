# Visual theme verification

Modern, Systems and Tron are implemented on the feature branch. Systems uses the supplied burgundy / pale-blue / green console reference through original EV mimic graphics. Navigation has original fictional streets, blocks, sample route and charger markers. All sources are explicitly simulated.

Reviewer findings reproduced in run37136818402: two failures among26 runtime tests (zero-height compact map and wrong-type preference startup exception). Fixes at9ec0cabc passed26/26 API29 runtime tests and two separate process-restart phases in run37137265431. The subsequent adapter commitac453e9 passed58/58 module unit tests, lint and APK assembly in run37137669114. Native400dp/font1.3 map screenshot inspected; map retains240dp height, long text wraps and footers scroll. API37 crashes before APK installation and remains an open acceptance gate. No main merge.

## Rulings and deferred findings

# SDD ledger — plan: docs/superpowers/plans/2026-10-03-dashboard-visual-themes.md
Approval: Jordan approved the written design and both plans; native execution retained. Image steering clarifies Systems mimic-panel burgundy/blue/green aesthetic.
Pre-flight: Task1→Task2 consumes immutable palette, style and persisted preferences; names agree.
Pre-flight: Task1→Task3 consumes style/palette and preferences; navigation-source preference extends display settings with safe default.
Pre-flight: Task3→assistant plan consumes local RoutePoint and navigation state; route IDs/waypoints will be added in the assistant slice.
Ruling: Continue in the already dedicated feature-branch clone — this checkout was created solely for recovery and has a clean baseline, no other writer — cost if wrong: reconcile competing repository writes.
Ruling: Reuse freshly verified 16c4b4d baseline (51 units/14 API29/2 process phases) with docs-only HEAD 00e8f86 — no production change since that run, local SDK unavailable — cost if wrong: latent environmental regression caught in CI.
Ruling: New API tests first necessarily report missing new Kotlin types at compile time before implementation — absent theme/persistence APIs are the expected feature failure, not a syntax error — cost if wrong: runtime assertions need separate GREEN verification.

Task1 RED: 16e8af2/run37133398357 unit compilation fails for missing DashboardVisualStyle/parseVisualStyle, expected absent API. Production style implementation now allowed.

Ruling: Run Gradle verification through the existing isolated CI emulators/toolchain, not unavailable local SDK; record actual CI commands/results instead of task-done rerunning them locally — evidence is fresh and avoids redundant remote suites — cost if wrong: CI/local environment differences remain untested.
Task 1: complete (commits 00e8f86..3634fed, tests: full module unit tests/lint/assemble + connectedDebugAndroidTest in run37133701935 → unit/build pass, 18 API29 tests zero failures/skips, both process phases pass; API37 startup blocked).

Task2 RED: 826465a/run37134114668 executes21 API29 tests, new3 fail at absent style-menu and systems-pack-quality. Existing18 tests pass; UI feature absence reproduced.

Task 2: complete (commits3634fed..2983d98, tests full units/lint/build + API29 connected runtime in run37134627531 → 21 tests zero errors/skips and both process phases pass). Inspected21 style fixtures via contact sheet and native Systems Energy/compact; primary values/nav/readable mimic panels, lower Systems content scrolls. API37 startup remains blocked.

Task3 RED: 93d5f0d/run37135312039 unit RoutePointTest.invalidFictionalCoordinatesAreRejectedBeforeRendering fails because NaN/out-of-bounds currently accepted.
Ruling: Navigation source defaults FOLLOW_LAYOUT (Full=own preview, Companion=factory placeholder), explicit Own/Factory choice persists — preserves existing Full behaviour and rotation tests while offering requested independent choice — cost if wrong: users can override but default may surprise.

Task3 UI RED: run37135312039 API29 executes23 tests; NavigationPreviewTest fails for missing Route unavailable and navigation-source-toggle, other21 pass. Renderer/source-control feature absence verified before implementation.

Task 3: complete (commits2983d98..83a11ea, tests full units/lint/build + runtime run37135786824 → unit/build pass, 24 API29 tests zero errors/skips and both process phases pass). Native Tron wide/portrait map captures inspected; source labels and fictional geometry visible. API37 startup blocked.

Final review (fresh reviewer): two Important findings retained by effect (compact map collapse; wrong-type preference startup crash). One Minor retained because repeated-point geometry requires malformed supplied state rather than normal interaction.
Final: minor (deferred): repeated identical route points display directions despite no drawable segment.
Final: Ruling: assistant conversation/proposals deferred by reviewer — next approved plan implements them; cost if wrong: current slice has no assistant yet.
Final: Ruling: live services, speech, vehicle commands and factory projection control excluded — offline labels remain explicit; cost if wrong: preview cannot satisfy live use.
Final: Ruling: actual DiLink layout compatibility unjudged — requires hardware evidence; cost if wrong: device adaptation remains necessary.
Final: Ruling: API37 startup failure remains open acceptance gate — emulator fails before installation, not app evidence; cost if wrong: target-runtime bugs remain undiscovered.
Final: Ruling: simulator calibration remains illustrative — source labelling makes assumptions explicit; cost if wrong: unsuitable for physical range prediction.
Final fix RED submitted e39ce6c/run37136818402: compactLargeTextKeepsUsableMapAndCompleteFooter and wrongTypeStoredStyleFallsBackWithoutLosingOtherChoices.

Final fix RED: run37136818402 executed26 tests, two expected failures: map Rect(0,0,0,0), wrong-type style ClassCastException; other24 and both process phases passed. Root causes confirmed at fixed-height compact layout and typed SharedPreferences getter boundary.

Final: fixed compact navigation collapse — compactLargeTextKeepsUsableMapAndCompleteFooter RED→GREEN, runtime26/26 and process2/2, subsequent module suite58/58 lint/build pass. Inspected native Systems compact map capture; map visible and full footer reachable.
Final: fixed wrong-type style preference crash — wrongTypeStoredStyleFallsBackWithoutLosingOtherChoices RED→GREEN, runtime26/26 and process2/2, subsequent module suite58/58 lint/build pass.
