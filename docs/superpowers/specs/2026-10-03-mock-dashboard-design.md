# BYD Seal Dashboard — proposed first milestone

Status: proposed for Jordan's review. This is a design, not a completed app or an implementation plan.

## Intent and scope

Build an original, premium Android centre-screen experience for Jordan's Australian 2024 BYD Seal Dynamic, single-motor RWD. The first major milestone is a genuinely installable APK with changing simulated telemetry, four navigable screens and no dependency on the car, accounts, internet or proprietary SDKs. Evaluate the actual interface before integrating BYD data.

The app is initially a normal Android application launched from the existing home screen. Its primary eventual driving arrangement is a two-app landscape view: factory Android Auto in the left two-thirds and a compact dashboard in the right third, closest to the driver. Default-home registration and automatic startup remain deferred until the normal app has been tested. The mock build represents Android Auto with an explicitly labelled simulation because the real projection host exists only on compatible vehicle hardware.

## Approach and alternatives

Recommend Kotlin and native Jetpack Compose: direct Android integration later, responsive state-driven UI and original Canvas graphics without a web bridge. A traditional Views app is viable but adds more UI wiring without evidence of a necessary compatibility benefit. A WebView dashboard permits rapid visual prototyping but adds a bridge and lifecycle/rendering work for telemetry, sensors and media. Neither alternative currently justifies replacing Compose.

Propose minSdk 24 (Android 7), subject to checking dependency manifests, and test Android 10/API 29 as a likely DiLink-era environment. Use stable, pinned Kotlin/Compose/AGP dependencies, an official Gradle wrapper with checksum and JDK 17. Select compatible versions during build setup. Use a current stable compile/target SDK for the ordinary mock app; do not lower target SDK speculatively to enable hidden APIs. A later integration build can revisit platform constraints with evidence. Do not require AAOS features, Google Play Services or native libraries for milestone 1.

## Visual design

Use near-black background, graphite surfaces, warm white text and restrained cyan accents. Use amber/red with text and icons for simulated fault states. Typography and whitespace establish hierarchy; avoid circular gauges, neon borders and a grid of unrelated tiles. Main touch targets should be at least 56 dp. Labels should generally be 16–18 sp or larger; speed should dominate at roughly 88–112 sp on a wide display. These are starting tokens to validate with screenshots and font scaling.

Landscape Drive has two responsive forms. Full-dashboard mode uses a navigation/demo-route region on the left and driving information on the right. Split-companion mode is the preferred eventual in-car arrangement: factory Android Auto occupies the left two-thirds while a purpose-built compact dashboard occupies the right third, nearer the driver in an Australian RHD car. A development preference can mirror the panes, but the first default is Android Auto left/dashboard right. A slim top status line carries time, outside temperature and a persistent SIMULATED label. Compact navigation provides Drive, Vehicle, Energy and Development without shrinking the companion's primary telemetry.

Speed and gear stay immediately readable. SOC, range and drive mode sit below them. A restrained horizontal power/regen indicator replaces small gauges. Secondary trip detail is compact. Animations are short transitions and subtle numeric/bar changes; speed must not lag behind the incoming sample through long easing. No simulated lane keeping, obstacle detection or ADAS imagery.

The navigation area and Android Auto pane in the mock APK are explicitly labelled simulations rendered with original geometry. They are not Google artwork, a real map or a projection feed. Media is clearly sample content. Navigation, media and projection have integration boundaries so later providers can replace them without changing vehicle telemetry.

## Screens

| Screen | First milestone content |
|---|---|
| Drive | Full-dashboard and 2/3 + 1/3 companion previews. Speed, gear, SOC, estimated range, drive mode, signed pack-power indicator, ambient temperature, climate summary, compact trip data and simulated Android Auto/navigation/media. |
| Vehicle | Original top-down sedan representation, four tyre pressures in kPa, optional simulated temperatures, individual door/boot states, SOC/range and charging summary. Graphic renderer accepts typed state and can later be replaced by proper Seal artwork. |
| Energy | Separate rear-motor output and pack-power readings; regen magnitude; HV voltage/current; 60-second signed pack-power history; trip distance, gross energy used, recovered energy and net efficiency; SOC history. No front motor display for the Dynamic. |
| Development | Demo/manual/pause/reset; target speed and accel/brake controls; SOC; gear/mode; per-wheel pressures; four doors and boot; charging; mock climate controls; raw signal values, source/freshness and bounded diagnostics. Real provider option visibly unavailable. |

