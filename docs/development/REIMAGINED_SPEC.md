# CodeLab — Reimagined: approved development contract

Owner supplied the complete specification on Nov 24, 2026. These are approved requirements for one comprehensive native Android/Kotlin/Jetpack Compose overhaul. Work on `iamlxwis/CodeLab`, branch `main`. Requested thinking level: medium. Do not introduce numbered development phases or unrelated features. This contract records requirements, **not completion**. Evidence belongs in [REIMAGINED.md](REIMAGINED.md).

## 01 — Overall design direction (1C): Apple Pro + Liquid Glass

Apple-inspired premium interface with an independent CodeLab identity; professional developer workspace, translucent navigation/contextual controls, refined typography/spacing/hierarchy, clean distraction-free coding. Avoid excessive cards, borders and unnecessary decorations.

## 02 — Theme customization (2C): Complete Theme Studio

Comprehensive customization, real-time previews, create/save/rename/duplicate custom themes, import/export, individual component customization and reusable presets. Users control application appearance.

## 03 — Existing themes (3C): Complete theme replacement

Replace old presentation with the Apple-inspired theme system. Preserve compatible preferences, projects and existing user data without unnecessary resets.

## 04 — Navigation architecture (4C): Adaptive Apple navigation

Floating navigation on appropriate main screens; compact contextual editor navigation; gesture-accessible panels; adaptive controls by activity/screen size; maximize coding space and eliminate redundant navigation.

## 05 — Animation direction (5B): Apple Fluid

Fluid spring animations, coordinated screen transitions, animated control states, smooth expansion/collapse, gesture-driven and interruptible interactions, adjustable behavior. No unnecessary delays or excessive bounce.

## 06 — Theme collection (6B): Six presets plus unlimited custom themes

| Preset | Appearance | Base color |
| --- | --- | --- |
| Obsidian | Pure AMOLED black | #000000 |
| Titanium | Neutral metallic dark | #17191C |
| Crystal | Neutral translucent glass | #252932 |
| Midnight | Deep navy | #090F1D |
| Porcelain | Clean light appearance | #F5F5F7 |
| Sandstone | Warm neutral appearance | #EAE3D7 |

No arbitrary count restriction on saved custom themes.

## 07 — Project dashboard (7C): Adaptive workspace dashboard

Compact Apple-inspired project lists, optional grid, recent/favorite projects, recognizable language icons, useful metadata, search/sort, contextual project actions and clear hierarchy. Remove redundant headings and unnecessary explanatory text.

## 08 — Editor interface (8C): Adaptive editor

Controls adapt to space, keyboard-aware layout, contextual tool visibility, customizable toolbar and pinned frequent tools. Reduce unnecessary chrome, prioritize source, preserve editing capabilities.

## 09 — Project creation (9C): Dual creation experience

Quick: minimal steps, project name, language, execution mode and immediate creation.
Guided: detailed language/template selection, beginner explanations, starter-code previews, offline runtime requirements and explicit online/offline selection. Users choose either experience.

## 10 — Branding (10C): Custom CodeLab monogram

Distinctive monogram, consistent launcher/splash/in-app branding, professional recognizable identity and animated presentation. Avoid generic terminal branding.

## 11 — Theme Studio: All 12 capabilities

1. Full RGB, HEX and HSL picker.
2. Individual component colors.
3. Glass blur and transparency.
4. Corner radius and borders.
5. Shadows and material depth.
6. Custom independent UI/editor typography.
7. Individual syntax colors.
8. Custom editor background.
9. Animation intensity.
10. Live interactive preview.
11. Theme import/export.
12. Automatic light/dark switching.

Customization must affect the actual app as well as the preview.

## 12 — Feedback (12B): Animations and subtle haptics

Refined touch feedback and subtle haptics where appropriate. Configurable intensity or availability; respect accessibility/system preferences. No unnecessary interface sound effects.

## 13 — Console (13B): Apple developer console

