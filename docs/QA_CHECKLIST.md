# QA and regression checklist

## Build verification

From the repository root with JDK 17 and Android SDK 35:

    ./gradlew :app:assembleDebug
    ./gradlew :app:testDebugUnitTest
    ./gradlew :app:connectedDebugAndroidTest

GitHub Actions:

- Android APK: Gradle compilation, signature verification, unit tests,
  direct APK release and SHA-256 output.
- Android device smoke: instrumented tests, real device performance measurements,
  accessibility inspection, airplane-mode editor/runtime journey, screenshots and logcat.
- CI outputs are factual measurements; no FPS, startup, or memory target is claimed without evidence.

## Core user journeys

1. Create, duplicate, rename and delete projects. Restore from trash.
2. Create nested folders and text files. Rename, move and copy.
3. Edit multiple tabs, undo/redo, search/replace and recover state after restart.
4. Execute JS, Lua, Java beta and SQL with real program output and input.
5. Confirm never to claim Python or C/C++ can run offline.
6. Preview HTML/CSS/JS offline with proper sandboxed asset paths.
7. Export and import ZIP archives with size and traversal limits.
8. Import public GitHub sources with bounded ZIP processing.
9. Initialize local Git, commit, and switch branches with a clean working tree.
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