Window positions, GPS comparison, G-force and 0–100 timing are later extensions. The model permits optional values, but milestone 1 does not need empty technical widgets for them. Portrait uses the same state/components with stacked content and scrollable secondary information. Landscape is the polished target; portrait must remain usable without clipping or state loss.

## Architecture and boundaries

Start with three Gradle modules, avoiding a module per screen:

- `:vehicle-core`: pure Kotlin state model, profiles, provider contract, quality metadata and derived energy/trip calculations. No Android or BYD imports.
- `:vehicle-mock`: pure Kotlin deterministic simulation engine, scenario scripts and mock provider using coroutines. Depends only on core.
- `:app`: Android application, composition root, ViewModels, Compose theme/components/screens, preferences and development controls. Depends on core/mock. Feature packages separate the four screens.

Later `:vehicle-byd` depends on core and Android/vendor integration only. The composition root selects one provider. Screen ViewModels consume the provider contract, not its implementation. Use constructor injection with a small app container; a DI framework is unnecessary initially.

A later `BydSplitScreenController` in the Android/BYD integration boundary owns detection and launch of the factory Android Auto package, firmware-native split requests, custom freeform/embedded fallback and restoration to a normal full-screen task. The UI requests a layout intent and observes status; it does not call hidden task/window APIs. Android Auto remains a separate factory task, not Compose content and not an `androidx.car.app` implementation. Failure returns the dashboard to full screen and explains that split mode is unavailable. No projection package names, activities or hidden methods are hard-coded until inspected on Jordan's car.

`VehicleDataProvider` exposes immutable `StateFlow<VehicleState>`, provider connection status/capabilities and bounded diagnostic events. Lifecycle operations are idempotent. A single owner starts/stops the provider; each screen must not create another polling job. The Android layer collects state with lifecycle awareness. Rotating preserves simulator state in an application-owned session. Pause simulation when the application is backgrounded; resume without integrating a large hidden time jump. Process death starts a fresh mock session and restores display preferences only.

`MockSimulationController` is a separate command surface. It belongs only to the mock provider and is passed only to Development. The read-only contract contains no actuator setters. Do not create a generic vehicle-command interface for future use.

## Shared state and signal semantics

`VehicleState` groups profile, motion, battery, powertrain, wheels, openings, climate, charging, environment and trip. Profile identifies market, model/year and driven axles; it is separate from telemetry. Initial RWD topology has one rear motor. Simulated battery capacity and power limits are editable, explicitly model assumptions until checked against a suitable Australian specification; do not claim calibrated vehicle performance.

Every signal has an optional typed value, monotonic observation timestamp, source (simulated, measured or derived) and quality (fresh, stale, unavailable or error). A capability map distinguishes unsupported from temporarily missing. Freshness is evaluated against a monotonic clock even if no new snapshot arrives. On disconnect preserve the last sample as stale, not live; speed becomes a dash with a stale indication. Unknown gear is not Park, and missing SOC is not zero. Never automatically substitute mock data into a real session.

Use explicit units in names/types: km/h, km, percent, kPa, degrees C, V, A, kW and kWh. Internal simulator physics uses SI units. Canonical battery current and pack power are positive for discharge, negative for charging/regen. `packPowerKw = voltageV * currentA / 1000`. Rear-motor output is a separate signal; HV pack power includes auxiliaries/losses and must never be renamed motor power. Regen magnitude is nonnegative and separate from charging state. Calculated efficiency is unavailable before meaningful distance, rather than dividing by zero.

Keep raw SDK fields and decoding errors out of the domain model; future integration diagnostics retain names, units, timestamps and limited raw values separately. History buffers are bounded. No permanent trip database is needed for this milestone.

## Simulator behaviour

