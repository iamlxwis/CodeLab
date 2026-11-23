# Python 3.13 runtime integration

Embedded CPython 3.13 via Chaquopy 17.2.0 (MIT); builds target arm64-v8a and x86_64 only.
This deliberately increases APK size and drops installation support for 32-bit-only devices.

User Python executes in a separate `isolatedProcess` with a bounded Messenger protocol.
The worker captures stdout/stderr, supports `input()`, and is forcibly terminated after
its run or timeout. CodeLab does not grant workspace paths or cloud credentials to Python.
CPython standard-library modules which depend on restricted platform APIs may not work.

**Release gate:** Android emulator + physical device test must confirm CPython starts
successfully from the isolated UID and runs Hello World, stdin, syntax errors and stop.
If initialization fails, this feature must be fixed or reverted rather than shipped
with a misleading offline label.

Android project uses Chaquopy version 17.2.0, Python 3.13.16 (runtime patch managed by
Chaquopy), and Maven Central plugin resolution. Do not claim universal package support.
