# Active development roadmap

Updated 2026-11-23. Ordered by dependencies, not feature marketing priority.

## P0 — build and runtime reliability

- [ ] Verify registry cleanup removes the two Python-as-bundled unit test failures ([45134808317](https://github.com/iamlxwis/CodeLab/actions/runs/45134808317)). Patch committed, CI pending.
- [ ] Verify searchable Web template unblocks emulator journey ([45133009129](https://github.com/iamlxwis/CodeLab/actions/runs/45133009129)). Patch committed, CI pending.

- [x] APK builds at source SHA `3c37ae8c`; see [build](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978).
- [x] Diagnose [emulator Python-output timeout](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951). Actual UI trace confirms `Exit 1`: Chaquopy reads SharedPreferences from an isolated process where UserManager is unavailable.
- [ ] Repair using a properly isolated, signed companion runtime APK (not simply an unisolated service in CodeLab), then demonstrate real offline stdout/stdin/errors.
- [ ] Achieve passing APK + Android emulator smoke for the same source commit; retain exact run URLs and SHA.
- [ ] Verify real device API/ABI coverage; Python integration currently targets arm64-v8a and x86_64, not 32-bit-only devices.

## P1 — optional module architecture (approved end state)

- [ ] Implement a versioned `EngineProvider` / capability contract and accurate installed/not-installed/loading/error states.
- [ ] Choose supported Android distribution model for **GitHub APK**, with signed companion packages (or validated alternative); explicitly verify package identity/certificates, version and ABI. Consider Play on-demand features separately for a Play Store channel.
- [ ] Authenticated, explicit and isolated IPC between core editor and each runtime package. Test cancellation, timeouts, bounded input/output and failure modes.
- [ ] Build a real Language Module Manager: list, sizes/permissions/provenance, install, update, uninstall, offline state, version/compatibility and storage usage. Do not offer fake install actions.
- [ ] Implement online-vs-offline mode choice at Run, with deliberate consent before remote source transfer and no forced cloud fallback.
- [ ] Migrate **all optional language engines**, including Python, JavaScript and Lua, out of the core APK only after working replacements are tested. Keep local editor and Android platform UI functionality intact.
- [ ] Make runtime state depend on actual installed/verified modules, not hardcoded roadmap assumptions.

## P2 — expand real offline language coverage

- [ ] First downloadable Python runtime prototype (existing Chaquopy implementation is **bundled**, not a module).
- [ ] Downloadable JavaScript and Lua modules; assess grouping TypeScript transpiler with JS without silently bundling either.
- [ ] Valid Android-compatible C/C++ toolchain prototype, with ABI/SDK tests.
- [ ] Full Java/Kotlin compilers versus existing limited BeanShell Java beta.
- [ ] Evaluate PHP, Ruby, Go, Rust and C# feasibility; only promise working features after verified install and run.
- [ ] Preserve starter examples, editing-only entries and optional online compilers for languages without offline engines.

## P3 — quality, design and distribution

- [ ] Replace overloaded UI controls across remaining screens; audit Glass/Clay fidelity, contrast, performance, reduced-motion and TalkBack behavior. Use **professional vector icons, no emojis**.
- [ ] Real-device smoke + performance, long-running cancellation, ABI coverage, source privacy review.
- [ ] Stable production/release signing before distributing updatable companion runtime packages; avoid breaking existing users.
- [ ] Keep docs, changelog, screenshots and release claims aligned with measured behavior.

## Persistent requirements

Every user suggestion, even when not yet implemented, belongs in [IDEAS.md](IDEAS.md) with an immutable ID and status. Keep it until **verified complete**; preserve completed and superseded entries indefinitely as history. Update it in the same development session as the request.

## Commit process

User approved direct commits to `main`. **In the same commit as implementation work**, update `docs/development/PROJECT_MEMORY.md`, and any relevant completed/roadmap/decision/issue docs. Store actual CI links after jobs complete in a follow-up docs-only commit if necessary.
