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
| IDEA-002 | 2026-11-23 | Run UI offers an intentional **Online** or **Offline** choice. Online has explicit consent before source leaves device. Offline prompts to install the runtime if absent, then runs without internet | In progress | Persisted explicit run mode in Run dropdown; prevents automatic offline-to-cloud fallback. Python missing-runtime dialog opens Languages; integration/device verification pending |
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
| IDEA-014 | 2026-11-23 | Fix the supplied phone screenshots across all five themes: readable text and icons, no clipped rectangular Glass/Clay glows, and consistent light/dark backgrounds. Validate real screens, not mocked thumbnails. | In progress | Root foreground fix, Canvas glow rendering and cross-theme contrast test in source; new APK/emulator/physical-device verification pending. |
| IDEA-011 | 2026-11-23 | **Native Spotify-style module management:** choose language, download with in-app progress, verify, run offline, update and remove inside CodeLab. The user must not be redirected to GitHub or manually manage separate APK files. Android-required installation confirmation may still be necessary for GitHub-distributed executable packages; show and manage that step from the CodeLab flow. | In progress | Python in-app DownloadManager progress/cancel/sig verification/system install/remove added in source; native install confirmation, update semantics and actual network/device QA still required. Play Store delivery must be separate. |

| IDEA-015 | 2026-11-23 | Resolve Android installation failures of GitHub release APKs, including signing-key continuity across published versions, certificate/ABI compatibility, clear project-preserving migration when signing keys differ, and real-device install/update acceptance. | In progress | User's package-invalid installer screenshot. Release workflow signing audit, source metadata and recovery notes added in source; stable production signing secrets and real phone acceptance remain pending (KI-017). |

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

## 2026-11-23 QA coverage extension

Source changes add an emulator UI journey for the persistent Run Offline / Run Online selector, verifying the selected menu state and returning to Offline before invoking the JavaScript runtime. This is added regression coverage for IDEA-002. Retain this item as in progress until the resulting emulator run passes.

## 2026-11-23 theme preview quality

IDEA-013: Appearance-gallery samples now use actual theme composables and surface tokens rather than independent fake palettes. Verification remains pending on the new source SHA and should include screenshots for all five styles.

## 2026-11-23 search UX repair

IDEA-011: emulator revealed the Python search result was hidden beneath a bulky availability panel. When searching, results now appear directly after search and filters, improving module discoverability. This remains in progress until the updated emulator test passes.

## 2026-11-23 — user-supplied appearance regression

IDEA-014 records the four screenshots: titles, project names and overflow icons become black on dark backgrounds, while Glass/Clay glows render as distinct rectangles. Source fixes are under test; retain this item until actual build and visual acceptance pass.

## 2026-11-23 — install reliability report

IDEA-015 captures the user's screenshot of Android rejecting `CodeLab.apk` as an invalid package. Same-build APK verification and emulator install pass, but fallback debug signing can change across CI runs. Upgrade conflict is a leading hypothesis, not a proven diagnosis without Android package-manager error or device details.

## 2026-11-23 — clean-install clarification

IDEA-015 revision: user explicitly states their normal process is **uninstall old CodeLab first, install new CodeLab APK second**, and that had always worked. This changes triage priorities: prove a clean installation of the exact release variant, investigate download integrity and handset Android compatibility, and collect an exact installer failure code. Signature continuity remains a future upgradability issue but is not an established explanation for this incident.

| IDEA-016 | 2026-11-23 | Redesign Projects and ZIP/GitHub Import with accessible layouts, useful states and secure existing import routing | In progress | New Compose UI source added; CI/screenshots pending |
| IDEA-017 | 2026-11-23 | Neutral Apple-inspired Liquid Glass visuals, with Android API/performance fallbacks, while preserving other four styles | In progress | Neutral palette and gradient diffusion added; no true pixel-sampled blur claim; visual QA pending |
| IDEA-018 | 2026-11-23 | Per-theme curated/custom accent and named save/edit/load/delete appearance presets, live preview, reset, persistence | In progress | Persistence and validation source added; editor UI and CI pending |
| IDEA-019 | 2026-11-23 | Clearly exposed Online/Offline choice beside Run, honest runtime status, module guidance and explicit upload consent | Approved | Dedicated selector and tests pending; existing consent route retained |
| IDEA-020 | 2026-11-23 | Practical immersive, skippable/replayable 14-topic interactive tutorial, safe for large fonts and existing users | Approved | Existing basic tutorial requires expansion and QA |
| IDEA-021 | 2026-11-23 | App-wide accessibility/visual/performance audit, real Android 13/15 screenshots, tests, successful installable release, accurate handoff commits | In progress | Build green before this checkpoint; emulator blocked by clean-install smoke bug now patched |

IDEA-018 CI note: Kotlin visibility mismatch from first source checkpoint corrected; build retry pending.

