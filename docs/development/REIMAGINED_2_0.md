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
| CI, signed APK and device verification | Earlier local app compilation passed. Branch APK and API 35 device checks pending |

## Architecture and evidence

The Compose backdrop is recorded only on the three library destinations. The Android 13 renderer samples the recorded layer in a local capsule, applies a distance-based AGSL lens and uses Android RenderEffect for blur. Android 12 uses blur. Older, low-memory and power-saving devices can use a frosted treatment. High contrast uses a solid surface. The native editor and live console output bypass backdrop recording.

CodeLab’s own implementation uses BitChord’s public navigation and backdrop source for reference. The GPL navigation, branding and assets were not copied. The task attachment contained the specification only. Existing CodeLab editor and web preview screenshots were inspected from `docs/screenshots`.

Baseline [APK build](https://github.com/iamlxwis/CodeLab/actions/runs/45518242552) and [API 35 device smoke](https://github.com/iamlxwis/CodeLab/actions/runs/45518254838) passed at `517f5bc`. The separate baseline publication workflow failed. The branch passed local static security invariants and 15 Python QA guard tests. Full Android and device verification remains pending.

## Next action

Review the branch APK workflow and device evidence. Fix real failures, download and inspect the candidate APK, and record remaining physical-device coverage before handing off.
