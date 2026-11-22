# CodeLab 2.0 implementation status

This is a feature-by-feature engineering status, not a claim of completion. A source
implementation, APK build, Android device test, and production release are distinct.

| Category | Implemented work | Still incomplete or unverified |
|---|---|---|
| 01 UI, UX and GUI | Native Compose themes, accent colors, dashboard, adaptive tablet sidebar, editor/console/preview, language search, appearance controls | Complete redesign of every screen, all layout variants and comprehensive visual fidelity |
| 02 Animations | Splash, shared system-aware reduced-motion controller, animated tabs, panels, cards and run feedback | Full navigation transitions, spring gestures, manual reduced-motion and cross-device jank QA |
| 03 Professional editor | Cancellable viewport regex highlighting, off-screen edit invalidation optimization, completion, snippets, search, outline, minimap, bookmarks, history | Tree-sitter parsing, LSP, folding, semantic diagnostics, multi-cursor IME and large-file matrix |
| 04 Mobile coding | Toolbar presets/custom keys, pinch font zoom, hardware shortcuts, input selection tools, status-bar swipe tab navigation | Full one-handed layout, all gestures, per-language keyboard intelligence |
| 05 File management | Nested folders, ZIP import/export, SAF external folder snapshot import, 2 MiB binary asset imports and offline web preview, multiple imports, GitHub snapshots, cross-project copy, recycle bin, restore, version history, regex search/replace | Live external SAF folder editing, full graphical diff/merge, larger binary files |
| 06 Projects | Starter templates, local autosave, session restoration, favorites, quick switch, duplicate, rename, ZIP-portable project description and preferred entry file | Advanced project metadata, per-project execution profiles and shared workspaces |
| 07 Languages | Offline Rhino JavaScript, LuaJ Lua, Android SQLite, BeanShell Java beta, offline HTML preview; runtime/version detection | Offline Python 3, TypeScript transpiler, C/C++ compilers and remaining language toolchains |
| 08 Execution | Isolated worker services, output/input, stop/timeout, configurable output caps, rerun and local history, JavaScript line breakpoints, Step/Continue | Multi-language debugging, variable inspector, profiler and additional local compilation |
| 09 Terminal & debugging | Real stdin/stdout/stderr console, ANSI styling, output export/input recall, Rhino JavaScript line debugger, safe offline workspace built-ins | PTY-backed terminal, native shell/process sessions, full variable-aware debugger |
| 10 Code Screenshot Studio | Offline syntax-colored 1080px PNG, three color palettes, aspect presets, solid/gradient/transparent canvas, highlight line, title/footer and line numbers | Advanced code layout templates, custom image backgrounds, editor selection capture and broad device QA |
| 11 Developer tools | Command palette, JSON format/minify, Base64, URL encode/decode, SHA-256 and bounded HTTPS GET inspector | POST/body/headers API tester, regex debugger, package tooling and broader transforms |
| 12 Git & GitHub | JGit local init, explicit staged-file commit, status, branches and public GitHub ZIP import; optional compiler credential in Keystore | Remote authenticated push/pull, PR/issue integration and conflict editor |
| 13 Performance & stability | Bounded viewport highlighting, serialized autosave, file/history caps, emulator startup/memory/jank metrics, 1x-motion smoke and static security gate | Long-duration profiling, generated baseline, code coverage and real low-end device results |
| 14 Accessibility | High-contrast palette, larger UI text, touch feedback controls, screen reader labels and hardware keyboard support | Comprehensive verified TalkBack, font-scale and contrast audit |
| 15 Distribution & QA | Optimized Gradle APK/signature, unit/instrumented tests, fallback-debug-signed public APK/SHA-256, emulator QA and static security checks | Stable production signing, Play Store listing, enforced lint/coverage baselines and acceptance tests |

## Verification definitions

- **Committed:** Source is in GitHub.
- **APK built:** Gradle assembled and validated its signature.
- **Unit tested:** JVM unit-test task completed successfully.
- **Device tested:** The emulator workflow completed its instrumented and UI journey checks.
- **Release ready:** Appropriate production signing, compatibility and acceptance checks are complete.

The public GitHub APK remains a **debug-signed early-access build**. Users should export
backups before reinstalling; the CI debug signing key is not guaranteed to remain stable.

## Architecture constraints

Python, C/C++, Rust and most listed languages have file editing but **no bundled offline
runtimes**. Cloud execution is optional, separately verified, and requires explicit
confirmation. Local JGit operations do not upload anything. Public GitHub imports download
a bounded source snapshot, not Git history.

A full PTY, semantics-based language server, production signing and Play Store compliance
must not be advertised as implemented until working code and tests exist.

## Architecture workstream (foundation implemented)

Source files are being split into activity, theme, workspace, library, dialogs and
native editor view units. The theme objects are memoized and edge-to-edge system
bars use AndroidX APIs. A UI regression test is added, but this is **not** a claim
of completed architecture cleanup. Business-logic state hoisting, stability
profiling, and device-layout verification remain pending. See `docs/ROADMAP.md`.
