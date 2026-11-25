# Reimagined 2.1 handoff

The refinement branches from `b4cbbf5`, the head of open Reimagined 2.0 PR #16. Its PR targets `reimagined-2.0`. Review 2.0 first, then review the refinement on top of it.

## Verified scope

- [x] Run-only chooser, offline and unavailable-online handling, revision-bound consent tests, rerun and stop
- [x] Compact editor, tabs, overflow and keyboard accessory
- [x] Integrated terminal, split and mode selector
- [x] Code, result and combined snippet images
- [x] Home and Projects separation and responsive cards
- [x] Language logo presentation and coverage
- [ ] Glass and floating navigation physical performance approval. Android 15 measured actual AGSL pixel displacement.
- [ ] Full secondary screen review. Representative settings, search, runtime and download routes passed.
- [ ] Manual TalkBack and physical-device accessibility review. Automated Android 15 regression checks passed.
- [x] Signed branch APK and owner handoff, with owner approval pending

The two task attachments are written specifications. The annotated BitChord, editor, console, project and keyboard screenshots mentioned in the new brief were unavailable. Earlier CodeLab emulator evidence and public BitChord rendering references from 2.0 are available.

The first integration checkpoint compiles, passes local JVM tests and compiles Android instrumentation. The workspace Run, file chooser, recent workspace and practice lesson use per-request execution selection. Code and output share a paginated render plan for preview and export. Home has one resume surface, and the editor panel selector has three modes.

Next action is to exercise the real Android UI and verify a signed APK. Snippet export stress coverage, narrow editor controls, secondary surfaces and physical acceptance are still in progress. [Draft PR #17](https://github.com/iamlxwis/CodeLab/pull/17)

## Android 15 integration findings

The first emulator run passed 8 of 10 stages, including 45 instrumentation tests. Functional smoke found the close action on a newly created tab clipped after the tab switcher appeared. The phone was usable with 13 complete lines above the IME in both appearances. Landscape had only three complete lines. The refinement now scrolls using the measured tab viewport and moves panel selection into Workspace Actions in short windows. [First Android 15 run](https://github.com/iamlxwis/CodeLab/actions/runs/45613861590/artifacts/15022342038)

The first signed build stopped at Android lint's deprecated break-strategy constant. Snippet rendering now uses the platform builder's simple, unhyphenated default. Extra tests cover exact ranges across multiline tokens, wrapping, whitespace, long transcripts, mode eligibility, and pixel identity of the first and last shared PNGs. Very large shares offer numbered page groups, while Save PNG writes the complete set. [First build](https://github.com/iamlxwis/CodeLab/actions/runs/45613865369)

The first signed candidate was build 1402 from \`3a16259\`, version \`0.6.1-alpha\`, with CodeLab's existing production certificate and a verified download checksum. It remained an intermediate checkpoint. The next candidate adds responsive project grid compositions, runtime download status labels, shared language icon sizing, and an explicit recovery message if the online compiler changes while the consent dialog is open. Selected snippet ranges preserve a trailing newline, and a device test compares actual AGSL backdrop pixels at the capsule edge. [Build 1402](https://github.com/iamlxwis/CodeLab/actions/runs/45628377294)

The second Android 15 run verified nine stages and 49 instrumentation tests. Both landscape appearances and the phone IME matrix passed, and the functional check exercised the active-tab fix. It later typed the Lua response into the source editor while in Split because the smoke script selected the first editable field. The revised check selects the labeled program input and verifies that Lua source remains unchanged. [Second Android 15 run](https://github.com/iamlxwis/CodeLab/actions/runs/45628375193/artifacts/15031342008)

Build 1403 from `dc3ee23` is available for manual evaluation. Its downloaded APK SHA-256 is `136ed93784821682bc2958c38543526fb61107c6e002dbd75ed84d2e515f84db`. GitHub access is required for the ZIP. [Build 1403 APK](https://github.com/iamlxwis/CodeLab/actions/runs/45629872711/artifacts/15030262852) · [Earlier signed Android 15 upgrade](https://github.com/iamlxwis/CodeLab/actions/runs/45628375859)

## Owner handoff

The app source in signed build **1403** is `dc3ee23`. The final Android smoke used `c094d1a`, which adds QA and documentation updates over the same app source. GitHub Actions verified the existing production certificate. The in-place Android 15 upgrade preserved project source and Dark appearance and ran the signed Python companion after the update. [Test APK](https://github.com/iamlxwis/CodeLab/actions/runs/45629872711/artifacts/15030262852) · [Signed upgrade run](https://github.com/iamlxwis/CodeLab/actions/runs/45630542286)

All 202 JVM tests, 15 Python QA guard tests and 50 Android instrumentation tests passed. Strict lint reported zero errors. The emulator passed all 10 stages, including clean install, six offline languages, HTML preview, console stdin in Split, rerun, stop, command availability, search, project data recovery and 200% font-scale startup. The matrix measured 13 complete code lines on the 360×800 dp phone above the IME and six at 800×360 dp in both appearances. Snippet tests verified exact multiline selections, wrap and pagination, first and final source pixels in decoded PNGs, content URI grants and accurate partial-output labels. [Final Android 15 evidence](https://github.com/iamlxwis/CodeLab/actions/runs/45632769173/artifacts/15034300327) · [Final CI build](https://github.com/iamlxwis/CodeLab/actions/runs/45632780984)

The real software-rendered emulator motion sample recorded 195 janky frames out of 218, or 89.45%, with a 150 ms p95. The 2.0 emulator sample recorded 97.2% and 350 ms p95. These measurements leave physical GPU and interaction performance open. The automated accessibility audit detected no undersized or unlabeled controls among ten sampled targets. [Android evidence archive](https://github.com/iamlxwis/CodeLab/actions/runs/45632769173/artifacts/15034300327)

Owner review covers a physical phone, TalkBack and keyboard interactions, a configured online provider and its consent flow, and saving or sharing PNGs through the device’s chosen document provider and target app. A complete secondary-screen visual pass remains open. The annotated references were unavailable, so the visual review used the brief and actual emulator captures. Results in Snippet Studio use the latest available execution transcript. Native console text selection remains available for copying.

PR #17 remains a draft stacked on open PR #16. Review 2.0 first, then verify and review the full stack before integration. [Reimagined 2.1 PR](https://github.com/iamlxwis/CodeLab/pull/17)
