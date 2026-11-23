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

**SESSION: STOPPED — interactive runtime-manager handoff** · Updated **Nov 24, 2026, 2:56 AM PHT**  
**Phase A checkpoint:** Download manager for real signed Python companion, existing bundled engines preserved. **Optional modules shipped: 1 (Python).** **Remaining runtime work:** publish and verify separate JS/Lua/TypeScript/native toolchains. **Validation:** new [APK CI 45243583986](https://github.com/iamlxwis/CodeLab/actions/runs/45243583986) and [Android 13/15 smoke 45243583725](https://github.com/iamlxwis/CodeLab/actions/runs/45243583725) pending at handoff; no new release claimed until gates pass.

**Current activity (one row, overwritten at each handoff):**

| Updated (Philippine time, AM/PM) | Task | Action / result | Next |
| --- | --- | --- | --- |
| Nov 24, 2026, 2:56 AM | Phase A — offline language downloads | Implemented uncluttered Language Manager Installed/Downloads/Downloadable views, signed Python download status/progress and retry, Wi-Fi transfer policy, APK/cache size, cache clearing, Android-confirmed removal/reinstall path, and source/unit/device regression checks. JS/Lua/Java/SQL remain bundled; other packs not falsely labeled available. **CI pending; session STOPPED.** | Confirm build, signed-upgrade and API33/35 QA; test actual download/installer acceptance on a device; produce safely migratable optional JS/Lua/TypeScript/native runtimes. |

**Remaining product gaps:** modular runtimes beyond Python; semantic LSP; editable inline folding/multi-cursor; PTY terminal; multi-language debugging; screen-anchored tutorial and full accessibility validation. See [implementation status](docs/IMPLEMENTATION_STATUS.md) and [project memory](docs/development/PROJECT_MEMORY.md).

*GitHub Actions may continue independently; the assistant does not keep coding after this reply.*


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
