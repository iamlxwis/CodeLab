# CodeLab modular offline execution: engineering contract

Status: **design and capability registry implemented; optional engine APKs NOT released**.

## Delivery decisions

- Existing built-in engines: Rhino JavaScript, LuaJ, Android SQLite, BeanShell Java beta and local HTML/CSS/JS preview.
- Next proposed **core** engines: real CPython and TypeScript-to-JavaScript transpilation. Neither is currently bundled.
- Optional signed engine packages, in priority order: C/C++, genuine Java/Kotlin compiler, PHP/Ruby, then Go/Rust/C# feasibility work.
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

This document is not a promise that Python or C/C++ currently works offline.
