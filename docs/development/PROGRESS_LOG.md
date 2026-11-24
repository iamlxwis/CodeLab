# CodeLab — task progress audit

Short, append-only development checkpoints. The [README tracker](../../README.md#codelab-development-tracker) shows the current summary. **Every meaningful change to a task's status or implementation progress must be committed with an update to both this log and the README in the same commit.** Never mark a task complete without verifiable test/build evidence.

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

| Nov 24, 9:39 AM PHT | Phase 1 / TypeScript device acceptance | Core runtime verified on API35; test text mismatch corrected, full smoke rerun pending | [Device 45304317096](https://github.com/iamlxwis/CodeLab/actions/runs/45304317096) actually ran offline TS and exited 0, output `Hello from CodeLab!`. QA expected `Hello, CodeLab!`; fixed assertion and added real typed-file check plus JVM regression. [Fast 45304307573](https://github.com/iamlxwis/CodeLab/actions/runs/45304307573) PASSED. |

| Nov 24, 2026 | T07 / T08 / T03 / T09 | Source updated; verification pending | Owner froze language roster to six signed offline modules plus existing online mappings and delegated module-install/runtime acceptance to self. Narrow-screen Appearance cards stack; tutorial uses compact navigation, heading/progress semantics, accurate six-module guidance and JVM progress-label regression. Latest Android APK, API35 smoke and release checkpoint were successful before this change. No physical-device UI/accessibility or fresh Gradle proof claimed. |

| Nov 24, 2026 | Phase 2 easy/medium | Source committed; CI pending | Nearby-ranked file-local completions exclude quoted/comment tokens; rename dialog displays full occurrence count; diagnostic menu starts at cursor; collapsed Tab indentation preserves caret column. Added focused JVM tests. User owns physical-device acceptance, no emulator smoke requested. |

| Nov 24, 2026 | Phase 2 medium | Source committed; CI pending | Python indentation-aware read-only fold preview (no source editing), same-file definition lookup no longer capped by 120-symbol outline. JVM coverage for nested Python suites, comments/blanks and long symbol lists. No LSP or project-wide semantic support claimed. |

| Nov 24, 2026 | T07 / UX | Source committed; CI pending | Replaced crowded Language Manager downloads list with compact tappable language cards, clear Available/Installed/All filters, expandable downloads and online panels, and verified capability label logic with unit tests. Existing optional pack and Python detail flows retained. User handles phone testing. |

| Nov 24, 2026 | CI / UX | Source committed, CI pending | Eight separate workflow artifact entries consolidated to three: CodeLab-APK, CodeLab-Offline-Packs (six APKs and SHA256SUMS), CodeLab-CI-Reports. Added no-emulator workflow safety tests. Individual signed release APK files and same-commit API35 publication gate preserved. |

| 2026-11-24 2:06 PM PHT | Phase 2 easy/medium | 0/6 new batch verified | Introduced one replaceable README activity line; six remaining tasks scoped separately from earlier verified editor enhancements; physical testing remains with owner. |

| 2026-11-24 2:12 PM PHT | Phase 2 easy/medium | 0/6 verified; CI pending | Added lexical reference nearest-first listing, preview of affected rename lines with source-change guard, and recent-first open tab switcher. JVM regression added; user remains physical tester. |

| 2026-11-24 2:08 PM PHT | Phase 2 easy/medium | 0/6 verified; current source build pending | Member-style lexical completion now favors existing dotted identifiers; Python fold preview ignores triple-quoted pseudo-code and supports colon before comments. JVM regression tests added. Earlier checkpoint timestamp corrected at this commit; user does all phone testing. |

| 2026-11-24 2:15 PM PHT | Phase 2 easy/medium | 6/6 source checkpoints verified; owner acceptance pending | Exact-source Android APK workflow [45338205953](https://github.com/iamlxwis/CodeLab/actions/runs/45338205953) passed compiler, JVM unit tests, lint, signed APK packaging and artifact upload; handoff to owner physical testing. Historical 2:12 PM entry was premature; commit timestamps are authoritative. |

| Nov 24, 2026 · 5:16 PM PHT | Phase 3 / WS1 Project Management | STARTED · 0/7 verified | Startup tracker initialized on main, baseline 22d7dd5e77b4; Phase 3 audit and source CI pending. Previous activity archived in README history: Phase 2 handed off at 2:15 PM PHT. Remaining: all seven Phase 3 workstreams. The startup commit SHA is recorded by GitHub history and will be referenced in next checkpoint. |

| Nov 24, 2026 · 5:19 PM PHT | Phase 3 / WS1, WS2 | INSPECTED / IN PROGRESS; 0/7 verified | Inspected remote main: prepared Phase 3 batch was not pushed; existing templates, favorites, scoped search, Git core and ZIP safeguards preserved. Added open-time recent project ordering and selective staged-path unstaging (including unborn HEAD), UI control, two project/Git test cases. CI pending; all seven workstreams remain unfinished. Previous commit 2badb03401b6. |

| Nov 24, 2026 · 5:21 PM PHT | Phase 3 / WS3–WS7 | IN PROGRESS; 0/7 verified | GitHub synchronization now records origin branch tracking after successful non-force push or fast-forward pull and masks lower-level transport failures. Added staged/published transaction for bulk file imports, restricted ZIP .git/.codelab injection, expanded command palette and keyboard shortcut search, ignored stale Compose project metadata callbacks. Added JVM regressions for failed imports, untrusted ZIP paths, Git tracking and command discovery. Previous commit 9f0fe2313b60; new source CI pending. |

| Nov 24, 2026, 5:24 PM PHT | Phase 3 / WS5 Productivity | IN PROGRESS; 0/7 verified | Extended existing editor snippet toolbar with searchable built-in snippets, selected-text-only local snippet saving, 24-item/4096-char limits, explicit deletion, safe bounded persistence codec and 3 JVM regression tests. Existing built-ins untouched. Prior code ae4ad81291b9; current CI pending. |

| Nov 24, 2026 · 5:26 PM PHT | Phase 3 / WS6 Performance | IN PROGRESS; 0/7 verified | Removed quadratic project-switcher indexOf sorting, preserving stable favorite-first recent order with O(n) partition and Compose remember. Scans project search lines by offsets rather than allocating split arrays per file; retains 200-hit cap. Added ordering and line-number JVM regressions. Performance change is structural, not a measured speedup. Prior commit 886d464d8fd4; new source CI pending. |

| Nov 24, 2026 · 5:27 PM PHT | Phase 3 / all 7 workstreams | CI RUNNING; 0/7 accepted yet | Verified source checkpoint c0fd4d7f841a exists on remote main; [Android APK CI 45365272240](https://github.com/iamlxwis/CodeLab/actions/runs/45365272240) state in_progress/none when checked. Audit/source review underway, no physical device test. Next: inspect exact-source build/test result, fix failures or record verified scope. |

| Nov 24, 2026 · 5:32 PM PHT | Phase 3 / WS2 Git | CI FAILED → FIXING; 0/7 accepted | Exact source c0fd4d7f841a [Android APK run 45365272240](https://github.com/iamlxwis/CodeLab/actions/runs/45365272240) completed 174 JVM tests, 1 failed: `GitWorkspaceTest.unstagingOnUnbornBranchPreservesWorkingFilesAndOtherStagedPaths` at test line 85 with JGitInternalException/EOFException due to committing an unwritten index. Replaced editor.finish + bare index.commit with editor.commit, which writes and publishes index atomically; new source CI pending. All other build/test stages not declared passed. |

| Nov 24, 2026 · 5:37 PM PHT | Phase 3 / WS1–WS7 | COMPLETED — 7/7 source/CI verified; physical acceptance pending | [Exact source 55a65774d56e](https://github.com/iamlxwis/CodeLab/commit/55a65774d56e949dd06ca1eb1db306026983ed0b) passed [Android APK run 45366121115](https://github.com/iamlxwis/CodeLab/actions/runs/45366121115); build, tests, lint, signature checks, TypeScript packaging and all three named artifacts succeeded. Prior failed CI 45365272240 was fixed in GitWorkspace `editor.commit()`; latest run passed. Existing offline packs and CI Option B preserved; no emulator run. Next owner phone QA and optional live GitHub/network checks. |


### CodeLab — Reimagined (source checkpoint audit)

| When (PHT; commit metadata) | Checkpoint | State | Evidence |
| --- | --- | --- | --- |
| 24 Nov 2026, 18:31:58 PHT | docs: start CodeLab Reimagined live tracker | Source committed; acceptance pending | [0b2bfb2](https://github.com/iamlxwis/CodeLab/commit/0b2bfb2ec0e3c29b96840cb96665243710eaa1ca) |
| 24 Nov 2026, 18:32:51 PHT | feat: introduce Reimagined visual presets and compact project hierarchy | Source committed; acceptance pending | [e236a56](https://github.com/iamlxwis/CodeLab/commit/e236a56e808f8610723cd46d9c93022d95a2d168) |
| 24 Nov 2026, 18:34:00 PHT | feat: persist Theme Studio materials, preview real editor and transfer presets | Source committed; acceptance pending | [e8a3051](https://github.com/iamlxwis/CodeLab/commit/e8a30514d18652adfc3821550a8cbde6b93114a8) |
| 24 Nov 2026, 18:35:00 PHT | feat: refine editor gutter, project setup, run controls and output panels | Source committed; acceptance pending | [4c9f1de](https://github.com/iamlxwis/CodeLab/commit/4c9f1dedb2b1a555ad1346f0fa00b034fba61332) |
| 24 Nov 2026, 18:37:04 PHT | feat: brand CodeLab Reimagined, bundle licensed fonts and expand Theme Studio | Source committed; acceptance pending | [b525a73](https://github.com/iamlxwis/CodeLab/commit/b525a73ed1001162e5157b47694d7ea4700f781e) |
| 24 Nov 2026, 18:39:07 PHT | feat: personalize workspace layouts, organize settings and add adaptive rendering | Source committed; acceptance pending | [6219843](https://github.com/iamlxwis/CodeLab/commit/6219843c1b326524c67d8d5225f71cb0bbc180d6) |
| 24 Nov 2026, 18:42:42 PHT | Preserve uncapped custom themes and validate font and color updates | Source committed; acceptance pending | [9a5f0c7](https://github.com/iamlxwis/CodeLab/commit/9a5f0c7689928ec3aec6a5ba4f85827df358ed48) |
| 24 Nov 2026, 18:43:08 PHT | Add user-controlled animation speed and spring response with reduced-motion support | Source committed; acceptance pending | [cac8108](https://github.com/iamlxwis/CodeLab/commit/cac81084326e3000359536f8238b9a9976f8cae0) |
| 24 Nov 2026, 18:43:35 PHT | Refine language badges, adaptive launcher and project import destinations | Source committed; acceptance pending | [95a4acb](https://github.com/iamlxwis/CodeLab/commit/95a4acb1fe1f6e52f4b803018cc486501d67fc2c) |
| 24 Nov 2026, 18:44:34 PHT | Present expandable file tree and add hierarchy and gutter regression tests | Source committed; acceptance pending | [f23d62f](https://github.com/iamlxwis/CodeLab/commit/f23d62ff5388f48898db625030a81f018b80b553) |
| 24 Nov 2026, 18:45 PHT | Acceptance ledger | Partial; build pending; 0/10 accepted | [Exact source CI](https://github.com/iamlxwis/CodeLab/actions/runs/45377658562) · [Remaining work](REIMAGINED.md) |

| Nov 24, 2026, 6:52:31 PM PHT | Build recovery | In progress | CI 45377658562 failed debug/release Kotlin compilation: undefined duration in ProjectLibraryScreen.kt; restored motion-derived duration. Rebuild pending. |

| Nov 24, 2026, 6:54:55 PM PHT | Theme Studio component fidelity | Source | Independent component color persistence, editor selection and workspace preview; CI pending. |

| Nov 24, 2026, 6:56:18 PM PHT | Workspace/editor polish | Source; tests added | Toolbar order/pin settings, motion, template choice, console panel states, project layout rename and notification styling. |

| Nov 24, 2026, 6:58:26 PM PHT | Regression compatibility | Source | Legacy appearance migration and current-device smoke selectors for six presets, signed runtimes, and explicit online/offline changes. Tests not yet executed. |

| Nov 24, 2026, 6:59:18 PM PHT | Build recovery | In progress | CI 45379503511 failed debug/release compile due to missing dp import in WorkspaceEditorPane.kt; import restored. |

| Nov 24, 2026, 7:01:05 PM PHT | Editor/import/onboarding polish | Source | Incremental document index and tests, keyboard-aware shortcuts, import metadata review and conflict guard, resume guide controls. CI pending. |

| Nov 24, 2026, 7:03:58 PM PHT | Navigation and visual refinement | Source | Shared search, independent favorites/sort, compact project choices, explicit execution segment, startup motion and glass highlight. CI pending. |

| Nov 24, 2026, 7:04:50 PM PHT | Theme Studio finalization | Source | Categorized controls, component preview, bounded theme file transfer, independent font revisions and compatible glass shape type. CI pending. |

| Nov 24, 2026, 7:06:02 PM PHT | Cumulative build verification | In progress | [CI 45380854708](https://github.com/iamlxwis/CodeLab/actions/runs/45380854708) at e9be726; source hold to allow a definitive result, no runtime or CI workflow changes. |

| Nov 24, 2026, 7:08:54 PM PHT | Editor indexing and explorer | Local Kotlin PASS | Exact blob hashes 58ea9c4445907fb55cd6df7a7257767c256532be and d706754a8264eb8fce72f8596bc0d11f1174aa86; 10,000 seeded random edits, every line start and offset checked against fresh index, plus 3 explorer scenarios. Android CI pending. |

| Nov 24, 2026, 7:11:47 PM PHT | Cumulative build recovery | CI PASS; device pending | [CI 45380854708](https://github.com/iamlxwis/CodeLab/actions/runs/45380854708) completed successfully for e9be726ff7f11de2f337910e0df37283c734c325; compilation, JVM tests, lint, security/artifact gates, packaged TS and APK signature verification succeeded. [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45380854708/artifacts/14912949223) · [runtime packs](https://github.com/iamlxwis/CodeLab/actions/runs/45380854708/artifacts/14915170119) · [reports](https://github.com/iamlxwis/CodeLab/actions/runs/45380854708/artifacts/14914990293). Release publication and device acceptance not run. |

| Nov 24, 2026, 7:16:18 PM PHT | Resume authorized | In progress | Prior build passed; remaining theme/workspace fidelity and Android device validation. |

| Nov 24, 2026, 7:17:59 PM PHT | Workspace and theme fidelity | Source committed; CI pending | Layout toolbar/explorer persistence, legacy compatibility tests, interactive preview using chosen editor font and component colors. |

| Nov 24, 2026, 7:19:22 PM PHT | Contrast and adaptive panels | Source committed; CI pending | Theme foreground/primary contrast, console text, system bar icons, tablet divider and maximize, accessible panel range actions, instrumentation assertions. |

| Nov 24, 2026, 7:21:33 PM PHT | Saved theme motion and preview | Source committed; CI pending | Motion serialization, bounded import, explicit Reduced preference guard, syntax sample and device smoke selector coverage. |

| Nov 24, 2026, 7:22:49 PM PHT | Cumulative fidelity CI | in_progress; device pending | [CI 45383476477](https://github.com/iamlxwis/CodeLab/actions/runs/45383476477) at 52fbd6f; prior verified build e9be726. Next: CI fixes, manual device smoke, remaining import/export and visual acceptance. |

| Nov 24, 2026, 7:25:56 PM PHT | Files-style import/export | Source; local preview logic PASS | Seven local Kotlin preview checks (ZIP expansion/entry limits, bounded text, binary and UTF-16); read-only preview, existing import validator retained, folder/export review and transfer feedback. Android integration pending. |

| Nov 24, 2026, 7:29:07 PM PHT | Workspace/theme cumulative CI and Files-style source | Prior CI PASS; latest CI pending | [CI 45383476477](https://github.com/iamlxwis/CodeLab/actions/runs/45383476477) passed; [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45383476477/artifacts/14914833531). Published 6e33876 after build completion, exact preview blob passed 11 local checks; [next CI](https://github.com/iamlxwis/CodeLab/actions/runs/45384534494) running. |

| Nov 24, 2026, 7:37:08 PM PHT | Final quality authorization | CI PASS; device pending | Nov 24, 2026, 7:37:08 PM PHT — Owner authorized final quality work and requires explicit next-to-build at every checkpoint. Exact source 6e33876f41b38249aa041ef48cef651688489dea passed [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45384534494); APK, packs and reports available. Next: manual API35 smoke and evidence-driven fixes. Connector cannot dispatch manual workflows; no device acceptance claimed. |

| Nov 24, 2026, 7:42:12 PM PHT | Editor and interaction polish | Source committed; cumulative CI pending | Indexed navigation and selection, accessible dirty tabs, pinch cancellation, empty-line editing, animated filtering, keyboard-aware chrome, new regression tests. Prior APK still last verified. |

| Nov 24, 2026, 7:47:09 PM PHT | Decision 34 — application-wide polish | Source committed; CI pending | Expandable sheets, actionable empty states, animated filtering, themed console notifications. Scope limited to the original 37 decisions. |


## Full specification resumed — Nov 24, 2026, 8:00:34 PM PHT

Owner supplied all 37 confirmed decisions in this session. Prior missing-label caveat is obsolete. Latest implementation 7304cbf passed Android CI 45387376191; screenshots, API35 execution and physical acceptance remain unverified. Source audit confirms settings currently has only eight mixed destinations rather than the nine approved groups. Next: implement navigable settings subpages and motion presets, then remaining gaps and exact-source CI. No runtime, signing or CI policy changes authorized beyond the approved design specification.


## Settings, motion and live preferences — Nov 24, 2026, 8:05:54 PM PHT

Source implementation: nine approved Settings destinations, searchable navigation and dedicated motion/editor/keyboard/storage/privacy/help pages; Theme Studio direct entry; live preference listener updates mounted editors without losing buffers; Reduced/Standard/Fluid presets and persistent None/Fade/Slide editor/onboarding transitions, also retained in theme JSON with legacy defaults. Added motion codec regression and settings instrumentation coverage. Android build/tests pending; local Gradle bootstrap failed with Network is unreachable. Latest verified Android source remains 7304cbf (CI 45387376191).

Device blocker: connector has no workflow-dispatch operation, gh is unauthenticated, and the fallback GitHub browser is signed out. No workflow configuration or release safeguards changed. Next: inspect exact-source CI, fix failures, complete language icon/onboarding gaps and obtain API35 visual/runtime/performance evidence. Physical acceptance remains with owner.


## Interactive lesson, logos, layouts and console — Nov 24, 2026, 8:13:24 PM PHT

Verified preceding checkpoint bb5172d7957f1584a39cf4679e893f4b6d6e60f1 passed Android CI 45390387550 (release, unit tests, instrumentation compilation, lint, security/signature gates). APK artifact 14919245609; offline packs 14918028788; reports 14917738844. No emulator or physical acceptance.

New source: optional practice lesson uses the real CodeEditor and existing RunSession isolated/offline architecture; records success only from actual expected output, stops its run on dismissal and returns to the same guide topic. Replaced letter badges with 19 licensed Android language-logo vectors (Simple Icons pinned d59bb32d4f94b1aa8c7cef9d264280d35d5f8f97 CC0, Devicon Java MIT). Added offline license reader from Help. Focus mode now participates in named/per-project layouts, with legacy nullable compatibility. JavaScript and TypeScript warnings now have an explicit Warning prefix distinct from errors in all three existing engines; console supports error/warning/text filters and readable severity colors while preserving cross-line ANSI formatting and full raw copy/export.

Local checks: static security gate and eight Python release/artifact checks passed; all 19 new vector XML files parsed. Added focused JVM and Android regressions; new Android build not yet verified. Runtime architecture, dependencies, workflows, production signer and six existing pack capabilities unchanged. Next: exact-source CI and fixes; manual API35 source-matched screenshots, gestures, persistence, all runtime/file operations, accessibility and measured performance remain outstanding. Cannot dispatch device workflow through current tools/authentication.


## Theme operations, exact palette and adaptive effects — Nov 24, 2026, 8:22:14 PM PHT

Prior cumulative source f2990a971982f54f14568c1ce65a10186c291e5c passed Android CI 45391600449, including release builds, JVM tests, instrumentation Kotlin compilation, lint and signature checks. This validates lesson/logo/layout/console source compilation and unit tests, not execution of the new instrumentation or device fidelity.

New source audit fixes: explicit saved-theme Rename and Duplicate operations preserve every recipe field and use a new ID; compact anchored theme menus replace crowded icons. Theme Studio now opens at its customization section. Crystal/Sandstone base colors corrected to exact approved #252932/#EAE3D7; neutral backdrop uses the actual theme base and removes decorative Sandstone lines. Glass border zero now removes the border rather than creating Android's zero-width hairline. Auto blur respects battery saver live, without polling. None transition preference now reaches all custom workspace transitions; persistent search semantics and labelled accessibility switches added. Full owner contract preserved in REIMAGINED_SPEC.md. Focused codec/policy/instrumentation regressions added; current batch Android verification pending.

Next to build: repair any exact-source CI failure; then outstanding device-driven screen/motion/accessibility corrections. API35 smoke/screenshots, live lesson execution, all-eight gesture conflict testing, real theme/font/import/export persistence, runtime acceptance and measured startup/typing/frame/memory evidence remain outstanding. Manual device dispatch still blocked by tool/auth availability. No workflow, dependency, runtime capability or release gate changes in this batch.


Nov 24, 2026, 8:24:37 PM PHT — Exact source 2b04bd19f0e17f697baf325ff1a08b8a51e27b0f is under CI 45392971931; last verified f2990a9. Consolidated current PROJECT_MEMORY and preserved prior text in PROJECT_MEMORY_ARCHIVE so new sessions do not treat obsolete pending-build statements as current. Full specification and 37-row audit are committed. Next: exact-source CI result/fixes, then manual API35 fidelity and functional acceptance.


## Verified theme batch and runtime selector correction — Nov 24, 2026, 8:30:42 PM PHT

Exact source 2b04bd19f0e17f697baf325ff1a08b8a51e27b0f passed [Android CI 45392971931](https://github.com/iamlxwis/CodeLab/actions/runs/45392971931); [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45392971931/artifacts/14919328907), six packs 14920606010 and reports 14920615908. This verifies compilation/JVM tests/lint/signatures, not emulator instrumentation execution.

Further source audit found the run-target picker supplied only Python to its installed-module set, hiding verified TypeScript under Runnable only. New shared InstalledRuntimeState checks existing signature/protocol validators, refreshes on lifecycle resume rather than on every keystroke, and supplies all verified module extensions to the picker and editor selector. Guided setup's status and Ready offline filter now use actual installed/bundled availability; missing TypeScript is no longer always labelled planned. Ordinary run labels use language names rather than internal JS/Lua engine brands. Added selected-state semantics for execution/language controls, JVM trust-state coverage and installed-TypeScript picker instrumentation regression. No automatic mode switching, uploads, downloader or runtime architecture changes.

Local static security, eight release/artifact Python tests and diff checks passed. This correction awaits its own Android CI. Next: fix any cumulative CI failure; manual API35 screenshots, gestures, runtime/file/persistence/TalkBack and measured-performance validation. Dispatch remains unavailable with current connection/authentication.


Nov 24, 2026, 8:33:19 PM PHT — Runtime selector source 97e31ad2668de8cb127c7c1bc9a171d16cd7e013 is under CI 45394263948 (in_progress); prior 2b04bd1 passed CI 45392971931. Source tree is clean, workflows/dependencies unchanged. No current-source manual API35 run found; next compile/test result and device acceptance.


## Verified runtime correction and large-theme-list rendering — Nov 24, 2026, 8:36:53 PM PHT

Exact source 97e31ad2668de8cb127c7c1bc9a171d16cd7e013 passed [CI 45394263948](https://github.com/iamlxwis/CodeLab/actions/runs/45394263948); [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45394263948/artifacts/14920642082), packs 14920337964, reports 14920756656. Compilation, JVM tests, instrumentation compilation, lint, packaged TypeScript and signatures passed. Device execution remains unverified.

New source: the gallery previously composed every saved theme inside one LazyColumn item. Saved themes now use individual lazy rows keyed by persistent recipe IDs, preserving all contextual operations and the unlimited saved-theme collection. This structurally bounds composed off-screen rows; no measured device performance improvement is claimed. Source build pending. Next: definitive cumulative CI and APK artifacts; manual API35 visual/gesture/runtime/persistence/accessibility/performance acceptance remains required. No new manual run observed; user was asked to start existing workflow because the connector cannot dispatch it.
