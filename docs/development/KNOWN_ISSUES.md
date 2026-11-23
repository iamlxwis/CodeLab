> **Latest:** Source `216a58aa` passed [APK](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472) and [emulator](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400). The historical failures recorded below were resolved in source; remaining security, signing, migration and physical-device gaps stay open.

# Known issues and unresolved risks

Updated 2026-11-23. Evidence reflects source SHA `3c37ae8c01dc`.

| ID | Severity | Finding / exact evidence | Required next work |
| --- | --- | --- | --- |
| KI-001 | High | [Android device smoke run 45129100951](https://github.com/iamlxwis/CodeLab/actions/runs/45129100951) **failed**. Log ended in `AssertionError: Timed out waiting for ('Hello from CodeLab!',)`. **Confirmed from `device-smoke-evidence/last-window.xml`:** console `Exit 1` and `Python runtime error: IllegalStateException: SharedPreferences cannot be accessed if UserManager is not available. (e.g. from inside an isolated process)`. Logcat shows the isolated Python worker started and exited. | Chaquopy `AndroidPlatform` initialization is incompatible with this current isolated-process setup. Implement a tested separate-app-UID Python module with guarded IPC (or a validated alternative) and rerun full emulator acceptance; do **not** simply drop isolation. |
| KI-002 | High | Final agreed execution architecture requires **user-installed language modules**; the editor APK still bundles Rhino/LuaJ/BeanShell engines; Chaquopy is now in the separate Python APK. | Implement, prove and migrate modules without disrupting existing projects; no fake install buttons. |
| KI-003 | High | No implemented downloadable runtime manager, authenticated IPC contract or module signature/version/ABI verification. `docs/ENGINE_PACKS.md` is a design, not installer code. | Design implementation and tests; don't ship a downloading ZIP-and-execute shortcut. |
| KI-004 | Medium | Current Run UI has execution capability labels and opt-in cloud path, but complete deliberate Online / Offline user selection and missing-pack install path remain. | Implement selection, consent, error states and offline-only tests. |
| KI-005 | High | Public APK release uses fallback debug signing. Keys can vary; in-place upgrades aren't guaranteed and module cross-package trust would be fragile. | Configure stable signing before dependent runtime packages or production release. Back up user projects before reinstall. |
| KI-006 | Medium | Chaquopy Python currently targets 64-bit ABIs only; 32-bit-only Android devices no longer supported for that release. | Document device support and test intended ABI matrix. |
| KI-007 | Medium | Theme gallery/styles exist, but not every screen has comprehensive contrast, layout, accessibility and performance validation; Glass is primarily translucent styling, not proven backdrop blur everywhere. | Visual audit, screenshots, device tests, accessibility checks. |
| KI-008 | Low | Legacy docs like `docs/ARCHITECTURE.md`, `docs/ROADMAP.md` may describe earlier workstream states; newer decisions can conflict. | Update them progressively. Current truth: inspected source and `docs/development/` snapshot, with actual CI evidence. |

Other unverified work: full Java/Kotlin offline compilation, TypeScript transpilation, C/C++, PHP/Ruby/Go/Rust/C# packs, production signing, physical-device acceptance, full language-server features.

**Verification note:** APK workflow [45129100978](https://github.com/iamlxwis/CodeLab/actions/runs/45129100978) passed, but it does **not** negate the failing emulator result.

## Migration note (unverified)

Core's Python dispatcher now expects an independently installed same-certificate
Python companion. Early APK releases still included embedded Chaquopy; preserve
project source/files while testing the migration. CI must build/sign the two
APKs together, publish them as a pair and verify the runtime device journey.

## 2026-11-23 — further CI evidence / fixes pending retest

- **KI-009 (build):** [45134808317](https://github.com/iamlxwis/CodeLab/actions/runs/45134808317) failed exactly two tests: `OfflineRuntimeRegistryTest.onlyAdvertisesExistingBundledEngines` and `RuntimePackCatalogTest.neverAdvertiseUnshippedCoreEngines`. Root cause: source still advertised embedded Python after the module migration. This commit removes stale registry entry; await next Gradle results.
- **KI-010 (device):** [45133009129](https://github.com/iamlxwis/CodeLab/actions/runs/45133009129) failed `AssertionError: Language result Web did not appear`. Evidence: `last-window.xml` showed only the search field and filter, since Web is a template missing from `LanguageCatalog.entries`. This commit makes the Web template searchable from the language browser; await device result.

## Verified resolution — 2026-11-23, source SHA `216a58aa`

- KI-001 **resolved for emulator**: CPython 3.13 executes in the signed companion app's separate UID; no unavailable isolated-process UserManager bootstrap. Python stdout, stdin, runtime failure behavior checked by device smoke + instrumentation. [Evidence](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400).
- KI-009 **resolved**: Removed Python from the core bundled-runtime inventory. All JVM tests passed in [APK CI](https://github.com/iamlxwis/CodeLab/actions/runs/45135511472).
- KI-010 **resolved**: Web template appears as a searchable language-browser result without inventing a `Web` language entry. The full Web preview journey passed in [device CI](https://github.com/iamlxwis/CodeLab/actions/runs/45135511400).
- KI-002, KI-003, KI-004, KI-005, KI-006, KI-007, KI-008 remain **open/partial**: other engines bundled; no native module install/update/uninstall; no explicit Online/Offline selector; fallback debug signing; limited ABIs; incomplete design/physical-device QA. Do not mark these complete.

## KI-011 — incomplete in-app module install acceptance (2026-11-23)

GitHub-distributed APK now includes a Python in-app downloader with progress and certificate validation, but tests have not verified network transfer, system unknown-source approval, installation/update/uninstall failures or storage pressure. The existing fallback-debug release signature can differ between builds, so downloadable Python can be rejected when the editor was installed from a different run. This is **intentional fail-closed security behavior**, not a reason to loosen signature checks. Stable signing and clear recovery flow are still required. For Google Play, Play Feature Delivery is required instead of arbitrary downloaded executable APKs.

## KI-012 — online/offline routing acceptance

Historically `WorkspaceModel.run()` silently selected cloud when no local engine existed. That contradicted the requested deliberate run choice. A persisted Run-mode dropdown now chooses Offline or Online, never auto-switches, and sends remote source only through explicit consent. Unit/UI and device acceptance are still required.

## 2026-11-23 — additional bugs fixed in source, awaiting verification

- Incompatible/signed-differently Python modules could not be removed from Languages because Remove required the module to be ready. The UI now offers user-confirmed removal for untrusted/incompatible modules too.
- DownloadManager now stops transfers exceeding the 120 MB size cap, rather than waiting until after download for validation.
- First-launch tutorial scrolls under large font settings; app-source emoji prevention is part of the static CI gate.
