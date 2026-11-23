## Nov 24, 2026, 4:23 AM PHT — Transient Maven Central throttling

- [x] [Fast six-runtime CI 45257846511](https://github.com/iamlxwis/CodeLab/actions/runs/45257846511) passed Kotlin/Java compile, JVM tests, lint and signature checks.
- [ ] [API35 45258495504](https://github.com/iamlxwis/CodeLab/actions/runs/45258495504) failed pre-emulator on TypeScript WebJar dependency fetch HTTP 429 (not device code). Retry wrapper added and triggered via checkpoint; check same-SHA API35 success.
- [ ] Protected production release and real network installation/update/uninstall not yet verified.
- [ ] Full offline compiler/interpreter expansion beyond six source engines still pending as in [LANGUAGE_FEASIBILITY.md](LANGUAGE_FEASIBILITY.md).

## Nov 24, 2026, 4:20 AM PHT — Current Phase1 release checkpoint

- [x] Fast six-pack source baseline dcb1daed passed [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45256384445).
- [ ] Current 34030469 asset-discovery/UX build and same-SHA API35 device smoke must pass; checkpoint workflow triggered with this commit.
- [ ] Guarded production-signer release publication for Python, JavaScript, Lua, Java beta, TypeScript, SQLite; user physical download/update/reinstall remains delegated to owner.
- [ ] Actual fully offline PHP/Ruby/C/C++/Java compiler/Kotlin/Go/Rust/C# remain blocked/untested; keep in scope/feasibility ledger.

## Nov 24, 4:14 AM PHT — Phase 1 broader offline language feasibility audit

- [x] Record accurate Android feasibility and blockers for C/C++, Java/Kotlin full compilation, PHP/Ruby WASM, Go, Rust and C# in [LANGUAGE_FEASIBILITY.md](LANGUAGE_FEASIBILITY.md).
- [ ] Finish same-commit fast CI, API35 runtime tests and signed public module releases for Python/JavaScript/Lua/Java beta/TypeScript/SQLite.
- [ ] Accept full TS single-file execution in airplane mode and actual installation lifecycle on Android; no false full type checker claim.
- [ ] Prototype PHP/Ruby WASM and native compiler packs only where compatible with Android W^X and signed standalone distribution. Keep explicit blockers visible.

## Nov 24, 2026 · 12:35 AM PHT — appearance accessibility

- [ ] Verify Android API33/API35 Compose AppearanceAccentAccessibilityTest and existing device/theme tests; fix real source issues without weakening assertions.
- [ ] Test 48dp touch and large-font mode choice with TalkBack and narrow devices; finish all five themes.

## Nov 24, 2026 · 12:32 AM PHT — verify tutorial lifecycle

- [ ] Watch new Android 13/15 `tutorial_resume_smoke.py` and `missing_python_smoke.py`; fix emulator-specific navigation errors using screenshots/XML, without erasing acceptance conditions.
- [ ] Only after pass mark Try-vs-Skip semantics verified; 14-topic large-text/TalkBack usability still outstanding.

## Nov 24, 2026 · 12:30 AM PHT — tutorial continuation checkpoint

- [ ] Verify `TutorialProgressTest` and APK compile, then Android API33/API35 smoke with new resume-index behavior.
- [ ] Add device flow that taps Try on a real first-launch topic, returns via More > Quick start guide, and confirms correct next topic; verify no false completion.
- [ ] Test tutorial with large font, TalkBack and narrow-screen layouts; do not mark T08 complete until accessibility/immersive practical journeys pass.

## Nov 24, 2026 · 12:26 AM PHT — module recovery emulator QA

- [ ] Confirm new `qa/missing_python_smoke.py` passes on API33 and API35, preserving successful legacy `qa/device_smoke.py` and release QA.
- [ ] Add downloadable APK install/update end-to-end QA with Android system consent; do not report installation success based only on missing-module prompt tests.
- [ ] Make security checks and module install/downgrade messages accessible with TalkBack.

## Nov 24, 2026 · 12:23 AM PHT — module upgrade guard checkpoint

- [ ] Verify new `PythonModuleVersionPolicyTest` and Android signing/install workflows in CI; fix failures without weakening policies.
- [ ] Device-test download progress/cancel/error, install approval, update/removal and missing-module recovery; JVM version guards are not equivalent to Android installer evidence.
- [ ] Consider separate release/version discovery rather than downloading a full APK merely to check for updates; current UI truthfully labels that action `Download latest Python`.
- [ ] Check Android 13/15 companion downgrade behavior and correct messaging on real UI; never fake offline language engines.

## Nov 24, 2026 — work session and audit timestamps

- [x] README development tracker now distinguishes **WORKING** vs **STOPPED — assistant handed off**, and records task events in **Asia/Manila, 12-hour AM/PM**.
- [ ] On every final response commit, mark **STOPPED** with local time and next exact action. Never leave stale WORKING when interaction ends.
- [ ] Implement T03/T05 next: reproduce a real defect, fix it, add regression coverage, run CI, update README and append-only audit in the same commit. Preserve stable signing and signed-upgrade gate.

## Post-acceptance checkpoint (2026-11-23) — remaining priorities

- [x] T01: real permanent-signer APK check passed, never rotate or replace release signing key ([CI](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835)).
- [x] T02: Android 13/15 device-smoke tests passed including Online status ([CI](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246)).
- [x] T04: Android 13/15 signed upgrade acceptance `adb install -r`, retained project/theme, Python execution, version `1286 -> 1288` ([CI](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286)).
- [ ] T03: comprehensive remaining app bug audit + regression tests; verify real cloud execution and consent beyond JVM guards.
- [ ] T04 additional real-phone signed upgrade with backed-up test data (separate from completed emulator acceptance).
- [ ] T05: end-to-end Python download/progress/cancel/Android approval/install/update/uninstall/missing-module recovery on real Android.
- [ ] T06: optional offline language module architecture beyond existing supported runtimes; feasibility claims only for verified engines.
- [ ] T07: premium all-screen UI/UX/accessibility refinements for five themes.
- [ ] T08: contextual interactive tutorial and first-run accessibility on different screen sizes/font scales.
- [ ] T09: comprehensive performance, security, QA and production-ready release gates; do not claim 1.0 stable prematurely.

## 2026-11-23 — signed-upgrade CI debug checkpoint

- [ ] Recheck Android 13 and 15 signed upgrade runs after repairing Appearance/Languages action search in `qa/production_upgrade_smoke.py`. Previous run 45207076639 failed before update at nested-menu navigation; no persisted-data pass.
- [x] Verified first actual permanent-keystore-signed APK at release CI 45205309835; earlier debug-signed APK metadata was false.
- [ ] Confirm same-key Android API33/API35 in-place upgrade with source/theme retention and offline Python companion execution, without uninstalling or clearing app data.

## 2026-11-23 — mobile GitHub Actions signing setup

- [ ] Ensure manual signing-key generator workflow is visible and valid after commit; do not manually run it with an unset user passphrase.
- [ ] Owner creates `CODELAB_KEYGEN_PASSPHRASE` repo secret, runs one-time workflow, locally unlocks encrypted artifact on Android, and saves two secure backups of original .jks.
- [ ] Owner fills four existing production signing secrets, builds and verifies `Signing mode: production`, never regenerates identity once shipped.
- [ ] Test a real-phone app-data-preserving in-place update between two production-signed releases; backup project data before the one-time debug→production migration.

- [ ] Regression: creating duplicate file cannot blank an editor tab or overwrite prior bytes. Android 13/15 smoke and JVM test.
## 2026-11-23 — update-signing acceptance gate

- [ ] Confirm new APK CI reports increasing `Version code` and correct signing mode; inspect workflow logs for signing audit.
- [ ] Configure stable production key via four GitHub Actions secrets; retain two secure backups outside repository.
- [ ] Test production-v1 → production-v2 in-place install for editor and Python on Android 13/15 and a physical phone; verify project data survives. Historical fallback → production needs a one-time backed-up reinstall.

## 2026-11-23 — active regression audit

- [ ] Verify Favorites-only listing, counts, empty state and unstar behavior on API 33/35 plus JVM tests.
- [ ] Verify duplicate-name project creation recovers to usable library and shows a clear error without losing files.
- [ ] Verify APK release signing and upgrade/versionCode continuity, then configure production keystore secrets and test a real-device in-place update.

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

- [ ] Retest native editor viewport in 280dp or less available height and before/after IME. Verify actual saved file bytes with `run-as` on both API33/API35 and review landscape screenshot.
