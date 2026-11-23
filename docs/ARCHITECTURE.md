> **Current approved runtime architecture and discrepancies:** [development decisions](development/DECISIONS.md) and [project memory](development/PROJECT_MEMORY.md). Existing bundled engines are legacy implementation, not the modular target.

# CodeLab architecture

CodeLab is a native Kotlin/Jetpack Compose Android app (minSdk 26, targetSdk 35).
Its storage and execution model is **local-first**, not a Linux terminal.

| Layer | Source | Responsibilities |
| --- | --- | --- |
| UI and themes | `MainActivity.kt`, `WorkspaceScreen.kt`, `ProjectLibraryScreen.kt`, `CodeLabTheme.kt` | Navigation, Material 3 surfaces, responsive editor/console/preview |
| Motion | `CodeLabMotion.kt`, `WorkspaceTab.kt`, `UiLatencyProbe.kt` | System-scale-aware durations, microinteractions, debug frame sample |
| State and persistence | `WorkspaceModel.kt`, `ProjectStore.java`, `WorkspaceSession.java` | Per-project files, session restoration, versioned editable buffers, one-second autosave, serialized background I/O, atomic writes |
| Editor | `SourceView.kt`, `Editor.kt`, `CodeAnalysis.kt` | Android EditText IME and hardware keyboard, cancellable off-thread viewport regex highlighting, editing tools |
| Runtimes | `RuntimeService.kt`, `JavaRuntimeService.kt`, `LuaRuntimeService.kt`, `SqlRuntimeService.kt` | Offline Rhino JavaScript, limited BeanShell Java, LuaJ Lua, Android SQLite; JS/Java/Lua use isolated processes |
| Project safety | `GitWorkspace.kt`, `GitPanels.kt`, `GitHubRemoteAddress.kt`, `ExternalFolderImporter.kt` | Selected-path staging, canonical GitHub HTTPS origin, FF-only pull, non-force push and bounded imports |
| Optional cloud | `CloudJudge0.java`, `CloudCredentialVault.kt` | Explicit online run confirmation, HTTPS compiler endpoint, Android Keystore AES-GCM for optional token |
| CI | `.github/workflows`, `qa/` | APK build/signature, unit and emulator instrumentation, security invariants, frame/memory and accessibility evidence |

## Data and security boundaries

- Project files are in app-private storage under `filesDir/workspace`. The
  editor saves asynchronously, debounced, with versioned in-memory buffers.
  Pause/stop requests a durable flush; projects are exported explicitly.
- Git commits only include explicitly staged files. Optional GitHub HTTPS
  sync has user-triggered Fetch, confirmed FF-only Pull and confirmed non-force
  Push. The access token is encrypted under a separate Android Keystore alias;
  origin URLs are validated again before network activity. Public GitHub
  source import still downloads a bounded ZIP, **not** Git history.
- Cloud source code leaves the device only after user confirmation.
  Optional cloud credentials are stored as AES-GCM ciphertext; the key
  remains in Android Keystore. Legacy plaintext preferences are migrated.
- Local JS, Java beta and Lua runtimes use isolated Android services.
  SQLite executes in a separate app process but is not an isolated process.
- Android app backup is disabled. Without a stable production signing key,
  the GitHub fallback-debug-signed APK may require reinstalling; export first.

## Known gaps

Tree-sitter/LSP semantic parsing, PTY terminal, GitHub OAuth and live
authenticated Git remote acceptance, low-end device profiling, generated
baseline profiles, production signing, and exhaustive accessibility acceptance
remain undone. Android lint and JaCoCo reports are observational only.
