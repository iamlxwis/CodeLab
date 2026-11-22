# CodeLab engineering roadmap

Work is reviewed on feature branches, with **main** kept unchanged until approval.
Items are marked implemented only after source and tests exist; CI status is tracked
in the related pull request. Android minSdk 26, targetSdk 35 and offline-first local
execution remain unchanged.

| Order | Workstream | Dependencies and acceptance evidence |
| --- | --- | --- |
| 1 | Architecture cleanup | Split oversized source files, stabilize theme allocation, migrate system bars; then hoist UI state out of composables. Build and device smoke required. |
| 2 | Design system and adaptive UI | Semantic tokens, accessible themes, typography, library redesign, compact/expanded layouts; screenshots and contrast evidence. |
| 3 | Interaction and discoverability | Split panels, run target, tab controls, undo, search and accessibility audits; Compose UI coverage. |
| 4 | Motion | One reduced-motion-aware controller, transitions and measurements; emulator jank evidence. |
| 5 | Editor engine | Incremental highlighting, editing primitives, diagnostics and IME correctness; large-file tests. |
| 6 | Git/project safety | Keystore credentials, explicit remote operations, staging and hardened imports; offline and remote integration tests. |
| 7 | Reliability and performance | Autosave, profiling, baseline profiles, coverage, lint/security checks; measured baselines and regression thresholds. |
| 8 | Documentation and polish | README imagery captured from emulator, changelog, architecture map, contribution guide, truthful QA status. |

## Workstream 1 current change

- Separate activity, theme, workspace shell, project library, project/file/Git/command dialogs and native
  editor view without changing project or runtime state management.
- Memoize the theme scheme, typography and shapes; use edge-to-edge APIs rather
  than writing deprecated system-bar colors directly.
- Add an instrumented regression check for root rendering and transparent status bar.
- Still required: move business logic from composables to state holders, audit stability
  annotations and derived state, and verify edge-to-edge on different navigation modes,
  IME and large-display postures.

No performance improvement, accessibility score or full workstream completion is
claimed from source extraction alone. Refer to the PR checks for verification.

## Workstream 2 — initial visual foundation (in review)

- Build a shared spacing, shape and elevation vocabulary and use lightweight
  Material 3 clay surfaces for project cards and empty states.
- Use file-type icons consistently in the project file explorer and tablet sidebar
  rather than incorrectly representing every file as a folder.
- Remove the redundant online/offline project-library metric and correct the
  Favorites sorter to rank rather than filter.
- Keep the source editor, console, diagnostics and text inputs flat and
  high-contrast. Decorative shadows must not encode status or accessibility state.
- Still unverified: comprehensive semantic color migration, font bundling,
  visual screenshots from the new branch, 200% text scaling, WCAG palette matrix,
  adaptive device matrix, and the long-form dialog redesign.
- This is a reviewable first slice, **not** completion of Workstream 2.
