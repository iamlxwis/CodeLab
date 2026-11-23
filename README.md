<div align="center">

# CodeLab

**A native code editor for Android.**  
Write, run, and manage projects from your phone.

[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/iamlxwis/CodeLab/releases/latest)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Offline](https://img.shields.io/badge/Execution-Offline-1F6F5C)
[![Android CI](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml)

**[Download APK](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk)** · [Releases](https://github.com/iamlxwis/CodeLab/releases) · [Issues](https://github.com/iamlxwis/CodeLab/issues)

</div>

<p align="center">
  <img src="docs/screenshots/editor.png" alt="CodeLab JavaScript editor on Android" width="290" />
  &nbsp;&nbsp;
  <img src="docs/screenshots/web-preview.png" alt="CodeLab offline HTML preview on Android" width="290" />
</p>
<p align="center"><sub>Actual Android emulator screenshots from CodeLab's automated device tests.</sub></p>

## Development tracker

**Session:** **WORKING — this ChatGPT response is still executing**  
**Last session change:** November 24, 2026 at 12:20 AM PHT (Philippine time, UTC+8)  
**When I finish replying:** This status must be changed to **STOPPED — assistant handed off**. GitHub Actions may continue independently, but ChatGPT cannot keep committing after its turn ends.

**3 / 9 completed · 6 remaining · 4 workstreams in progress · 0 blocked**

**Current task:** T05 module lifecycle, T08 tutorial, T07 appearance accessibility, T03 reliability; CI pending.

**Latest verified (2026-11-23):** [Production APK and signed upgrades: Passed Android 13/15](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286) · [Device smoke: Passed Android 13/15](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246) · [Download release](https://github.com/iamlxwis/CodeLab/releases/latest)

| Task | Status / latest verified milestone | Last change (PHT, AM/PM) |
| --- | --- | --- |
| `T01` Permanent production APK signing | DONE — actual signer matches permanent key | Nov 23, 2026 · 11:26 PM |
| `T02` Android 15 online-run smoke | DONE — Android 13/15 device smoke passed | Nov 23, 2026 · 11:49 PM |
| `T03` App-wide bug audit | IN PROGRESS — module error/recovery review and regressions | Nov 24, 2026 · 12:23 AM |
| `T04` Data-preserving signed upgrades | DONE for API 33/35 emulators; physical-device validation remains | Nov 23, 2026 · 11:49 PM |
| `T05` Python download/install/update | IN PROGRESS — missing-module UI passed API35; legacy QA labels fixed; retest pending | Nov 24, 2026 · 1:00 AM |
| `T06` Additional offline modules | QUEUED — feasibility and real runtimes required | Not started |
| `T07` Five-theme UI/UX refinement | IN PROGRESS — contextual selected-style semantics added; old/new upgrade QA assertions aligned, CI pending | Nov 24, 2026 · 1:02 AM |
| `T08` Interactive tutorial and accessibility | IN PROGRESS — resume/Skip process-restart passed API35, API33/full accessibility pending | Nov 24, 2026 · 1:00 AM |
| `T09` Full QA and stable release readiness | QUEUED | Not started in this session |

**Recent task events (Philippine time, 12-hour clock)**

| When | Task | Result |
| --- | --- | --- |
| Nov 23 · 11:49 PM | T02, T04 | Android 13/15 CI passed; signed release published |
| Nov 24 · 12:18 AM | Session | Resumed after the previous chat reply ended |
| Nov 24 · 12:20 AM | T03, T05 | Source inspection started |
| Nov 24 · 12:23 AM | T05 | Download recovery and safer Python update/reinstall policy added; CI pending |
| Nov 24 · 12:26 AM | T05 | Added Android 13/15 missing-module navigation smoke; CI pending |
| Nov 24 · 12:30 AM | T08 | Fixed premature completion; resume index and JVM tests added |
| Nov 24 · 12:32 AM | T08 | Added Android 13/15 tutorial resume UI smoke; CI pending |
| Nov 24 · 12:35 AM | T07 | Six accents labeled with selection semantics and 48dp targets |
| Nov 24 · 12:39 AM | T07 | CI compiler error from selected-name shadowing diagnosed/fixed |
| Nov 24 · 12:48 AM | T08 | Fixed Skip persistence by durable preference commit and dismissal check |
| Nov 24 · 1:00 AM | T08, T05 | API35 tutorial/missing-Python tests passed; old Python button assertion updated |
| Nov 24 · 1:02 AM | T07, T04 | API33 upgrade screenshot confirms Clay persisted; test expected obsolete icon; contextual style semantics and dual-version QA added |

The complete append-only [task audit log](docs/development/PROGRESS_LOG.md) records commits, failures, tests and handoffs. Times are in **Asia/Manila**, written with **AM/PM**, not military time.

**Last audit:** Nov 24, 2026 · 1:02 AM PHT — fixed signed-upgrade style-selection assertion and semantics; CI pending · [Task update history](docs/development/PROGRESS_LOG.md) · [Detailed roadmap](docs/development/ROADMAP.md)
## Features

- **Code editor** — Syntax highlighting, tabs, autocomplete, undo/redo, search, snippets, and formatting.
- **Run offline** — JavaScript, Lua, SQL, and limited Java support, with an integrated console.
- **Web preview** — Build and preview HTML, CSS, and JavaScript locally.
- **Projects** — Templates, autosave, file management, and ZIP import/export.
- **Tools** — Selective Git commits, optional GitHub remote sync, JavaScript debugging and code screenshots.
- **Privacy** — Optional cloud and GitHub credentials are encrypted in Android Keystore. No source upload without an explicit action.

## Optional GitHub sync

From a project, open **Git**, then expand **GitHub sync** and configure an `owner/repository` origin. **Fetch** only downloads remote history. **Pull** requires a clean working tree and only fast-forwards. **Push** requires confirmation, a GitHub access token and a clean branch; it never forces changes. Save a fine-grained token with only the repository permissions you need.

## Install

Download **[CodeLab.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk)** on an Android 8.0+ device and open it. Allow installation from your browser or file manager if prompted.

> **Before updating:** The permanent production signer was verified in [CI 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835), with actual APK certificate SHA-256 `c81d2aa1…c9e42392`. Earlier APKs were signed by rotating Android Debug certificates despite incorrect metadata. Back up and export projects before the **one-time reinstall** needed to move from a debug-signed version. Same-key in-place upgrade **passed** on Android 13 and 15 emulators; physical-phone validation remains pending. Export projects before reinstalling. Java is an interpreter-backed beta; other languages may require an optional online compiler.

## Build

Requires JDK 17 and Android SDK 35.

```bash
./gradlew :app:assembleRelease :app:testDebugUnitTest
```

[Implementation status](docs/IMPLEMENTATION_STATUS.md) · [Architecture](docs/ARCHITECTURE.md) · [Contributing](CONTRIBUTING.md) · [QA](docs/QA_CHECKLIST.md) · [Android CI](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml)

---
<sub>CodeLab keeps local projects on your device. Online execution only sends source code when you explicitly approve a cloud run.</sub>
