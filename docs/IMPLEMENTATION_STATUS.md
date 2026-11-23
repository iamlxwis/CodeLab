# CodeLab implementation status

Updated Nov 24, 2026. This is a source-level feature inventory; **committed code is not proof of a successful build, device test or release**. See the [README tracker](../README.md#development-tracker) for current verification.

| Area | Present in source | Remaining or unverified |
| --- | --- | --- |
| UI and appearance | Five themes, per-theme accents, saved presets, redesigned project/import surfaces, mobile editor and console | Complete screen-by-screen visual QA, font-scale and TalkBack coverage |
| Editor | Syntax highlighting, file-local completion/references/rename, regex find/replace, structural diagnostics, snippets, line tools, bookmarks, folding **preview**, Git conflict-marker resolver | Semantic LSP, true inline editable folding, multi-cursor, project-wide semantic rename, large-file stress testing |
| File and project management | Local autosave, ZIP import/export, SAF snapshot import, templates, favorites, history, recycle bin, session restore | Live SAF folder editing, collaborative projects, broad failure-recovery QA |
| Languages and runtimes | Bundled Rhino JavaScript, LuaJ Lua, SQLite, limited BeanShell Java beta, HTML preview; optional CPython 3.13 companion APK with download manager, installed/downloadable filters, Android DownloadManager progress/cancel/retry, Wi-Fi policy, installer cache cleanup, reported package APK bytes and uninstall confirmation | Modularize bundled engines; offline C/C++, TypeScript, Kotlin, Rust, Go, PHP and other toolchains; verify real network download/system installer/removal/reinstall on a device |
| Run and debugger | Explicit Offline/Online selection, consent before cloud source transfer, isolated local services, stdin/stdout, timeout/stop, JS breakpoints and Step/Continue, **local variable display in source** | True PTY terminal, multi-language debugger, full stack/variable watches, profiler |
| API and developer tools | Command palette, JSON/Base64/URL/SHA-256 tools, HTTPS inspector with GET/POST/PUT/DELETE, headers and body in source | Richer API collections, environment variables, multipart/streaming requests, package tooling |
| Git and GitHub | Local branches/staging/commits, GitHub fetch/fast-forward pull/non-force push, import snapshot, **manual conflict-marker resolution in editor** | Full three-way merge integration, GitHub PR/issue/OAuth workflows, authenticated remote acceptance |
| Tutorial and accessibility | 14-topic guide with Try/Skip/replay and saved progress, labeled controls, high-contrast palettes | Screen-anchored coach marks and full accessibility/device audit |
| Screenshot studio | Selection-only capture, syntax-colored image export, palettes, aspect ratios, line highlighting | Advanced layout presets and device QA |
| Quality and distribution | Android Gradle builds, JVM and instrumented tests, CI, prior production-signed upgrade proof | New feature commits require successful independent verification; physical-device QA remains |

## Verification checkpoint

- [Android 13/15 device smoke at d13de118](https://github.com/iamlxwis/CodeLab/actions/runs/45242235734) and [release workflow](https://github.com/iamlxwis/CodeLab/actions/runs/45242235726) are **pending** at Nov 24, 2026, 2:52 AM PHT. Do not imply a fresh APK exists until production publication completes.
- Previous source [75c1c051](https://github.com/iamlxwis/CodeLab/commit/75c1c051e77e9ddb0ee565dab0ad6c48fbe61ef4) passed Android 13/15 device smoke, but [release CI 45237884590](https://github.com/iamlxwis/CodeLab/actions/runs/45237884590) failed in signed-upgrade onboarding navigation waiting for Next. The new scripted test waits for each guide stage.
- Existing GitHub [latest release](https://github.com/iamlxwis/CodeLab/releases/latest) contains a signed `CodeLab-Python.apk` installer (~36.5 MB). The current release asset predates this manager source change.
- Source commits: [7636fda7](https://github.com/iamlxwis/CodeLab/commit/7636fda74405befd73e9469b74401f9e684edf73), [30e9ab56](https://github.com/iamlxwis/CodeLab/commit/30e9ab568f0ed994b6d1001e7d07b5ebf5d846ed), [0059a8ba](https://github.com/iamlxwis/CodeLab/commit/0059a8badbe8b4dbe6946aa55365ca6af8f3a964), [d13de118](https://github.com/iamlxwis/CodeLab/commit/d13de118984ba1c875960f9ef0b00c9699c46c5a).


## Cleanup safety

The tracked repository tree was reviewed for generated build directories, committed APKs, temporary archives and caches; none were identified. Shared source helpers were checked for references before deletion, and none were removed speculatively. Redundant explicit imports in the split editor, source view, command panel and console files were removed without changing their runtime logic. Preserve signing identity, Android manifest, runtime engines, user project storage and test fixtures.

For prior decisions, open [development memory](development/PROJECT_MEMORY.md) and [idea ledger](development/IDEAS.md). Git history preserves superseded plans; this inventory supersedes contradictory historical status text.
