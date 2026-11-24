# Final release acceptance

This checkpoint supersedes historical implementation-first instructions. Statuses: Not started, In progress, Implemented, Verified, Accepted, Blocked. Source presence and historical CI never establish current acceptance. Stable publication requires W01–W08, D01–D37 and U01–U10 accepted, plus independent same-commit device, production signing and upgrade gates. W09 is accepted only after public assets are verified. Production verification may run before acceptance; publication remains blocked.

**Current: 0/9 verified complete; 0/37 accepted; 0/10 UI accepted.**

## Stabilization

| ID | Workstream | Status | Acceptance criteria | Evidence / remaining |
| --- | --- | --- | --- | --- |
| W01 | Build reliability and API35 smoke | In progress | Real API35 smoke plus build/JVM/instrumentation/lint on candidate | Current evidence pending; see session memory |
| W02 | UI and UX polish | In progress | Every screen, two themes and three sizes reviewed | Current evidence pending; see session memory |
| W03 | 37 decisions | In progress | D01–D37 accepted | Current evidence pending; see session memory |
| W04 | 10 UI refinements | In progress | U01–U10 accepted | Current evidence pending; see session memory |
| W05 | Execution reliability | In progress | Six packs, offline execution/lifecycle, Web preview, consent/errors/cancel | Current evidence pending; see session memory |
| W06 | Signing and upgrade | In progress | Permanent key, six pack signatures, data-preserving install -r and checksums | Current evidence pending; see session memory |
| W07 | Performance/accessibility/stability | In progress | Measured runtime/startup/frame/memory plus accessibility, scaling and state checks | Current evidence pending; see session memory |
| W08 | Public documentation | In progress | Genuine current screenshots, accurate installation/release notes and working links | Current evidence pending; see session memory |
| W09 | Production release | In progress | All preceding criteria; signed same-commit smoke/upgrade/build and public assets | Current evidence pending; see session memory |

## Redesign decisions

