# CodeLab — Reimagined: current continuation record

Updated Nov 24, 2026, 8:38:05 PM PHT. Session ACTIVE at this checkpoint; README records the final session state. Historical checkpoint text has been preserved without deletion in [PROJECT_MEMORY_ARCHIVE.md](PROJECT_MEMORY_ARCHIVE.md); milestone history is [PROGRESS_LOG.md](PROGRESS_LOG.md). This file supersedes obsolete pending-build claims in that archive.

## Approved scope

Read [REIMAGINED_SPEC.md](REIMAGINED_SPEC.md): the complete owner-approved 37 decisions and all associated requirements. This is one comprehensive redesign, not another numbered phase. Do not restart planning or ask approval for approved work. Requested thinking level: medium. Direct commits to `main`.

## Current source and evidence

- Latest implementation: `03ef0325cf68de989add4622796bc9c0450191e3`; [CI 45395252404](https://github.com/iamlxwis/CodeLab/actions/runs/45395252404) running for the cumulative source including keyed lazy theme rows.
- Latest verified implementation: `97e31ad2668de8cb127c7c1bc9a171d16cd7e013`; [CI 45394263948](https://github.com/iamlxwis/CodeLab/actions/runs/45394263948) passed; [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45394263948/artifacts/14920642082), packs 14920337964, reports 14920756656.
- Prior verified `2b04bd19f0e17f697baf325ff1a08b8a51e27b0f`: [CI 45392971931](https://github.com/iamlxwis/CodeLab/actions/runs/45392971931) passed.
- Verified implementation `f2990a971982f54f14568c1ce65a10186c291e5c`: [CI 45391600449](https://github.com/iamlxwis/CodeLab/actions/runs/45391600449) passed release builds, JVM tests, instrumentation Kotlin compilation, lint, security and signature checks. [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45391600449/artifacts/14919956476), [six packs](https://github.com/iamlxwis/CodeLab/actions/runs/45391600449/artifacts/14919582010), [reports](https://github.com/iamlxwis/CodeLab/actions/runs/45391600449/artifacts/14919048305).
- Previous `bb5172d7957f1584a39cf4679e893f4b6d6e60f1`: [CI 45390387550](https://github.com/iamlxwis/CodeLab/actions/runs/45390387550) passed settings/motion/live-editor preference source.
- No redesigned-source API35 emulator or physical-device acceptance in this session. **0/37 fully accepted**, distinct from implemented source and passing Android builds. Earlier 0/10 workstreams were a grouping, not a count of decisions.
- Local Gradle bootstrap cannot download its distribution (Network is unreachable); Android SDK/adb are unavailable locally. Static security and eight Python release/artifact checks passed. All 19 language-vector XML assets parsed. These are not emulator evidence.

## Implemented during this session

1. Searchable nine-category Settings; editor/keyboard/storage/privacy/help pages, direct Theme Studio, motion controls and offline legal-notice viewer.
2. Already mounted editors observe preference changes, retaining unsaved source and cursor state.
3. Reduced/Standard/Fluid presets; persisted speed/intensity/spring and None/Fade/Slide selection; JSON compatibility and reduced-motion precedence; None reaches shared custom workspace transitions.
4. Practical optional editing/running lesson using the real CodeEditor and existing RunSession isolation, actual output-based completion, stop on dismissal and return to the same guide topic. No simulated run or source upload.
5. Nineteen native language-logo vectors; Simple Icons CC0 pinned d59bb32d4f94b1aa8c7cef9d264280d35d5f8f97, Devicon Java MIT, bundled licenses and provenance.
6. Focus mode saved in per-project/named layouts, with legacy fields preserved.
7. Distinct console warning/error prefixes, severity/text filters and contrast-aware display; existing raw output/export/copy and cross-line ANSI preserved. Warning output updated consistently in app JS, JS pack and TS pack, without changing architecture.
8. Explicit theme rename/duplicate preserving all recipe fields and giving copies distinct IDs; compact contextual menus; direct Studio scroll target.
9. Exact approved Crystal/Sandstone base colors and neutral backdrops; no Sandstone decorative lines; zero glass border removes the stroke; battery-saver-aware blur without polling.
10. Persistent search/control descriptions, contract, per-decision audit and honest tracker.

## Additional runtime selector correction (source and JVM checks passed; device pending)

Further source audit found the run-target picker supplied only Python to its installed-module set, hiding verified TypeScript under Runnable only. New shared InstalledRuntimeState checks existing signature/protocol validators, refreshes on lifecycle resume rather than on every keystroke, and supplies all verified module extensions to the picker and editor selector. Guided setup's status and Ready offline filter now use actual installed/bundled availability; missing TypeScript is no longer always labelled planned. Ordinary run labels use language names rather than internal JS/Lua engine brands. Added selected-state semantics for execution/language controls, JVM trust-state coverage and installed-TypeScript picker instrumentation regression. No automatic mode switching, uploads, downloader or runtime architecture changes.


Runtime correction 97e31ad passed CI 45394263948. Saved-theme rows now render lazily under stable IDs without limiting collection size; that latest rendering source requires a fresh build. No emulator or physical acceptance.

## Next unfinished work

1. Check latest source SHA Actions; fix compiler/test regressions before any delivery claim. Download/check APK artifacts when successful.
2. Run existing **manual** Android 15/API35 `device.yml` against current source. Inspect all test failures and fix actual regressions, preserving safeguards.
3. Inspect real screenshots across **every screen**, six themes, small phone/tablet/landscape, IME and larger font settings. Check preview and actual theme fidelity. Fix observed screen and motion defects; Material contextual/control animations still need reduced-motion acceptance.
4. Execute all eight gestures with alternatives and system/text-selection conflict checks; test toolbar, named/per-project layouts and focus persistence through restart/rename.
5. Execute practice lesson; test real runtime execution, consent, pack lifecycle, file/project operations, import/export conflicts, theme/font JSON transfer and custom typography persistence.
6. Record TalkBack/keyboard/touch-target/contrast results and actual startup/typing/frame/memory measurements. Prior performance claims or old screenshots do not satisfy this redesign.
7. Physical-device acceptance remains with owner; do not infer it from Actions. Final public release still requires existing same-commit device/signing gates.

## Current verification blocker

GitHub connector exposes no workflow-dispatch operation; `gh`/git push have no local credential; browser fallback is signed out. Do not change manual-only triggers, bypass gates or fabricate a device run. Repository mutations work through the authorized GitHub connector. Clone/fetch work, but shell push does not. New source is already published on main.

## Tracking and safeguards

- README: one concise current activity, completed/remaining distinction and exact next task; PHT UTC+8, 12-hour AM/PM; meaningful checkpoint updates. Mark session STOPPED before final reply, separately from independent CI activity.
- Append history to PROGRESS_LOG; keep this record current and acceptance ledger evidence-based. Keep new owner ideas in IDEAS until verified complete.
- Preserve all existing functionality/data, offline runtime architecture, six packs, explicit online upload consent, encrypted credentials, production signer/release gates, source-only CI, cancel-in-progress and manual API35 policy. No unrelated features, phases or UI emojis.

## Reusable continuation prompt

Continue CodeLab — Reimagined in iamlxwis/CodeLab main using REIMAGINED_SPEC.md, README, REIMAGINED.md and PROJECT_MEMORY.md. Inspect current source and Actions first, resume the latest verified checkpoint, fix exact-source CI failures, then complete remaining visual/functional/accessibility/performance acceptance and evidence-driven fixes. All 37 decisions are approved; do not restart planning or add phases. Commit directly to main, preserve functionality/data/runtime/security/optimized CI/manual API35 testing, and keep one concise README activity plus next-to-build in 12-hour Philippine time. Distinguish source, build, emulator and physical acceptance; do not claim complete until supported.
