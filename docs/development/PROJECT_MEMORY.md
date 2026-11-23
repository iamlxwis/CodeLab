
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
