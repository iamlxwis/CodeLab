# Active development roadmap

Updated 2026-11-23. Ordered by dependencies, not feature marketing priority.

## P0 — build and runtime reliability

- [ ] **IDEA-015 / KI-017 — BLOCKER:** Resolve clean-install rejection after user *already uninstalled* the previous CodeLab. New source QA explicitly clean-installs the release APK and checks launch on emulator (CI pending). Verify actual release download SHA-256/size, Android version and package installer failure code on phone; signing continuity is a separate future upgrade issue.
- [ ] Check the source-stage release audit, corrected checksum packaging and latest tag alignment in the APK CI run triggered by this commit.


- [ ] **IDEA-014 / KI-015:** Verify on APK, Android instrumentation, emulator screenshots and physical phone that all five themes have readable text/icons and no rectangular Glass/Clay glows; code and screenshot test added, latest CI pending.
- [ ] **KI-016:** [Emulator run 45145653756](https://github.com/iamlxwis/CodeLab/actions/runs/45145653756) failed in motion benchmark after functional phases; source now navigates to the library before measuring. Retest full device workflow.


- [ ] **KI-014:** Verify Languages sheet explicit Close action returns to project library after visiting Python details; previous [device fail](https://github.com/iamlxwis/CodeLab/actions/runs/45144260324). Source fix committed, CI pending.

- [ ] **KI-013:** Retest Language Manager search with status panel hidden during search, ensuring Python row is visible/clickable; [previous failure](https://github.com/iamlxwis/CodeLab/actions/runs/45142776614). Source fix committed, CI pending.

- [ ] Verify expanded Android smoke: first-run guide, Glass/Minimal gallery, Python Language Manager installed-pack controls, run mode selection, and complete offline runtime suite. Source committed; CI pending.

- [ ] Verify updated `qa/device_smoke.py` covers tutorial and Run mode selection and still passes full offline runtime journey. Changes committed; CI pending.

- [x] Verify Python bundled-registry cleanup: [APK CI passed](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) on `216a58aa`.
- [x] Verify searchable Web template: [emulator passed](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) on `216a58aa`.

- [x] APK builds at source SHA `3c37ae8c`; see [build](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978).
- [x] Diagnose [emulator Python-output timeout](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951). Actual UI trace confirms `Exit 1`: Chaquopy reads SharedPreferences from an isolated process where UserManager is unavailable.
- [x] Separate signed Python companion APK using its own UID and signature-checked IPC; [emulator offline Python passed](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) including stdio/error instrumentation.
- [x] Both [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) and [device smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) passed for `216a58aa`.
- [ ] Verify real device API/ABI coverage; Python integration currently targets arm64-v8a and x86_64, not 32-bit-only devices.

## P1 — optional module architecture (approved end state)

- [ ] **IDEA-011:** Python now has in-app DownloadManager download/progress/cancel, signature verification and launch to Android-confirmed install/uninstall. Still needs real download/install/update/rollback/error-case device QA and production signing; separate Google Play-compliant dynamic feature delivery. Other languages pending.
- [ ] Expand bug audit beyond green CI: installation/uninstall/update/error states, quick-run/cloud/offline routing, project data durability, UI navigation and accessibility, and real-device compatibility. Record reproducible defects and test coverage in KNOWN_ISSUES.md.


- [ ] Implement a versioned `EngineProvider` / capability contract and accurate installed/not-installed/loading/error states.
- [ ] Choose supported Android distribution model for **GitHub APK**, with signed companion packages (or validated alternative); explicitly verify package identity/certificates, version and ABI. Consider Play on-demand features separately for a Play Store channel.
- [ ] Authenticated, explicit and isolated IPC between core editor and each runtime package. Test cancellation, timeouts, bounded input/output and failure modes.
- [ ] Build a real Language Module Manager: list, sizes/permissions/provenance, install, update, uninstall, offline state, version/compatibility and storage usage. Do not offer fake install actions.
- [ ] Test new explicit Online/Offline Run selection on emulator, including unavailable provider and Python-missing-pack states. Source implementation committed; no automatic offline-to-cloud fallback.
- [ ] Migrate **all optional language engines**, including Python, JavaScript and Lua, out of the core APK only after working replacements are tested. Keep local editor and Android platform UI functionality intact.
- [ ] Make runtime state depend on actual installed/verified modules, not hardcoded roadmap assumptions.

## P2 — expand real offline language coverage

- [x] First separately installable Python APK (Chaquopy in companion app; core editor is 3.53 MB), [verified on emulator](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400). Automatic in-app pack installer/uninstaller is still pending.
- [ ] Downloadable JavaScript and Lua modules; assess grouping TypeScript transpiler with JS without silently bundling either.
- [ ] Valid Android-compatible C/C++ toolchain prototype, with ABI/SDK tests.
- [ ] Full Java/Kotlin compilers versus existing limited BeanShell Java beta.
- [ ] Evaluate PHP, Ruby, Go, Rust and C# feasibility; only promise working features after verified install and run.
- [ ] Preserve starter examples, editing-only entries and optional online compilers for languages without offline engines.

## P3 — quality, design and distribution

- [ ] Verify first-launch tutorial shows once, supports Skip/Next/Start and Settings replay, is accessible at 200% font and survives restart. Source/UI journey added.
- [ ] Verify all five themes now differ in actual rendered screens and remain legible in editor/console; examine emulator screenshots and contrast. Source rendering upgraded.

- [ ] Replace overloaded UI controls across remaining screens; audit Glass/Clay fidelity, contrast, performance, reduced-motion and TalkBack behavior. Use **professional vector icons, no emojis**.
- [ ] Real-device smoke + performance, long-running cancellation, ABI coverage, source privacy review.
- [ ] Stable production/release signing before distributing updatable companion runtime packages; avoid breaking existing users.
- [ ] Keep docs, changelog, screenshots and release claims aligned with measured behavior.

## Persistent requirements

Every user suggestion, even when not yet implemented, belongs in [IDEAS.md](IDEAS.md) with an immutable ID and status. Keep it until **verified complete**; preserve completed and superseded entries indefinitely as history. Update it in the same development session as the request.

## Commit process

User approved direct commits to `main`. **In the same commit as implementation work**, update `docs/development/PROJECT_MEMORY.md`, and any relevant completed/roadmap/decision/issue docs. Store actual CI links after jobs complete in a follow-up docs-only commit if necessary.

## UI/UX redesign workstream (2026-11-23)
- [ ] IDEA-016: validate Projects and Import redesign, test ZIP/GitHub paths on emulator.
- [ ] IDEA-017: verify neutral translucent Glass and other four styles with actual device screenshots; document no platform-wide pixel-sampled blur.
- [ ] IDEA-018: finish appearance editor for custom hex and saved recipes; verify persistence and restore.
- [ ] IDEA-019: move execution mode into a visible selector by Run, show module/provider status and maintain consent.
- [ ] IDEA-020: replace three-page introduction with a practical 14-topic replayable guide and guided actions.
- [ ] IDEA-021: run Android 13/15, large fonts, and complete release QA; retain phone clean-install blocker independently.

- [ ] Verify visibility correction via new CI before continuing screen-level changes.

- [ ] Test IDEA-018 AppearanceGallery custom accent validation, multi-style preset CRUD and persistence across app restart; check large-font screenshots.

- [ ] Check Offline/Online chips and Module/Configure actions on emulator. Verify persisted state and Run buttons with 200% font; inspect source upload confirmation.

- [ ] Validate FirstRunTutorial 14 topics, essential dismissal/replay, Try-it actions and font-scale behavior; add anchored highlights if needed.

- [ ] Confirm two-API Android emulator workflow succeeds for API 33 and API 35; inspect actual screenshots separately.
- [ ] Confirm AppearanceRecipesTest JVM tests pass. Do not mark physical-device acceptance from CI.

- [ ] Confirm corrected Run strip vector icon compiles in APK/device workflows.

- [ ] Verify project card file counts, newly created/imported/renamed projects, loading state and large-font card clipping in real emulator screenshots.

- [ ] Retest full motion benchmark after replacing legacy onboarding selectors; no fabricated screen tests.
