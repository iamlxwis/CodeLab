> **Nov 24, 2026, 3:54 AM PHT Phase 1 source:** Python, JS, Lua, Java beta and TypeScript single-file compiler/transpiler companion projects now integrated into build, signed installer and run manager. Only Python was previously confirmed published. TS runs through official TypeScript 5.9 in a network-blocked WebView plus Rhino; no semantic type checking, imports or Node. CI/device/publication pending; older notes below are historical.

> **Nov 24, 2026, 3:49 AM PHT current source:** four companion projects (Python, JavaScript, Lua, Java beta) with manager and signed release integration. Only Python was previously confirmed public; new CI/publish pending. Java beta is an interpreter, not a compiler. Older historical notes below are superseded where contradictory.

> **Verified milestone, 2026-11-23:** Python 3.13 is available as a separate 36.45 MB APK for the 3.53 MB editor, with signature-bound IPC and passing [offline emulator test](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400) at source `216a58aa`. This is **not** yet a built-in installation manager, and all other optional language packs remain pending.

# CodeLab modular offline execution: engineering contract

Status: **design and capability registry implemented; optional engine APKs NOT released**. For the latest user-approved product contract and handoff, see [development decisions](development/DECISIONS.md) and [project memory](development/PROJECT_MEMORY.md).

## Delivery decisions

- Existing built-in engines: Rhino JavaScript, LuaJ, Android SQLite, BeanShell Java beta and local HTML/CSS/JS preview.
- Existing **bundled** CPython 3.13 was introduced via Chaquopy; it must be migrated to an optional module under the newly agreed architecture. Offline Python device acceptance is still failing/unverified (see development/KNOWN_ISSUES.md). TypeScript transpilation is not implemented.
- Target: **every optional execution engine is installed only when the user chooses it**, including Python, JavaScript, Lua and other runtimes. Users choose Online (explicit remote-source consent) or Offline (module installed); C/C++, genuine Java/Kotlin compiler, PHP/Ruby then Go/Rust/C# require engineering feasibility and validation.
- Editing and examples for all catalog entries remain offline, irrespective of engine availability.

## Why not download ZIP compilers?

Android 10+ prevents direct execution of arbitrary binaries stored in writable application data. On modern Android, compiled native code requires ABI-specific, OS-compatible packaging and trusted loading. Engine-pack availability must never be inferred from a downloaded file alone.

## Security contract for future engine packages

1. Deliver runtime code as independently signed Android companion packages, or Play dynamic features where applicable. Do not dynamically execute unpacked arbitrary binaries.
2. Each companion executes code in its own app UID/process, not CodeLab's workspace process. Never share app-private projects or tokens.
3. Explicitly bind to a known package and verify its signing certificate against the release trust policy before submitting source.
4. Use a versioned, size-limited IPC protocol with bounded source, stdin, stdout, stderr, cancellation and timeouts. Avoid implicit intent selection.
5. Disclose engine size, ABI support, provenance and permissions before installation; verify checksums and signatures. Do not upload source or run remotely without permission.
6. Provide offline installation and uninstall verification, safe fallback, error attribution, and runtime version reporting.
7. A pack is marked "Ready offline" only after actual device tests cover airplane mode, startup, input, output, timeout, termination, malformed source and reinstall.

**Prerequisite:** configure a stable signing key before distributing companion packages. GitHub Actions' current debug-key fallback cannot guarantee cross-release signature continuity.

## Acceptance gate

Every new engine needs: reproducible build; API 26+ compatibility on supported ABIs; zero-network execution; sandbox/security review; source-file and multi-file tests where promised; output capture and cancellation; performance and install-size measurement; emulator and physical-device coverage.

This document is not a claim that any downloadable language module exists. Python has a bundled implementation but its latest emulator smoke failed; C/C++ toolchains are not implemented.
