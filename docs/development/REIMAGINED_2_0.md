# Reimagined 2.0 handoff

**Status:** Ready for owner APK review. Work stopped Nov 25, 2026, 6:33 PM PHT.

The draft PR targets `main` from `reimagined-2.0`. The starting revision is `517f5bc`. The APK source revision is `0c93b17`, with the application implementation at `85f0a7b`. Later verification commits changed tests and documentation. [Draft PR #16](https://github.com/iamlxwis/CodeLab/pull/16) · [APK source](https://github.com/iamlxwis/CodeLab/commit/0c93b17)

## Workstreams

Four workstreams have automated verification for their listed scope. The device suite stages measure separate checks.

| Workstream | Evidence or remaining validation |
| --- | --- |
| Appearance, migration and design tokens | Verified app System, Light and Dark selection, neutral contrast and legacy Dark migration. Editor appearance and font settings remain independent. |
| Backdrop glass and fallback | Implemented recorded backdrop, Android 12 blur, Android 13+ AGSL lens and frosted or solid fallback. Capability policy passed JVM tests. Physical visual fidelity and frame pacing remain open. |
| Navigation, Home, Projects and Explore | Verified primary destinations, project favorites, navigation to runtime management and workspace return. |
| Languages and runtime presentation | Verified all 95 mappings, 87 bundled drawable resources, 8 documented monograms, and six offline runtimes. Live transfer and system install/remove flows remain open in Downloads. |
| Universal search and active workspace | Verified cross-project code search and resume. Large-project response time and the complete search and command matrix remain open. |
| Editor, console and secondary surfaces | Verified editing, keyboard input, file creation, console, tabs, split view, autosave, restore, rotation and local preview. Imports and secondary dialogs still need device review. |
| Settings, onboarding and accessibility | Verified guide resume and skip, settings behavior, 200% font launch, and sampled touch targets and semantics. TalkBack and keyboard focus order remain open. |
| Local developer activity | Verified the dashboard with real offline executions. Disable, reset and sharing remain open for device review. |
| Official updater and unified downloads | Verified the updater parser against official release metadata and checked the signed APK assets. Live download interruption, retry, runtime install/remove and a newer app release installation remain open. |
| CI, signed APK and Android 15 verification | Verified signed APK and six pack certificates, 202 JVM tests, strict lint, all 10 device stages with 45 instrumentation tests, and same-cert in-place upgrade. |

## Distribution and evidence

- **CodeLab.apk** is version `0.6.0-alpha`, build `1399`, package `site.lxwis.codelab`, Android 8.0+. Verified APK SHA-256 `a71bb3975b765bc7babf6ba568ff70cd7176ffb22a1424240f080e79aea2310c`. The production certificate SHA-256 is `c81d2aa187febd5252ad0369f2b4cfba368b5f16f6b4b049963fa284c9e42392`. [APK artifact](https://github.com/iamlxwis/CodeLab/actions/runs/45591658540/artifacts/15011378513) · [Optional packs](https://github.com/iamlxwis/CodeLab/actions/runs/45591658540/artifacts/15010999630)
- GitHub Actions passed 202 JVM tests with zero failures and strict lint with zero errors and 76 warnings. The test APK and runtime bundle used the existing production signer. [APK build](https://github.com/iamlxwis/CodeLab/actions/runs/45591658540)
- The signed Android 15 upgrade installed editor and Python build `1397` over published build `1337`. Android retained project text and the Dark mode setting, and the upgraded Python companion executed real code. Build `1399` and the earlier PR build `1396` have identical application code, resources and native libraries. [Signed upgrade](https://github.com/iamlxwis/CodeLab/actions/runs/45591086074)
- The Android 15 device suite passed all 10 stages and 45 instrumentation tests. It exercised six offline languages, console input, autosave, process restoration, cross-project code search and real activity. Both themes retained 13 complete lines with the phone IME and 6 in landscape. [Device run](https://github.com/iamlxwis/CodeLab/actions/runs/45591645578) · [Logs and screenshots](https://github.com/iamlxwis/CodeLab/actions/runs/45591645578/artifacts/15012039749)
- Motion collection recorded 104 janky frames out of 107 in a software-rendered emulator, with p95 at 350 ms. The debug emulator measured 5,033 ms cold start and 106,043 KiB PSS. These results leave physical performance and visual acceptance open. [Performance evidence](https://github.com/iamlxwis/CodeLab/actions/runs/45591645578/artifacts/15012039749)

## Architecture and next action

Glass capture is scoped to the three primary destinations and the floating controls. The native editor and console output retain opaque code surfaces. Existing project storage, runtime modules and explicit source-upload approval remain in use. BitChord’s public navigation and rendering source served as reference for CodeLab’s independent implementation. The attachment supplied the written specification, and the repository supplied the earlier CodeLab screenshots. [BitChord source](https://github.com/kushagrasinghx/BitChord/tree/main/app/src/main/java/com/music/bitchord/ui/components)

Owner review starts with build 1399 on physical Android 13 and 15 devices. Remaining acceptance covers glass and frame pacing, TalkBack and keyboard focus, secondary screens and imports, live online compilation with source-upload approval, and download interruption and installer confirmation. Stable publication remains gated on owner approval.
