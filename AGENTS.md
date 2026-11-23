## Nov 24, 2026 · 2:01 AM PHT — release workflow reconstruction

b913936d accidentally corrupted android.yml from a JavaScript string replacement: replacing shell text containing `$'` interpreted it as the suffix insertion token. This split the shell grep quote and duplicated 97+ lines, resulting in Actions 45232960818 failing at workflow parsing before any jobs. Corrected by rebuilding android.yml from fully passing e85214f release and using a **function callback for replacement** to preserve literal `$'` in shell code, re-adding actions:read, four gate tests, signer continuity check, same-commit device success gate. Validated one publish step/one gate step/one emulator section and 232ish total lines. New CI pending; check actual build and jobs. Keep only one live README row; STOPPED handoff at end of reply.

## Nov 24, 2026 · 1:54 AM PHT — release publication gate, stable signing, module migration and file-history reliability

Prior source e85214f completed both APK/signed upgrades [45229584184](https://github.com/iamlxwis/CodeLab/actions/runs/45229584184) and full device smoke [45229584342](https://github.com/iamlxwis/CodeLab/actions/runs/45229584342) on API33/35. New source adds same-SHA device workflow prerequisite to publish latest (40-minute max polling), rejects production signer rotation relative to previous production release, and tests the gate off-network. Python verification now refuses to install over an existing differently signed companion and instructs deliberate removal; security test added. ProjectStore now rejects oversized/null text before creating history snapshots; regression added. **New commit CI pending.** Do not claim full T03/T05/T09 or stable release completed. Before final reply overwrite one README action entry with STOPPED handoff, AM/PM timestamp, next precise task; use [skip ci] docs-only.

## Single-entry README activity protocol — Nov 24, 2026

The README contains exactly ONE current activity row. On meaningful code changes, CI result checks, failure fixes or handoffs, REPLACE that row with the newest timestamp, task, action/result and next step; use Asia/Manila timezone and 12-hour AM/PM. NEVER append a second historical event there. Keep history in Git commits, docs/development/PROGRESS_LOG.md and PROJECT_MEMORY.md. Separate interactive assistant activity from independent GitHub Actions status. Before the last reply, commit SESSION STOPPED — assistant handed off with the latest actual activity and time. Use docs-only [skip ci] for the final handoff, avoiding interruption to real CI.

Last fix: invalid assertExists import in Android Compose UI test prevented device QA compilation on API33/API35 in CI 45223423018; remove import, keep assertion. Production APK CI 45223423186 passed without compiling instrumentation; new release gate explicitly compiles Android instrumentation test Kotlin before publishing. Only new CI will verify source fix.

## Nov 24, 2026 · 1:02 AM PHT

T07/T04: after real signed upgrade on API33, Clay visibly retained but previous QA assertion relied on icon contentDescription="Selected" removed in accessibility change. The app now exposes stable `Clay style, selected` card label; signed-upgrade QA accepts new contextual or actual legacy icon aligned with Clay. New CI pending. Continue T03/T05/T07/T08; before final chat mark session STOPPED with AM/PM.

## Nov 24, 2026 · 1:00 AM PHT

API35 device run 45220967836 proved tutorial resume + missing-module recovery test passed, then old smoke expected obsolete `Check for updates` UI text. Actual label `Download latest Python` deliberately reflects full APK retrieval. Updated both old device and signed-upgrade QA scripts. Wait for renewed API33/35 CI before marking overall device/release passes. Mark STOPPED at chat boundary.

## Nov 24, 2026 · 12:48 AM PHT

T08 API35 failure: immediate process death after intentional Skip reopened topic3, despite Try-resume working. Persist guide flags using checked synchronous SharedPreferences.commit(), and QA now checks dismissal before force-stop. Rerun required; keep T08 open. Before final answer commit STOPPED session marker.

## Nov 24, 2026 · 12:39 AM PHT

T07 Kotlin compile break caused by semantics property/selected String argument name collision; corrected to currentChoice. Verify next CI; do not claim test pass yet. Same kind of shadowing previously caused signing bug: avoid receiver property name reuse in Compose/Gradle DSLs.

## Nov 24, 2026 · 12:35 AM PHT

T07 source: 48dp labeled color swatches and selection semantics for appearance controls, with Android instrumentation. Pending CI and manual TalkBack. Preserve existing appearance presets. Before final response mark README STOPPED with AM/PM local timestamp.

## Nov 24, 2026 · 12:32 AM PHT

New Android 13/15 `qa/tutorial_resume_smoke.py` tests live project detour, process restart and guide resume, plus intentional skip. Runs before missing-Python test and installed-runtime smoke. Treat as unverified until device CI finishes. Preserve buildability, permanent signing, and stopped-at-chat-boundary audit.

## Nov 24, 2026 · 12:30 AM PHT

T08 source now separates temporarily exploring a live CodeLab action from actually completing onboarding. `first-run-guide.resume-index` preserves next topic across reopen/restart. JUnit tests added, CI pending. T05 download-version/missing-module updates still pending CI. Before final response, commit accurate STOPPED session marker and timestamp.

## Nov 24, 2026 · 12:26 AM PHT

Added missing-Python **device UI smoke** to the Android13/15 workflow before installed-companion smoke, checking offline Run guides users to the in-app module manager without silently downloading anything. Tests are not complete until Actions passes. Remember to record pause state with AM/PM timestamp before final ChatGPT reply.

## Nov 24, 2026 · 12:23 AM PHT checkpoint

T05 source now contains `PythonModuleVersionPolicy` and JVM regressions. Prevent Python module downgrade (without changing Android's package signature checks), report completed transfer whose APK disappeared, and label actual full downloads accurately. CI must run before claiming verified. Continue T03 app audit + T05 Android runtime installation lifecycle. Session WORKING only until final turn; mark STOPPED at handoff with AM/PM timestamp.

## Session and audit protocol (Nov 24, 2026)

This agent operates only during an interactive turn. Before its final chat reply, **update README Session to STOPPED — assistant handed off** with an exact Asia/Manila, 12-hour AM/PM timestamp; do not claim background coding. At the start of a new user-authorized turn, set WORKING and update the per-task timestamp. Log important task actions, CI failures, fixes, and handoff checkpoints in README and `docs/development/PROGRESS_LOG.md` in the same commit. GitHub Actions can continue independently; label CI as pending, not as agent working. Never fabricate progress or completion.

## Verified release and upgrade checkpoint — 2026-11-23

- Main release workflow [45209632286](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286) **PASSED** real existing-keystore certificates, APK/JVM/lint, API33 and API35 `adb install -r` upgrade acceptance for editor and companion, persisted project source and Clay appearance, real offline Python execution. Released `19123aa9`; versionCode `1286 -> 1288` for both APKs. Do not replace signing key.
- Device smoke [45209632246](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246) **PASSED** API33 and API35, including corrected Android15 Online-mode expectation. T01, T02, T04 complete; T03 audit still open and source-upload approval now bound to revision/file/mode/endpoint/language id with JVM regression. Full module manager, further module downloads, UI/accessibility and exhaustive QA still open; **physical Android handset upgrade NOT TESTED**.
- Prior APKs signed by rotating Android Debug certificates require a **one-time backup/export and reinstall** before future same-key updates; users must not uninstall without confirming readable project backups. Never claim 1.0 stable or full production readiness based only on these gates.

**Upgrade QA navigation (2026-11-23):** [signed-upgrade run 45207076639](https://github.com/iamlxwis/CodeLab/actions/runs/45207076639) reached the real Android 13 app/project but failed at `Appearance` because the Workspace actions sheet shows categories by default. The test now searches actions and taps the TextView result; the same fix is applied to Languages. Await new CI. Never weaken the data-preservation/real-Python acceptance gate.

**Signing status correction (2026-11-23):** Production signing is now cryptographically verified for both APKs at [release workflow 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835), source `7e04d47`, actual editor SHA-256 certificate `c81d2aa1...c9e42392`. Earlier debug APK certificates differ and need backed-up migration. T04 same-key emulator upgrade remains pending. Do not rotate the existing production key. Online consent is now bound to source revision, file, mode and compiler target (new CI pending).

# CodeLab — development agent instructions

Read this file before making changes to CodeLab, then read:
1. [Project memory](docs/development/PROJECT_MEMORY.md) — current source-of-truth snapshot and next action.
2. [Decisions](docs/development/DECISIONS.md) — user-approved product and engineering rules.
3. [Known issues](docs/development/KNOWN_ISSUES.md) — blockers and test failures.
4. [Roadmap](docs/development/ROADMAP.md) — prioritized remaining work.
5. [Completed](docs/development/COMPLETED.md) — what is implemented, with verification scope.
6. [Idea ledger](docs/development/IDEAS.md) — every new user thought, approved feature and requirement, including pending items.
7. [Public README progress tracker](README.md#development-tracker) and [progress audit](docs/development/PROGRESS_LOG.md) — task counts, active work, completed and pending tasks, actual CI results and concise audit history.

This repository is the durable handoff between chat sessions. Assume a new assistant knows **nothing** from past chats. Do not rely on conversation memory for implementation decisions.

## Product contract

CodeLab is a native Kotlin + Jetpack Compose Android code editor, intended to be lightweight, local-first, and easy to navigate.
- **Execution:** User chooses **Run Online** or **Run Offline**. Online execution needs internet, a supported configured provider and explicit consent before source is uploaded. Offline execution requires a previously installed compatible language module; offer installation when absent.
- **Modularity:** Every optional language runtime, including Python, JavaScript, Lua and other runnable engines, is intended to be individually installable/removable. No language runtime should be silently mandatory in the final core APK. Some engines are still bundled today; this is **migration work**, not completed behavior.
- **Catalog:** Editing and starter examples work regardless of whether an execution module is installed. Do not conflate a recognized file type (JSON, Markdown, CSS) with an executable language.
- **UX:** Professional vector icons only; **no emojis anywhere in CodeLab** (UI, copy, assets, docs intended for users). Minimal, Glass, Clay, AMOLED and Paper styles; prioritize clarity, accessible code contrast and performance over visual effects.
- **Security:** Never submit source to an online compiler without user action/permission. Never load arbitrary untrusted downloaded executable code into the editor process. Signed/verifiable Android-compatible runtime packs must use isolated execution and authenticated IPC with size/time limits.
- **Distribution (2026-11-23 correction):** APKs from a11d15e, 7acd475 and ee24cd3 actually had three different Android Debug signer certificates, despite `production` build metadata. Build-step signing credentials were missing from the Gradle environment. The workflow now passes the existing secrets into Gradle and compares both APK certificates with the permanent keystore before release. **Not verified until CI passes.** The first CI with secrets failed packaging at `:app:packageRelease` because Kotlin Gradle SigningConfig's `keyPassword`/`keyAlias` properties shadowed identically named outer values. Outer variables are now uniquely named; retest pending. **Do not regenerate the existing key.** Old debug-signed installations need a backed-up one-time migration. Compilation does not prove emulator/phone QA.

## Development/commit discipline

- User authorized **direct commits to `main`**; no PR required unless they change this instruction.
- Before coding, inspect the actual files and latest GitHub Actions results. The repository may have changed since the last handoff.
- Keep edits narrowly scoped, incremental and buildable. Preserve project data and do not remove working functionality until a replacement is validated.
- **Every new user thought, feature request, rule or correction must be recorded in `docs/development/IDEAS.md` in the same session** (even if implementation must wait). Assign a stable ID and status. Keep the item until verified complete; then mark it `Completed` with evidence instead of deleting it. If requirements change, mark the prior item `Superseded` with a link to its replacement; retain the history. Never silently remove an unfinished item. Updates to the ledger should be committed to `main`.
- **Mandatory public progress audit in every meaningful task commit:** Update `README.md`'s Development tracker and append a short dated row to `docs/development/PROGRESS_LOG.md` whenever a task starts, advances, blocks, unblocks or completes. Include current task, exact done/remaining/in-progress/blocked counts, one-line task status, and most recent VERIFIED Android CI (links, not guesses). Update these in the same commit as source and Project Memory. Do not skip checkpoints, claim untested work is complete, or invent progress. Keep the README short, no marketing filler. Track task statuses against immutable Txx IDs from the audit baseline; if adding tasks adjust denominators and record it in the log. Status is commit-based, not instant/live between commits.
- **Every meaningful implementation commit must update project memory in the same commit. At minimum update `docs/development/PROJECT_MEMORY.md` with: date, commit context, implemented/unverified/blocked changes, latest CI SHA/status, and precise next task. Update `COMPLETED.md`, `ROADMAP.md`, `DECISIONS.md`, `KNOWN_ISSUES.md` as applicable.
- Never mark work *verified* merely because source compiles. Distinguish source committed, Gradle build, automated unit tests, emulator smoke and real physical-device acceptance.
- For a failure, include exact failing workflow URL and error text; do not guess the root cause.
- Don't add secrets, tokens, passwords, personal details, fake screenshots or untested claims to repo documentation.
- Use short commit subjects describing actual changes; record commits and CI evidence in these handoff files, or a subsequent documentation-only verification commit if the workflow result arrives after the source commit.
- If context or usage limits interrupt a session, finish with accurate handoff state; future agents should resume using the repo files.

## First task for a new agent

Open `docs/development/PROJECT_MEMORY.md`, verify `main` and latest CI, then execute the next action in `docs/development/ROADMAP.md`. Report concrete completed work and remaining verification. If docs conflict with inspected code, **inspect and fix the docs** rather than treating the handoff as infallible.
