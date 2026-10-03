# Assistant preview execution checkpoint

Task1 complete at ac453e9: 58 unit tests, lint/build and26 API29 runtime tests plus two process phases pass.
Task2 complete at26704d5: 66 unit tests (eight controller scenarios), lint/build and26 API29 runtime tests plus two process phases pass in run37138400868.
Task3 brief: implement parked typing and driving preview with SCRIPTED PREVIEW, explicit speech-unavailable labelling, sticky telemetry, large choices and apply/dismiss route proposals. Existing destinations remain reachable at400dp/font1.3.
Ruling: Workspace disconnected after verified controller commit; task-start commands could not run. Execute the exact approved Task3 brief through the same branch Git connector and existing CI, checkpoint in this tracked file — cost if wrong: local checkout needs fast-forward synchronization after reconnection.
Ruling: UI tests own an injected controller and cancel its scope on composition disposal; production owns one process controller — cost if wrong: test-session lifecycle differs from production lifecycle.

Task3 RED atb22b32e/run37138983940: three new AssistantPreviewTest cases fail at missing Chat navigation; existing26 runtime tests pass. Production assistant UI implementation follows.
Ruling: Parked/driving control lives in Assistant itself; Chat tab is the compact entry, with compact Car/Power/Chat labels retaining full accessibility destination names — cost if wrong: different entry location than a separate Drive/Development control.
