# Known issues and unresolved risks

Updated 2026-11-23. Evidence reflects source SHA `3c37ae8c01dc`.

| ID | Severity | Finding / exact evidence | Required next work |
| --- | --- | --- | --- |
| KI-001 | High | [Android device smoke run 45129100951](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951) **failed**. Log ended in `AssertionError: Timed out waiting for ('Hello from CodeLab!',)` during emulator journey; adb exit-code-1 messages also appear. | Inspect automation/UI hierarchy, isolated Python initialization and stdout path with logcat. Don't attribute failure without proof. |
| KI-002 | High | Final agreed execution architecture requires **user-installed language modules**; current `app/build.gradle.kts` includes Chaquopy and Rhino/LuaJ/BeanShell engines. | Implement, prove and migrate modules without disrupting existing projects; no fake install buttons. |
| KI-003 | High | No implemented downloadable runtime manager, authenticated IPC contract or module signature/version/ABI verification. `docs/ENGINE_PACKS.md` is a design, not installer code. | Design implementation and tests; don't ship a downloading ZIP-and-execute shortcut. |
| KI-004 | Medium | Current Run UI has execution capability labels and opt-in cloud path, but complete deliberate Online / Offline user selection and missing-pack install path remain. | Implement selection, consent, error states and offline-only tests. |
| KI-005 | High | Public APK release uses fallback debug signing. Keys can vary; in-place upgrades aren't guaranteed and module cross-package trust would be fragile. | Configure stable signing before dependent runtime packages or production release. Back up user projects before reinstall. |
| KI-006 | Medium | Chaquopy Python currently targets 64-bit ABIs only; 32-bit-only Android devices no longer supported for that release. | Document device support and test intended ABI matrix. |
| KI-007 | Medium | Theme gallery/styles exist, but not every screen has comprehensive contrast, layout, accessibility and performance validation; Glass is primarily translucent styling, not proven backdrop blur everywhere. | Visual audit, screenshots, device tests, accessibility checks. |
| KI-008 | Low | Legacy docs like `docs/ARCHITECTURE.md`, `docs/ROADMAP.md` may describe earlier workstream states; newer decisions can conflict. | Update them progressively. Current truth: inspected source and `docs/development/` snapshot, with actual CI evidence. |

Other unverified work: full Java/Kotlin offline compilation, TypeScript transpilation, C/C++, PHP/Ruby/Go/Rust/C# packs, production signing, physical-device acceptance, full language-server features.

**Verification note:** APK workflow [45129100978](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978) passed, but it does **not** negate the failing emulator result.
