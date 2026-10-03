# BYD Seal Dashboard — assistant and visual themes

Status: design and both offline implementation plans approved by Jordan on 3 October 2026. This document captures the additions discussed after the original foundation plan; these features are not implemented yet.

## Intent

Jordan wants a polished centre-screen dashboard for his Australian 2024 BYD Seal Dynamic (RWD), primarily alongside factory Android Auto. He also wants alternative Collins submarine-inspired systems visuals, a futuristic cyberpunk/Tron navigation screen with recognisable street-map geography, and a voice/chat assistant that can discuss a trip and find a useful charger on the way. Voice should reduce manual searching while driving. Preserve the existing telemetry/provider separation and evaluate the interaction before connecting paid services or the car.

## Delivery sequence

1. Finish the original mock foundation verification. Record any emulator environment blocker explicitly; screenshots or successful compilation do not replace runtime evidence.
2. Add selectable Modern and Systems themes to the existing dashboard. Add an offline Tron navigation preview using original street/block geometry and a labelled sample route. Keep all three usable in compact windows.
3. Add an assistant preview with clearly labelled scripted conversations, charging-stop options and route proposals. Demonstrate follow-up choices and route changes against the same sample navigation state. This is a conversation prototype, not an AI model or real charger service.
4. Plan live speech, AI, mapping, routing and charger providers separately after the preview is accepted. Choose services based on coverage, credentials, licensing, price and Android/DiLink compatibility then; no provider is assumed available by this design.

Themes and the assistant are separate implementation slices so either can be tested/rejected independently. Existing four-screen dashboard behaviour remains the foundation.

## Visual system

Use `DashboardVisualStyle` values `MODERN`, `SYSTEMS`, `TRON`. Modern preserves the current graphite/warm-white/cyan baseline. Systems is an original Collins-inspired instrumentation aesthetic: dark burgundy instrument backgrounds, pale-blue schematic lines, bright-green valid/active indicators, squared frames, compact technical labels, clear numeric readouts and original energy-flow/status graphics. Jordan’s supplied reference on 3 October clarified an older industrial mimic-panel look, rather than a generic blue/green modern dashboard. Draw original EV battery, inverter, motor and available cooling/temperature relationships; do not reproduce the reference’s actual submarine topology. Keep labels at the agreed readable size even where the reference is denser. Do not use defence screenshots, logos, operational layouts or proprietary assets. Systems graphics show only available typed signals; unsupported components remain unavailable.

Tron uses dark map surfaces, subdued street/building geometry, cyan route glow, a clear vehicle marker, prominent next-turn instruction, ETA/distance and charging-stop cards. Geography should feel recognisable rather than a decorative abstract grid. The offline preview uses a fictional map and says `SIMULATED MAP`; it is not Google Maps tiles, directions or a real route. Live map styling is a later adapter choice, not permission to reuse Google artwork.

Implement immutable `DashboardPalette` and shape/typography tokens through composition locals, avoiding mutable global colours. Each theme reaches Drive, Vehicle, Energy and Development. Preferences persist style plus existing layout/mirroring only; raw telemetry is never persisted. Unrecognised stored styles fall back to Modern.

Speed, gear, SOC, range, signed pack power and source/quality remain prominent. Keep touch targets at least 56 dp, normal labels at least 16 sp, support font scale 1.3, and use text/icons as well as colour for warnings. Primary readouts must not disappear behind decorative panels or assistant content.

## Navigation preview

Extend the existing `NavigationProvider` with a separate preview-only route command contract. Read-only navigation state carries route ID, destination, ordered waypoints, next manoeuvre, distance/ETA, route geometry, estimate source and proposed alternatives. Coordinate types distinguish fictional local map coordinates from later geographic latitude/longitude. Keep vehicle telemetry out of map graphics and keep assistant commands out of `VehicleDataProvider`.

Default in-car goal remains Android Auto left two-thirds/dashboard right third. The preview can switch its simulated left pane between Android Auto preview and our own navigation preview. Assistant tools act only on our route state; they cannot read or change factory Android Auto's route unless a later explicit integration proves it. A narrow actual app window gets a compact dashboard with a reachable navigation/assistant entry point. Actual DiLink split control is still hardware-dependent.

## Assistant preview

Add an Assistant destination/overlay with short transcript turns, input, suggested prompts and charging/route cards. The parked preview supports typed conversation. A reachable compact action opens the assistant without hiding primary telemetry. Provide a user-selectable driving preview that presents short responses and large choices; speech controls remain visibly unavailable until a real speech adapter exists. Do not imply a mock speed sample provides driving-lock enforcement on the car.

Sample interactions: “Find a charger on my way”, “Show the smallest detour”, “I need a faster charger”, and “Add the second option to my trip”. Maintain context for the current route and displayed options so follow-up references resolve consistently. Ambiguous requests produce a clarification rather than a guessed destination. The controller exposes ready, responding, choice-required, proposal-ready, failed and cancelled states; a newer request cancels the previous response and stale results cannot replace it.

Use separate contracts: `AssistantService`, `TripAssistantController`, `ChargerSearchProvider`, and preview route actions. Inject scripted adapters into the composition root. Responses identify their source as `SCRIPTED PREVIEW`; no external requests, microphone access or credentials are added in the offline phase.

Route changes are reviewable proposals. The user selects a station/route proposal and applies it; the navigation state changes once. Dismissed or stale proposals do not mutate the route. Charger cards display connector, advertised power, detour, estimate/source and availability. Missing live availability says unknown. Demo arrival SOC/range is explicitly illustrative and cannot be presented as a validated vehicle prediction. Ranking uses deterministic fixture data, with ranking reason shown; an AI text answer never invents station metadata.

## Live integration boundary

Later voice recognition and speech synthesis wrap Android/service capabilities with clear unavailable, denied, offline and cancelled states. Live model calls go through a configured service boundary; no shared secret is embedded in the APK. Function/tool calls use validated typed arguments and return structured provider results. Navigation changes use the same reviewable proposal flow. Establish map/routing/charger providers and attribution before claiming actual guidance. Charging feasibility uses validated vehicle/route estimates, reserve preferences, connector compatibility and station data freshness. Missing data yields uncertainty rather than fabricated availability or arrival SOC.

## Acceptance

- All three styles are selectable, persist across process restart, and retain the same simulated telemetry, source/quality and selected screen.
- Both wide companion/full previews, 400 dp compact windows, portrait and font scale 1.3 keep primary values and navigation accessible.
- Tron preview shows original street/block geometry, a route, turn cue, ETA/distance and sample charger markers, with clear simulation/source labelling.
- Scripted assistant handles the four sample requests and a follow-up, ambiguity, cancellation, failure and stale route/proposal. Applying a proposal updates our sample route exactly once.
- Typed chat never suggests speech or live AI is working. No additional production permission, network dependency or account is needed to use the preview.
- Existing foundation tests remain green; new theme/conversation/route-boundary tests run before UI verification. Review screenshots of all styles and both assistant presentations.
- No real BYD commands, factory projection hosting, instrument-cluster changes or sentry-camera recording are included.

## Review decisions

Recommendation: ship themes and assistant interaction as two offline preview slices, then evaluate live services. Connecting AI/maps immediately adds credentials and coverage decisions before we know the layout works. Expanding the entire project to cluster/cameras now would delay the centre-screen evaluation without resolving vehicle access.

The written implementation plans describe only the next offline slices. Live services remain a later, separately designed integration task.
