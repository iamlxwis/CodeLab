# CodeLab — development agent instructions

Read this file before making changes to CodeLab, then read:
1. [Project memory](docs/development/PROJECT_MEMORY.md) — current source-of-truth snapshot and next action.
2. [Decisions](docs/development/DECISIONS.md) — user-approved product and engineering rules.
3. [Known issues](docs/development/KNOWN_ISSUES.md) — blockers and test failures.
4. [Roadmap](docs/development/ROADMAP.md) — prioritized remaining work.
5. [Completed](docs/development/COMPLETED.md) — what is implemented, with verification scope.
6. [Idea ledger](docs/development/IDEAS.md) — every new user thought, approved feature and requirement, including pending items.

This repository is the durable handoff between chat sessions. Assume a new assistant knows **nothing** from past chats. Do not rely on conversation memory for implementation decisions.

## Product contract

CodeLab is a native Kotlin + Jetpack Compose Android code editor, intended to be lightweight, local-first, and easy to navigate.
- **Execution:** User chooses **Run Online** or **Run Offline**. Online execution needs internet, a supported configured provider and explicit consent before source is uploaded. Offline execution requires a previously installed compatible language module; offer installation when absent.
- **Modularity:** Every optional language runtime, including Python, JavaScript, Lua and other runnable engines, is intended to be individually installable/removable. No language runtime should be silently mandatory in the final core APK. Some engines are still bundled today; this is **migration work**, not completed behavior.
- **Catalog:** Editing and starter examples work regardless of whether an execution module is installed. Do not conflate a recognized file type (JSON, Markdown, CSS) with an executable language.
- **UX:** Professional vector icons only; **no emojis anywhere in CodeLab** (UI, copy, assets, docs intended for users). Minimal, Glass, Clay, AMOLED and Paper styles; prioritize clarity, accessible code contrast and performance over visual effects.
- **Security:** Never submit source to an online compiler without user action/permission. Never load arbitrary untrusted downloaded executable code into the editor process. Signed/verifiable Android-compatible runtime packs must use isolated execution and authenticated IPC with size/time limits.
- **Distribution:** GitHub release APK currently uses fallback debug signing, not stable production signing. Never represent a successful Gradle build as proof that emulator and physical-device tests passed.

## Development/commit discipline

- User authorized **direct commits to `main`**; no PR required unless they change this instruction.
- Before coding, inspect the actual files and latest GitHub Actions results. The repository may have changed since the last handoff.
- Keep edits narrowly scoped, incremental and buildable. Preserve project data and do not remove working functionality until a replacement is validated.
- **Every new user thought, feature request, rule or correction must be recorded in `docs/development/IDEAS.md` in the same session** (even if implementation must wait). Assign a stable ID and status. Keep the item until verified complete; then mark it `Completed` with evidence instead of deleting it. If requirements change, mark the prior item `Superseded` with a link to its replacement; retain the history. Never silently remove an unfinished item. Updates to the ledger should be committed to `main`.
- **Every meaningful implementation commit must update project memory in the same commit.** At minimum update `docs/development/PROJECT_MEMORY.md` with: date, commit context, implemented/unverified/blocked changes, latest CI SHA/status, and precise next task. Update `COMPLETED.md`, `ROADMAP.md`, `DECISIONS.md`, `KNOWN_ISSUES.md` as applicable.
- Never mark work *verified* merely because source compiles. Distinguish source committed, Gradle build, automated unit tests, emulator smoke and real physical-device acceptance.
- For a failure, include exact failing workflow URL and error text; do not guess the root cause.
- Don't add secrets, tokens, passwords, personal details, fake screenshots or untested claims to repo documentation.
- Use short commit subjects describing actual changes; record commits and CI evidence in these handoff files, or a subsequent documentation-only verification commit if the workflow result arrives after the source commit.
- If context or usage limits interrupt a session, finish with accurate handoff state; future agents should resume using the repo files.

## First task for a new agent

Open `docs/development/PROJECT_MEMORY.md`, verify `main` and latest CI, then execute the next action in `docs/development/ROADMAP.md`. Report concrete completed work and remaining verification. If docs conflict with inspected code, **inspect and fix the docs** rather than treating the handoff as infallible.
