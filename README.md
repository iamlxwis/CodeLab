# CodeLab

Native Android code editor and hybrid offline/online execution workspace. Built with Kotlin, Jetpack Compose, Android's standard file APIs, and an offline JavaScript/Lua/SQLite runtime services.

## Download and install

### [Download CodeLab.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk)

Direct public APK download: **no ZIP, GitHub login, API key, or signup required.** Android 8.0 (API 26) or newer. On Android, open the downloaded APK and approve installation from your browser or file manager if prompted.

The link points to the most recently published GitHub Release. A new APK is published automatically after the `main` branch's APK build, signature verification, and unit tests pass. The [latest release](https://github.com/iamlxwis/CodeLab/releases/tag/latest) includes a SHA-256 checksum. The separate [device smoke workflow](https://github.com/iamlxwis/CodeLab/actions/workflows/device.yml) performs emulator tests.

**Early-access warning:** This is a debug-signed APK, not a production release. The GitHub Actions debug signing key may change between builds; Android may reject an update if the signature differs. Back up your projects using **Export project ZIP** before uninstalling or replacing the app. Do not assume an APK can be installed over an older version without losing local app data.

## What works

| Capability | Status |
|---|---|
| Native dark/light/system UI | Implemented |
| Multiple files, editor tabs, highlighting, search and replace, code keyboard toolbar | Implemented |
| Local projects, autosave, session recovery, ZIP import/export | Implemented |
| Offline JavaScript execution | Implemented via Mozilla Rhino interpreter |
| JavaScript console output, errors and `readLine()` input | Implemented |
| Offline SQLite | Implemented with Android SQLite (in-memory database per Run) |
| Offline Lua | Implemented with LuaJ 3.0.1 (Lua 5.2 style), local Run console and readLine input |
| Java beta | Offline BeanShell interpreter for basic Java statements and a single `main` method; no javac, compiled classes, or full Java API compatibility |
| Verified online compiler | Optional Judge0-compatible service; available language IDs fetched from the configured server |
| Stop, timeout, isolated runtime process | Implemented for supported runtimes |
| Offline HTML/CSS/JavaScript preview | Implemented for local text assets |
| Python, C, C++, Java, Rust, Go and other cloud languages | Available only when a reachable public Judge0-compatible server actually reports the requested language and accepts submissions; no guaranteed uptime |
| Real local Git history and branches | Implemented using JGit; remote push/authentication pending Android-device verification |
| Import public GitHub repository source | Implemented; HTTP download with size/path validation, no Git history |
| Command palette and developer toolbox | Implemented: JSON, Base64, URL transformations, SHA-256 |
| PNG code screenshots | Offline syntax-colored cards, gradient/solid/transparent backgrounds, aspect presets, line highlights and PNG export |
| Project metadata | Description and preferred entry file stored in ZIP-portable project settings |
| External-folder import | User-selected SAF folder is copied into an editable local project; not live external editing |
| JavaScript debugger | Rhino source-line pause, breakpoints, Step and Continue; JavaScript only |
| Workspace commands | Built-in offline pwd, ls, tree, cd, cat, head, wc, grep and git status; not a Linux shell or PTY |
| HTTPS Inspector | Opt-in bounded GET inspection with headers, JSON formatting and 10-second network timeout |
| File history and line diff | Implemented with bounded local snapshots and textual line differences |
| Full Linux shell / PTY | Not implemented |

**JavaScript:** Open a `.js` file and tap Run. This is the Rhino JavaScript runtime, not Node.js or a web browser. Use `print("hello")`, `console.log()`, `console.error()`, and `readLine("Name?")`. Network and filesystem host APIs are not provided. A 60-second worker timeout and console output limit are enforced.

**Java beta:** Create a Java starter project and Run `Main.java`. This is an offline BeanShell interpreter running basic Java statements inside the body of `public static void main(String[] args)`. It supports common expressions, loops, method calls, `System.out.println` and `System.in` input where compatible. It is **not javac**, does not compile a JVM class, and does not yet support arbitrary custom class definitions, multi-file Java projects, or all standard Java language features. The Java runtime runs in a dedicated isolated process and remains subject to timeout and output limits. This interpreter is not a proven security sandbox.

**Lua:** Create a Lua project or open a `.lua` file and tap Run. Lua executes entirely offline using the LuaJ interpreter. Use `print('Hello')`, `readLine('Name?')` or `input('Name?')` for interactive input, and Lua's built-in string, table, math and coroutine features. Host filesystem, OS, module-loading, and Java reflection APIs are not exposed. This is LuaJ-compatible Lua 5.2-style behavior, not a full native Lua installation. The runtime is independently stopped by the worker service's 60-second timeout.

**SQLite:** Create a `.sql` file, write SQL statements, and tap Run. CodeLab runs statements against a new in-memory SQLite database, prints query results and statement diagnostics, and returns a nonzero exit status for invalid SQL. State does **not** persist between runs. `ATTACH`, `DETACH`, and `VACUUM` are deliberately unavailable.

## JavaScript debugger and workspace commands

Open a `.js` file, choose **⋮ → Debug JavaScript…**, and optionally enter comma-separated source line numbers. The debugger pauses at the first statement and at configured breakpoints. Open the Console to **Step**, **Continue**, or **Stop**. This is real Rhino source-line instrumentation for JavaScript only. It does not yet include variable inspectors or debugging other runtimes. Normal Run continues to execute without debugger overhead.

