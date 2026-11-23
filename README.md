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

**0 / 9 completed · 9 remaining · 3 in progress · 0 blocked**

**Current task:** `T01` — Correct release APK signer; verification pending

**Latest verified (2026-11-23):** [APK passed but actual signer was Android Debug](https://github.com/iamlxwis/CodeLab/actions/runs/45201489255) · [Android 13: Passed](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470) · [Android 15: Failed](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470)

| Task | Status |
| --- | --- |
| `T01` Permanent production APK signing | In progress — signer fix unverified |
| `T02` Fix Android 15 online-run smoke | In progress — test fixed, awaiting CI |
| `T03` Complete app-wide bug audit | Queued |
| `T04` Verify production-signed data-preserving upgrades | In progress — QA gate committed, unverified |
| `T05` Validate Python module download/install/update | Queued |
| `T06` Expand optional offline language modules | Queued |
| `T07` Refine all five themes and app navigation | Queued |
| `T08` Finish interactive tutorial and accessibility | Queued |
| `T09` Complete full QA and release readiness | Queued |

**Last audit:** 2026-11-23 — signing discrepancy corrected in source · [Task update history](docs/development/PROGRESS_LOG.md) · [Detailed roadmap](docs/development/ROADMAP.md)
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

> **Before updating:** Earlier releases falsely reported production signing; their actual certificates were rotating Android Debug signers. The corrected production-signing build is pending CI validation. Existing debug-signed installations require a **one-time backed-up reinstall** to move to the permanent identity. Export projects before uninstalling. Export projects before reinstalling. Java is an interpreter-backed beta; other languages may require an optional online compiler.

## Build

Requires JDK 17 and Android SDK 35.

```bash
./gradlew :app:assembleRelease :app:testDebugUnitTest
```

[Implementation status](docs/IMPLEMENTATION_STATUS.md) · [Architecture](docs/ARCHITECTURE.md) · [Contributing](CONTRIBUTING.md) · [QA](docs/QA_CHECKLIST.md) · [Android CI](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml)

---
<sub>CodeLab keeps local projects on your device. Online execution only sends source code when you explicitly approve a cloud run.</sub>
