# Reimagined 2.0 handoff

Branch `reimagined-2.0` starts at main commit `517f5bc5e2ee0b88d42fc17151a205328fb9023a`. The app package remains `site.lxwis.codelab`. Candidate version name is `0.6.0-alpha`. The APK workflow assigns the increasing build code and compares the candidate and pack certificates with the published CodeLab APK.

## Workstreams

| Workstream | State |
| --- | --- |
| Appearance, migration and design tokens | Neutral app palettes, legacy preferences migration, independent code appearance and shared controls implemented |
| Backdrop glass and fallback | Compose graphics recording, Android blur, AGSL refraction and low-cost fallbacks implemented. Device rendering pending |
| Navigation, Home, Projects and Explore | Destinations, floating navigation, recent workspace and project imagery implemented. Visual acceptance pending |
| Languages, runtime presentation and icon audit | Dedicated pages preserve the runtime managers. All 95 catalog entries map to 87 bundled logos or 8 documented monograms |
| Universal search and active workspace | Debounced local search, category filters, history, commands and resume implemented |
| Editor, console and secondary surfaces | Neutral code and tool surfaces, editor header and project cards integrated. Secondary screen audit pending |
| Settings, onboarding and accessibility | Searchable groups and revised tutorial integrated. Font scaling, keyboard access and TalkBack review pending |
| Local developer activity | Foreground coding time, distinct files, creations and actual run outcomes with disable, reset and sharing implemented |
| Official updater and unified downloads | Official asset metadata, SHA-256, package/version/signer checks, DownloadManager and Android installer implemented. Live update/install flow pending |
| CI, signed APK and device verification | First PR APK and signer gate passed. Updated-source APK, API 35 device and in-place upgrade checks pending |

## Architecture and evidence

The Compose backdrop is recorded only on the three library destinations. The Android 13 renderer samples the recorded layer in a local capsule, applies a distance-based AGSL lens and uses Android RenderEffect for blur. Android 12 uses blur. Older, low-memory and power-saving devices can use a frosted treatment. High contrast uses a solid surface. The native editor and live console output bypass backdrop recording.

CodeLab’s own implementation uses BitChord’s public navigation and backdrop source for reference. The GPL navigation, branding and assets were not copied. The task attachment contained the specification only. Existing CodeLab editor and web preview screenshots were inspected from `docs/screenshots`.

Baseline [APK build](https://github.com/iamlxwis/CodeLab/actions/runs/45518242552) and [API 35 device smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45518254838) passed at `517f5bc`. The separate baseline publication workflow failed. The branch passed local static security invariants and 15 Python QA guard tests. Full Android and device verification remains pending.

## Branch evidence

[Draft PR #16](https://github.com/iamlxwis/CodeLab/pull/16) tracks the entire overhaul. The first source checkpoint `a67ca92` passed the [Android APK workflow](https://github.com/iamlxwis/CodeLab/actions/runs/45586362366), including signer continuity for the editor and optional packs. The [first PR artifact](https://github.com/iamlxwis/CodeLab/actions/runs/45586362366/artifacts/15009042968) is a QA build. The [initial API 35 smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45586416088) runs on that earlier source.

The updated candidate simplifies editor-settings migration, applies workspace and console chrome, checks trusted update metadata and runtime download sizes, and extends smoke coverage to cross-project search and activity. A manual dispatch of the Android APK workflow on this branch runs the signed in-place upgrade gate. The stable publication steps are gated to main.

The `febfa99` local JVM suite passed 202 tests and compiled the instrumentation suite. Strict lint flagged `ProduceStateDoesNotAssignValue` on two runtime verification producers. The follow-up keeps verification on the I/O dispatcher using keyed effects and separate observable state. It also gives the manual signed-upgrade run a separate concurrency group from the fast PR build.

Commit `85f0a7b` produced the [signed test APK, build 1396](https://github.com/iamlxwis/CodeLab/actions/runs/45588932391/artifacts/15011095244). The PR workflow passed 202 JVM tests and strict lint with 76 warnings. The distributed APK verifies with production signer `c81d2aa1…c9e42392` and APK SHA-256 `304222016a759f8373eddcc97f330db65988a2e3e0348b8fcbc6ea59bde6a056`. All six packs' signer and SHA-256 values were also independently verified.

The [initial signed upgrade](https://github.com/iamlxwis/CodeLab/actions/runs/45588925015) installed the newer signed packages and restored the saved project and source. It stopped at the Dark mode assertion. The archived accessibility tree shows the Dark option as `checkable=true, checked=true`. The earlier assertion read only `selected`. The follow-up verifies the real checked state and reruns the upgrade and runtime gates.

The [first complete API 35 device run](https://github.com/iamlxwis/CodeLab/actions/runs/45589161235) passed 9 of 10 stages and all 45 instrumentation tests. The separate Python functional smoke stopped at the same Android checked-state assertion on Light. The Compose instrumentation assertions read Compose semantics and passed as written. The updated smoke reads the Android accessibility tree's checked state. Layout measurements captured 13 visible editor lines on the phone with IME and 6 lines in landscape in both appearances. [Device evidence](https://github.com/iamlxwis/CodeLab/actions/runs/45589161235/artifacts/15009958933)

## Next action

Review the branch APK workflow and device evidence. Fix real failures, download and inspect the candidate APK, and record remaining physical-device coverage before handing off.
