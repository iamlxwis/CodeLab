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

**Last committed checkpoint:** Nov 24, 2026 · **Assistant: HANDED OFF** · **CI: UI/tutorial source changes awaiting build verification**

> This is a **commit checkpoint**, not a live clock or background agent. `HANDED OFF` means this interactive turn stopped after committing; independent GitHub Actions may still run. Each subsequent turn can resume from the [project memory](docs/development/PROJECT_MEMORY.md).

| Workstream | Verified / implemented | Remaining before completion |
| --- | --- | --- |
| **CI Option B — Smart CI** | Fast compile/unit/lint/signed APK, API35 smoke, and protected six-pack release all passed on [b4adaa96](https://github.com/iamlxwis/CodeLab/commit/b4adaa96209a1086df181adbd279c926ff64d62c). Current UI/tutorial change [34d34252](https://github.com/iamlxwis/CodeLab/commit/34d342527c1f650846bc9a2ebeae28cc0f118225) is awaiting fast build verification. | Review current CI and perform targeted narrow-screen, large-font and TalkBack acceptance. |
| **Phase 1 — Frozen runtime baseline** | [Android 15 device smoke 45306690201](https://github.com/iamlxwis/CodeLab/actions/runs/45306690201) **PASSED**, protected release checkpoint [45306673023](https://github.com/iamlxwis/CodeLab/actions/runs/45306673023) **PASSED**, and all six optional APK assets are published. | Owner handles runtime and module installation acceptance. Assistant is responsible for UI/accessibility T07, tutorial T08, defects T03 and release/docs accuracy T09. Additional compiler packs are explicitly deferred by owner decision. |
| **Phase 2 — Editor intelligence** | Not started | Begin after Phase 1 UI/tutorial/bug stabilization and owner-provided runtime acceptance |

**Current counts:** Six optional signed module APKs published (Python, JavaScript, Lua, Java beta, TypeScript, SQLite). Their installation lifecycle and physical-device runtime acceptance are owner-delegated. Language roster is frozen by owner request; no additional compiler packs in Phase 1. UI/accessibility, tutorial and release/documentation stabilization remain open.

**Latest implementation:** Phase 2 easy/medium source work: rank same-file completions by proximity, ignore comments/strings in candidates, show rename impact count, navigate structural hints from cursor, and preserve caret when indenting a single line. JVM regressions added; new fast CI pending. User retains all handset testing.

**Next:** verify exact-source fast APK build/JVM tests, resolve failures if any, then continue Phase 2 easy/medium changes. Owner handles physical APK testing and module/runtime acceptance. Phase 1 UI/tutorial remains owner-acceptance pending.

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
