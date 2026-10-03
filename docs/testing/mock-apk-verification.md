# Mock APK verification checkpoint

Status: in progress; not a completed or car-tested milestone.

## Proven evidence

- Core/model, deterministic simulator and reactive/lifecycle unit tests passed before UI development (49 tests).
- Four-screen UI commit `ea75dab` passed 50 unit tests, Android lint and APK assembly in [run 37123387865](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37123387865).
- API 29 installed/launched the APK. Runtime tests verified navigation, manual telemetry changes, eight size/font combinations and 2:1 pane widths.
- [Run 37124615125](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37124615125) executed 9 API 29 tests: 7 passed, 2 failed on inherited black text and dark system-bar icons. Those regressions were observed before the targeted production corrections in the next commit.
- API 37 has not completed installation/runtime validation. Emulator 37.2.12 SurfaceFlinger repeatedly aborted in RegionSampling with `hasReadColorBufferDma`; Android package/activity services became unavailable before app testing. A renderer change alone did not fix it.
- Runtime CI now requires at least 9 executed tests, zero failures/errors and zero skips. A Gradle SUCCESS with no executed tests is explicitly rejected.

## Latest verified checkpoint

At `1c2fc49` in [run 37125265354](https://github.com/jhaago/BYD-Seal-Dashboard/actions/runs/37125265354), all 50 unit tests, Android lint and APK assembly passed. API 29 installed/launched the corrected APK and passed all 9 runtime tests, with zero failures or skips. Both contrast regressions passed RED→GREEN. Seven screenshot fixtures were captured and pulled successfully (472,674 bytes total); their corrected visual inspection is still pending workspace recovery. API 37 failed during emulator window-service setup before app tests; it is not a passing runtime gate.

## Fixes and unresolved environment

Provide explicit warm-white inherited content color and light system-bar icons. Keep diagnostic screenshots through test cleanup using the test app's normal MediaStore insertion API, with no production storage permission. API 37 additionally tests disabling emulator GLDirectMem: host render-control source ties advertised read-color-buffer DMA to direct-memory support. This is an emulator graphics compatibility hypothesis, not a vehicle/app permission workaround.

References: [host render-control source](https://fuchsia.googlesource.com/third_party/android/device/generic/vulkan-cereal/+/ba29ba979efcfc2c2cd0f67d4060f451df459fcd/stream-servers/RenderControl.cpp), [emulator feature configuration](https://android.googlesource.com/platform/external/qemu/+/06acc5e964e44ec664c2bc5f8ee7a0a06daae8d4/android/android-ui/modules/aemu-ui-window/src/android/main-common-ui.c).

## Remaining gates

- Re-run contrast regressions, inspect every screenshot (full/companion, Vehicle, Energy, Development, portrait and large text).
- Complete API 37 install/launch/tests; do not substitute API 29 evidence.
- Rotation/navigation/background/process-recreation runtime regression tests.
- Fresh committed-HEAD build, manifest/permissions/hash and evaluation documentation.
- Independent whole-branch review; keep main untouched.

Workspace file/terminal/download operations became unresponsive during verification. Targeted fixes are being made through the already configured GitHub connection on the same feature branch. Reconcile the existing checkout with these remote commits when workspace operations recover, preserving any unrelated user changes. No merge or force push is authorized by this checkpoint.

This app uses simulated data only. Do not rely on it for real driving speed, range or navigation. Factory Android Auto hosting/split control, cluster integration, cameras/sentry and real BYD telemetry remain deferred. No vehicle commands, firmware changes or vehicle/network permissions are implemented.
