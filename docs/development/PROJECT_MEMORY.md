# CodeLab project memory — handoff snapshot

**Updated:** 2026-11-23
**Source head inspected before this documentation commit:** `3c37ae8c01dcdab225b5f13273c5d01f870983e9`
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

## Latest CI observed for source head `3c37ae8c`

- [Android APK run 45129100978](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978): **success** (source builds; this does not establish device functionality).
- [Android device smoke run 45129100951](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951): **failure**. Emulator journey timed out waiting for visible text `Hello from CodeLab!`. The uploaded device evidence definitively shows `Exit 1` and `Python runtime error: IllegalStateException: SharedPreferences cannot be accessed if UserManager is not available. (e.g. from inside an isolated process)` during Chaquopy bootstrap. An isolated service cannot initialize Chaquopy as currently configured. Keep a strong process boundary: plan separately installable companion runtime with an independent app UID and authenticated IPC rather than disabling isolation in the core.
- After the documentation commit, these CI statuses describe the **previous implementation SHA**, not the new docs-only commit. Check new run results.

## Next action — do this before broader features

1. Fix the now-confirmed Chaquopy isolated-worker initialization failure by migrating Python into a separately signed, independently sandboxed Android runtime module (or validate another safe mechanism); preserve build success, then retest real offline Python stdout/stdin/errors.
2. Design and implement a **real modular runtime contract**: signed Android companion engine packages or appropriate store-delivered features, certificate checks, versioned authenticated IPC, install-detection, ABI and version checks, uninstall/status, resource bounds and isolation.
3. Migrate existing bundled runtimes, including Python, to **optional** modules only when their offline behavior is independently proven; guard migrations with tests.
4. Build a clear Run choice (Online / Offline), and an install/manage-runtimes experience. Never auto-upload source; online support can depend on provider availability.
5. Release with stable signing, physical-device verification and verified emulator CI. Additional languages follow only when genuine compatible engines exist.

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