Structured output, clear execution status, distinguishable errors/warnings, refined controls, filtering, expandable console, improved editor separation and professional presentation.

## 14 — File explorer (14B): Expandable bottom panel

Apple-inspired expandable bottom sheet, nested hierarchy/expandable folders, language icons, active-file indicators, search, contextual operations, clear create-file/create-folder actions, smooth open/resize/dismiss.

## 15 — Settings (15B): Apple Settings-inspired

Searchable, recognizable icons, organized groups, dedicated subpages, concise descriptions; avoid overwhelming users with every setting at once. Required organization:

- Appearance
- Theme Studio
- Motion & Haptics
- Editor
- Languages & Runtimes
- Keyboard & Gestures
- Storage & Projects
- Privacy & Security
- Help & About

## 16 — Onboarding (16C): Complete adaptive onboarding

Animated introduction, optional interactive tutorial, contextual discovery, replayable lessons and skippable onboarding. Teach project creation, editing, execution, runtime downloads, file management and customization through practical guidance, rather than promotional slides.

## 17 — Gestures: All eight

1. Swipe between open files.
2. Swipe to reveal explorer.
3. Drag to resize editor/console.
4. Long-press contextual actions.
5. Pinch editor font size.
6. Swipe project rows for quick actions.
7. Drag/reorder open tabs.
8. Double-tap console header to maximize.

Accessible alternatives are mandatory. Avoid Android system-navigation and text-editing conflicts.

## 18 — Execution (18C): Adaptive execution controls

Explicit online/offline during creation and compact editor selector. Clearly show runtime availability, download requirements, running/stopped/success/failed states. Never silently switch execution mode. Preserve explicit consent before source upload. Keep internal provider branding out of ordinary controls while retaining required legal attribution.

## 19 — Glass (19C): Adaptive Liquid Glass engine

Best practical Android-native approximation: high fidelity where supported, lightweight fallback, adaptive performance, manual quality, configurable blur/transparency, neutral glass rather than excessive colored gradients, readable text.

## 20 — QA (20C): Strict visual fidelity

Repeated screenshot inspection and iterative corrections across every app screen, themes, screen sizes, keyboard interactions, animations, accessibility and rendering performance. Verify functionality after visual changes. Compilation alone does not establish visual quality.

## 21 — Typography (21C): Professional customization

Default UI Inter, editor JetBrains Mono. Selectable alternatives and custom imports, independent UI/editor selection, appropriate sizes/weights, consistent hierarchy and properly licensed assets.

## 22 — Icons (22C): Apple-inspired contextual system

Professional rounded icons, filled active states where appropriate, recognizable programming-language logos, contextual presentation, animated states, consistent size/alignment, accessible descriptions and licensed assets. No emoji application icons.

## 23 — Language Manager (23B): App Store-inspired pack library

Searchable collection, pack detail pages, installed/available indicators, real runtime availability, download/install controls and progress, clear offline requirements, professional presentation. Preserve runtime architecture and supported packs. Never fabricate pack sizes or capabilities.

## 24 — Dialogs (24C): Contextual presentation

Centered important confirmations, appropriate bottom action sheets, anchored quick menus, compact notifications, consistent destructive styling, appropriate animations and clear action hierarchy. Do not use the same oversized dialog for every interaction.

## 25 — Workspace: All nine capabilities

1. Customizable editor toolbar.
2. Pin/unpin frequent actions.
3. Reorder actions.
4. Interface density.
5. Editor/console proportions.
6. Show/hide status bar.
7. Distraction-free focus mode.
8. Remember layouts per project.
9. Save/restore layouts.

All personalization must persist correctly.

## 26 — Animation (26C): Complete motion customization

Reduced, Standard and Fluid presets; speed, spring intensity, transition effects, glass effects and haptic feedback. Respect Android accessibility and reduced motion.

## 27 — Accessibility (27C): Comprehensive

TalkBack, control descriptions, appropriate contrast, scalable type, adequate touch targets, reduced motion, color-vision-friendly indicators, keyboard navigation, responsive phone/tablet/landscape, accessible alternatives to gesture-only controls.

