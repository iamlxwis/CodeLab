# CodeLab engineering roadmap

This tracks implemented code separately from evidence and unfinished acceptance work.
From Workstream #4 onward the maintainer has authorized direct commits to `main`,
with GitHub Actions required for validation.

| Workstream | Implemented foundation | Remaining work |
| --- | --- | --- |
| #1 Architecture | Split activity, theme, workspace, dialogs, editor; memoized theme allocation | UI state hoisting and layout/performance audits |
| #2 Design | Material 3 tokens, restrained clay cards, project/library discoverability | Complete contrast, adaptive device and typography matrix |
| #3 Interaction | PRs #9–#14 integrated through PR #15: phone split, tab controls, run targets, Undo, search/replace, palette, error navigation, 200% font smoke | Manual TalkBack, keyboard and foldable acceptance |
| #4 Motion | Shared duration controller watches Android animator scale; even Full respects system disabled animations; existing tabs, panels, run and cards are connected | Fuller screen transitions, manual reduced-motion and device-wide jank review |
| #5 Editor | Cancellable viewport-highlighting; redundant worker jobs avoided for edits below visible viewport | Tree-sitter/LSP, semantic editing, IME matrix, multi-cursor, large-files |
| #6 Safety | Device-bound Keystore AES-GCM for optional cloud API token, automatic migration from plaintext; selective Git stage before commit; bounded imports | Explicit remote OAuth/push/pull, conflict resolution, hostile-file/provider testing |
| #7 Reliability | Serialized atomic saves, session restoration, curated baseline rules, startup/memory/gfx metrics, static security CI guard, 1x motion emulator samples | Generated baseline profiles, code coverage, lint baseline, multi-hour soak and low-end devices |
| #8 Documentation | Real emulator screenshots in README; architecture map, contributor guide, changelog, status/QA docs | Physical-device screenshots, stable-signing and release acceptance |

## Evidence

The `Android APK` workflow builds an optimized release APK, runs JVM tests, verifies
its signature and publishes the main-branch release. `Android device smoke` runs
offline JS/Lua/SQL/Java journeys, instrumentation tests, emulator frame/memory
measurements, 200% font inspection and accessibility hierarchy checks. Evidence
is valid **only for the tested SHA**. Production signing is not claimed without
stable signing secrets; fallback debug keys may change.

## Deliberate limitations

Rhino JS, LuaJ Lua, SQLite and limited BeanShell Java execute locally.
Other editing languages do not automatically have an offline runtime.
GitHub source import is a bounded ZIP download, not Git clone/sync.
Remote Git auth/push/pull, a PTY, full Tree-sitter/LSP and physical
accessibility certification are not implemented.

The entries above represent workstream **slices**, not proof of complete delivery.