Choose **⋮ → Workspace commands…** to navigate local project files with safe built-ins (`help`, `pwd`, `ls`, `tree`, `cd`, `cat`, `head`, `wc`, `grep`, `git status`). No external shell commands or host processes can be executed.

## Hybrid language execution

CodeLab **detects local execution capability and actual online compiler availability separately**. The Languages & Cloud sheet identifies JavaScript, Java (beta), Lua and SQLite as local runtimes, and HTML/CSS as offline web-preview targets. Other languages can be edited without internet, but **execution requires a reachable Judge0-compatible compiler** that reports the language in its `GET /languages` endpoint. Supported remote languages depend on your server, permissions, quotas and runtime versions.

To run an online language **without an API key or account**:

1. Connect your phone to the internet.
2. Open **⋮ → Languages** and tap **Check free compiler availability**.
3. If a public compiler responds with a language list, create or open a matching source file.
4. Press **Run** and explicitly approve sending the current file and optional standard input.

The app uses a public Judge0-compatible endpoint by default. **Free online compilation is best-effort**: the operator may rate-limit requests, disable languages, require authentication, or become unavailable without notice. CodeLab does not ask you to supply an API key. An online language is not labeled ready until the server reports it. Local JavaScript, Lua, SQLite, and limited Java run without internet.

Online submissions use Judge0's token-based asynchronous API and poll until execution finishes. Cloud input is specified **before** execution; interactive `readLine()` is supported only by suitable offline runtimes. The app can cancel local monitoring, but submitted code may continue executing remotely. The UI does not claim cloud functionality simply because internet is connected—only the server's verified `/languages` response enables the corresponding run options.

**Privacy:** No source files are uploaded automatically. Only a confirmed cloud run sends the current source and its provided input. The default public compiler endpoint is configured in the app; users do not need credentials. Do not include secrets in code submitted to public compiler services. The cloud server may retain execution data or impose quotas according to its own policies. Offline preview remains restricted to local project assets.

## HTTPS Inspector

Choose **⋮ → HTTPS Inspector…** and enter a URL beginning with `https://`. CodeLab sends a GET request only after you press **Send GET**. Redirects are not followed; requests use a 10-second network timeout and read at most 128 KiB of the response. Embedded credentials and localhost URLs are rejected. No project code is uploaded. This feature is optional and requires internet access.

## Imported binary web assets

CodeLab imports image and font assets as bytes rather than forcing them through UTF-8 text decoding. Local web previews can load project-relative PNG/JPEG/WebP/GIF, fonts and other supported assets from the offline virtual origin. Editor text files remain limited to 256 KiB, while supported binary assets can be up to 2 MiB each, within the 16 MiB project import ceiling. This does not add a binary image editor.

## Project templates and offline web preview

The new-project dialog includes JavaScript, Java beta, Lua, SQLite, and Web starter projects. Web projects contain `index.html`, `style.css`, and `script.js`. Open an HTML file and choose **Preview** to render the page in a restricted embedded WebView. Relative project CSS/JS/text assets load from local app storage; external network resources are blocked. The preview can display browser `console.log` messages and refresh its document.

## Build locally

Use Android Studio with JDK 17 and Android SDK Platform 35. From the repository root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

To run device tests, connect an emulator or device and run `./gradlew :app:connectedDebugAndroidTest`.

The automated [Android APK](https://github.com/iamlxwis/CodeLab/actions/workflows/android.yml) workflow signs, verifies, tests, and publishes a directly downloadable [CodeLab.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk) to GitHub Releases. GitHub Actions artifacts remain ZIP-wrapped; the public Release asset is the APK itself. The [device smoke](https://github.com/iamlxwis/CodeLab/actions/workflows/device.yml) workflow uses an Android emulator to exercise project creation, offline execution, editor state, and rotation.

## Project structure

- `app/src/main/java/site/lxwis/codelab/` — Android editor, UI, file store and runtime.
- `app/src/test/` — JVM tests.
- `app/src/androidTest/` — instrumented Android tests.
- `qa/` — offline emulator smoke test.
- `.github/workflows/` — APK and device test pipelines.

## Engineering status and QA

- [Implementation status by category](docs/IMPLEMENTATION_STATUS.md)
- [Regression and release QA checklist](docs/QA_CHECKLIST.md)
- [Changelog](CHANGELOG.md)

## Current limitations

This is not yet a full desktop-class IDE. There is no offline Python interpreter, C/C++ compiler, semantic language-server completion, full PTY shell, remote GitHub push, or full browser development server. Basic local Git repositories, commits and branches are provided through JGit, and public GitHub source snapshots can be imported without Git history. Git runtime support is being validated on Android devices. Editing support for a file extension does not imply runnable support. The Android SQLite engine is included with the OS, and SQL databases are ephemeral per run.

JavaScript executes in an Android isolated process. The SQLite runner uses a separate, non-exported app-UID process because the Android SQLite API is not available to the isolated UID on tested Android versions. SQL runs against an in-memory database with restricted statement types. Neither runtime should be treated as a proven sandbox against arbitrary hostile code.

## Privacy & licensing

No account, analytics SDK, ads, or remote code execution service is required for local languages. A network permission enables compiler discovery and user-confirmed cloud submissions. Project files stay on-device unless exported. JGit 7.6.0 is distributed under the Eclipse Distribution License 1.0. BeanShell 2.1.1 is available under the Apache License 2.0 license. LuaJ 3.0.1 is available under the MIT license and bundled as a Maven dependency. The bundled Rhino engine is distributed with its license and notices under `app/src/main/assets/licenses/rhino/`. Verify third-party licenses before redistribution.