| ID | Decision | Status | Acceptance criteria | Evidence / remaining |
| --- | --- | --- | --- | --- |
| D01 | Overall design direction (1C): Apple Pro + Liquid Glass | Implemented | Every requirement in REIMAGINED_SPEC.md section 01, exercised on current source | CodeLabTheme, neutral CodeLabBackdrop, shared themed presentation and adaptive workspace; current behavioral acceptance pending |
| D02 | Theme customization (2C): Complete Theme Studio | Implemented | Every requirement in REIMAGINED_SPEC.md section 02, exercised on current source | Theme presets, live native editor preview, component overrides, save/rename/duplicate/import/export; current behavioral acceptance pending |
| D03 | Existing themes (3C): Complete theme replacement | Implemented | Every requirement in REIMAGINED_SPEC.md section 03, exercised on current source | CodeLabVisualStyles canonical presets and legacy preference aliases; additive JSON fields; current behavioral acceptance pending |
| D04 | Navigation architecture (4C): Adaptive Apple navigation | Implemented | Every requirement in REIMAGINED_SPEC.md section 04, exercised on current source | HomeNavigation, compact editor controls, explorer sheet, responsive wide workspace and tool pages; current behavioral acceptance pending |
| D05 | Animation direction (5B): Apple Fluid | Implemented | Every requirement in REIMAGINED_SPEC.md section 05, exercised on current source | Shared LocalWorkspaceMotion, interruptible modal entrances, custom anchored-menu transitions and keyboard navigation; current behavioral acceptance pending |
| D06 | Theme collection (6B): Six presets plus unlimited custom themes | Implemented | Every requirement in REIMAGINED_SPEC.md section 06, exercised on current source | Exact approved base colors; uncapped recipes and stable lazy saved-theme rows; current behavioral acceptance pending |
| D07 | Project dashboard (7C): Adaptive workspace dashboard | Implemented | Every requirement in REIMAGINED_SPEC.md section 07, exercised on current source | ProjectLibraryScreen list/grid, favorites/recent metadata, icons, sorting, search and row actions; current behavioral acceptance pending |
| D08 | Editor interface (8C): Adaptive editor | Implemented | Every requirement in REIMAGINED_SPEC.md section 08, exercised on current source | Keyboard-aware WorkspaceEditorPane, customizable pinned toolbar and source-first focus layout; current behavioral acceptance pending |
| D09 | Project creation (9C): Dual creation experience | Implemented | Every requirement in REIMAGINED_SPEC.md section 09, exercised on current source | GuidedProjectSheet modes, language/template preview, verified runtime labels and explicit execution mode; current behavioral acceptance pending |
| D10 | Branding (10C): Custom CodeLab monogram | Implemented | Every requirement in REIMAGINED_SPEC.md section 10, exercised on current source | Launcher/splash vector assets, native animated mark and coordinated app entrance; current behavioral acceptance pending |
| D11 | Theme Studio: All 12 capabilities | Implemented | Every requirement in REIMAGINED_SPEC.md section 11, exercised on current source | RGB/HEX/HSL, component/material/syntax controls, fonts/sizes, real editable SourceView preview, JSON and system mode; current behavioral acceptance pending |
| D12 | Feedback (12B): Animations and subtle haptics | Implemented | Every requirement in REIMAGINED_SPEC.md section 12, exercised on current source | Configurable native touch feedback and system/app reduced-motion-aware custom animation policy; current behavioral acceptance pending |
| D13 | Console (13B): Apple developer console | Implemented | Every requirement in REIMAGINED_SPEC.md section 13, exercised on current source | RunConsole status, severity/text filters, warning/error styling, resizing, expansion, copy/export/history; current behavioral acceptance pending |
| D14 | File explorer (14B): Expandable bottom panel | Implemented | Every requirement in REIMAGINED_SPEC.md section 14, exercised on current source | Nested folders via explorerRows, active-file reveal, search, create/actions and expandable native sheet; current behavioral acceptance pending |
| D15 | Settings (15B): Apple Settings-inspired | Implemented | Every requirement in REIMAGINED_SPEC.md section 15, exercised on current source | ReimaginedSettings searchable destinations and dedicated configuration/tool pages; current behavioral acceptance pending |
| D16 | Onboarding (16C): Complete adaptive onboarding | Implemented | Every requirement in REIMAGINED_SPEC.md section 16, exercised on current source | FirstRunTutorial, real isolated-runtime InteractiveCodeLesson and persistent dismissible/replayable contextual tips; current behavioral acceptance pending |
| D17 | Gestures: All eight | Implemented | Every requirement in REIMAGINED_SPEC.md section 17, exercised on current source | SourceView pinch; editor/title swipe; explorer reveal; project swipe; tab drag/menus; panel resize; console double-tap; alternatives retained; current behavioral acceptance pending |
| D18 | Execution (18C): Adaptive execution controls | Implemented | Every requirement in REIMAGINED_SPEC.md section 18, exercised on current source | RunModeStrip/RunTargetMenu, verified companion snapshots, explicit creation mode and OnlineRunConfirmation; current behavioral acceptance pending |
| D19 | Glass (19C): Adaptive Liquid Glass engine | Implemented | Every requirement in REIMAGINED_SPEC.md section 19, exercised on current source | Native backdrop approximation, manual quality, transparency/blur/edges/shadows and battery/low-RAM fallback; current behavioral acceptance pending |
| D20 | QA (20C): Strict visual fidelity | In progress | Every requirement in REIMAGINED_SPEC.md section 20, exercised on current source | Existing screenshot/device/accessibility/font/motion scripts retained; execution deferred by owner; current behavioral acceptance pending |
| D21 | Typography (21C): Professional customization | Implemented | Every requirement in REIMAGINED_SPEC.md section 21, exercised on current source | Licensed Inter/JetBrains Mono, independent custom imports and saved UI/editor sizes consumed by real views; current behavioral acceptance pending |
| D22 | Icons (22C): Apple-inspired contextual system | Implemented | Every requirement in REIMAGINED_SPEC.md section 22, exercised on current source | 19 licensed language-logo vectors, rounded app icons, animated selected tint and accessible surrounding labels; current behavioral acceptance pending |
| D23 | Language Manager (23B): App Store-inspired pack library | Implemented | Every requirement in REIMAGINED_SPEC.md section 23, exercised on current source | LanguageManager search/filter/details, actual signed-pack states, install/download controls and progress; current behavioral acceptance pending |
| D24 | Dialogs (24C): Contextual presentation | Implemented | Every requirement in REIMAGINED_SPEC.md section 24, exercised on current source | Full pages for substantial tools, centered confirmations, expandable action sheets and real anchored animated menus; current behavioral acceptance pending |
| D25 | Workspace: All nine capabilities | Implemented | Every requirement in REIMAGINED_SPEC.md section 25, exercised on current source | WorkspaceLayout stores toolbar/pins/density/proportions/focus/editor+system status/output/maximize/coding-accessory preferences per project/named layout; current behavioral acceptance pending |
| D26 | Animation (26C): Complete motion customization | Implemented | Every requirement in REIMAGINED_SPEC.md section 26, exercised on current source | Reduced/Standard/Fluid; persisted speed/spring/intensity/transitions, glass and haptic controls; system reduction precedence; current behavioral acceptance pending |
| D27 | Accessibility (27C): Comprehensive | In progress | Every requirement in REIMAGINED_SPEC.md section 27, exercised on current source | Semantic loading/selection/control descriptions, popup arrow/Escape navigation, large-text status sizing and gesture alternatives; current behavioral acceptance pending |
| D28 | Default (28A): Obsidian | Implemented | Every requirement in REIMAGINED_SPEC.md section 28, exercised on current source | Pure black #000000 default with changeable saved presets; current behavioral acceptance pending |
| D29 | Performance (29C): Adaptive | In progress | Every requirement in REIMAGINED_SPEC.md section 29, exercised on current source | Incremental editor work, bounded lazy lists, adaptive glass and opt-in foreground-only real frame/input/launch/PSS recorder; current behavioral acceptance pending |
| D30 | Existing functionality (30B): Preserve capabilities, redesign access | Implemented | Every requirement in REIMAGINED_SPEC.md section 30, exercised on current source | Existing tools/engines/projects/security retained; preference/layout JSON additions preserve legacy values; current behavioral acceptance pending |
| D31 | Import/export (31B): Apple Files-inspired | Implemented | Every requirement in REIMAGINED_SPEC.md section 31, exercised on current source | ImportReviewDialog/DocumentPreview/ExportReviewDialog with guarded copies, conflicts and actual progress/notifications; current behavioral acceptance pending |
| D32 | Splash (32C): Cinematic animated | Implemented | Every requirement in REIMAGINED_SPEC.md section 32, exercised on current source | Native animated monogram, app entrance and reduced-motion custom-transition behavior; no artificial wait; current behavioral acceptance pending |
| D33 | Editor: All 12 enhancements | Implemented | Every requirement in REIMAGINED_SPEC.md section 33, exercised on current source | Native gutter/active line/indent/brackets/syntax/selection, logos, reorderable keyboard accessory, animated tabs, dirty/cursor/IME status; current behavioral acceptance pending |
| D34 | App polish: All 12 enhancements | In progress | Every requirement in REIMAGINED_SPEC.md section 34, exercised on current source | Shared page/alert/menu layer, animated skeleton/search/icon/tab changes, empty states, destructive styles and adaptive system bars; current behavioral acceptance pending |
| D35 | Continuous tracking | Implemented | Every requirement in REIMAGINED_SPEC.md section 35, exercised on current source | One README current activity, PHT AM/PM timestamps, separate history, source-vs-validation ledger and current continuation memory; current behavioral acceptance pending |
| D36 | Completion and quality | In progress | Every requirement in REIMAGINED_SPEC.md section 36, exercised on current source | Existing build/signing/release/device gates retained; new-source build, error checks and installable APK execution deferred; current behavioral acceptance pending |
| D37 | Identity (37C): CodeLab — Reimagined | Implemented | Every requirement in REIMAGINED_SPEC.md section 37, exercised on current source | One comprehensive update; canonical approved contract and persistent source implementation ledger; current behavioral acceptance pending |

