# Changelog

## 0.3.0-alpha — 2026-11-22

Additional source implementations; Android device verification is performed by CI.

- Syntax-colored Code Screenshot Studio with square, landscape and portrait formats,
  transparent/gradient/solid backgrounds, title/footer and highlighted lines.
- Portable per-project descriptions and preferred entry files, preserved in ZIP backups.
- Android Storage Access Framework folder snapshot import with size and path checks.
- Rhino source-line debugger with breakpoint, Step and Continue controls.
- Safe interactive offline workspace command panel for browsing and searching project files.
- Raw image/font asset support in offline HTML/CSS/JS previews.
- HTTPS GET inspection with size/time bounds and JSON formatting.
- JVM and Android-device tests covering screenshot rendering and debugger behavior.

The roadmap is **not complete**. Unfinished major work includes semantic LSP, Tree-sitter,
offline CPython and compiled language toolchains, full Linux PTY, remote authenticated
GitHub sync, production signing, and extended physical-device acceptance testing.

## 0.2.0-alpha — 2026-11-22

Early-access development milestone, not a production or fully complete CodeLab 2.0 release.

- Dark-first native Compose workspace with themes, accents, adaptable tablet layout,
  animated splash, motion preferences, contrast and interface size controls.
- Editor navigation, bookmarks, buffer-word completion, structural hints, minimap,
  project-wide find/replace, mobile keyboard controls, configurable shortcuts.
- Local project favorites, duplication and quick switching.
- File management with recoverable trash, history snapshots, textual diffs,
  ZIP and multi-file imports, and explicit UTF-16 BOM handling.
- Offline JavaScript, Lua, SQLite and limited interpreted Java execution retained.
- Local execution records, configurable run limits, ANSI console support.
- Offline code screenshot PNG exporter and developer tools.
- Initial offline JGit history workflow and public GitHub source imports.
- Emulator performance/a11y reporting and additional JVM/instrumentation tests.

### Not yet delivered

Complete Tree-sitter/LSP, full PTY and shell, breakpoint debugger, offline Python 3
and native compilers, remote GitHub authentication/push/pull, advanced screenshot
compositing, production signing and comprehensive physical-device QA.

### Installation notice

This is a debug-signed APK. Updating may require uninstalling the previous build
if GitHub Actions generated a different debug key. Export projects first.

## 0.1.0 — Initial functional prototype

Kotlin/Jetpack Compose Android project, basic project store, editor, tabs,
offline runtime services, web preview and direct APK distribution.
