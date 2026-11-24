> **Current Phase 3 status (Nov 24, 2026 · 5:37 PM PHT):** Project Management & Developer Workflow **7/7 source/CI complete** on [`55a65774d56e`](https://github.com/iamlxwis/CodeLab/commit/55a65774d56e949dd06ca1eb1db306026983ed0b) with passing [Android APK workflow 45366121115](https://github.com/iamlxwis/CodeLab/actions/runs/45366121115) and three artifacts. Owner physical-device acceptance and real GitHub credential integration checks remain pending. Historic Phase1/2 entries below are retained as past scopes; declined advanced Phase2 features are not part of this acceptance. [Details](IMPLEMENTATION_STATUS.md).

> **Current next-work priorities (superseding the older workstreams below):** [docs/development/ROADMAP.md](development/ROADMAP.md). New product requirement: online/offline user choice and optional install of **all** language runtimes, including Python.

# CodeLab engineering roadmap

This tracks implemented code separately from evidence and unfinished acceptance work.
From Workstream #4 onward the maintainer has authorized direct commits to `main`,
with GitHub Actions required for validation.

| Workstream | Implemented foundation | Remaining work |
| --- | --- | --- |
| #1 Architecture | Split activity, theme, workspace, dialogs, editor; memoized theme allocation | UI state hoisting and layout/performance audits |
| #2 Design | Material 3 tokens, restrained clay cards, project/library discoverability | Complete contrast, adaptive device and typography matrix |
| #3 Interaction | PRs #9–#14 integrated through PR #15: phone split, tab controls, run targets, Undo, search/replace, palette, error navigation, 200% font smoke | Manual TalkBack, keyboard and foldable acceptance |
| #4 Motion | Central duration controller observes system scale; even Full respects disabled animations; lightweight project/language transitions and tablet-sidebar fade/slide | Full navigation choreography, manual reduced-motion and physical-device jank review |
| #5 Editor | Cancellable viewport highlighting, less offscreen work, logical-line indent/outdent, IME and near-limit file persistence tests | Tree-sitter/LSP, semantic editing, extended IME matrix, multi-cursor and multi-MiB files |
| #6 Safety | Device-bound encrypted cloud and GitHub tokens; selective local Git staging; GitHub HTTPS origin, user-triggered Fetch, fast-forward Pull and non-force Push | Real remote auth/network tests, OAuth, merge conflict UX and hostile-provider matrix |
| #7 Reliability | Serialized atomic saves, session restore, curated baseline rules, security gate, near-limit persistence test, lint/JaCoCo reports and release-mode 1x emulator jank | Generated baseline profiles, enforced lint/coverage thresholds, soak and low-end devices |
| #8 Documentation | Emulator screenshots, GitHub sync guide, architecture map, contributor guide, changelog/status/QA docs and 0.5.0-alpha | Physical-device screenshots, stable signing and release acceptance |

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
GitHub HTTPS sync supports user-supplied tokens, but not an OAuth account
flow or conflict editor. A PTY, full Tree-sitter/LSP and physical accessibility
certification are not implemented.

The entries above represent workstream **slices**, not proof of complete delivery.
