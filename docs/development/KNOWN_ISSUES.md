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

## 2026-11-23 — fixed mismatch in visual previews (awaiting CI)

AppearanceGallery previously used five static mocked colors for style samples, which did not reflect live backgrounds and panel effects. Previews now render actual CodeLabTheme + CodeLabBackdrop + ClayPanel in each thumbnail; device/visual QA remains open.

## 2026-11-23 — module dialog stacking issue fixed in source

When a missing Python pack opened Languages with an initial selected language, the AlertDialog previously appeared in the composition before the ModalBottomSheet. The sheet could cover the details on initial navigation. The details are now composed after the sheet, and emulator QA covers a real Python module management dialog. CI pending.

## KI-013 — hidden language search results, source fix awaiting retest

[Emulator run 45142776614](https://github.com/iamlxwis/CodeLab/actions/runs/45142776614) failed `Timed out waiting for ('Python runtime',)`. `last-window.xml` showed the search field had Python, the filter had 1 match, but the one language result was not in the visible hierarchy because an oversized execution-status header consumed the available LazyColumn viewport. Fixed by omitting the execution status panel while search text is nonblank and by making QA tap only the matching TextView instead of an EditText. **Not yet verified by new emulator CI.**

## KI-014 — Languages sheet exit reliability (source fixed, retest pending)

[Device 45144260324](https://github.com/iamlxwis/CodeLab/actions/runs/45144260324) passed initial UI steps but timed out waiting for `New project` at `qa/device_smoke.py:153`. XML showed Languages bottom sheet still open after closing module details and one Android Back key. Added visible `Close languages` toolbar action and QA uses it explicitly; this also improves real-user navigation discoverability. Retest needed.

## KI-015 — incorrect foreground colors and clipped Glass/Clay effects

Four user-supplied physical-phone screenshots (2026-11-23) show black text/icons against dark backgrounds and hard-edged translucent rectangles. The root transparent Material Surface lacked an explicit `onSurface` content color; large blurred decoration layers were clipped. Source now provides the theme foreground at root, uses edge-fading Canvas radial gradients, and prevents dark-only Minimal presets from overriding Light mode. Added an Android contrast test across all five styles and both modes and five-style screenshot capture. **Awaiting new APK/emulator/device results**; visual review of all actual screens and physical-device acceptance remains open.

## KI-016 — motion benchmark cannot reach the project library

[Device workflow 45145653756](https://github.com/iamlxwis/CodeLab/actions/runs/45145653756) failed at `qa/motion_smoke.py:55`, exact error `AssertionError: Timed out waiting for ('New project',)`. The benchmark assumed the prior stages had left the app in the home library. Source now checks onboarding, project navigation and Back transitions to reach a known library state; **not retested yet**. Earlier app/JVM success does not prove the motion stage passed.

## KI-017 — Android rejects public APK as invalid on existing phone (2026-11-23)

User-supplied screenshot displays the Android installer message `App not installed as package appears to be invalid` when selecting latest `CodeLab.apk`. The exact Android PackageManager failure code is not available; this generic text is insufficient to assert a single root cause. The latest main source `a62738cf` passed [APK build/signing checks](https://github.com/iamlxwis/CodeLab/actions/runs/45151364074) and [emulator smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45151364143), and the release asset is `3,544,146` bytes. CI's fallback-debug signing is ephemeral, so an older installed version may block update with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; an incomplete download or an OS/device-specific package parse failure remains possible until inspected on the user's phone.

Source-stage mitigation: workflow compares new signer to previous public APK, requires editor and Python companion to share the signer, includes build provenance, makes checksums portable, displays a backup/uninstall/reinstall warning when signing differs, and updates mutable `latest` tag to the actual build. Documented backup-first recovery instructions in `docs/INSTALLATION.md`. **Unverified until new CI; permanent fix requires stable production signing secrets and physical-phone acceptance. Never recommend uninstall before project ZIP export.**

## KI-018 — signing-audit workflow invalid YAML on first attempt

[Workflow 45163194992](https://github.com/iamlxwis/CodeLab/actions/runs/45163194992) failed before any job was created after the initial signing-audit commit; inspected workflow source showed malformed multiline release-note assignments outside the YAML script indentation. Replaced them with single-line quoted shell assignments. **Retest required.**

## KI-017 addendum — user rules out ordinary upgrade conflict

User confirms that, as with earlier successful releases, they **uninstall the old app before installing the new version**. This substantially reduces the likelihood that two installed CodeLab versions' signing certificates are conflicting. Do not repeat the upgrade/signing hypothesis as the demonstrated cause. New source smoke tests **fresh install of the RELEASE variant** (not just debug install or release-over-debug), startup and captured screenshot on Android 35 emulator. Other platform versions, browser download integrity and the user's phone remain unverified; capture real install failure code before claiming a root cause.

## KI-018 — latest pre-redesign emulator smoke failure (2026-11-23)
[Run 45163951663](https://github.com/iamlxwis/CodeLab/actions/runs/45163951663) stops at \`qa/clean_install_smoke.py:32\`, \`AssertionError: adb shell pm path site.lxwis.codelab failed (1)\`. The check incorrectly treated a missing package's exit status as fatal. Source now passes \`check=False\` and needs fresh CI. This does not prove the user-reported physical install failure (KI-017) is resolved.

## KI-019 — appearance and onboarding acceptance gap
New appearance/Projects/Import source has not yet passed Gradle, emulator screenshots, Android 13/15 contrast/TalkBack/large-font or user-device testing. Neutral Glass is a composited appearance, not a true system compositor liquid-glass shader. Follow up on status before describing feature as released.

## KI-021 — source visibility compile failure
[APK 45169778823](https://github.com/iamlxwis/CodeLab/actions/runs/45169778823): `WorkspaceModel.kt:282:9 Property 'public' exposes its 'internal' type 'AppearanceRecipe'`. Scope corrected to `internal`; retest pending.

KI-022: New appearance editor is not yet device-tested, and entering or editing colors/presets must undergo accessibility, contrast and persistence QA.

KI-023: Visible run strip has not been validated at narrow widths or large font sizes; show module/provider statuses accurately and preserve explicit consent.

KI-024: New tutorial and QA script are not emulator verified. True anchored coach-mark highlights are not yet available; interaction is via Try-it shortcuts. Test on Android 13/15.

KI-025: Android 13 and 15 emulator smoke matrix is source-only until workflows complete. No physical-device or TalkBack acceptance; test artifact images must be inspected before visual fidelity claims.

KI-026: [45170798149](https://github.com/iamlxwis/CodeLab/actions/runs/45170798149) failed Kotlin compile `Unresolved reference InfoOutline` in RunTargetMenu.kt:10,67; fix committed, retest pending.

KI-027: New project summary metadata lookup uses background I/O but has not yet been stress-tested on large project libraries; verify counts after modifications and imports.

KI-028: `qa/motion_smoke.py` contained obsolete tutorial selectors (`Start coding`) after 14-topic guide migration. Fixed script; CI pending.

## KI-029 — CI emulator post-restart text visibility / storage uncertainty
Android 13 and Android 15 runs [45171811506](https://github.com/iamlxwis/CodeLab/actions/runs/45171811506) failed at `qa/device_smoke.py:235`: editor source not located by accessibility after force-stop. API35 screenshot shows near-zero-height source viewport in landscape and during IME; this does NOT yet establish content loss. Mitigation in CodeEditor uses compact-height layout; QA now reads saved file bytes with `run-as` and forces portrait for accessibility. Verify on new CI before closing. Real phone still unverified.

## KI-028 and KI-029 — project library audit (2026-11-23)

- KI-028: Favorites used a sorting comparator, not a filter, so nonfavorites appeared. Recent also pushed favorites above truly recent projects. Source fixes both and adds unit/emulator regression; CI pending.
- KI-029: An optimistic new/open-project navigation could leave an empty editor after an I/O failure. Source restores library when the failing navigation is still current; CI/device regression pending.

### KI-030 — release version/signing continuity (2026-11-23)

Existing editor and Python app codes were static at 5/1, and published CI builds use fallback debug keystores, preventing guaranteed in-place updates. Source now derives release version codes from the increasing APK workflow run number, rejects partial production signing configuration, and prevents falling back to a debug-signed latest after production is established. **CI and owner key configuration pending.** Do not claim this resolves the physical phone's earlier clean-install invalid-package error; it is a distinct unresolved issue.

- **KI-031:** Failed duplicate file creation left an optimistic blank/stale editor tab. Source now rejects known collisions before switching and restores the previous tab on I/O failure if no intervening edit/navigation occurred; unit + Android tests added, CI pending.

- **KI-032 (QA regression)**: API35 emulator [run 45181285928](https://github.com/iamlxwis/CodeLab/actions/runs/45181285928) timed out waiting for `Internet connection required`; actual XML showed `No online compiler for this file` for selected Online. Both are valid offline/unconfigured provider states; source fixes QA assertion to accept either, retest needed.

## KI-033 — GitHub Actions key provisioning acceptance (2026-11-23)

One-time keygen, authenticated encrypted artifact and local phone WebCrypto unlocker have been authored. A public repository's workflow artifacts must **never contain the raw JKS or plaintext Base64 key**. User must create the strong generator passphrase as a GitHub Actions repository secret. No production secrets are currently verified configured, and local Android HTML/browser recovery and actual production-signed upgrade are not yet verified. Never solicit secret values in chat or confuse generator workflow with production release.

## KI-034 — API35 online availability smoke assertion (2026-11-23)

[Device workflow 45191732470](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470) failed at `qa/device_smoke.py` because the test accepted only offline-provider errors; downloaded `last-window.xml` shows `Online: source upload needs confirmation` after selecting Online. This is a valid no-upload-yet status and not a proven app bug. Source test now accepts all three truthful outcomes. **Await API33/API35 CI rerun before marking fixed.**

## KI-035 — signed release upgrade acceptance pending (2026-11-23)

Production-key-only pre-publication API33/API35 `adb install -r` tests are now authored. They verify project source, Clay preferences and updated same-certificate Python module through real Android UI execution. No passing result is claimed until Actions reports it. Previous signer mismatch is explicitly skipped, not a successful upgrade; physical-device acceptance remains open.

## KI-036 — misleading production metadata, Android Debug signer in released APKs (P0, 2026-11-23)

Actual APK signing blocks for [a11d15e](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919), [7acd475](https://github.com/iamlxwis/CodeLab/actions/runs/45200242402), [ee24cd3](https://github.com/iamlxwis/CodeLab/actions/runs/45201489255) have different `CN=Android Debug` certificate hashes (`514931a7`, `43dd76c8`, `ef86c79d` prefixes), despite `production` release metadata. Signing credentials configured in one workflow step did not reach Gradle, causing debug fallback. Passing existing secrets into build and enforcing keystore-versus-APK certificate match now authored; CI pending. **Do not regenerate permanent key.** Prior debug APKs need backed-up migration; two same-production-signer release builds have not passed upgrade QA.

## KI-037 — production SigningConfig Kotlin DSL property shadowing (P0, 2026-11-23)

[APK run 45204023605](https://github.com/iamlxwis/CodeLab/actions/runs/45204023605) failed at `:app:packageRelease`: `SigningConfig "production" is missing required property "storePassword"`. Root cause was receiver shadowing: `storePassword = keyPassword` and `keyAlias = keyAlias` in the app Gradle script selected uninitialized SigningConfig properties instead of top-level env variables. Fixed by renaming the top-level inputs and using unambiguous receiver assignments. Companion configuration already used distinct variable names. CI retest required. Existing permanent key preserved.

## KI-036/KI-037 resolved for release signing — 2026-11-23

[Release CI 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835) PASSED actual app + Python signer verification against existing permanent keystore; editor X509 digest `c81d2aa1...c9e42392`. Production-signed release published. Prior debug APKs still require backed-up migration. Same-key Android upgrade T04 not tested yet. This supersedes the earlier source-only/failed status in KI-036 and KI-037.

## KI-038 — stale online upload approval (2026-11-23)

`WorkspaceModel.startCloudRun` formerly checked only active filename, allowing a pending cloud execution after source revision, Online/Offline mode or compiler settings changed. Source now tracks one-time `OnlineRunApproval` bound to file, revision, mode, endpoint, language id, and cancels on mode/network changes; JVM regression added. **CI/device proof pending**, no live external provider run is claimed.

## KI-039 — production-upgrade QA cannot find nested Appearance action (2026-11-23)

[CI 45207076639](https://github.com/iamlxwis/CodeLab/actions/runs/45207076639) failed its API33 signed-upgrade test at `Timed out waiting for ('Appearance',)`. Screenshot/XML show a healthy app displaying searchable Workspace actions sheet; Appearance exists under Settings, not at root. This is **test-navigation drift**, not proof of Android data loss. `qa/production_upgrade_smoke.py` now searches action names and taps result TextView for Appearance and Languages. New CI pending; upgrade data retention still unverified.

## KI-034 / KI-039 — device smoke and signed-upgrade test navigation resolved (2026-11-23)

[Device CI 45209632246](https://github.com/iamlxwis/CodeLab/actions/runs/45209632246) passed both Android API33/35 after accepting legitimate online source-upload consent status. [Release CI 45209632286](https://github.com/iamlxwis/CodeLab/actions/runs/45209632286) passed both API33/35 signer-matched in-place upgrade acceptance after changing the UI test to search nested Appearance and Languages actions. Confirmed data retention, Python companion and versionCode `1286 -> 1288`; do not repeat earlier failure as current. Physical device check remains OPEN.

## KI-038 — source-upload consent revision guard: unit-verified, full end-to-end still open

`ca4ffc9` regression test passed with 12/12 branch coverage for `OnlineRunApproval.matches` and `45209632286` release JVM checks passed. A live external cloud execution test has NOT run; broader audit remains T03.

## KI-040 — Python module downloaded APK version not checked; stale completed-download file (Nov 24, 2026, 12:23 AM PHT)

Source fix authored: prior `Check for updates` actually re-downloaded a full APK and offered installation irrespective of whether the APK version was older than the installed version; DownloadManager could claim successful transfer after the APK file was removed. Introduced signed+same-package APK version comparison, downgrade rejection, explicit update/reinstall labels and local-file missing failure. 5 JVM policy regressions. **CI and true Android download/install not yet verified**; T05 open.

## KI-041 — Missing Python recovery UI not previously tested without installed companion (Nov 24, 2026, 12:26 AM PHT)

The existing Android device smoke installed Python before app launch, so it could not prove missing-module/offline Run guidance. A new disposable emulator test launches the editor with no companion, selects Python, executes Offline Run, verifies error and in-app Manage Python > Download Python. **Source test added, no passing emulator proof yet**, no downloads or installation performed; T05 remains open.

## KI-042 — Interactive tutorial Try action falsely marked onboarding complete (Nov 24, 2026, 12:30 AM PHT)

`FirstRunTutorial` called `onFinish()` when user tapped `Try it in CodeLab`, marking guide complete even though they were only visiting a real feature. Source fix saves next topic in `first-run-guide` preferences, leaves complete=false and allows returning through guide replay; Skip/Finish still deliberately completes. Added 3 JVM checkpoint tests; Android CI/large-font/TalkBack acceptance pending. T08 open.

## KI-043 — tutorial resume process-restart not yet verified (Nov 24, 2026, 12:32 AM PHT)

The saved progress correction for KI-042 needs actual Android process death verification. New API33/API35 device smoke asserts Try-a-feature resumes at next topic after force-stop, whereas deliberate Skip finishes onboarding. This is source QA authored, **CI pending**; don't count as accessible tutorial completion.

## KI-044 — unlabeled small appearance accent circles (Nov 24, 2026 · 12:35 AM PHT)

Unselected color swatches were anonymous clickable 40dp circles; screen readers lacked names. Source fix: named 48dp accents with selected semantics, selection on style/mode choices, Android Compose regression. CI pending; full TalkBack and 5-theme accessibility still open.

## KI-045 — appearance semantics property shadowing breaks compile (Nov 24, 2026 · 12:39 AM PHT)

CI [45218655670](https://github.com/iamlxwis/CodeLab/actions/runs/45218655670) failed compile because new `semantics { selected = active }` targeted outer `selected: String` function parameter. Renamed outer parameter to `currentChoice` so receiver semantics receives Boolean. Retest CI required. This is a real source failure, not device regression; signing and previous release remain intact.
