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