## 28 — Default (28A): Obsidian

Pure black AMOLED #000000, professional dark-first appearance, changeable at any time.

## 29 — Performance (29C): Adaptive

Responsive typing/scrolling; adapt effects to device capabilities and reduce expensive rendering as necessary; avoid unnecessary background processing; optimize startup/memory; smooth interactions without sacrificing editing. Measure actual performance rather than assuming improvement.

## 30 — Existing functionality (30B): Preserve capabilities, redesign access

Retain existing functional features, simplify discovery/navigation without removing tools, preserve projects/data/runtime capabilities/security/privacy. Avoid unrelated expansion or functionality downgrade.

## 31 — Import/export (31B): Apple Files-inspired

Clean file/project selection, clear file/folder information, appropriate previews, improved controls, progress/completion feedback, naming-conflict handling, preserve existing transfers and protect originals/project data.

## 32 — Splash (32C): Cinematic animated

Animated monogram, premium Apple-inspired launch, coordinated app transition, consistent branding and reduced-motion support. No artificial startup delay.

## 33 — Editor: All 12 enhancements

1. Active-line highlight.
2. Indentation guides.
3. Matching bracket highlight.
4. Improved line numbers/gutter.
5. Consistent syntax highlighting.
6. Language-specific file icons.
7. Customizable coding keyboard accessory.
8. Animated tab transitions.
9. Improved text selection.
10. Visible unsaved indicators.
11. Compact cursor/file status.
12. Keyboard-aware workspace.

Functional improvements, never decorative placeholders.

## 34 — App polish: All 12 enhancements

1. Redesigned empty states.
2. Animated loading/progress.
3. Skeleton loading where appropriate.
4. Consistent error/success notices.
5. Animated contextual menus.
6. Expandable/dismissible sheets.
7. Consistent destructive confirmations.
8. Edge-to-edge layouts.
9. Adaptive system status/navigation bars.
10. Professional concise copy without filler/emojis.
11. Consistent spacing/touch feedback.
12. Animated search/filtering.

Apply throughout the app, including screens absent from the original screenshots.

## 35 — Continuous tracking

Update README immediately at start and every meaningful checkpoint. Show activity, completed/remaining work and always next to build. Exactly one concise current activity entry; history separately. Philippine time UTC+8 and 12-hour AM/PM. Persistent continuation record. Separate implemented source from verified results; never mark unfinished work complete.

## 36 — Completion and quality

Implement the entire specification continuously within available environment/usage. Repeated fidelity checks; fix compilation/regressions; preserve functions. Verify theme customization/persistence, navigation/gestures, editor/console, runtimes, projects/files, accessibility, animation quality and performance. Produce a working installable APK. Separate source implementation, successful build, emulator verification and physical acceptance. Never claim complete implementation without evidence.

## 37 — Identity (37C): CodeLab — Reimagined

One comprehensive whole-app update. Not Phase 4, Phase 5 or another numbered development phase.

## Mandatory execution and continuation rules

Before implementation: inspect current source, README/tracker, Reimagined ledger/history, latest commits/Actions; audit all 37 for fully implemented, partial, missing and unverified requirements; resume the latest verified checkpoint without rebuilding completed work.

Commit directly to main. Do not repeatedly ask approval for approved requirements. Do not intentionally stop at one category. Preserve functional capabilities, offline architecture, privacy/security, production signing/release safeguards and optimized source-only CI. Keep cancel-in-progress and manual Android 15/API35 smoke. No unrelated features, phases, UI emojis or promotional copy. Fix build failures before delivery claims. Update README continuously and always state next to build.

Persistent references: README.md, REIMAGINED.md, PROGRESS_LOG.md and PROJECT_MEMORY.md. If execution limits interrupt, record exact checkpoint, remaining requirements, verification results and next action. Completion requires every associated requirement of all 37 decisions to be implemented and appropriately verified; Actions success alone is insufficient visual/functional evidence.