Use a deterministic engine with an injected monotonic clock and an explicit elapsed-time step. Target roughly 10 updates/second for speed/power and 1 history sample/second. Keep 60 seconds of power and up to 15 minutes of SOC history. Sample acquisition, history and Compose rendering are separate rates.

The automatic demonstration cycles through parked, gentle acceleration, cruising, braking/regen and a stopped charging segment. Manual mode lets the user set target speed or accel/brake input; these controls are clearly simulation-only. Switching mode cancels the previous scenario. Pause freezes engine time; reset restores a documented baseline and clears trip/history.

Derive acceleration and speed, distance, traction demand, regenerative recovery and auxiliary climate load coherently. Clamp SOC to 0–100; integrate battery energy from signed pack power and elapsed time. Range derives from remaining simulated energy and a bounded consumption estimate. Track gross discharge, recovered driving energy and net trip energy separately; plug-in charging does not count as trip regen. HV voltage/current remain consistent with pack power.

Charging requires zero speed and Park; engaging charging stops driving demand. Empty battery prevents positive traction, full battery stops further charge and reduces regen to zero. Park cannot retain moving speed. Door changes are stationary scenarios. Manual speed uses a bounded ramp rather than instantaneous jumps. Tyre and temperature evolution is deliberately illustrative. Development supports unavailable/stale signal and provider-error scenarios to exercise the same UI failure paths expected later.

## Safety and integration

Milestone 1 requests no BYD permissions, writes no vehicle commands, uses no ADB daemon, platform key, CAN transport, firmware modification, overlays or cluster APIs. Climate changes affect mock state only. The app remains on the centre infotainment screen and has an obvious route back through Android system navigation. Factory camera/phone interruptions and recovery will require car testing later.

Navigation, Android Auto presence and media have separate state/providers, initially demo implementations. The first APK includes a visual 2/3 + 1/3 preview and a device-safe full-dashboard mode; it does not pretend to host a real Android Auto session on an emulator or phone. Actual factory-host launching, multi-window control, navigation intents, notification/media access and playback controls follow car inspection. No account/key/network dependency is needed to run the first build. No analytics or outbound telemetry.

## First major milestone and acceptance

Build incrementally: reproducible Android shell and core contracts; deterministic simulator with meaningful tests; polished Drive plus basic Vehicle/Energy; Development controls and failure states; APK/emulator verification. Each slice has a logical commit on the feature branch. Do not merge experimental code into main.

The milestone is complete only when:

1. An APK is assembled successfully with the pinned wrapper and dependencies; record commit, toolchain, checksum and artifact location.
2. Core tests prove speed/braking/regen behaviour, timestep energy accounting, SOC limits, charging interlocks, zero-distance efficiency, reset/pause and stale-data handling.
3. Android lint and module unit tests pass; inspect merged manifest for minimum SDK and unintended vehicle permissions.
4. Install and launch on an Android 10/API 29 emulator and a current supported Android emulator; record runtime evidence rather than equating compilation with usability.
5. All four destinations work and share one changing simulation. Manual controls affect the dashboard; acceleration and braking visibly change power/regen; charging raises SOC at a physically consistent simulation rate.
6. Inspect full-dashboard and Android Auto 2/3 + dashboard 1/3 landscape screenshots (including a large tablet layout), portrait rotation, system insets and enlarged text. The compact dashboard keeps speed, gear, SOC, range and power legible; no overlaps or lost simulation state.
7. Exercise foreground/background, pause/reset, unavailable telemetry and integration errors; no frozen reading masquerades as fresh.
8. Provide installation instructions and distinguish emulator-tested from car-tested. Hardware compatibility remains unverified until installed on Jordan's car.

## Decision for review

Recommend the RHD-oriented split-companion layout, with Android Auto on the left two-thirds and the dashboard on the right third, plus a normal launchable full-dashboard mode for milestone 1. This preserves the premium home-screen direction while making the first APK easy to evaluate on Jordan's phone/tablet. After this written design is reviewed, prepare the implementation plan and begin the runnable mock foundation. See [DiLink research](../../research/2026-10-03-dilink-feasibility.md) for evidence and remaining unknowns.