## UI refinement

Criteria transcribed from the owner’s CodeLab_UI_10-10_prompt.md; source changes are in 852ef2e, bfe7366 and f61a774. All items require Gradle/JVM/instrumentation/lint and Obsidian/Porcelain screenshots on 360x800dp, landscape phone and >=840dp tablet.

| ID | Workstream | Status | Acceptance criteria | Evidence / remaining |
| --- | --- | --- | --- | --- |
| U01 | Editor chrome | Implemented | At least 12 visible lines on 360x800dp with keyboard; actions reachable; visibility logic tested | Current full device/screenshot acceptance pending |
| U02 | Header | Implemented | Active file subtitle, compact Run semantics and title fit | Current full device/screenshot acceptance pending |
| U03 | Language status | Implemented | Noninteractive status outside navigation; status visibility preference works | Current full device/screenshot acceptance pending |
| U04 | Preview controls | Implemented | Viewport single selection, separate log toggle, semantic state and UI regression | Current full device/screenshot acceptance pending |
| U05 | Symbol keys | Implemented | Uniform >=44x48dp keys, press/haptic policy and six layouts | Current full device/screenshot acceptance pending |
| U06 | Syntax | Implemented | New kinds across palettes, contrast, bounded viewport work and regressions | Current full device/screenshot acceptance pending |
| U07 | Responsive layout | Implemented | Shared 600/840 thresholds; usable panels, landscape/foldable tests | Current full device/screenshot acceptance pending |
| U08 | Splash | In progress | Default Obsidian match; light startup and reduced motion reviewed | Current full device/screenshot acceptance pending |
| U09 | Platform | In progress | Predictive back audit, opt-in system color, gated haptics and localized touched copy | Current full device/screenshot acceptance pending |
| U10 | Consistency | In progress | Shared shapes/spacing/elevation; six palette contrast and clean first run | Current full device/screenshot acceptance pending |
