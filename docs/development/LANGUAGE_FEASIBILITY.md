# Offline language-pack feasibility — Nov 24, 4:14 AM PHT

This is an engineering audit, **not** an executability or release claim. CodeLab must never advertise a language as runnable simply because the editor highlights or provides starter code for it. The user's approved goal is modular, on-device execution, without Termux, untrusted ZIP execution, automatic source upload, or forced online compilation.

## Source implementation versus actual release

| Language | Current implementation boundary | Remaining acceptance |
| --- | --- | --- |
| Python | Separate CPython companion APK, previously publicly released and emulator exercised | Complete latest network-download, installer, reinstall/update and Android 13/15 user journey |
| JavaScript | Signed Rhino companion project with IPC, download manager and bundled core fallback | Same-commit device acceptance and public signed APK; actual download/update testing |
| Lua | Signed LuaJ companion project with IPC and bundled core fallback | Same as JS |
| Java | Signed **BeanShell beta interpreter**, basic statements/main syntax only; core fallback | Device/public release for beta; **full javac still absent** |
| TypeScript | Signed optional official TS 5.9 compiler/transpileModule plus Rhino, **single-file .ts**, no full type checking or project imports | Build, actual .ts annotation transpilation, offline emulator/device, distribution acceptance |
| SQLite | Signed Android platform SQLite execution companion plus working built-in fallback | Build, emulator/device, distribution; Android provides SQLite binary, not a downloaded compiler |

## Further packs: evaluated but not falsely advertised as executable

| Requested language | Feasible technical direction | Current blocker / prerequisite |
| --- | --- | --- |
| C and C++ | ABI-specific signed Android-native LLVM/Clang or WASM compiler-and-runner package | No proven self-contained Android-hosted Clang, linker, standard library, execution sandbox or bounded-run QA. Host NDK cross-compilation does not mean the compiler runs on Android. |
| Java (full) | ECJ/compatible Java compiler + D8 + Android-safe in-memory class loading inside signed companion | No end-to-end Java source → DEX → run lifecycle or verified reflection/security contract; BeanShell cannot replace javac. |
| Kotlin | Kotlin compiler/stdlib and Android DEX conversion in a separate executable pack | Compiler/runtime compatibility, APK size, dependency resolution and Android API/ABI tests still unproven. |
| PHP | PHP.wasm browser build in a signed companion isolated process | Must package official interpreter WASM + its JS glue offline, provide filesystem/stdio bridge, disable arbitrary networking and pass actual PHP code execution; none integrated yet. |
| Ruby | ruby.wasm / CRuby WASI+browser bridge in a signed companion | WASM+stdlib packaging, WASI IO, app startup/memory budget and bounded execution not yet integrated or tested. |
| Go | Ship Android-hosted compiler or verified WASM SDK with ABI-specific runtime | No tested Android-hosted compiler/linker/stdlib with isolation, space/timeout limits or repeatable build. |
| Rust | Ship Android-hosted rustc/LLVM/linker or validated offline WASM compiler | Significant compiler/stdlib/toolchain size and ABI/in-process execution issues; no real Android build/runner. |
| C# | Signed Roslyn + Android-compatible managed runtime/compiler and isolated DEX/.NET execution path | No available proven build/runtime integration and language/project support in CodeLab. |

**Result:** The six companion projects above are implemented **in source**. All remaining languages are **research candidates or blocked integrations**, not installed runtimes. They stay in the persistent idea ledger and roadmap. Do not move to Phase 2 by quietly closing these requirements. A separate user-approved scope change would be required to defer them.

## Android execution constraints

- Android 10+ forbids `execve` of binaries placed in the writable app home directory by untrusted apps. Downloading generic compiler ZIPs then marking them executable is not an option. Packaged, signature-verified Android runtime packages or approved Android-native code-loading facilities are required. [Official Android 10 changes](https://developer.android.com/about/versions/10/behavior-changes-10).
- Android 14+ enforces stronger read-only rules for dynamically loaded code. Avoid downloaded DEX/JAR execution from mutable files. [Official Android 14 changes](https://developer.android.com/about/versions/14/behavior-changes-14).
- Android NDK Clang is a supported **host-side cross-compiler**, not proof that an Android phone includes Clang itself. [NDK build systems](https://developer.android.com/ndk/guides/other_build_systems).
- PHP.wasm has official browser-facing runtime packages. [WordPress PHP.wasm packages](https://developer.wordpress.org/playground/developers/architecture/php-wasm-packages/).
- CRuby WASI/browser packages exist. [ruby.wasm documentation](https://ruby.github.io/ruby.wasm/).

## Pack acceptance contract

Build Android release/debug on CI; verify matching production APK signing and exact package/service identity; confirm on-device zero-network execution, valid syntax/IO/error exit, cancellation/timeouts/output bounds, no source or project leakage, module install/update/uninstall and project-data retention; document ABI and version compatibility. No test means **source implemented**, not **verified complete**. Existing built-ins remain until safe replacement is demonstrated.
