> **Current CI/architecture notes:** See [development project memory](development/PROJECT_MEMORY.md) and [known issues](development/KNOWN_ISSUES.md).

# QA and regression checklist

## Build verification

From the repository root with JDK 17 and Android SDK 35:

    ./gradlew :app:assembleDebug
    ./gradlew :app:testDebugUnitTest
    ./gradlew :app:connectedDebugAndroidTest

GitHub Actions:

- Android APK: Gradle compilation, signature verification, unit tests,
  report-only Android lint and JaCoCo coverage, public APK and SHA-256 output.
- Android device smoke: instrumented tests, **emulator** performance measurements,
  accessibility inspection, airplane-mode editor/runtime journey, screenshots and logcat.
- CI outputs are factual emulator measurements; no phone FPS, startup, or memory claim is made without physical evidence.
- The static gate in `qa/security_gate.py` checks selected secret signatures, plaintext-token persistence, stage-all Git calls, backup policy and isolated runtime configuration.
- `qa/motion_smoke.py` measures the R8-optimized release APK at 1x animation scale, records frame percentiles and restores the previous setting. Device functional tests remain on debug. Emulator jank is not physical-device certification.

## Core user journeys

1. Create, duplicate, rename and delete projects. Restore from trash.
2. Create nested folders and text files. Rename, move and copy.
3. Edit multiple tabs, undo/redo, search/replace and recover state after restart.
4. Execute JS, Lua, Java beta and SQL with real program output and input.
5. Check Python 3.13 isolated worker output/timeout on emulator; latest device smoke FAILED waiting for Hello World. C/C++ remains editing-only. Do not claim Python works on device before a passing emulator journey.
6. Preview HTML/CSS/JS offline with proper sandboxed asset paths.
7. Export and import ZIP archives with size and traversal limits.
8. Import public GitHub sources with bounded ZIP processing.
9. Initialize Git, stage only selected files, commit and switch branches.
   Test opt-in GitHub HTTPS Fetch/fast-forward Pull/non-force Push with a disposable
   remote, invalid tokens, divergent history and offline access.
10. Create and verify PNG code screenshots, with user-selected destination.
11. Change themes, interface scaling and motion preferences.
12. Use external keyboard and touch input, portrait and landscape layouts.
13. Test TalkBack labels, focus order and minimum touch targets on a physical device.
14. Test large input, full console buffer, crash/restart and low-storage recovery.

## Known CI distinction

A successful APK workflow does not automatically imply the device smoke workflow passed.
The emulator suite has previously exposed a Python project creation dialog obscured by
the software keyboard; the dialog and smoke script were updated, but newer device
verification must complete to establish that regression as fixed.

JGit's runtime compatibility must be tested on Android, in addition to JVM tests.
The full performance and accessibility reports are uploaded as artifacts by CI when
their corresponding steps run.