IDEA-018 editor UI source now implements custom hex, preset CRUD, load and style defaults; CI/device validation pending.

IDEA-019 visible execution-mode controls committed in source; run-mode persistence, consent, missing packs and large-font QA pending.

IDEA-020 now has a 14-topic progressive hands-on guide with action shortcuts in source. Screen-anchored coach-mark highlight overlays and Android acceptance pending.

IDEA-018 regression tests added for preset codec; IDEA-021 two-API emulator matrix requested/added in source, verification pending.

IDEA-019 source compile correction: unsupported vector icon InfoOutline replaced by standard Info. Verification pending.

IDEA-016: project card metadata now displays measured template/file count/description asynchronously; source-only, device QA pending.

IDEA-019 polish: eliminated duplicate hidden mode controls; choice remains visible in Run strip.
IDEA-020: aligned motion QA with revised onboarding; pending two-API smoke.

IDEA-021 evidence: Android 13+15 emulator jobs failed on text visibility after process restart; reproduced native-editor clipping in actual API35 screenshots. Source viewport responsive mitigation and authoritative file-byte QA added. Build retest pending.

| IDEA-022 | 2026-11-23 | Audit app functions for failures/incorrect results, repair Favorites view and other reproducible issues, preserve regression coverage | In progress | Source fixes Favorites/Recent, failed-create recovery and new JVM/emulator tests; CI and broader path coverage pending. |

IDEA-015 revision (2026-11-23): User explicitly wants future CodeLab versions to install as updates without uninstalling. Source now automates editor/Python version codes and guards production signing; permanent owner-managed signing secrets and same-certificate Android 13/15/physical-phone upgrade tests remain open. No tool should ask the user to send a signing key or password in chat.

IDEA-022 checkpoint: discovered and patched file creation collision corrupting optimistic tab state; added disk non-overwrite unit test and Android UI test. Still requires CI.

| IDEA-023 | 2026-11-23 | User chooses a GitHub Actions-only, Android-phone-friendly signing-key provisioning process; provide a clear step-by-step walkthrough for each GitHub repository secret, without Termux or a PC and without disclosing unencrypted keys in public artifacts | In progress | One-time keytool workflow, authenticated encrypted backup, standalone offline unlock page, full mobile guide authored. GitHub workflow key-generation test + end-user setup required. |

| IDEA-024 | 2026-11-23 | Keep concise README task tracker with precise done/remaining counts, current task, current verification, per-task statuses, and an auditable entry for every meaningful task progress checkpoint; prevent chat-hopping for status | In progress | README dashboard, audit baseline, agent commit policy added on main; continued compliance and verification required. |

| IDEA-025 | 2026-11-23 | Complete seven stabilization workstreams: comprehensive defect fixes, signed data-preserving upgrade proof, authentic optional offline runtimes, five-theme refinement, interactive tutorial/accessibility, Android 13/15 regression QA and accurate non-stable release. Work on main in verified milestones; never replace signing key or invent completion evidence. | In progress | Existing T01–T09 tracker breaks work into verification gates. First API35 smoke expectation corrected; signed upgrade test and all other gates remain pending. |

IDEA-015 / IDEA-023 / IDEA-025 correction (2026-11-23): Real APK certificates disprove previous `production` signing metadata; a11d15e, 7acd475 and ee24cd3 used three different Android Debug signers. Existing secret scope and real APK certificate checks fixed in source awaiting CI; T01 reopened. Preserve permanent signing key and require backed-up debug-to-production migration.

