# CodeLab persistent idea ledger

Updated: 2026-11-23. **This is the authoritative, durable list of user requests and decisions.**

## Ledger rules

1. When the user introduces a new idea, requirement, correction, or design rule, record it in this file **in the same development session**, even before work begins. A proposal can be marked `Proposed` until accepted, but it must not be lost.
2. Every idea has an immutable `IDEA-NNN` ID, date, owner (user/request or engineering), status, and concrete acceptance criteria. Link relevant decisions and commits.
3. Allowed status values: `Proposed`, `Approved`, `In progress`, `Blocked`, `Verified complete`, `Superseded`. Do **not** mark verified complete without code + appropriate test results.
4. Never delete an idea row, even after completion. Preserve completed and superseded rows here, with evidence and replacement links. If an approved item changes, append a clear revision history and update the older row.
5. A future AI must check this ledger alongside `AGENTS.md` and `PROJECT_MEMORY.md` before coding. Roadmap priorities may change; pending requirements do not silently disappear.

## Ideas and requirements

| ID | Added | Request / acceptance criteria | Status | Evidence / follow-up |
| --- | --- | --- | --- | --- |
| IDEA-001 | 2026-11-23 | CodeLab is a lightweight Android editor; all optional execution engines are separate installable/removable language modules, **including Python** | In progress | Python companion passed emulator; JS/Lua/Java still bundled. [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) |
| IDEA-002 | 2026-11-23 | Run UI offers an intentional **Online** or **Offline** choice. Online has explicit consent before source leaves device. Offline prompts to install the runtime if absent, then runs without internet | Approved | D-002, D-006; full choice and module installation incomplete |
| IDEA-003 | 2026-11-23 | Allow editing and provide examples for every catalog entry; project choices Beginner Example, Hello World, Blank | In progress | StarterExamples + GuidedProjectSheet committed; complete device acceptance pending |
| IDEA-004 | 2026-11-23 | Five high-quality themes: Minimal, Glass, Clay, AMOLED, Paper; simplify menus and screens | In progress | AppearanceGallery committed; overall screen-wide fidelity and accessibility unverified |
| IDEA-005 | 2026-11-23 | No emojis anywhere in the APK; use consistent professional vector icons and typography | Approved | D-007; complete UI audit pending |
| IDEA-006 | 2026-11-23 | Provide a **development Second Brain** stored in GitHub, not an in-app AI chatbot. It must tell future assistants what was completed, pending, blocked and next | In progress | AGENTS.md and development docs committed; retention enforcement added by this commit |
| IDEA-007 | 2026-11-23 | **Every newly introduced thought/request remains recorded until done.** New sessions must retain unfinished items; completed ones remain in history, never silently deleted | Approved | D-012; this ledger and AGENTS.md rule |
| IDEA-008 | 2026-11-23 | Direct commits to main with frequent verification and handoff updates, unless user changes workflow | Approved | D-009; AGENTS.md |
| IDEA-009 | 2026-11-23 | Support popular offline languages via modular engine packs, prioritize Python, JavaScript, Lua, C/C++, TypeScript, Java/Kotlin, PHP/Ruby, then feasibility for Go/Rust/C# | Approved | docs/ENGINE_PACKS.md; runtime packs not yet delivered |
| IDEA-010 | 2026-11-23 | Python runtime must actually pass offline device execution, including stdout, stdin and error handling, rather than merely building | Verified complete | APK + emulator + connected instrumentation passed at `216a58aa` ([CI](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400)); physical-device coverage is a separate unresolved quality gate |
| IDEA-012 | 2026-11-23 | Deliver a brief, skippable first-launch tutorial covering project creation, editor execution and modules; persist completion and support replay from Settings | In progress | Three-step onboarding added to app source and emulator smoke; CI pending |
| IDEA-013 | 2026-11-23 | The five advertised themes must have materially different **real UI rendering** across the app, not just names/previews/accent swaps: Glass layered blur/transparency, Clay tactile elevation, AMOLED true black, Paper warm textured light, Minimal clean flat | In progress | Backdrop and surface treatments committed; screenshot/contrast/device acceptance pending |
| IDEA-011 | 2026-11-23 | **Native Spotify-style module management:** choose language, download with in-app progress, verify, run offline, update and remove inside CodeLab. The user must not be redirected to GitHub or manually manage separate APK files. Android-required installation confirmation may still be necessary for GitHub-distributed executable packages; show and manage that step from the CodeLab flow. | Approved | Correction to IDEA-001/002: prior Python download link UX is wrong; feature not implemented. Investigate Play Feature Delivery for store channel and verified package installer for standalone releases. |

## Revision history

- 2026-11-23: Created persistent ledger and non-deletion policy after user clarification. Earlier requirements reconstructed from agreed project decisions; if any historical detail differs from original intent, preserve the row and append a correction.

## 2026-11-23 implementation evidence

IDEA-001 / IDEA-010: removed stale bundled Python runtime metadata from the
core (source patch; unit tests pending). IDEA-003: Web project template is now
searchable in the guided creation browser (source patch; emulator retest pending).
No new approved user requirement was dropped or marked complete.

## 2026-11-23 verification milestone

The original IDEA-010 requirement is now **verified complete on emulator** at `216a58aa`, with [passing APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) and [passing device smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400). Preserve IDEA-010 as historical verification evidence. IDEA-001/002/003/004/005/006/007/008/009 remain tracked at their stated statuses; Python's success does not imply the full modular marketplace or end-to-end UI redesign is complete.

## 2026-11-23 clarification — module UX

IDEA-011 records the user's specific correction: downloadable language modules must be installed, updated and removed *from inside CodeLab*, with visible progress; running the downloaded module must occur through the CodeLab editor/console. A browser redirect to a GitHub APK download does not satisfy this requirement. Preserve this item until real user-facing functionality passes device tests. Continue to preserve IDEA-001, IDEA-002 and IDEA-010 and their history.
