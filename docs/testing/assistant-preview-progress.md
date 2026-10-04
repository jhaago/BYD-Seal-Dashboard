# Assistant preview execution checkpoint

Task1 complete at ac453e9: 58 unit tests, lint/build and26 API29 runtime tests plus two process phases pass.
Task2 complete at26704d5: 66 unit tests (eight controller scenarios), lint/build and26 API29 runtime tests plus two process phases pass in run37138400868.
Task3 brief: implement parked typing and driving preview with SCRIPTED PREVIEW, explicit speech-unavailable labelling, sticky telemetry, large choices and apply/dismiss route proposals. Existing destinations remain reachable at400dp/font1.3.
Ruling: Workspace disconnected after verified controller commit; task-start commands could not run. Execute the exact approved Task3 brief through the same branch Git connector and existing CI, checkpoint in this tracked file — cost if wrong: local checkout needs fast-forward synchronization after reconnection.
Ruling: UI tests own an injected controller and cancel its scope on composition disposal; production owns one process controller — cost if wrong: test-session lifecycle differs from production lifecycle.

Task3 RED atb22b32e/run37138983940: three new AssistantPreviewTest cases fail at missing Chat navigation; existing26 runtime tests pass. Production assistant UI implementation follows.
Ruling: Parked/driving control lives in Assistant itself; Chat tab is the compact entry, with compact Car/Power/Chat labels retaining full accessibility destination names — cost if wrong: different entry location than a separate Drive/Development control.

Task3 complete at39d8ddd/run37139325163:66 unit tests, lint/build,29 API29 runtime tests and two process phases pass. Nine native parked/driving/compact screenshots across Modern, Systems and Tron were inspected; telemetry remains above the conversation and speech/live-AI limitations are visible.

Final review at7b06c26: no Critical findings; one Important stale-card selection race and one Minor ordinal-test weakness. The reviewer confirmed request generations, serialized state, one-time route mutation, unknown metadata handling and persistent telemetry.

Final fix RED at2391a0c/run37166972138: `aCardRenderedBeforeOptionsReorderCannotSelectADifferentStation` fails to compile because selection revision APIs are absent, demonstrating the missing boundary.

Final fix GREEN at00453a3/run37167196028: stable charger ID, route ID and options revision travel with each rendered card. A stale card is rejected after options reorder. 67/67 unit tests, lint/APK assembly,29/29 API29 runtime tests and two separate process-restart phases pass.

Ruling: Run verification on the existing CI Android toolchain because the local SDK is unavailable — cost if wrong: local and CI environments may differ.
Ruling: Keep CI pushes sequential after branch concurrency cancelled an earlier run — cost if wrong: verification takes longer.
Ruling: Native screenshot review travelled through CI logs during workspace disconnection; PNG artifacts remained unchanged — cost if wrong: visual evidence would need reconciliation.
Ruling: Assistant live services, speech, real charger coverage and credentials remain a later provider decision; the product says SCRIPTED PREVIEW and unknown availability — cost if wrong: this build cannot fulfil live use.
Ruling: Actual DiLink layout compatibility remains unjudged without vehicle hardware — cost if wrong: device-specific adaptation may be required.
Ruling: API37 remains an open acceptance gate because the emulator guest mapper fails before APK installation — cost if wrong: target-runtime defects may remain undiscovered.

Deferred minor: the ordinal follow-up test uses a three-option reverse order whose middle option stays the same, so that assertion is weaker than intended. Stable card selection has a separate deterministic revision test.
