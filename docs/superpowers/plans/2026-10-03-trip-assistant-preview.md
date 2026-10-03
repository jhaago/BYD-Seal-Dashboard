# Trip Assistant Preview Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. Preserve native execution and feature-branch-only work.

**Goal:** Provide a testable offline assistant conversation that proposes charging stops and updates our sample trip after explicit selection.

**Architecture:** Separate assistant service/controller, charger search and preview route actions in `:app`. Scripted adapters provide deterministic fixture data. UI renders transcript/cards/state; read-only vehicle telemetry remains separate. Live speech/model/map services are later integrations.

**Tech Stack:** Existing Kotlin/Compose/coroutines; existing dependency pins; no new network or audio dependency.

**Spec:** `docs/superpowers/specs/2026-10-03-assistant-and-visual-themes-design.md`, approved offline extension; native execution authorized.

## Global Constraints

- Output says `SCRIPTED PREVIEW`; map/routes/charger/arrival estimates are simulated.
- Minimum API 24, compile/target API 37, normal labels at least 16 sp, targets at least 56 dp and font scale 1.3.
- No microphone, network, credentials or account needed; unavailable speech must be labelled unavailable.
- No real BYD commands or factory Android Auto route reading/writing.
- Charger availability remains unknown without a live source. AI text never supplies authoritative charger facts.
- Applying a proposal is an explicit user action and updates our route exactly once.

## Review Focus

1. A slower old response must not overwrite a newer/cancelled request — Task 2.
2. “Second option” after route/options change must not apply a stale station — Task 2.
3. Missing/invalid charger metadata must remain unknown, not invented — Task 1.
4. Repeated Apply/dismiss actions must not duplicate waypoints — Task 2.
5. Compact assistant content must not cover telemetry; driving preview must not advertise functioning speech — Task 3.

## Files and interfaces

Package prefix `P` means `io/github/jhaago/sealdashboard`.

- Create `app/src/main/kotlin/P/assistant/AssistantModels.kt`, `AssistantService.kt`, `TripAssistantController.kt`, `ScriptedAssistantService.kt`, `ChargerSearchProvider.kt`, `DemoChargerSearchProvider.kt`.
- Create `app/src/main/kotlin/P/demo/PreviewRouteActions.kt`; modify `demo/NavigationProvider.kt` for mutable sample route adapter.
- Create `app/src/main/kotlin/P/ui/assistant/AssistantScreen.kt`, `AssistantTranscript.kt`, `ChargerOptionCard.kt`, `RouteProposalCard.kt`.
- Modify `AppContainer.kt`, `ui/DashboardUiState.kt`, `ui/DashboardShell.kt`, compact Drive entry points and Development preview controls.
- Mirror pure controller tests in `app/src/test/kotlin/P/assistant/`; UI tests in `app/src/androidTest/kotlin/P/ui/`.

### Task 1: Structured fixtures and route proposal boundary

**Produces:** `AssistantRequest(text: String, routeId: String, optionIds: List<String>)`; `AssistantReply` sealed results Message/ChargerOptions/Clarification/RouteProposal; `suspend fun AssistantService.respond(request: AssistantRequest): AssistantReply`; `suspend fun ChargerSearchProvider.search(routeId: String): List<ChargerOption>`; `ChargerOption(id: String, name: String, connector: String, advertisedKw: Double?, detourMinutes: Int?, availability: ChargerAvailability, sourceLabel: String)` with `enum class ChargerAvailability { UNKNOWN, AVAILABLE, UNAVAILABLE }`; `RouteProposal(id: String, baseRouteId: String, chargerId: String)`; `PreviewRouteActions.apply(proposal: RouteProposal): ProposalResult` with Applied/Stale/AlreadyApplied/UnknownCharger.

- [x] Write `PreviewRouteActionsTest`: base route `demo-1`, station `demo-fast-2`; Apply adds one waypoint and changes route ID; second Apply adds none; old route ID rejected; unknown station rejected. Literal expected waypoint counts are 1 after first Apply and 1 after repeated Apply. Add metadata tests for missing advertised kW/detour => unknown, non-finite power rejected and demo availability unknown.
- [x] Run `:app:testDebugUnitTest`; expected failures from absent contracts/adapter. Implement fixture models and deterministic route actions, with immutable emitted state and consumed-proposal IDs. Keep factory projection provider unchanged.
- [x] Run full module unit tests plus `:app:lintDebug :app:assembleDebug`; expect pass. Commit `feat: define scripted trip and charger proposal adapters`.

### Task 2: Contextual assistant controller

**Consumes:** Task 1 service/search/actions. **Produces:** `TripAssistantController.state: StateFlow<AssistantState>`; `fun submit(text: String)`; `fun cancel()`; `fun applyProposal(id: String)`; `fun dismissProposal(id: String)`; state stores transcript, route/options snapshot, current proposal and phase Ready/Responding/ChoiceRequired/ProposalReady/Failed/Cancelled.

- [x] Add virtual-time `TripAssistantControllerTest`: requests “Find a charger on my way”, “Show the smallest detour”, “I need a faster charger”, then “Add the second option to my trip” resolve against current displayed station IDs; ambiguous destination asks clarification. Slow first request after completed second does not replace transcript/options; cancellation prevents late update; provider failure becomes Failed; stale proposal Apply leaves route unchanged; double Apply adds no extra stop.
- [x] Run targeted unit tests and observe failures. Implement controller with injected service/search/actions and coroutine scope, serialized state changes and request generation token. Scripted service handles only documented examples and returns clarification for unsupported input; do not imply general natural-language intelligence.
- [x] Run full module tests/lint/build. Commit `feat: add contextual scripted trip assistant controller`.

### Task 3: Parked chat and compact driving preview

**Consumes:** Controller state/actions and theme tokens. **Produces:** `@Composable fun AssistantScreen(state: AssistantState, onSubmit: (String) -> Unit, onCancel: () -> Unit, onApply: (String) -> Unit, onDismiss: (String) -> Unit, drivingPreview: Boolean)` and Assistant navigation/compact entry.

- [ ] Add `AssistantPreviewTest`: type sample request, receive labelled station cards, send follow-up, review proposal and Apply; nav returns to updated own-navigation preview. Cancel/error/clarification are visible. Driving-preview uses short responses/large choices and labels speech unavailable; full typing is in parked preview. At 400 dp/font 1.3 and wide companion layout, primary speed/SOC/source/navigation remain reachable without overlap. Existing four destinations continue working after adding Assistant.
- [ ] Run instrumentation tests; expected missing assistant UI failure. Implement transcript/cards/inputs and a parked-preview toggle owned by display UI, not inferred as reliable from mock speed. Add no production audio/network permission. Wire controller once in AppContainer; no per-screen duplicate service/session.
- [ ] Run all module tests, Android lint/build, full runtime suite and screenshot review. Update test evidence count expectations to actual executed tests, with zero skips/errors. Commit `feat: introduce offline assistant conversation preview`.

## Evaluation handoff

Record APK commit/hash and new screenshots; identify scripted responses and sample station facts plainly. Live AI, speech, navigation/charger coverage, credentials and pricing are separate provider decisions after Jordan evaluates this preview. No functioning live-assistant claim is permitted for this slice.
