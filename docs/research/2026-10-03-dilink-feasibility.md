# DiLink feasibility research

Reviewed 3 October 2026. These are public-project observations, not verification on Jordan's Australian 2024 Seal Dynamic. No external project code, binaries, artwork or signing keys have been imported.

## Repository and environment

The project repository was empty: no source, history, build configuration or AGENTS.md. A project introduction establishes main; proposed design lives on feature/mock-dashboard-foundation. The current workspace has JDK 17 but no gradle or adb executable on PATH. Android SDK/emulator availability and dependency downloads must be established during build setup. No APK has been compiled or run at this stage.

## Evidence and implications

| Primary source | Observation | Project implication |
|---|---|---|
| [BYD Trip Stats](https://github.com/angoikon/byd-trip-stats), reviewed at commit `6a7d0e31bfb2736fcaf1d3f332020b49584ac8ff` | Maintainer reports production DiLink use with Kotlin, Compose and StateFlow; DiLink 3 and 5 have different integration paths. | Native Compose is a credible starting point. Firmware-specific integration must be separate. |
| [DashCast](https://github.com/Kiroha/byd-dashcast) README | Reports a 2024 EU Seal using DiLink 3 / Android 10. Documents firmware-dependent BYD permission restrictions. | Android 10 is a useful test target, not confirmation of the Australian unit. Sideloading alone does not establish telemetry access. |
| [ABRP Sealion 7 telemetry](https://github.com/CRCinAU/abrp-telemetry-sl7) README | Uses a TS vendor service; reports signature restrictions on standard AAOS vendor properties. | Do not assume Android CarPropertyManager is the Seal's integration route or that Sealion 7 techniques transfer. |
| [Kinex](https://kinex.lexwah.com/) release notes | Lists Seal support and reports embedding the factory Android Auto and CarPlay apps in launcher panes, with a compact layout added for the embedded projection. It also reports a hard resolution ceiling. | A dashboard beside Android Auto is credible on DiLink 3, but resizing, resolution, input and resume behaviour require testing on this car. |
| [BYDMate](https://github.com/AndyShaman/BYDMate) source and README | Implements two-app 1/3 + 2/3 layouts through custom freeform windows or firmware-native split mode; behaviour varies by DiLink generation. | Isolate split control behind a platform interface and prefer the firmware-native mechanism when available. Do not make Android task/window APIs part of the UI. |
| [AndroidX releases](https://developer.android.com/jetpack/androidx/versions) and [Compose setup](https://developer.android.com/develop/ui/compose/setup) | Minimum OS depends on selected dependency versions; the current AndroidX page gives API 24 as the default for new library releases. | Propose minSdk 24 and check the actual merged manifest. Modern compile SDK does not require that same OS on the vehicle. |

Trip Stats is source-available under BUSL-1.1, not an unrestricted reuse dependency. It reports speed, gear, battery and tyre telemetry, while cabin temperature is unresolved. Its VehicleSdkAccess source also reports successful reads despite denied GET permissions on one DiLink 3 car. Permission flags alone therefore cannot prove availability. Its manifest requests some SET permissions; we will not copy that manifest.

DashCast includes privileged cluster and command features outside our scope. It is evidence about the platform, not an architecture to adopt. No platform signing, ADB daemon, cluster projection or CAN writes are proposed for milestone 1.

## Signal evidence ledger

| Signals | Public evidence | Status for this Seal |
|---|---|---|
| Speed, gear, SOC, range | Reported by Trip Stats on DiLink; corresponding SDK domains present in its source | Candidate; units, refresh and access unverified |
| HV voltage/current, signed power | Reported telemetry; interpretation varies by source | Candidate; distinguish pack power from motor output |
| Tyre pressure/temperature | Reported where exposed | Candidate per wheel; encoding and temperature units unverified |
| Drive mode, ambient temperature, charging | Reported by public projects | Candidate; enum mappings and update behaviour unverified |
| Door/boot/window positions, climate details | Bodywork/AC SDK domains appear in public source | Requested features; domain existence does not prove each signal |
| Cabin/battery/motor temperature | Availability varies; cabin temperature explicitly unresolved in Trip Stats | Optional, unavailable until demonstrated |
| GPS speed, acceleration/G-force | Potential Android location/sensor sources | Device capabilities, permissions and mounting calibration unverified |

## First car session, later

Record Android API level, firmware version, display size/density, rotation behaviour and CPU ABI. Identify the factory Android Auto package/activity and whether the firmware marks it resizable. Test firmware-native split with Android Auto at 2/3 and the dashboard at 1/3 before attempting embedded/freeform rendering. Check projection connection, audio, touch mapping, resolution, phone calls, reverse camera interruption, rotation, resume and sleep behaviour. Install the ordinary mock APK and check factory UI access and system insets. Only then investigate documented read APIs and actual permission results. Validate each received signal against the factory display, including units, sign, unknown sentinel values, timing and dropouts. Treat unavailable data as unavailable; do not silently replace missing real values with mock values.

## Remaining uncertainty

Australian firmware, SDK/service availability, app lifecycle restrictions, proprietary SDK licensing and actual signal access cannot be established without the car. The vendor PDF surfaced at `https://oip.byd.com/uploads/20210824/46cd2d7c2e878b0a1c6b066967e7f6fd.pdf` could not be retrieved, so its permission/API details are not relied upon. Public maintainer reports are useful feasibility evidence, not manufacturer guarantees.
