## Nov 24, 2026, 4:04 AM PHT — TypeScript test mismatch repair and SQLite source checkpoint

TypeScript source at 89b3ed700 passed source compilation/most tests but [Android APK CI 45254495018](https://github.com/iamlxwis/CodeLab/actions/runs/45254495018) failed **RuntimePackCatalogTest:15** because it still expected TypeScript not downloadable while new module catalog includes it. Updated the negative set to C++/Rust, preserving catalog verification. Added separately signed SQLite companion source using Android platform SQLite with package discovery/manager/CI/release and built-in SQL fallback. No TypeScript or SQLite device validation/publishing yet. Latest CI must run again. Session WORKING at checkpoint.

## Nov 24, 2026, 4:01 AM PHT — optional SQLite module source (unverified)

Added engine-sql companion using Android SQLite in own signed package/UID, code-only Messenger interface, exact package verification and runtime routing, module manager downloads and CI/release/signing setup. Retains built-in SQLite for existing users. SQLite is supplied by Android platform (not downloaded native sqlite binary). TypeScript CI pending; SQL requires build and device acceptance. No APK release claimed. Session WORKING at source checkpoint.

## Nov 24, 2026, 3:54 AM PHT — Phase 1 TypeScript optional offline engine integration

Unverified full-source implementation on top of prior Java beta: independent engine-ts signed APK using official TypeScript 5.9.3 npm WebJar copied into assets, offline network-disabled WebView single-file transpileModule to ES5, then Rhino execution over the existing bounded Messenger protocol; .ts editor run routing, signed module discovery/download/install/uninstall UI, Android package visibility, CI build/certificate/checksum/release assets, Android15 smoke test with real TypeScript starter and airplane mode, capability tests/security scans. **This is transpile-only, no TS semantic type checking/import resolution; unsupported APIs remain unavailable.** Not yet build tested/published. User handles physical device tests. Preserve all prior fallback engines and permanent signer. Other language packs pending. Session WORKING at source checkpoint.

## Nov 24, 2026, 3:49 AM PHT — Phase 1 live module status and security

Code source update: optional download progress is polled while active and refreshed from manager begin/cancel/cache actions; Java beta listed as bundled fallback, module dialogs scroll on phones. Security gate now scans every companion source and asserts signature-protected binding, no INTERNET permission and no backup for Python/JS/Lua/Java. Source CI and real network installation tests pending. User handles phone acceptance, not CI/build/security. Java beta is not javac; TypeScript/native and other compiler runtimes not shipped. Session WORKING at checkpoint.

## Nov 24, 2026, 3:48 AM PHT — Phase 1 Java beta build wiring

Added :engine-java release/debug CI builds, signer checks, Java beta optional module catalog and Android 15 package/UI smoke assertions. Core bundled Java fallback is retained; beta is BeanShell rather than javac. CI and public release still unverified. Next: fix optional transfer-status polling, harden security gate, inspect new source CI and run device workflow. Session WORKING at checkpoint.

## Nov 24, 2026, 3:46 AM PHT — Phase 1 Java beta implementation checkpoint (source only)

User authorized broad Phase 1 work before Phase 2 and will handle final physical-phone acceptance. Created engine-java signed optional companion source using the existing BeanShell Java **beta** interpreter; signed IPC, runtime package allowlist, release download URL, app package visibility and RunSession routing with bundled Java fallback. This is **not javac**, and is not yet published or CI-tested. Next: wire Gradle and release workflow, static security scan, emulator QA, module dialog refresh, verify build and publish. Other full language compilers remain pending. Session WORKING at this source checkpoint.

## Nov 24, 2026, 3:26 AM PHT — Phase 1 integration handoff

User explicitly requested finishing Phase 1 and proceeding to Phase 2. Committed 9c722abe: OptionalRuntimeLocator allowlists JS/Lua exact package IDs and signature-protected service metadata, OptionalRuntimeDownload implements per-pack Android DownloadManager, persisted transfer state, APK signing/package/version verification, Android installer/uninstaller, cache cleanup, Wi-Fi policy. RunSession routes JS/Lua to verified signed companions when installed, retains bundled fallback. LanguageManager shows both in Downloadable/Downloads with install/reinstall/remove controls. b19db610 fixed source syntax. de387a2b and c62c9522 extend API35 QA to install both debug companion APKs and check verified module dialogs. Existing projects and bundled engines preserved. **Do not mark finished**: GitHub Actions fast build still pending, no manual API35 smoke passed for this implementation, no new public JS/Lua APK release, no actual network download test, no TypeScript/native compiler toolchains. Only Python is currently a known public runtime asset. Smart CI remains manual device gate and cancels superseded runs. Check latest [Android APK runs](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml); then diagnose actual build errors, manual device.yml and release publication. README checkpoint timestamp is not a heartbeat. Assistant HANDOFF.

## Nov 24, 2026, 3:18 AM PHT — end-of-turn handoff

CI Option B code implemented: ca9b0f51 and a7c5b5af (release gate now checks same-commit workflow_dispatch device run, not obsolete push event). API35-only manual device workflow. Docs-only commits skip Android CI. User explicitly requests cancel-in-progress, so intermediate source runs may be cancelled; do not claim they passed. Latest source bc9fc2b9 fixes fragile emulator smoke assertion waiting for offscreen JavaScript row. Current fast run [45247505643](https://github.com/iamlxwis/CodeLab/actions/runs/45247505643) queued at checkpoint; verify after completion. JS/Lua companion APK sources cc906c40 are not yet connected to in-app manager or editor execution, so Phase 1 is NOT done; do not proceed to Phase 2 as if finished. Need integrate signed companion discovery/download and safe execution route, and manually dispatch API35 smoke before publishing. No assistant background work after handoff.

## Nov 24, 2026, 3:16 AM PHT — Option B Smart CI and JavaScript/Lua APK packaging

Direct main commits: ca9b0f51 switches .github/workflows/android.yml to source-only paths (docs-only skipped), removes auto publication and expensive emulators from normal pushes, keeps compile/unit/lint/security/signature APK artifacts. Manual publish_release=true dispatch gates release on same-commit device smoke. .github/workflows/device.yml is manual workflow_dispatch only, API35 only, cancel-in-progress true. Release adds JS/Lua APK artifacts and signing/upgrade checks. Prior cc906c40 adds independent Android APK source for JS and Lua, with dedicated signature-permission-protected services; their downloader/UI and execution routing are NOT yet implemented, so do not claim installable working module UX. Core JS/Lua stay bundled.

CI run 45247033903 is in progress at handoff; device smoke old run 45244890612 failed due to UI assertion wait_for JavaScript after filter selection, not due to emulator failure. Need fix test/UX and run API35 manual checkpoint. Only Python downloadable in the actual editor.

## Nov 24, 2026, 3:01 AM PHT — Phase 1 resumed; tracker transparency fix

Explicit user request: fix confusing README stopped state (2:52 timestamp while another interactive turn continued), and complete remaining downloadable runtime scope. Implemented new checkpoint semantics: last source update, assistant state at that source update, separate CI links, explicit warning timestamps are not live. User prioritizes Phase 1 before Phase 2. Resume building real packs; preserve working core until replacements tested. Session ACTIVE at last checkpoint; final interaction requires HANDED OFF state and accurate remaining.

## Nov 24, 2026, 2:56 AM PHT — UI refinement and final new CI gate; session STOPPED

The initial runtime-manager implementation was refined in ca7465f3 to separate **Downloads** progress/storage controls from **Installed** and **Downloadable** lists; default Installed shows engines immediately rather than hiding first result beneath a tall status panel. Search automatically switches to All. ed89942c updates Android smoke to use the auto-expanding search rather than tapping an off-screen All filter chip. Existing code, user project data, Python APK, signing configuration and bundled engines preserved. Source from d13de118 had passed Gradle APK assembly on API33/35, JVM compilation, release lint and signing checks, but its emulator acceptance was interrupted by subsequent commits, so NOT a verified release.

Current code SHA ed89942c. [APK run 45243583986](https://github.com/iamlxwis/CodeLab/actions/runs/45243583986) and [device run 45243583725](https://github.com/iamlxwis/CodeLab/actions/runs/45243583725) pending at handoff. The latest public release can still be older than this source. Stop further CI-triggering commits until these runs finish; diagnose actual failures from logs/evidence if any. Python is the ONLY optional downloadable pack, other engines are NOT fully modular; IDEA-001/009/011/022 remain open. The full user-requested Phase A cannot be marked complete.

## Nov 24, 2026, 2:52 AM PHT — Phase A Language Manager; session STOPPED

User prioritized genuinely installable optional offline runtimes and an in-app download manager, and asked to finish this phase before editor Phase B. Implemented and pushed direct to main: 7636fda7 (Installed/Downloadable views, in-app module transfer section, safe cache clearing and uninstall confirmation, capability test); 30e9ab56 (fix Compose syntax and align UI smoke selectors); 0059a8ba (Wi-Fi/unmetered Android DownloadManager transfer policy, installed Python package APK size + version code, retry text and UI smoke assertions); d13de118 (Android device cancellation confirmation test, first-run QA taps synchronized between tutorial stages). Previous release Python installer is real: asset CodeLab-Python.apk ~36.5 MB. All downloaded executable packages must continue matching editor signing key and installed package identity. No project or runtime assets deleted.

**Verification:** [release CI 45242235726](https://github.com/iamlxwis/CodeLab/actions/runs/45242235726) and [device CI 45242235734](https://github.com/iamlxwis/CodeLab/actions/runs/45242235734) pending at handoff. Previous source CI 45237884590 failed in signed-upgrade tutorial test waiting for Next, though that source's API33/35 device smoke passed; the brittle test was revised. Never claim new APK shipped or full modular support based on source commits alone. **Only Python is a removable/downloadable optional pack.** JavaScript/Lua/Java beta/SQL remain built into core. TypeScript/C/C++/Rust and other toolchains are NOT available for download. Need fully verified optional JS/Lua migration, real Android network download/install/uninstall QA, and further compiler/toolchain feasibility/packaging. Next phase B editor work may be planned, but Phase A remains incomplete against the full user-approved all-engines-modular acceptance criteria.

## Nov 24, 2026 · 2:29 AM PHT — feature source compiles; device acceptance pending

[Feature APK CI 45237884590](https://github.com/iamlxwis/CodeLab/actions/runs/45237884590) advanced past release build, JVM unit tests, lint, quality reports, APK and Python signer checks and signing compatibility with all those steps marked SUCCESS; Android 13 signed-upgrade emulator acceptance is still running. [Device CI 45237884296](https://github.com/iamlxwis/CodeLab/actions/runs/45237884296) passed Gradle assembly for both API33/API35 and entered emulator runner; full smoke not yet completed. README's one live activity row updated to distinguish successful build from unverified device results. Session STOPPED. Import-only cleanup was committed with [skip ci] and not separately compiled; no runtime logic changed.

## Nov 24, 2026 · 2:27 AM PHT — feature delivery, source cleanup and handoff

User directed actual CodeLab features and safe cleanup over repeated CI work. Direct main commits in this turn: 63d6acf3 HTTPS GET/POST/PUT/DELETE request builder with bounded headers/body and validation tests; e088840a fixed user-triggered Rename/Replace All not notifying autosave and added Android regression test; 50357909 interactive per-hunk Git conflict-marker resolver with tests; a93eaa56 fixed previously broken escaped-quote JVM fixture; 0ddba088 bounded Rhino JS local-variable snapshot at debugger pauses and console UI; 66ba33aa read-only, non-destructive brace-fold preview with jump-to-editor; 75c1c051 linear-time structural diagnostics and language-aware comment handling for rename/navigation, with JVM tests. A source import audit found redundant imports in Editor, SourceView, CommandPanels and RunConsole; removed only statically unreferenced explicit imports, no production files or engines deleted. Tracked repository tree contains 205 blobs and no obvious committed APK/build cache/temp junk. This documentation/cleanup commit uses [skip ci] to avoid cancelling the already-running feature-source verification.

At handoff: [APK CI 45237884590](https://github.com/iamlxwis/CodeLab/actions/runs/45237884590) and [device CI 45237884296](https://github.com/iamlxwis/CodeLab/actions/runs/45237884296) were in progress, NOT passed. Previously a JVM compilation error in a quote assertion at f9ac616 was found from CI and corrected in a93eaa56. Do not claim full implementation, successful build or release of the new feature set until verified. Remaining: real downloadable engines beyond Python, LSP, true editable inline folding and multi-cursor, PTY, multi-language debugger, screen-anchored tutorial, device and accessibility acceptance. README tracker has one activity row and is STOPPED at handoff. No background work continues after response.

## Nov 24, 2026 · 2:11 AM PHT — product work: uncapped safe lexical rename

User redirected effort toward real app features, README tracker accuracy and conservative cleanup. Reviewed editor CodeAnalysis and discovered lexicalRename called references(..., 500), silently leaving identifiers unchanged in files with more than 500 occurrences. Implemented uncapped streaming replacement of visible lexical matches, retaining identifier validation and exclusion of comments/strings, and added regression tests for 751 occurrences and partial identifiers. Existing display-only reference cap remains for UI responsiveness. README now contains one latest activity, marked STOPPED at session handoff. No unverified runtime engine or PTY claim; no speculative deletion of files. Tests still need independent execution. Future priority: real offline engines, LSP/folding/multi-cursor, terminal/debugger, API tooling and tutorial. No background work promised.

## Nov 24, 2026 · 2:01 AM PHT — invalid workflow root cause and fix

The previous release hardening source commit b913936d failed before build jobs because JS String.replace interpreted shell-quoted string `$'` as a special suffix replacement and inserted duplicated YAML tail into android.yml. Reconstructed from successful previous commit e85214f with callback replacements to preserve literal `$'` and other shell `$` characters; retained new publication gate, production-key protection and gate unit tests. Confirmed one release publish step, one release gate and no duplicated emulator sections by source inspection. Full CI still needs independent verification. The prior public APK e85214f remains installable/verified. Do not claim entire release ready; 3/9 milestones done; 6 remain.

## Nov 24, 2026 · 1:54 AM PHT — next verified checkpoint and hardened release pipeline

At source e85214f, [APK CI 45229584184](https://github.com/iamlxwis/CodeLab/actions/runs/45229584184) and [device CI 45229584342](https://github.com/iamlxwis/CodeLab/actions/runs/45229584342) both SUCCESS for Android 13/API33 and Android 15/API35. Actual production-signed update and persisted project/theme/Python acceptance remained passing. New code commits: require that a **same SHA, push-triggered, full device workflow** succeeds before latest APK publication (fail closed on missing, incomplete, failed, canceled or timed-out runs), with Python unit tests covering independent retry/commit matching. Release CI adds actions: read, tests gate, and refuses a new production certificate if the prior release used production signing. Python installer checks signer of already installed companion and prompts removal on mismatch (no silent deletion). ProjectStore checks null/oversized edit bytes before history snapshot; JVM test prevents phantom history on rejected saves. New CI not yet proven. README has ONE replaced current activity row; keep history in docs and commits. 3/9 done, 6 remaining, T03/T05/T07/T08/T09 active; T06 queued. Must mark session STOPPED before final response.

## Nov 24, 2026 · 1:36 AM PHT — interactive work resumed; single-entry tracker and instrumentation compiler fix

User directed continuous implementation during this active session, no additional discussion. Replaced README's long accumulating action list with ONE overwritten current activity row and separate session indicator; prior history is kept in Git and docs. Release CI 45223423186 passed, but device CI 45223423018 failed on both APIs at AppearanceAccentAccessibilityTest.kt:5:33 Unresolved reference assertExists. assertExists() is an interaction member; invalid top-level import removed without weakening assertion. Added compileDebugAndroidTestKotlin task to release workflow before production publishing; source not yet reverified until CI. 3/9 complete, 6 remaining, T03/T05/T07/T08 active; physical device upgrades and real module download/install remain unverified. Final interactive reply MUST be preceded by STOPPED README handoff commit; never imply background agent execution.

## Nov 24, 2026 · 1:02 AM PHT — signed-upgrade Clay selection QA drift, not lost preference

[APK workflow 45220967890](https://github.com/iamlxwis/CodeLab/actions/runs/45220967890) reached API33 actual production signed upgrade and failed at `verify_clay` **after upgrading** because old QA expected `content-desc="Selected"` on a decorative icon. Previous T07 accessibility work removed that generic spoken icon description. Downloaded failing screenshot from artifact 14840334546 clearly displays the **Clay card selected with mint outline and check mark** after upgrade, so loss of appearance setting is not supported. Accessibility tree had Clay label but no old `Selected` icon or `selected=true` attributes. Fixed by explicitly setting `contentDescription = "Clay style, selected"` on selected card and adjusting QA to require either that context-rich label (plus visible Clay name) for new APK, or the legacy marker spatially aligned to Clay on old APK. Does not weaken state verification: both versions require a selected indicator. New CI pending. Prior T05/T08 API35 tests passed in device run 45220967836, though old device smoke failed on obsolete button label and was already fixed. Remain 3/9 verified complete; T03/T05/T07/T08 ongoing. No key changes. Mark STOPPED with timestamp at final reply.

## Nov 24, 2026 · 1:00 AM PHT — Android 15 tutorial and missing Python recovery passed; legacy QA label drift

[Device run 45220967836](https://github.com/iamlxwis/CodeLab/actions/runs/45220967836) Android15 sequential logs prove `qa/tutorial_resume_smoke.py` and `qa/missing_python_smoke.py` **passed** after the Skip persistence correction. The subsequent established `qa/device_smoke.py` failed because it still waited for `Check for updates`, but Python manager now deliberately says `Download latest Python` (a truthful full APK download, not a lightweight metadata check). Fixed that assertion in `qa/device_smoke.py` and the same stale expectation in `qa/production_upgrade_smoke.py` so the signed upgrade QA can test actual data retention without old text drift. **New CI still pending; do not count entire device suite pass yet.** No modification of signing key or loss of existing production APK. T05/T08 and full QA remain in progress. Before final response mark README session STOPPED with 12-hour local time.

## Nov 24, 2026 · 12:48 AM PHT — Android 15 tutorial completion persistence failure

Device run 45219173196 failed tutorial resume smoke at wait for New project after intentional Skip and immediate force-stop/relaunch. Artifact XML still shows guide topic 3; Try-resume earlier in the same run succeeded. Source previously used SharedPreferences.apply() for both completion and resume-index, which can race immediate process termination. Changed to checked synchronous commit() before navigation and added explicit in-test wait to prove Skip closed the dialog before stop. Still unverified until next API33/35 run; do not attribute to source persistence until retest. T08 remains open. Session STOPPED marker required at final reply.

## Nov 24, 2026 · 12:39 AM PHT — T07 CI compilation failure identified and fixed

Both 7db7e3e workflows failed Kotlin compilation in AppearanceGallery.kt:117 (`val cannot be reassigned`, Boolean cannot assign to String). This was an accidental name collision between SettingChoices `selected: String` argument and Compose semantics `selected` receiver property. Changed local parameter to `currentChoice` and retains `semantics { selected = active }`. Source correction pending CI. No release was published by failed run. Other tests never executed; do not treat T05/T08 tests as failed runtime acceptance or verified passing until next run. Continue T03/T05/T07/T08. Session must be set STOPPED before reply.

## Nov 24, 2026 · 12:35 AM PHT — accessible Appearance palette source committed

T07: Previous unselected accent circles had 40dp touch targets with no spoken name. Implemented labeled 48dp selectable swatches, scrollable for narrow devices; theme cards and color mode options also expose selected semantics, with minimum 48dp mode target height. New AppearanceAccentAccessibilityTest instrumentation verifies six spoken labels and selection change. CI pending; manual TalkBack and complete theme checks remain. T03/T05/T07/T08 active; 3/9 completed. Remember STOPPED state at final reply.

## Nov 24, 2026 · 12:32 AM PHT — tutorial UI process-restart acceptance test committed

The new `qa/tutorial_resume_smoke.py` is integrated into `.github/workflows/device.yml` after clean install. On a disposable Android 13/15 emulator it goes to topic 2, taps **Try it in CodeLab** to open a real New Project dialog, force-stops and relaunches to assert auto-resume at topic 3; then explicitly skips, force-stops and relaunches to confirm no forced onboarding. Captures evidence. **CI pending, no device success claimed.** After this, `missing_python_smoke.py` resets the test app, then existing full offline smoke runs. T08 and T05 are unfinished and need more accessibility and runtime-install checks. Session must be marked STOPPED at reply.

## Nov 24, 2026 · 12:30 AM PHT — resumable interactive tutorial fix committed

T08 confirmed defect: `FirstRunTutorial`'s **Try it in CodeLab** button called `onFinish()`, which set `first-run-guide.complete=true` and abandoned the unfinished tutorial. Implemented two distinct callbacks: intentional Finish/Skip marks complete and clears `resume-index`; Try saves next topic index, hides the dialog and opens the real feature. On reopening the guide, the stored topic restores (including advanced stage); invalid indices are clamped. Added ZIP import as actionable first-run tutorial control, changed stale More-menu description (three-step vs 14-topic), and 3 new JUnit tests. **CI and actual accessibility/large-font device proof pending.** T08 now in progress (3/9 done, 6 remaining, T03/T05/T08 active). Never claim entire tutorial complete from these changes.

## Nov 24, 2026 · 12:26 AM PHT — missing-Python recovery Android QA authored

Added Android 13/15 instrumentation-style device UI acceptance in `qa/missing_python_smoke.py`, scheduled in `device.yml` after clean APK installation and before `device_smoke.py` installs Python. It uses a test-only project with Python missing, checks Offline Run error, intentional Manage Python action to module-manager Download Python UI, and absence of automatic module installation; writes screenshots and a per-API result. **CI unverified.** Current `T05` remains in progress: actual DownloadManager internet transfer, APK signature-validation on downloaded bytes, Android installer approval, cancellation, update/removal still outstanding. Prior version/downgrade JVM tests also awaiting CI. Keep session marker WORKING only during this interactive response and STOPPED at handoff.

## Nov 24, 2026 · 12:23 AM PHT — Python module update correctness, source committed

Confirmed previous module UI said `Check for updates` but unconditionally downloaded the full latest module; it did not compare downloaded APK version with installed version and would offer an installer even for downgrades. Also DownloadManager `STATUS_SUCCESSFUL` did not verify the local APK still existed. Source changes: added pure `PythonModuleVersionPolicy` (fresh install/update/reinstall/downgrade/invalid), 5 JVM regression tests including longVersionCode beyond 32-bit, checks archive/installed versions after package name and signing-certificate verification, blocks downgrades, provides explicit Update/Reinstall action and recoverable missing-file failure. **Not yet CI-verified and not an actual in-app APK download/install test.** T05 remains in progress, T03 remaining audit in progress, previous 3 completed tasks preserved. Session still WORKING; must set STOPPED before final reply.

## Nov 24, 2026 · 12:20 AM Philippine time — interactive development resumed

The previous ChatGPT turn ended after commit 82225c4; ChatGPT execution stopped at its response, rather than continuing in the background. A user-requested README session indicator and task timestamps have been added. All new task actions are logged in Asia/Manila (UTC+8), 12-hour AM/PM; historical timestamp-free rows are preserved without fabricated times. **Session currently WORKING only during this assistant turn; must be switched to STOPPED (assistant replied) before the final message.** GitHub Actions are independent. Current app work: T03 reliability and T05 Python module lifecycle audit, with no new implementation claim yet. T01/T02/T04 remain verified; T06/T07/T08/T09 open.

## 2026-11-23 — SUCCESS: both signed upgrades and device-smoke suites verified

- **Release CI [45209632286](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286): SUCCESS**, production certificate verified for editor and Python. The `latest` GitHub release published at source commit [`19123aa9`](https://github.com/iamlxwis/CodeLab/commit/19123aa9e98d0cc5917e7dfdf73f921419806117). On **Android 13/API33 and Android 15/API35 disposable emulators**, QA installed old editor/Python APKs, created project/source and Clay theme, force-stopped/relaunched, upgraded both using `adb install -r` without uninstall/clear, verified application IDs, matching signing certificates and versionCode `1286 -> 1288`, confirmed source/theme survived, then ran **real offline Python** from the updated companion. CI acceptance reported `passed_api33_api35`; evidence artifact `production-upgrade-evidence` is attached to the run. **Physical-device update remains unverified**; older debug APKs need exported project ZIP backup and one-time reinstall to adopt permanent signer.
- **Device smoke CI [45209632246](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246): SUCCESS** on BOTH API33 and API35, resolving T02 Online-mode status assertion. Source-upload consent was hardened at `ca4ffc9`: one-use approval bound to file/source revision, mode, endpoint and language ID; JVM coverage shows 12/12 approval-guard branches. No external-provider run was sent as part of this claim.
- T01 signing identity, T02 Android 15 Online-mode smoke, and T04 signed emulator upgrade/data retention **DONE**; T03 whole-app defect audit is *in progress*, T05 Python module end-to-end download/update/removal, T06 optional offline module expansion, T07 comprehensive UI, T08 immersive accessible tutorial, T09 complete QA/release-readiness are **NOT DONE**. Do not label release 1.0 stable or claim full production readiness; comprehensive physical-device QA and module-download path remain. **3/9 done; 6 remaining; 1 active; 0 blocked.** Next: continue T03 bug review with actual regression tests, then T05 module-manager lifecycle; always preserve working release and signing identity.

## 2026-11-23 — signed-upgrade Android 13 emulator failure reproduced and test fixed

[APK run 45207076639](https://github.com/iamlxwis/CodeLab/actions/runs/45207076639) successfully built production-signed `ca4ffc9` with matching previous keystore certificate and passed JVM, lint and the certificate comparison; it correctly started the real API33 `adb install -r` upgrade acceptance. The test failed before performing the actual upgrade, after initial test project creation, at `qa/production_upgrade_smoke.py` `ui.tap("Appearance")`: screenshot and last-window.xml show the *actual* modal bottom sheet called `Workspace` with searchable actions and categories; Appearance is under Settings, not a top-level visible item. No app crash evidence. Test now uses a shared helper to open the workspace sheet, search for an action and tap the result TextView instead of the query EditText (same correction for Languages). **This is test navigation repair only**, not data retention proof, and no release was published by the failed workflow. New run API33/API35 evidence is required before claiming T04 complete. T01 remains genuinely complete at `7e04d47`; T03 consent guard JVM 12/12 branches covered in ca4 test suite. Next: inspect upgrade test and full Android device smoke results; fix actual defects without weakening criteria. Counts 1/9 done, 8 remaining, T02/T03/T04 active.

## 2026-11-23 — first genuine permanent-signed APK confirmed; online approval hardened

[APK workflow 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835) passed Gradle release, JVM tests, lint, both APK signature verification, and the new mandatory permanent-keystore-vs-actual-signing-certificate check. Independently extracted editor v2 X509 signer SHA-256 `c81d2aa187febd5252ad0369f2b4cfba368b5f16f6b4b049963fa284c9e42392`. The signed release was published. This **closes T01**, correcting the formerly false 'production' metadata. Older debug APKs require backed-up reinstall; the T04 in-place upgrade test was skipped because its prior release had a different debug signer. The next commit uses the same permanent key, allowing conditional real `adb install -r` upgrade QA to run before publication. Android 13/15 device workflow for 7e04 was still running at this checkpoint; T02 not marked complete.

Source defect in `WorkspaceModel.startCloudRun`: approval previously consisted only of a filename; edits, changes to execution mode, endpoint or compiler choice between approval presentation and submit were not checked. Added a one-use `OnlineRunApproval` snapshot of file, source revision, endpoint and language ID. Sending now requires unchanged state and selected Online mode; canceled on switching modes or losing connectivity. Action explicitly choosing cloud now sets Online mode. JVM regression covers each changed dimension. This source change is **awaiting new CI**; no external cloud upload was performed. Tracker 1/9 done, 8 left, T02/T03/T04 in progress. Next: inspect new production-signed upgrade emulator QA and device smoke for failures.

## 2026-11-23 — P0 signing compile failure repaired in source; CI retry pending

[APK workflow 45204023605](https://github.com/iamlxwis/CodeLab/actions/runs/45204023605) tested the release-signing secret-scope fix but failed specifically at `:app:packageRelease` with `SigningConfig "production" is missing required property "storePassword"`. Cause is a **second independent bug** in `app/build.gradle.kts`: an outer `val keyPassword` intended for the keystore store password was shadowed by SigningConfig receiver's `keyPassword` property; `storePassword = keyPassword` read a null receiver value. Similarly, `keyAlias = keyAlias` self-assigned null. Renamed outer values to unambiguous `releaseStorePassword`, `releaseAlias`, `releaseEntryPassword` and added a security regression check. The companion module already used distinct names and needed no change. **Source only; new APK CI must pass actual signer-to-keystore verification.** No signing secrets or keystore rotated. No APK published by the failed run; T01 still in progress, 0/9 done, 9 remaining. Continue with actual CI and in-place upgrade after real production release.

## 2026-11-23 — CRITICAL production-signing error found; fix awaiting CI

Inspected APK v2 X509 certificates from builds [a11d15e](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919), [7acd475](https://github.com/iamlxwis/CodeLab/actions/runs/45200242402) and [ee24cd3](https://github.com/iamlxwis/CodeLab/actions/runs/45201489255). All certificates were **CN=Android Debug** with different SHA-256 prefixes `514931a7`, `43dd76c8`, `ef86c79d`. The CI `Signing mode: production` metadata falsely reflected key provisioning, not actual APK signer. Root cause: the GitHub Actions Gradle build step lacked store password, alias and key password; both modules fell back to debug. Fixed scoped build credentials and added `keytool`/`apksigner` check requiring both APKs to match the existing permanent keystore before publication. **The existing key was not regenerated.** Fix SOURCE-ONLY pending CI. ee24 APK workflow passed compilation but upgrade tests were skipped as signer changed. The first real production-signed APK cannot install over earlier debug-signed copies; backed-up migration necessary. Only a subsequent same-key signed release can meet T04 emulator upgrade gate. README tracker corrected to 0/9 done, 9 remaining, T01/T02/T04 active. Next verify actual certificate in new run and repair failures.

## 2026-11-23 — signed upgrade/data-retention gate authored (CI pending)

Before publishing a release, `android.yml` now conditionally tests **both Android 13 and 15** if the prior release and candidate share the existing permanent production signer. New `qa/production_upgrade_smoke.py` verifies unchanged editor/Python IDs, strictly increasing version codes and matching SHA-256 signer for all four APKs; installs older builds, creates/saves source and Clay appearance, tests restart persistence, upgrades via `adb install -r` (no uninstall/clear), verifies source and theme survive and runs offline Python with the updated companion. Evidence artifact and release-build-info gate included. If prior signer differs, test is skipped with existing signature_changed warning; first debug-to-production migration remains backed-up reinstall. **Implementation is not verification:** new CI must pass; a real physical-device upgrade is still outstanding. Latest prior verified production APK run 45194290919. T02 patch commit 7acd475 awaiting device CI. T04 now in progress; 1/9 done, 8 remain. Next: inspect new CI and repair any failure without weakening acceptance.

## 2026-11-23 — API35 online execution test repaired, CI pending

Inspected latest main `4b2f9ca`, device workflow [45191732470](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470) and downloaded its API35 QA artifact `last-window.xml`. API35 failed in `qa/device_smoke.py` after tapping Online: instead of an unavailable reason, the real UI displayed `Online: source upload needs confirmation`. Root cause is a falsely restrictive smoke-test expectation; an emulator's airplane setting does not prove no network/provider. Updated smoke to accept all valid states, preserving deliberate source-upload consent. This is a test-only correction, NOT evidence that online execution was run. Latest verified release [APK 45194290919](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919) is production-signed; device API33 passed and API35 failed on earlier source. Next: verify API33/API35 CI for this commit; then implement production-signed upgrade/data-retention test without disturbing signing keys. 1/9 done, T02 in progress, 8 remaining.

## 2026-11-23 — README development progress and audit checkpoint

User requires a single concise README progress dashboard showing how many tasks are done/remaining, current task, per-task statuses and latest verified CI; every meaningful task transition or checkpoint must be documented in the same commit. Added a nine-workstream README table and append-only `docs/development/PROGRESS_LOG.md`, made updates mandatory in `AGENTS.md`, and corrected obsolete README signing caveat. Baseline is 1/9 done, T02 Android 15 online-run smoke in progress, 8 remaining. T01 production signing verified at [APK workflow 45194290919](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919); API33 pass/API35 fail [device workflow 45191732470](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470). Tracker is a commit-based audit, not an automatic real-time reflection of work not yet committed. Next address Android 15 online-run regression, then test production-signed upgrade retention; CI for tracker commit pending.

## 2026-11-23 — GitHub Actions-only signing key setup (source commit pending CI)

User rejected requiring Termux and approved a phone-only GitHub Actions flow. Add a dispatch-only keystore generation workflow that takes a strong passphrase from `CODELAB_KEYGEN_PASSPHRASE` **repository secret** (never a public workflow input), runs Java keytool to create RSA4096 PKCS12 `codelab-release.jks` with alias `codelab`, encrypts using PBKDF2-SHA256 600k + AES-256-GCM, validates WebCrypto recovery, and uploads **only the encrypted JSON** and a self-contained offline unlocker as a seven-day artifact. The user opens it locally on Android, decrypts, preserves two private backups, and copies Base64 into the four existing production signing secrets. Document steps in `docs/SIGNING_GITHUB_ANDROID.md`; old debug-signed installs require one backed-up migration. This DOES NOT auto-configure any secrets or prove phone upgrade acceptance.

**Last verified main before this commit:** `7f70bf9c98408b88fd65dd9358662cb616469c76`, [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45183738315) PASS, [API33+35 device](https://github.com/iamlxwis/CodeLab/actions/runs/45183738358) PASS. Next: verify workflow syntax and Actions appearance after commit; user creates private generator passphrase, runs generator, unlocks on phone, creates production secrets and runs APK CI; verify actual `Signing mode: production`, then test safe phone update without losing data.

## 2026-11-23 — API35 emulator smoke diagnosis

[Device run 45181285928](https://github.com/iamlxwis/CodeLab/actions/runs/45181285928) failed on API35 at `qa/device_smoke.py:222`: expected `Internet connection required` after Online selection. Downloaded `device-smoke-evidence-api-35` and inspected actual accessibility XML. The app **correctly displays** `No online compiler for this file` alongside Online and Configure; the emulated network is validated even though the test toggled airplane mode (Android/emulator network state is not a reliable cloud-availability oracle). The QA assertion is now tolerant of either truthful unavailability reason. Fix is test-only, not a demonstrated app bug. New Android 13/15 CI required. Other source fixes and signing remain as previously recorded.

## 2026-11-23 — duplicate-file creation audit (source awaiting CI)

Found another optimistic navigation failure: `WorkspaceModel.select(create=true)` switched tabs before validating a duplicate file name and did not restore the prior editor on I/O failure. Added preflight collision handling and stale-safe rollback on failed file switch, with data-retention unit test and emulator journey. Prior work already fixes duplicate-project creation. New source/device validation pending; current release signing awaits permanent owner secrets.

## 2026-11-23 — immediate workflow syntax repair

[APK run 45180692671](https://github.com/iamlxwis/CodeLab/actions/runs/45180692671) failed at YAML validation because the first signing-workflow edit accidentally duplicated a script suffix. The workflow has been restored from source at `5503cb62` then re-applied with safe literal substitution to remove the duplication; new APK CI needed. No APK was published from failed YAML workflow. Do not claim release-version automation verified before new CI passes.

## 2026-11-23 — release update-signing patch (awaiting CI)

After project library patch `5503cb62`, editor and Python APKs now read an increasing CI release `CODELAB_VERSION_CODE`, defaulting to 6/2 for local builds; GitHub Actions derives it from workflow run number. CI rejects partially configured signing secrets, warns on fallback-debug, blocks accidental replacement of a prior production-signed release with a fallback-debug release, and records version in build info. Owner must generate or reuse stable private keystore and set four GitHub Actions secrets (secure instructions in docs/INSTALLATION.md). Cannot assert update continuity until production-signing builds and physical-device upgrade tests pass. Prior verified APK/device CI `09e94e2c`; new CI status pending. Next: inspect latest jobs, correct failures, then guide owner through secrets.


## 2026-11-23 — defect sweep, source awaiting CI

Prior main `09e94e2c` passed [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45174421033) and [Android 13/15 emulators](https://github.com/iamlxwis/CodeLab/actions/runs/45174420811). User reports nonfavorites visible in Favorites: code and outdated test confirmed the defect. Source now truly filters Favorites, preserves actual last-modified Recent ordering, corrects results count and empty state, and recovers the library on failed project creation. New unit and device smoke regressions added. Still unverified on CI until new workflow completes. Separate signing/version changes and wider failure-path audit pending. Next: watch CI failures and fix; inspect release signing continuity and help owner configure permanent key.

> **2026-11-23 — important user correction:** User confirms their established update procedure is always **uninstall the old CodeLab first, then install the new APK**; previous releases installed successfully this way. Therefore an old/new signing conflict is NOT the leading explanation assuming the uninstall completed. The phone's generic `App not installed as package appears to be invalid` requires a clean-install/release-asset integrity test and the actual PackageManager failure detail, not a guessed signature failure. New `qa/clean_install_smoke.py` now tests installing and launching the release APK on a fresh CI emulator before debug smoke; source/CI pending. Latest prior build and emulator were green at `a62738cf`; signing and provenance audit is separate preventative work. Next: check the new workflows and have user try a verified re-download through the Files app; check Android version/installer code if still rejected. Do NOT recommend reinstall steps as a fix when the user is already performing a clean install.

> **2026-11-23 — workflow syntax correction:** First implementation of signing-audit workflow had a malformed multi-line release notes assignment; [APK workflow 45163194992](https://github.com/iamlxwis/CodeLab/actions/runs/45163194992) was rejected before creating any jobs. Replaced release notes with valid single-line shell assignments; new CI must pass before declaring install/release pipeline repaired. Previous code baseline `a62738cf` remains APK+device green. Physical-device package invalid still unresolved pending exact installer diagnostic and stable signing.

> **2026-11-23 — installation report / next action (source changes unverified):** A physical-phone screenshot shows Android's `App not installed as package appears to be invalid` for the GitHub latest CodeLab.apk. At main `a62738cf`, both [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45151364074) and [device smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45151364143) passed, but this does NOT prove updates on previously installed user devices. The CI release uses ephemeral fallback-debug signing absent configured repo secrets; Android may reject upgrades with mismatched signing identities. The release's `latest` tag also remained at old source `07b1d01` while assets were refreshed. Added release-audit step to compare previous/new editor certificate and editor/Python signers, build manifest and actionable backup-first warning, fixed portable checksums, and aligned mutable latest tag. **These changes are not yet run in CI.** Next: check new APK workflow and signing audit; distinguish mismatch from download corruption/OS compatibility using Android installer failure code; configure a secure persistent signing key via GitHub Actions secrets, then test real physical-device upgrades, without deleting user projects before export.

> **2026-11-23 — CI follow-up queued in source (unverified):** Expanded device smoke gallery screenshots from Glass/Minimal to all five styles; hardened `qa/motion_smoke.py` startup after [prior run 45145653756](https://github.com/iamlxwis/CodeLab/actions/runs/45145653756) failed waiting for the project library. Retest against the appearance-fix commit and record actual CI outcome. Next after green CI: inspect five screenshots and continue in-app Python installation/uninstall QA, JS/Lua runtime modularization, stable signing.

> **2026-11-23 — theme correction in source, verification pending:** Four phone screenshots exposed black foreground text/icons on dark themes and rectangular Glass/Clay decorations. The transparent root Material Surface lacked an explicit onSurface content color; large blurred child layers were clipped by graphics bounds. Source now supplies onSurface at the app root, uses full-viewport edge-fading Canvas gradients, and prevents dark-only Minimal presets from overruling Light mode. Added a 5-style/2-mode Android contrast regression test. Latest prior main `c8344890`: [APK passed](https://github.com/iamlxwis/CodeLab/actions/runs/45145653793); [device workflow failed](https://github.com/iamlxwis/CodeLab/actions/runs/45145653756) with `qa/motion_smoke.py:55 AssertionError: Timed out waiting for ('New project',)` after earlier smoke phases. **Current source changes are not yet compiled or emulator/phone verified.** Next: expand theme screenshot QA, fix motion startup navigation, run CI, inspect artifacts, then resume Python module installation and optional runtime migration.

> **Further emulator issue, resolved in source awaiting retest:** Latest device run [45144260324](https://github.com/iamlxwis/CodeLab/actions/runs/45144260324) successfully opened the Python module details (installed and verified), then timed out returning to the home screen. Artifact `last-window.xml` proved the Languages bottom sheet remained open after a single Back key. Added a permanent visible Close button to the Languages header and QA now invokes it, rather than relying on IME/system Back ambiguity.

> **2026-11-23 emulator regression discovered at source 7c382afd:** [Device smoke 45142776614](https://github.com/iamlxwis/CodeLab/actions/runs/45142776614) failed because a bulky execution status panel hid the searched Python item beneath the Language Manager viewport. Its XML showed "1 languages matching" and no Python TextView in view. New source hides that header while searching; QA now selects the actual TextView and not the input field. APK workflow [45142776553](https://github.com/iamlxwis/CodeLab/actions/runs/45142776553) passed. **The UI fix awaits its own new CI.**

> **UI regression coverage (new source, CI pending):** Language-module details are now composed after the Languages bottom sheet so direct opening from a missing-module action cannot be hidden behind it. Android smoke now checks Glass/Minimal appearance and Python in-app installed/download/manage controls before existing offline runtime journeys.

> **Gallery fidelity correction (awaiting CI):** Theme previews now compose the same CodeLabTheme, CodeLabBackdrop and ClayPanel pipeline as actual screens, rather than fixed invented sample colors. Changes respond to the requirement for genuine, consistent themes.

> **Additional source fixes pending CI:** cap runtime downloads at 120 MB while downloading; allow uninstall of mismatched/incompatible engine packages; scroll tutorial under large font scaling; enforce no emoji characters in app source.

> **QA follow-up (unverified):** Device smoke script now exercises the explicit Online/Offline Run selector, asserts the selected mode, returns to Offline, and then runs the active JavaScript file. Preserves the original emulator journeys while covering the new menu. Latest CI results for this revision must be checked before closing the issue.

> **Run-mode routing change (unverified):** Run dropdown now lets users choose persistent Offline or Online; offline never uploads code automatically, online always goes through existing consent confirmation. Missing Python shows a direct Manage Python action, preselecting its in-app downloader. Device and unit regression tests pending.

> **Current implementation step (unverified by CI):** Python runtime now downloads inside the Languages sheet via Android DownloadManager with progress/cancel/persistent job, verifies exact APK package ID and signing certificate against CodeLab before launching Android's installation confirmation, and supports removal via system approval. The browser redirect is removed. This is designed for **GitHub-distributed** CodeLab; Google Play prohibits unrestricted externally downloaded executable modules and needs Play Feature Delivery. Network/installer end-to-end verification is still pending.

> **New session changes (not yet verified):** Three-step first-launch tutorial and replay action, plus distinct real theme backdrop/surfaces (blurred Glass, raised Clay, true-black AMOLED, warm Paper, flat Minimal). These are source changes awaiting APK/emulator CI for the new SHA. In-app runtime downloading and broader bug audit remain in progress; do not mark them complete.

> **2026-11-23 user correction / urgent product requirement:** See [IDEA-011](IDEAS.md). The current Python button redirects to GitHub; this does NOT satisfy the requested Spotify-style *in-app* download/install/manage/run flow. Implement in-app progress, verification, install/update/remove and local execution. Platform-required Android confirmations for executable components may still be needed on standalone APKs; do not promise prohibited silent installs. User requested **discussion before implementation**. Current APK/emulator green at `9cd9a16c` but broader bug audit remains unperformed.

> **Current canonical verification:** SHA `216a58aa` passed [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) and [emulator](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400). Earlier section headings marked “unverified” are preserved historical snapshots and are superseded by this verified status.

# CodeLab project memory — handoff snapshot

**Updated:** 2026-11-23
**Latest VERIFIED source SHA:** `216a58aaf5b7b01e91840344d11f2144952cf1b8` (both APK and emulator CI passed).
**Repository:** `iamlxwis/CodeLab` · default branch `main`
**Purpose:** Let another AI coding assistant resume accurately without access to earlier chats.
**Persistent idea ledger:** [IDEAS.md](IDEAS.md); every new user thought is logged with an immutable ID, remains visible until tested/complete and is never silently deleted.

## Product goal — user-approved

A high-quality **native Android editor** with professional vector icons (no emojis) and five visual styles (Minimal, Glass, Clay, AMOLED, Paper).

**Execution UX is a choice:** The user selects **Online** (consented remote compiler) or **Offline** (locally installed language module). If an offline language module is missing, offer download/install, then execute offline after a successful install. Python is optional too. The final lightweight editor APK should not pre-bundle optional execution runtimes. Language editing and starter examples remain available without engine installation.

**Critical mismatch:** Current main **still bundles CPython 3.13 (Chaquopy), Rhino JavaScript, LuaJ, BeanShell Java beta, and platform SQLite/WebView support**. This is pre-migration implementation, NOT the agreed final architecture. Do not quietly accept it as the desired outcome or remove it abruptly before working module replacements exist.

## What exists in source now

- Kotlin/Jetpack Compose Android app; editor/projects/files/session storage.
- Theme gallery and five visual styles; new project flow with visual language cards, starter modes and sample previews.
- `StarterExamples.java` covers the 95-entry language/file-type catalog (source starter, not execution promise).
- `OfflineRuntimeRegistry.kt` describes current bundled engines; `RuntimePackCatalog.kt` contains planned-pack labels, not a working engine pack installer.
- CPython 3.13 through Chaquopy integrated into an isolated Python worker; implementation in current source, but end-to-end device testing is unresolved.
- Existing opt-in cloud execution machinery through `CloudJudge0.java`; current run-target UI includes capability-aware online/local status. A **complete explicit online-vs-offline run choice and downloadable pack manager** are not yet implemented or verified.
- `docs/ENGINE_PACKS.md` documents proposed signed runtime-pack security requirements, not released packs.

## Current verified CI — source `216a58aa`

- [Android APK build + JVM tests](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472): **PASS**. Produces signed (fallback-debug) `CodeLab.apk` and separately signed `CodeLab-Python.apk`; artifacts are published to the same release.
- [Android emulator smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400): **PASS**, including CodeLab/Python companion installation, airplane-mode Python execution, Python stdin/error instrumentation, JavaScript/Lua/Java beta, SQLite, HTML preview, editor storage/tab journeys, Android connected instrumentation, motion and accessibility scripts. Device artifact `result.txt` explicitly records these functional outcomes.
- Release: `CodeLab.apk` **3,527,082 bytes**, `CodeLab-Python.apk` **36,453,454 bytes**. Fallback debug signing still prevents guaranteed cross-release updates. Physical Android device acceptance remains unverified.
- Superseded failures: Python isolated-process bootstrap ([45129100951](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951)), two stale bundled-runtime assertions ([45134808317](https://github.com/iamlxwis/CodeLab/actions/runs/45134808317)), and missing searchable Web template ([45133009129](https://github.com/iamlxwis/CodeLab/actions/runs/45133009129)). Preserved in `KNOWN_ISSUES.md` as history; resolved by the companion architecture and commit `216a58aa`.

## Next action — after verified Python module

1. Implement clear explicit **Online / Offline** Run selector, with consent before remote upload. Do not silently send code online or assume a cloud compiler is always available.
2. Build a real language pack manager with installation/update/uninstall state, package compatibility and signing checks; current Python button opens a GitHub release APK and requires Android installation.
3. Move the remaining optional Rhino JS, LuaJ Lua and BeanShell Java beta engines to verified companion modules, protecting existing project data; maintain tests.
4. Validate absence/untrusted/wrong-version pack behavior, cancellation/timeouts, upgrades, physical-device and ABI coverage.
5. Add more executable languages only when Android-compatible runtime modules and CI tests actually work. Preserve all 10 ideas in `IDEAS.md` until done; maintain history.

Read `DECISIONS.md` and `KNOWN_ISSUES.md` before acting. Update this file in **every meaningful implementation commit**.

## Handoff format for subsequent sessions

When updating, retain a short, explicit snapshot:

- **Latest source SHA / work completed:** ...
- **Verified:** build run, unit tests, emulator, physical device, with links ...
- **Failed / unverified:** exact steps or evidence ...
- **Next task:** one concrete, reproducible objective ...
- **Decision changes:** link/update `DECISIONS.md` ...

## Current migration work — source stage (unverified)

A new `:engine-python` Android application and signature-protected Messenger service
were added as a **prototype**, using Chaquopy within its own app UID (not `isolatedProcess`).
The core still uses its embedded Python worker until the next integration commit.
No installation/download, build or emulator claims for the companion are valid yet.
The earlier Python failure was confirmed by the GitHub device artifact: isolated
Chaquopy could not access `SharedPreferences` / `UserManager`.

## Python companion integration — unverified (this commit)

The editor no longer directly embeds Chaquopy: the separate `:engine-python`
application is the Python execution target. Core checks exact package name,
signing-certificate match, signature permission and protocol version before
explicitly binding; a missing or untrusted pack is rejected. Existing
JavaScript/Lua/Java runtimes remain embedded pending their own migrations.
**Build and offline-device verification of this integration are outstanding.**

## CI delivery for Python companion (this source commit)

Gradle workflows now build the editor and companion APK together, sign and
verify both, publish `CodeLab-Python.apk` alongside `CodeLab.apk`,
and install both before emulator smoke. The pipeline and runtime have not
yet been proven successful for this SHA; check separate APK and device run
statuses. Fallback debug signing means users should install both APKs from
the same release/build.

## Runtime capability labels (this commit, verification pending)

Python is no longer statically labelled as a bundled runtime. The file picker
and new project sheet distinguish installed/verified Python from missing-pack
and online-only states. Tests cover the three corresponding Python capability
states. Future general online/offline choice and fully native pack management
remain pending.

## Python package download entry point (this commit)

Language Manager now reads the verified package status and offers a button
to open the exact Python APK asset in CodeLab's GitHub release. Android handles
the download/installation; the user may recheck status after returning.
**This is not yet a native module download/install manager.** The companion
asset becomes available only after a successful release workflow.

## Build feedback — same-session follow-up

GitHub Actions [45132438924](https://github.com/iamlxwis/CodeLab/actions/runs/45132438924)
failed compiling both source modules. The exact errors were Java `illegal forward
reference` in PythonRuntimeService and Kotlin `Unresolved reference 'app'`
in WorkspaceModel at lines 138/1235/1236. This commit fixes those compile
errors; **new CI verification is still required**.

## JVM catalog/test migration follow-up

The language catalog no longer hardcodes Python as bundled offline; JVM tests
now assert Python is not part of the core runtime registry. Actual installed
pack availability remains a PackageManager verification on Android devices.

## 2026-11-23 — registry and Web-template regression fixes

- Latest APK CI [45134808317](https://github.com/iamlxwis/CodeLab/actions/runs/45134808317) failed 2 unit tests because `OfflineRuntimeRegistry.kt` still falsely included Python in the **core** after the companion migration. Removed that stale entry, matching the already-correct tests; new CI verification required.
- Device smoke [45133009129](https://github.com/iamlxwis/CodeLab/actions/runs/45133009129) successfully got through Python, JavaScript, console input, editor operations and SQLite before failing on `Language result Web did not appear`. Device evidence `last-window.xml` shows the `Web` search field but no result, because Web is only a **multi-file project template** and is not an entry in `LanguageCatalog.entries`. Added an explicit Web project result to the language search UI. Do not add a fake executable language to the catalog.
- **Verification pending on this commit:** APK unit/build pass and Android emulator smoke including Web/Java/Lua. Do not mark complete before both workflows pass for same SHA.

## 2026-11-23 — appearance and library UI implementation checkpoint

- Implemented in source: per-style accent/mode memory; validated named appearance recipes with create/update/load/delete; hex accent parsing and luminance-based foreground; neutral Glass palette and diffused backdrop; import-option cards; responsive horizontal project filters, clearable search, refreshed project cards/empty state; corrected \`qa/clean_install_smoke.py\` absent-package exit handling.
- Tested: source patch construction and GitHub tree write; **Gradle/JVM/emulator/physical-device verification pending CI**. The existing APK workflow was green at \`aff23e19\`; device workflow 45163951663 failed before installation due to the smoke-script check above.
- No claim of genuine OS compositor backdrop sampling: Glass uses layered translucent Compose surfaces and gradients with stable, low-cost fallbacks. Actual screenshots, accessibility and contrast validation remain mandatory.
- Next: wire full appearance preset management UI, visible run-mode choice, deeper tutorial; run CI and fix compile/device regressions. Keep IDEA-015 installation blocker open until tested.

2026-11-23 CI follow-up: [APK run 45169778823](https://github.com/iamlxwis/CodeLab/actions/runs/45169778823) and [device run 45169778754](https://github.com/iamlxwis/CodeLab/actions/runs/45169778754) failed before tests: Kotlin exposed internal AppearanceRecipe through public WorkspaceModel property. Scoped that property to `internal`; rebuild pending. No device result claimed.

2026-11-23 — appearance editor now has immediate valid custom hex preview, named save/update/load/delete, deletion confirmation and restore defaults. Advanced density/motion options are progressively disclosed. Source commit pending Gradle/JVM/emulator QA. Next: Run strip and guide.

2026-11-23 source: editor Run now has an adjacent always-visible Offline/Online chip row, active-mode status (available/missing module/cloud provider) and direct Languages configuration. File/entry run actions remain in the overflow. No source upload occurs on selecting Online; prior run-time confirmation remains. CI/device tests pending.

2026-11-23 source: FirstRunTutorial now has 14 practical steps split into four essential and ten optional topics, with progress and contextual Try-it actions opening real screens; completion and replay remain wired in Studio. Device smoke selectors updated for new tutorial and run controls. This is not a system-level spotlight overlay; true screen-anchored coach marks remain pending. CI pending.

2026-11-23 QA expansion source: added JVM tests for custom color validation, corrupt and roundtrip preset persistence, name/size limit; emulator smoke matrix now API33 (Android 13) and API35 (Android 15), with per-API artifacts and clean-install result naming. These tests have not yet run successfully on the current source; physical device acceptance remains unverified.

2026-11-23 build correction: [APK workflow 45170798149](https://github.com/iamlxwis/CodeLab/actions/runs/45170798149) failed compile with `RunTargetMenu.kt:10:47 Unresolved reference InfoOutline` and line 67. Replaced unsupported icon with Material Info; new CI pending, no APK/build green claim.

2026-11-23 Projects metadata refinement in source: project cards now show real starter-template name, file count and optional description, asynchronously fetched via serialized project store executor (no filesystem I/O during Compose draws). Missing metadata has a local-project fallback; new work pending CI/emulator screenshot validation.

2026-11-23 QA synchronization: motion benchmark still referenced removed three-step tutorial actions (`Start coding`). Updated fallback navigation to new four essential steps + Finish basics. Removed redundant Online/Offline controls from file/entry dropdown because mode is now always visible beside Run; offline guidance copy updated. New CI required.

2026-11-23 emulator failure investigation: APK CI run 45171811507 PASSED release/JVM/lint/signatures and published editor APK from 618c123e. Android 15 run 45171811506 failed near qa/device_smoke.py:235 after force-stop because accessibility could not see `print(6*7)` in a clipped landscape editor. Android 13 same failure. Downloaded actual API35 screenshots and window.xml; identified extremely small native editor viewport in landscape and IME. New source adapts editor by hiding optional shortcut/status rows below 280dp available space. Smoke now asserts actual on-device saved file via `adb run-as` both before/after process death and rotation, while forcing portrait for source TextView checks. This distinguishes a real persistence failure from clipped accessibility. Fresh CI pending.
