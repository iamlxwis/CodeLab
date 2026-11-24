# CodeLab — task progress audit

Short, append-only development checkpoints. The [README tracker](../../README.md#development-tracker) shows the current summary. **Every meaningful change to a task's status or implementation progress must be committed with an update to both this log and the README in the same commit.** Never mark a task complete without verifiable test/build evidence.

### Initial baseline — 2026-11-23

| Task | State | Evidence / remaining issue |
| --- | --- | --- |
| `T01` Done | Production-signed release confirmed; upgrade test is separate · [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919) |
| `T02` In progress | Android 15 online-run smoke failed; Android 13 passed · [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470) |
| `T03` Queued | Workstream pending end-to-end verification |
| `T04` Queued | Workstream pending end-to-end verification |
| `T05` Queued | Workstream pending end-to-end verification |
| `T06` Queued | Workstream pending end-to-end verification |
| `T07` Queued | Workstream pending end-to-end verification |
| `T08` Queued | Workstream pending end-to-end verification |
| `T09` Queued | Workstream pending end-to-end verification |

### Subsequent updates (append entries; never overwrite historical events)

| When (historical rows in UTC; new events in Asia/Manila 12-hour AM/PM) | Task | New state | What changed / proof |
| --- | --- | --- | --- |
| 2026-11-23 | T01–T09 | Baseline recorded | Initial tracker created; earlier milestones summarized rather than inventing historical event logs |

#### Required update format

With each meaningful task start, commit checkpoint, failure, retry, or verified completion, append a row with the date, task ID, correct state, actual change and evidence link. Update README task row and counts **in the same commit**, plus `PROJECT_MEMORY.md`. In-progress checkpoints remain In progress until done. If a test fails, preserve Failed/Blocked evidence rather than declaring completion. Keep entries short and factual.

| 2026-11-23 | T02 | In progress | Confirmed API35 failure in [device CI 45191732470](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470): XML shows `Online: source upload needs confirmation`, a valid state. Smoke now accepts all three legitimate status messages; API33/35 rerun pending. 1/9 done, 8 remaining, 1 active, 0 blocked. |

| 2026-11-23 | T04 | In progress | Added pre-publish API33/API35 same-signer production `adb install -r` acceptance: project/theme retention, Python module trust and real execution. CI pending; physical handset unverified. 1/9 done, 8 remaining, 2 active, 0 blocked. |

| 2026-11-23 | T01 | Reopened / In progress | Examined APK v2 certificates from [a11](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919), [7acd](https://github.com/iamlxwis/CodeLab/actions/runs/45200242402), [ee24](https://github.com/iamlxwis/CodeLab/actions/runs/45201489255): distinct Android Debug signers despite metadata `production`. Corrected missing Gradle signing env and added true keystore-to-APK signer check; CI pending; key untouched. Counts: 0/9 done, 9 remain, 3 active, 0 blocked. |

| 2026-11-23 | T01 | In progress, CI failure fixed in source | [APK workflow 45204023605](https://github.com/iamlxwis/CodeLab/actions/runs/45204023605) failed at `:app:packageRelease`: `SigningConfig "production" is missing required property "storePassword"`. Root cause: Kotlin DSL `storePassword = keyPassword`, `keyAlias = keyAlias` resolved RHS to uninitialized `SigningConfig` receiver properties, not outer local values. Renamed outer values to `releaseStorePassword`, `releaseAlias`, `releaseEntryPassword`, with static guard; retest pending. Counts unchanged: 0/9 done, 9 remain, 3 active, 0 blocked. |

| 2026-11-23 | T01 | Done | [APK workflow 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835) passed build/JVM/lint/signature verification AND actual editor/Python signer comparison against permanent keystore at source 7e04d47. Independently extracted editor certificate SHA-256 `c81d2aa187febd5252ad0369f2b4cfba368b5f16f6b4b049963fa284c9e42392`. Published latest; old debug APKs still need backed-up migration. T04 upgrade skipped for signer mismatch, not passed. |
| 2026-11-23 | T03 | In progress | Found stale online-run confirmation could be accepted after editing, switching mode or target. Bound approval to exact file, revision, mode, endpoint and compiler language ID; consumed once, canceled on network loss/mode switch. Added JVM regression; source CI pending. Counts 1/9 done, 8 remain, 3 active (T02/T03/T04). |

| 2026-11-23 | T04 | In progress; CI failure fixed in test source | [signed upgrade run 45207076639](https://github.com/iamlxwis/CodeLab/actions/runs/45207076639) passed build/JVM/lint/production signer match and reached Android 13's actual app; failed `Timed out waiting for ('Appearance',)` because old test expected a top-level item in searchable Workspace actions sheet. Failure screenshot/XML show categorized sheet; no crash evidence, not a data-retention failure. Added `open_workspace_action()` searching and tapping result TextView (not input) for Appearance and Languages. New API33/35 CI pending, T04 not marked done. Counts 1/9 done, 8 remaining, 3 active. |

| 2026-11-23 | T02 | Done | [Device smoke 45209632246](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246) **success** in Android 13 and 15 emulator jobs, including Online-mode status corrected in `7acd475`. This does not imply a live external provider execution occurred. |
| 2026-11-23 | T04 | Done for emulator acceptance | [Release CI 45209632286](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286) **success** on API33/35 production-signed `adb install -r` tests; prior/current editor + Python versionCodes `1286 -> 1288`, same keystore signer, persisted test project source and Clay theme, verified companion and real offline Python. Publication succeeded at commit `19123aa9`. Physical handset upgrade outstanding, historical debug certificate requires backed-up migration. |
| 2026-11-23 | T03 | In progress | Cloud consent guard `ca4ffc9` with JVM 12/12 branch coverage passed in release CI; full app-wide functional audit and live remote execution remain unverified. **Tracker: 3/9 complete (T01, T02, T04); 6 remaining, T03 active, T05–T09 queued; 0 blocked.** No unverified requirement silently closed. |

| Nov 24, 2026 · 12:18 AM PHT | Session | Resumed | Prior turn stopped when the assistant responded, not because the app was finished. Starting a new interactive development session; no autonomous work occurs after the assistant's final reply. |
| Nov 24, 2026 · 12:20 AM PHT | T03/T05 | In progress | Resumed repository inspection; validating app defects and Python module download/install state. No code or test completion claimed at this checkpoint; next action: implement verified change, run CI, then mark handoff STOPPED with an explicit timestamp. |

| Nov 24, 2026 · 12:23 AM PHT | T05 | In progress — awaiting CI | Implemented Python module version decision (fresh install/update/reinstall/downgrade/invalid), rejects downgrades and invalid version codes after same-package/signer verification; reports missing completed DownloadManager files as recoverable failures. Language UI distinguishes install/update/reinstall and labels downloading latest rather than checking without download. Added 5 JVM tests incl longVersionCode (>32-bit). Tests/build/emulator verification pending; cannot yet claim Android in-app download/install success. |

| Nov 24, 2026 · 12:26 AM PHT | T05 | In progress, device QA pending | Added `qa/missing_python_smoke.py` to Android 13/15 CI **before** installing the Python companion. On disposable emulator script creates a Python project, presses Offline Run, demands actual `Python runtime module not installed` error, navigates through `Manage Python` to the in-app `Download Python` control, checks no silent install and captures screenshots. Does NOT download, sideload or approve installation. CI pending; subsequent existing device smoke still tests installed Python after its own setup. |

| Nov 24, 2026 · 12:30 AM PHT | T08 | In progress, CI pending | Confirmed tutorial `Try it in CodeLab` called `onFinish`, marking first-run guide completed prematurely. Now saves next topic number, dismisses temporarily, invokes real feature, resumes at next topic through More > Quick start guide or next first launch. Skip/Finish deliberately completes and clears saved checkpoint. Added Import ZIP practical action and corrected menu label to 14 topics; added 3 JVM regression tests for next/restore/clamp/advanced stage. Android small-screen, large-font and TalkBack flow pending. |

| Nov 24, 2026 · 12:32 AM PHT | T08 | In progress — device QA pending | Added `qa/tutorial_resume_smoke.py` to Android 13/15 CI before other device smokes: on clean app, try a live New project action at topic 2, force-stop/restart to demand topic 3 resume, deliberately Skip, restart to demand onboarding no longer appears. Screenshots and per-API evidence. Real Android test is not yet passing; full 14-topic accessibility remains open. |

| Nov 24, 2026 · 12:35 AM PHT | T07 | In progress, Android QA pending | Confirmed unselected swatches were anonymous 40dp circles, below accessible touch-target guidance. Introduced individually labeled 48dp swatches, selected semantics, narrow-width scrolling, plus selected semantics for theme cards and color mode. Added Android Compose UI regression: six labels and actual selection update. Full manual TalkBack audit still open. |

| Nov 24, 2026 · 12:39 AM PHT | T07 | CI failed; fix committed, retest pending | [APK CI 45218655670](https://github.com/iamlxwis/CodeLab/actions/runs/45218655670) and [Android device 45218655206](https://github.com/iamlxwis/CodeLab/actions/runs/45218655206) failed at AppearanceGallery.kt:117 with Kotlin `val cannot be reassigned` / Boolean expected String. Cause: SettingChoices parameter `selected: String` shadowed Compose semantics receiver property in `semantics { selected = active }`. Renamed to `currentChoice`, avoiding collisions while preserving selected semantics and 48dp swatches; new CI pending. Nothing falsely marked complete. |

| Nov 24, 2026 · 12:48 AM PHT | T08 | Failed API35; root investigated and fix pending CI | [Device CI 45219173196](https://github.com/iamlxwis/CodeLab/actions/runs/45219173196) failed after Skip plus immediate force-stop/restart: last-window.xml showed topic 3 (`Choose language and template`), whereas Try-resume screenshot showed topic 3 correctly. Prior async SharedPreferences.apply() risked losing last write. Changed Try and Finish/Skip to checked synchronous commit before navigation and explicitly wait for dismissal before forcing process death; this distinguishes missed UI tap from failed persistence. New CI pending. |

| Nov 24, 2026 · 1:00 AM PHT | T05/T08 | Partial Android 15 pass, old QA expectation failure fixed | [Android device CI 45220967836](https://github.com/iamlxwis/CodeLab/actions/runs/45220967836) API35 executed `qa/clean_install_smoke.py`, `qa/tutorial_resume_smoke.py`, `qa/missing_python_smoke.py` successfully, then failed in **old** `qa/device_smoke.py` waiting for `Check for updates`. This app label was intentionally corrected to `Download latest Python`; aligned both device and signed-upgrade QA with new truthful label. Existing data-retention/runtime assertions preserved. New CI required; API33 still running at checkpoint. |

| Nov 24, 2026 · 1:02 AM PHT | T07/T04 | Source fix, retry pending | [APK CI 45220967890](https://github.com/iamlxwis/CodeLab/actions/runs/45220967890) failed API33 signed-upgrade test **after actual `adb install -r`**, when old test looked for `content-desc=Selected`. Inspecting failure screenshot verifies Clay card visibly selected after upgrade; failure due removal of decorative icon description in T07 accessibility change, not data loss. Added contextual `Clay style, selected` semantics on card and QA that validates newer contextual label or old-position-specific marker (because test installs previous APK then upgrades). Source UI selection remains verified visually; CI retry pending. |

| Nov 24, 2026 · 1:36 AM PHT | T07/T09 | Source fix pending CI | [Release CI 45223423186](https://github.com/iamlxwis/CodeLab/actions/runs/45223423186) passed, but [device CI 45223423018](https://github.com/iamlxwis/CodeLab/actions/runs/45223423018) failed both APIs compiling Android tests due invalid assertExists import. Removed import, kept assertion; release workflow now compiles instrumentation tests before publishing. README changed to ONE live overwritten action entry, with append-only history in docs/Git. |

| Nov 24, 2026 · 1:54 AM PHT | T03/T05/T09 | New source pending CI | Verified both [release upgrade 45229584184](https://github.com/iamlxwis/CodeLab/actions/runs/45229584184) and [device CI 45229584342](https://github.com/iamlxwis/CodeLab/actions/runs/45229584342) PASS API33/API35. Fixed release timing gap by requiring the same SHA's device workflow SUCCESS before publishing (Python gate with 4 unit tests), fail-closed on rotating production key. Improved Python module installer trust for already-installed differently signed companion (4 JVM cases), and validated save bytes before snapshot (rejected-save history regression). New CI pending; no actual Python in-app network installation claimed. |

| Nov 24, 2026 · 2:01 AM PHT | T09 | Invalid workflow YAML fix committed; CI pending | [Run 45232960818](https://github.com/iamlxwis/CodeLab/actions/runs/45232960818) failed with zero jobs because JavaScript String.replace treated shell regex substring `$'` as a replacement metacharacter. Restored workflow from passing e85214f baseline and inserted all intended safety gates with literal callbacks; validated exactly one Publish step, one device gate step and one emulator block. |

| Nov 24, 2026, 2:52 AM PHT | Phase A / IDEA-011 & IDEA-022 | Source implemented; CI pending | Committed Language Manager installed/downloadable controls, downloadable Python status/progress/retry, persisted unmetered-download policy, installer cache storage and deletion, APK version and size, uninstall confirmation with device smoke coverage. Only signed Python APK is actually downloadable; JS/Lua remain bundled; native compiler packs unavailable. [Release CI](https://github.com/iamlxwis/CodeLab/actions/runs/45242235726), [device CI](https://github.com/iamlxwis/CodeLab/actions/runs/45242235734) still require passing result. Session STOPPED. |

| Nov 24, 2026, 2:56 AM PHT | Phase A / IDEA-022 | Source complete for first Python manager increment; new CI pending | ca7465f3: dedicated Downloads pane avoids pushing installed language list offscreen; default Installed with search switching to All. ed89942c: Android QA taps visible filters only. Current [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45243583986) and [device](https://github.com/iamlxwis/CodeLab/actions/runs/45243583725) checks pending; no all-language completion claim. Session STOPPED. |

| Nov 24, 2026, 3:01 AM PHT | Phase 1 / tracker | ACTIVE at committed checkpoint | Replaced ambiguous STOPPED header with explicit last-commit timestamp and separate code-work vs independently running GitHub Actions statuses; full downloadable pack work resumed. |

| Nov 24, 2026, 3:16 AM PHT | CI Option B / Phase 1 | CI source implemented, verification pending; session HANDOFF | ca9b0f51 source-only CI, manual API35 smoke, cancel-in-progress, manual publish gate; cc906c40 independent signed JS/Lua APK project source. [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45247033903) in progress. JS/Lua not yet downloader-routed. |

| Nov 24, 2026, 3:18 AM PHT | Option B / Phase 1 | HANDED OFF; CI queued | Fixed manual workflow_dispatch device release gate (a7c5b5af), corrected offscreen Android smoke assertion (bc9fc2b9). [Latest fast CI 45247505643](https://github.com/iamlxwis/CodeLab/actions/runs/45247505643) queued; previous runs cancelled as requested. Signed JS/Lua APK source exists but UI/routing incomplete. |

| Nov 24, 2026, 3:26 AM PHT | Phase 1 signed JS/Lua modules | Source integrated, QA/release pending; HANDED OFF | 9c722abe verified optional package discovery, per-pack download management, IPC routing and bundled fallback; b19db610 syntax fixes; de387a2b/c62c9522 API35 signed pack install + UI test. CI still needs passing result; no public JS/Lua release yet. |

| Nov 24, 2026, 3:46 AM PHT | Phase 1 / IDEA-001, IDEA-009 | In progress; source only | New signed Java **beta** companion source, allowlisted IPC and optional route; bundled fallback preserved. CI/release integration and full javac remain open. |

| Nov 24, 2026, 3:48 AM PHT | Phase 1 / IDEA-026 | Source integration in progress; CI pending | Added Java beta companion to CI build, signing, release APK, module catalog and Android15 smoke. This is not javac; no public APK confirmed. |

| Nov 24, 2026, 3:49 AM PHT | Phase 1 / IDEA-011, IDEA-026 | Source improved, CI pending | Fixed JS/Lua/Java optional download progress refresh, small-screen manager scrolling, and extended companion manifest security gate. Full Phase 1 not done. |

| Nov 24, 2026, 3:54 AM PHT | Phase 1 / IDEA-009, IDEA-026 | TypeScript source integrated; CI pending | New TS 5.9 WebJar/WebView/Rhino APK, .ts signed Run routing, manager and CI/release QA. Single-file transpilation only, no type checking or imports; not published or device accepted. |

| Nov 24, 2026, 4:01 AM PHT | Phase 1 / IDEA-001, IDEA-026 | SQLite source in progress, CI pending | Added optional SQLite companion based on Android platform SQLite, signed IPC/pack manager and CI/release wiring; preserves bundled engine. No extra compiler/device pass claimed. |

| Nov 24, 2026, 4:04 AM PHT | Phase 1 / IDEA-026, IDEA-028 | Source fix; CI rerun pending | Examined [Android APK CI 45254495018](https://github.com/iamlxwis/CodeLab/actions/runs/45254495018); exact failure was obsolete TypeScript-is-unavailable assertion at RuntimePackCatalogTest:15, not compilation. Updated test expectation and integrated optional SQLite Android-platform companion with signed CI/release. |

| Nov 24, 2026, 4:07 AM PHT | Phase 1 / IDEA-002, IDEA-011 | UI source changed; CI pending | Missing optional runtime error actions open exact Language Manager pack; Run-strip Languages opens active file pack. No silent source upload or installer bypass. |

| Nov 24, 2026, 4:08 AM PHT | Phase 1 / QA | Diagnostics added, rerun pending | [Device 45253146184](https://github.com/iamlxwis/CodeLab/actions/runs/45253146184) failed 1/27 Android instrumentation tests in RuntimeServiceTest infinite-loop/output-limit test. Added assertion diagnostics without weakening coverage; need exact next-result evidence. |

| Nov 24, 2026, 4:10 AM PHT | Phase 1 / runtime cancellation, IDEA-029 | Source verification pending | Main commit dcb1daed adds a guarded post-stop bind delay and explicit output-limit messages. Preserved alongside module navigation and strict Android instrumentation diagnostics. Device rerun required. |

| Nov 24, 4:13 AM PHT | Phase 1 / IDEA-011, IDEA-029 | Release discovery source, CI pending | Official GitHub release metadata verifies published APK names/states/size before Download is enabled; unsigned/unshipped URLs cannot trigger download. Cached verified installs remain possible; parser tests added. |

| Nov 24, 4:14 AM PHT | Phase 1 / IDEA-009, IDEA-026 | Feasibility audit complete, implementations pending | Added per-language proof/blocker matrix and Android 10+ W^X/14+ dynamic-loading limits; PHP/Ruby WASM research candidates. No unsupported runtime claimed shipped, unfinished Phase 1 remains open. |

| Nov 24, 4:17 AM PHT | Phase 1 / documentation | Source status corrected | Installation/implementation docs distinguish six source runtimes from published signed APKs and preserve language feasibility audit. CI/Android QA pending. |

| Nov 24, 2026, 4:20 AM PHT | Phase 1 / six-pack release checkpoint | Source built, API35 dispatched on new commit; CI pending | Fast six-pack source at dcb1daed [passed](https://github.com/iamlxwis/CodeLab/actions/runs/45256384445). New 34030469 CI pending; one-time phase1-checkpoint push will dispatch same-SHA Android 15 run then protected release only on success. No publication claimed. |

| Nov 24, 2026, 4:23 AM PHT | Phase 1 / CI | HTTP 429 blocked API35; retry source committed, new acceptance queued | [Fast 45257846511](https://github.com/iamlxwis/CodeLab/actions/runs/45257846511) PASSED. [API35 45258495504](https://github.com/iamlxwis/CodeLab/actions/runs/45258495504) failed pre-emulator resolving TypeScript WebJar from Maven Central HTTP 429; [checkpoint](https://github.com/iamlxwis/CodeLab/actions/runs/45258466450) correctly withheld release. Scoped 429-only Gradle retry with backoff added to both Android builds; checkpoint rerun triggered. |

| Nov 24, 4:38 AM PHT | Phase 1 / TypeScript module-manager crash | Real defect reproduced; fix source + JVM regression, CI pending | [API35 45259024641](https://github.com/iamlxwis/CodeLab/actions/runs/45259024641) failed in LanguageManager at DOWNLOAD MANAGER; inspected actual logcat FATAL NPE caused by `engine!!` when TypeScript optional runtime ready but no bundled implementation. Null-safe `LanguageRuntimeLabel` and regression tests committed; new gate queued. |

| Nov 24, 9:20 AM PHT | Phase 1 / TypeScript | Source fix + packaged-compiler regression added; CI pending | [Device 45261469481](https://github.com/iamlxwis/CodeLab/actions/runs/45261469481) failed TS console with TS5047 isolatedModules/CommonJS requirement. Verified exact compiler from APK under Node with CommonJS ES5. Updated in-app TS bridge, added APK asset execution test to both CI workflows, triggered same-SHA device checkpoint; no release claimed. |
