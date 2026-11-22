# CodeLab

Native Android code editor and offline execution workspace. Built with Kotlin, Jetpack Compose, Android's standard file APIs, and an isolated JavaScript/SQLite runtime service.

## Install an APK

Open the [Android APK workflow](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml), choose a successful workflow run, and download the **CodeLab-debug** artifact. Extract the ZIP and install `app-debug.apk` on Android 8.0 (API 26) or newer. You may need to allow installation from your browser or file manager. Debug artifacts are signed with a CI-generated debug key; do not treat them as production releases.

CodeLab is early-access software; back up projects with **Export project ZIP** before uninstalling or resetting the app. Project files reside in app-private storage.

## What works

| Capability | Status |
|---|---|
| Native dark/light/system UI | Implemented |
| Multiple files, editor tabs, highlighting, search and replace, code keyboard toolbar | Implemented |
| Local projects, autosave, session recovery, ZIP import/export | Implemented |
| Offline JavaScript execution | Implemented via Mozilla Rhino interpreter |
| JavaScript console output, errors and `readLine()` input | Implemented |
| Offline SQLite | Implemented with Android SQLite (in-memory database per Run) |
| Stop, timeout, isolated runtime process | Implemented for supported runtimes |
| Offline HTML/CSS/JavaScript preview | Implemented for local text assets |
| Python, C++, Java, Rust compilation | Not implemented |
| Full Linux shell / PTY | Not implemented |

**JavaScript:** Open a `.js` file and tap Run. This is the Rhino JavaScript runtime, not Node.js or a web browser. Use `print("hello")`, `console.log()`, `console.error()`, and `readLine("Name?")`. Network and filesystem host APIs are not provided. A 60-second worker timeout and console output limit are enforced.

**SQLite:** Create a `.sql` file, write SQL statements, and tap Run. CodeLab runs statements against a new in-memory SQLite database, prints query results and statement diagnostics, and returns a nonzero exit status for invalid SQL. State does **not** persist between runs. `ATTACH`, `DETACH`, and `VACUUM` are deliberately unavailable.

## Project templates and offline web preview

The new-project dialog includes JavaScript, SQLite, and Web starter projects. Web projects contain `index.html`, `style.css`, and `script.js`. Open an HTML file and choose **Preview** to render the page in a restricted embedded WebView. Relative project CSS/JS/text assets load from local app storage; external network resources are blocked. The preview can display browser `console.log` messages and refresh its document.

## Build locally

Use Android Studio with JDK 17 and Android SDK Platform 35. From the repository root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

To run device tests, connect an emulator or device and run `./gradlew :app:connectedDebugAndroidTest`.

The automated [Android APK](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml) workflow signs and uploads a debug APK. The [device smoke](https://github.com/iamlxwis/CodeLab/actions/workflows/device.yml) workflow uses an Android emulator to exercise project creation, offline execution, editor state, and rotation.

## Project structure

- `app/src/main/java/site/lxwis/codelab/` — Android editor, UI, file store and runtime.
- `app/src/test/` — JVM tests.
- `app/src/androidTest/` — instrumented Android tests.
- `qa/` — offline emulator smoke test.
- `.github/workflows/` — APK and device test pipelines.

## Current limitations

This is not yet a full desktop-class IDE. There is no Python interpreter, C/C++ compiler, language-server completion, PTY, Git integration, or a full browser development server. Editing support for a file extension does not imply runnable support. The Android SQLite engine is included with the OS, and SQL databases are ephemeral per run.

JavaScript executes in an Android isolated process. The SQLite runner uses a separate, non-exported app-UID process because the Android SQLite API is not available to the isolated UID on tested Android versions. SQL runs against an in-memory database with restricted statement types. Neither runtime should be treated as a proven sandbox against arbitrary hostile code.

## Privacy & licensing

No account, analytics SDK, ads, or remote code execution service are required. Project files stay on-device unless exported. The bundled Rhino engine is distributed with its license and notices under `app/src/main/assets/licenses/rhino/`. Verify third-party licenses before redistribution.
