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
