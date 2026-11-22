# Contributing

CodeLab is an early-access local-first Android editor. Prefer small, testable
changes that preserve offline execution and existing projects. Contributors
may use pull requests; the maintainer also commits directly to `main`.

## Build and test

JDK 17 and Android SDK 35 are required.

```bash
bash gradlew :app:assembleDebug :app:testDebugUnitTest
bash gradlew :app:connectedDebugAndroidTest
python3 qa/security_gate.py
```

The device task requires an emulator or connected Android device. CI also
builds a release APK and captures runtime screenshots, performance stats and
accessible UI hierarchy evidence.

## Acceptance rules

- Keep APK builds and real JS/Lua/SQL/Java execution working offline.
- Do not call a syntax-only language an offline runtime.
- Run file and credential network operations only with user consent.
- Keep editor text input fluid; add tests for IME, save/restart and large files.
- Respect Android's **system-wide disabled animations**, regardless of app theme.
- Never stage every Git file as an unannounced side effect. Explicitly select
  paths and verify after commit.
- Separate emulator automation from physical TalkBack, contrast and
  performance certification. CI green is necessary, not universal acceptance.

## Release signing

`Android APK` produces an optimized build and SHA-256 checksum. Without
stable signing secrets it uses a fallback debug key, which may change between
builds. Back up/export projects before reinstalling. Never commit keystores
or cloud API tokens.
