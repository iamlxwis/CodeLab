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
