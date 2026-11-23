<div align="center">

# CodeLab

**A native code editor for Android.**  
Write, run, and manage projects from your phone.

[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/iamlxwis/CodeLab/releases/latest)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Offline](https://img.shields.io/badge/Execution-Offline-1F6F5C)
[![Android CI](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml)
[![Device CI](https://github.com/iamlxwis/CodeLab/actions/workflows/device.yml/badge.svg?branch=main)](https://github.com/iamlxwis/CodeLab/actions/workflows/device.yml)

**[Download APK](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk)** · [Releases](https://github.com/iamlxwis/CodeLab/releases) · [Issues](https://github.com/iamlxwis/CodeLab/issues)

</div>

<p align="center">
  <img src="docs/screenshots/editor.png" alt="CodeLab JavaScript editor on Android" width="290" />
  &nbsp;&nbsp;
  <img src="docs/screenshots/web-preview.png" alt="CodeLab offline HTML preview on Android" width="290" />
</p>
<p align="center"><sub>Actual Android emulator screenshots from CodeLab's automated device tests.</sub></p>

## Development tracker

**Last committed checkpoint:** Nov 24, 4:14 AM PHT · **Assistant: WORKING at checkpoint** · **CI: six-pack source/feasibility pending**

> This is a **commit checkpoint**, not a live clock or background agent. `HANDED OFF` means this interactive turn stopped after committing; independent GitHub Actions may still run. Each subsequent turn can resume from the [project memory](docs/development/PROJECT_MEMORY.md).

| Workstream | Verified / implemented | Remaining before completion |
| --- | --- | --- |
| **CI Option B — Smart CI** | Source/build changes trigger fast compile, unit, lint and signed APK artifacts. Docs-only pushes skipped. Manual API 35 device smoke. `cancel-in-progress: true` retained. Manual release publication requires same-commit device success. | Validate full run and manually trigger API 35 smoke |
| **Phase 1 — Runtime manager** | Six signed module projects in source and missing-runtime deep links; in-app Download buttons now require official GitHub release metadata confirming an uploaded APK with a safe size. Cached signed APKs remain installable. | Remote release discovery, new source CI and Android15 acceptance pending; PHP/Ruby/C/C++/Kotlin/Rust/Go/C# remain research or blocked. |
| **Phase 2 — Editor intelligence** | Not started | Begin only after agreed Phase 1 completion gate |

**Current counts:** Six optional module implementations in source, one historically confirmed public (Python); remaining five need published installer checks. Java beta is not javac; TS is single-file transpilation, SQLite uses Android platform.

**Latest implementation:** Signed Java beta, TypeScript and SQLite companion sources plus direct missing-runtime entry points. Runtime stop/restart and output-limit race source fix [dcb1daed](https://github.com/iamlxwis/CodeLab/commit/dcb1daeda2fb1e8a494a26171a7ef30e8b32d6b9). New fast/device CI pending.

**Next:** verify release asset API parsing/unit tests in fast CI, run API35 and publish six modules if gates pass; document compiler feasibility honestly.

[Compiler feasibility](docs/development/LANGUAGE_FEASIBILITY.md) · [Full audit](docs/development/PROGRESS_LOG.md) · [Implementation inventory](docs/IMPLEMENTATION_STATUS.md) · [Project memory](docs/development/PROJECT_MEMORY.md)

## Features

- **Code editor** — Syntax highlighting, tabs, autocomplete, undo/redo, search, snippets, and formatting.
- **Run offline** — JavaScript, Lua, SQL, and limited Java support, with an integrated console.
- **Language Manager** — Installed/Downloadable filtering, signed optional Python APK download, progress/cancel/retry, network policy, cache controls, Android-approved installation and removal.
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