IDEA-015 / IDEA-025 2026-11-23 signing CI follow-up: Passing repo signing secrets into Gradle exposed a Kotlin DSL receiver-shadowing error (`storePassword = keyPassword`, `keyAlias = keyAlias`). [45204023605](https://github.com/iamlxwis/CodeLab/actions/runs/45204023605) failed release packaging. Outer env variables renamed to avoid property collisions; source plus static regression check committed; real production signing still not verified.

IDEA-015/023/025 milestone (2026-11-23): Verified first real existing-keystore production signer at [APK CI 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835), actual editor cert starts `c81d2aa1`; closes signing T01 only. Same-key data-preserving update T04 remains unverified until next release's Android 13/15 tests. IDEA-002/022/025: online approval bound to file, revision, mode, endpoint, language ID and consumed once; JVM regression authored, CI pending.

IDEA-025 T04 checkpoint 2026-11-23: First same-signer release run 45207076639 reached API33 emulator and genuine app, but QA action discovery failed because Appearance was nested in searchable Workspace sheet. Test helper now searches actual UI for Appearance/Languages, excluding EditText false matches. Still pending full install -r + persisted-data + Python proof; not finished.

IDEA-025 verified checkpoint (2026-11-23): T01 permanent signer DONE, T02 online status device smoke DONE, T04 same-signer Android API33/35 in-place `adb install -r` and source/theme/Python retention DONE via [45209632286](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286), with both API33/35 [device smoke success 45209632246](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246). Physical-phone update pending; broad remaining T03/T05/T06/T07/T08/T09 preserved. No false full-completion or stable label.

## Nov 24, 2026, 2:52 AM PHT — IDEA-022: installed/downloaded language manager

| ID | Added | Request / acceptance criteria | Status | Evidence / follow-up |
| --- | --- | --- | --- | --- |
| IDEA-022 | 2026-11-24 | Dedicated, discoverable manager must show which offline runtimes are installed/downloaded, permit user-driven download, cancellation, retry, Android-confirmed removal, update and reinstall, report per-module version/storage and let users clear installers without touching source projects. Download state must survive app closure; no fictional available packs. | In progress | Source commits 7636fda7, 0059a8ba, d13de118 add Python manager, DownloadManager persisted job and device UI checks; verify [release CI](https://github.com/iamlxwis/CodeLab/actions/runs/45242235726) and [device CI](https://github.com/iamlxwis/CodeLab/actions/runs/45242235734). Only Python optional, full pack marketplace / live download QA remain. |

The user's choice to prioritize Phase A over editor Phase B is explicit. Preserve missing modular-runtime work rather than closing IDEA-001/009/011/022 early.

Nov 24, 2026, 2:56 AM PHT: IDEA-022 UX revision. Dedicated Downloads pane separates transfer/cache/storage settings from Installed and Downloadable language results. Search defaults to broad language results, while the screen opens on Installed. Source commits ca7465f3 and ed89942c; [APK CI](https://github.com/iamlxwis/CodeLab/actions/runs/45243583986) and [device CI](https://github.com/iamlxwis/CodeLab/actions/runs/45243583725) still pending. No native C/C++/Rust/TypeScript pack claimed.

| IDEA-026 | 2026-11-24 | User explicitly authorizes finishing broader Phase 1 before Phase 2: current Python/JS/Lua packages, more feasible real offline runtimes, module lifecycle, explicit run modes and accurate CI; user handles handset acceptance but implementation must still compile and not overstate validation. | In progress | Java **beta** companion source this checkpoint; full javac, TypeScript, C/C++, Kotlin, PHP, Ruby, Rust, Go, C# and distribution acceptance remain. |

| IDEA-027 | 2026-11-24 | Engineering implementation of the approved TypeScript offline pack: official TypeScript single-file transpilation, signed companion IPC, no network, clearly disclose absence of static type checking and multi-file module resolution until implemented. | In progress | New engine-ts source added; not built or app-routed. |

| IDEA-028 | 2026-11-24 | Engineering optional SQLite runtime pack for modular architecture using Android platform SQLite in companion UID, preserving core fallback; do not claim standalone SQLite binaries. | In progress | Source and release wiring staged, CI pending. |

| IDEA-029 | 2026-11-24 | Route missing offline execution errors and Run-strip Languages directly to the corresponding module manager, not a generic language-listing dialog; retain explicit download/install consent. | In progress | Source changed for Python/TS/other optional packs; test pending. |

| IDEA-030 | 2026-11-24 | Modules must not display an active download action until an official GitHub release actually publishes the matching signed module APK; check metadata inside app, preserve cached signed installer flow, avoid automatic download. | In progress | Source RuntimeReleaseAssets + tests; CI and network/device QA pending. |

| IDEA-031 | 2026-11-24 | Maintain a grounded per-language Android offline compiler feasibility matrix with technical blockers/next evidence; only mark genuine, isolated, signed and offline execution done after tests. | In progress | docs/development/LANGUAGE_FEASIBILITY.md added; future real compilers remain open. |


### R37 — Full CodeLab Reimagined specification (Nov 24, 2026, 8:00:34 PM PHT)
Status: In progress. All 37 decisions supplied by owner are authoritative; supersedes historical uncertainty about exact decision labels. Implement the entire design, retain functionality and data, direct main commits, medium thinking requested, continuous concise PHT tracking, no numbered phases. Acceptance remains evidence-based; see REIMAGINED.md for per-decision audit.


R37 checkpoint — Nov 24, 2026, 8:44:57 PM PHT: source 03ef032 passed CI 45395252404; full contract in REIMAGINED_SPEC.md, acceptance 0/37 fully accepted. Status remains In progress pending API35, visual/functional/accessibility/performance and physical acceptance. Continue from PROJECT_MEMORY.md; retain approved requirements.


## Implementation-first instruction — Nov 24, 2026, 8:56:25 PM PHT

Owner explicitly deferred testing until the 37-decision implementation is finished. Stop waiting on CI/device verification; implement remaining source gaps now. Use [skip ci] implementation checkpoints without altering workflow triggers, cancellation policy or release/signing safeguards. No tests, smoke runs or builds are requested during this implementation pass; do not mark untested results verified. Source queue: shared motion-aware modal/menu layer; editable native Theme Studio preview; component typography controls; reorderable coding accessory; full per-project/named layouts and system-bar preference; contextual guidance; consistent loading/search/accessibility polish; real opt-in performance instrumentation. Existing source remains intact. Next: first four source gaps, then remaining four in the same sitting.


## Implementation-first handoff — Nov 24, 2026, 9:16:04 PM PHT

SESSION STOPPED; no background coding continues. Source checkpoints 16556e4b2bdd44a929ccd6f98ea67ac2a1317f64 and 2fded2e4e36dd27b7f1fb70b102e64ff580dadbd implement all eight audited source gaps, with all 37 decisions mapped to concrete source or retained QA/delivery infrastructure. No build, automated test, device smoke, screenshot inspection or performance experiment was run during this pass. [skip ci] commits defer automatic checks without changing workflows, cancellation, signing or release gates. The previous request to start manual API35 testing is superseded by the owner's implementation-first instruction for this pass.

New source: shared app-motion dialogs, full tool pages, actual anchored animated/keyboard-navigable menus; native editable Theme Studio preview and persisted independent typography sizes; reorderable coding shortcuts; full named/per-project output/system-bar/accessory layouts with live updates; persistent optional/replayable contextual discovery; animated loading/search/icon/tab polish and scalable status layout; bounded opt-in foreground-only performance instrumentation. Real performance measurements and the quality execution portions of decisions 20/29/36 remain deferred, not falsely complete.

Next: cumulative build/error checking after this source implementation pass, then the deferred functional/visual/device/accessibility/performance work. No APK includes the current changes yet. Continue from the latest source, preserve existing data/capabilities/security/runtime architecture and the complete approved contract.


## Build and direct APK delivery resumed — Nov 24, 2026, 9:39:00 PM PHT

Owner authorized the deferred build/error-check pass and requested a direct app download link. Added regression coverage for complete workspace profiles and theme typography sizes/legacy defaults; cumulative Android CI next. Existing explicit release checkpoint can dispatch manual API35 smoke and guarded publication on the same SHA; retain all release/signing guards and do not publish an older APK as the new source. Next: inspect compiler/test failures and fix them; then run the existing guarded checkpoint.


### UI10-001 — Nov 25, 2026, 12:27 AM PHT

Owner-supplied UI 10/10 quality specification: 1 chrome, 5 key row, 6 syntax, 3 language label, 4 preview, 2 header, 7 responsive, 8 launch, 9 platform, 10 consistency. No new product features except the explicit opt-in system color choice. Preserve existing preferences, accessibility, motion policy, dependencies, signing and security. Pending acceptance, not complete. UI 10/10 source checkpoint: toolbar collapse/overflow, symbol keys, new native syntax token inks, redundant header search removal, language-label cleanup, segmented preview, responsive thresholds and default launch color; 8 workstreams touched, none accepted; build/device checks pending; platform polish, localization, theme QA and actual 12-line measurement remain.


### Nov 25, 2026, 12:27 AM PHT — UI10-001 source follow-up

UI refinement source follow-up: opt-in API31 Material You mode, persisted new syntax ink overrides, real scheme JVM contrast assertions, accessible key test, compact Run, status language and predictive-back manifest opt-in; actual default Obsidian is black, so static splash matches it and the activity window uses saved colors. Full initial OS splash for user-selected light presets, predictive gesture audit/device tests, 12-line measurement, broader localization, haptics and style consistency remain pending. No new APK or test pass claimed.


### Nov 25, 2026, 12:32 AM PHT — UI10-001 refinement

UI10-001 source checkpoint: enforce usable medium-width split ranges, add preference-gated panel and handle haptics, keep preview log toggle fixed beside scrollable viewport choices, localize touched preview/header copy, add preview control regression and maximize keyboard space. Source-only; latest cumulative Gradle and emulator acceptance still required. Existing signing and security gates unchanged.


### Nov 25, 2026, 12:33:50 AM PHT — UI10-001 handoff

SESSION STOPPED — UI10-001 handoff. Source commits 852ef2e, bfe7366, f61a774 address portions of all ten workstreams; 0/10 accepted, 10/10 still require validation. Cumulative Android APK run 45438526779 pending at handoff. No new release, device screenshots, measured 12-line viewport, predictive gesture proof, complete localization or comprehensive consistency audit. Preserve approved pure-black Obsidian default; saved light palette applies to activity window but OS starting splash still needs validation. Next inspect exact-run Gradle build/JVM/instrumentation compilation/lint, fix failures, complete toolbar/language UI regression coverage, back audit and remaining design cleanup, then manual API35 and three-size/two-theme screenshots. Existing signing, data and security gates preserved. No assistant coding continues after this checkpoint.
