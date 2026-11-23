> **Current verified baseline**: `216a58aa` — [APK green](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472), [emulator green](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400). Earlier rows describing bundled Python and failing emulator are historical and superseded by this verification.

# Completed work — implementation versus verification

Updated 2026-11-23; status inspected at source SHA `3c37ae8c01dc`.

| Area | Implemented in source | Verification/remaining caveat |
| --- | --- | --- |
| Android foundation | Kotlin, Jetpack Compose, project persistence, editor and file management | Latest APK workflow succeeds; not production certified |
| Appearance | Five visual styles (Minimal, Glass, Clay, AMOLED, Paper), theme selection/gallery, accent, density, motion controls | Visual variants exist; complete screen-wide/a11y/physical device audit remains |
| Project setup | Guided sheet, language chooser, example previews, Beginner/Hello/Blank modes | Source and JVM tests; emulator journey has a failure; demo templates remain |
| Language catalog | 95 language/file-type entries and example content | **Editing/examples do not equal executability** |
| Bundled local execution (legacy) | JS (Rhino), Lua (LuaJ), Java beta (BeanShell), SQL (Android SQLite), HTML/CSS/JS preview (WebView) | These are **currently bundled**, contrary to newly approved optional-module target |
| Python (legacy bundled implementation) | Chaquopy CPython 3.13 isolated worker and basic I/O/timeout integration | Latest emulator smoke fails waiting for visible Python output; not certified usable offline on device |
| Cloud execution | Explicit confirmation and configurable HTTPS remote compiler integration | Remote provider availability / real-world integration not assumed |
| Runtime planning | Catalog/status contract and security design document for optional modules | **No downloadable language pack or working install/uninstall manager yet** |
| CI/distribution | GitHub Actions APK build, SHA-256 and public release; JVM tests, lint/security and emulator pipeline | APK pass at `3c37ae8c`; emulator smoke fail at that SHA; debug-signing fallback |
| Development continuity | `AGENTS.md` plus `docs/development/` memory, decisions, roadmap and issues | This documentation commit establishes handoff discipline; future agents must keep it current |

Evidence:
- [APK workflow (success) — source 3c37ae8c](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978)
- [Device smoke (failure) — source 3c37ae8c](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951)

**Do not mark online/offline user-choice flow, module marketplace, Python module migration, C/C++ or TypeScript offline execution complete.**

## Regression correction awaiting verification

This commit removes Python from the core runtime inventory and restores the
Web multi-file template to language search. These source fixes are **not yet
marked verified**; check both build and emulator workflows for the new SHA.

## Verified on 2026-11-23 — source `216a58aa`

| Item | Result | Evidence |
| --- | --- | --- |
| Python optional companion APK, exact-package/same-signature binding | APKs built, signed and published; emulator installs pack before running code offline | [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472), [emulator](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) |
| CPython code execution, stdin/error handling | Emulator smoke and Android instrumentation passed | [Device](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) |
| Python removed from core bundled runtime registry | JVM unit tests passed | [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) |
| Web multi-file starter discoverability | Search/creation/preview journey passed on emulator | [Device](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) |
| Other supported emulator journeys | JS/Lua/Java beta, SQL, HTML preview, editor storage/rotation, instrumentation/performance scripts passed | [Device](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) |

Not verified: physical phones, native module install/update/uninstall workflow, explicit Online/Offline selector, stable signing or additional downloadable engines.
