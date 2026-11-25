<div align="center">

# CodeLab

**A native Android IDE. Write, run and share code from your phone.**

[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/iamlxwis/CodeLab/releases/latest)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
[![Android CI](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml)

**[Download APK](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk)** · [Releases](https://github.com/iamlxwis/CodeLab/releases) · [Report an issue](https://github.com/iamlxwis/CodeLab/issues)

</div>

## What's new — Reimagined 2.1

- Cleaner Light and Dark interface with backdrop-aware glass navigation.
- Distinct Home dashboard, project library and Explore screens.
- Compact editor, responsive file tabs and keyboard-aware coding shortcuts.
- Choose Offline or Online when tapping Run or Rerun.
- Integrated console with Editor, Console and Split modes.
- Snippet Studio: export selected code, entire files, results or combined code and output in Light or Dark, with pagination for long content.
- Refined language icons, project cards, grouped settings and universal search.

**Availability:** Reimagined 2.1 (0.6.1-alpha) is merged into main. The public APK may be an earlier build until the next release is published. [Release notes](docs/RELEASE_NOTES.md) · [Changelog](CHANGELOG.md)

## Features

- Syntax highlighting, autocomplete, file tabs, search and formatting.
- Local projects, autosave, templates, ZIP import/export and file history.
- Offline execution with built-in engines and optional runtime packs.
- Online execution with explicit source-upload approval.
- Local HTML, CSS and JavaScript preview.
- Git history and optional GitHub sync.
- Code and console image sharing, local activity and update checks.

## Offline languages

| Language | Execution |
| --- | --- |
| Python | Optional CPython pack (64-bit devices) |
| JavaScript | Interpreter |
| Lua | Interpreter |
| Java | Interpreter beta |
| TypeScript | Single-file transpilation |
| SQLite | Local SQL |

Editing support extends beyond runnable languages. [Runtime and installation guide](docs/INSTALLATION.md)

## Install

Download [CodeLab.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk) on Android 8.0 or newer and open it. Export important projects before updating; older debug-signed installations may require a one-time reinstall.

## Development

Built with Kotlin and Jetpack Compose. Requires JDK 17 and Android SDK 35.

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

[Documentation](docs/README.md) · [Contributing](CONTRIBUTING.md)

---

Local-first by design. Source is sent for online execution only after explicit approval.
