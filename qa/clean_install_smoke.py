"""Check a clean installation of the actual release variant before functional QA.

This runs only on a disposable CI emulator. Never run its uninstall step on a
user's phone: uninstalling CodeLab can delete locally stored project data.
"""
from pathlib import Path
import subprocess

PACKAGE = "site.lxwis.codelab"
APK = Path("app/build/outputs/apk/release/app-release.apk")

def adb(*args, check=True):
    result = subprocess.run(["adb", *args], capture_output=True, text=True,
                            check=False, timeout=55)
    if check and result.returncode:
        raise AssertionError(
            f"adb {' '.join(args)} failed ({result.returncode}): "
            f"{result.stdout.strip()} {result.stderr.strip()}"
        )
    return result.stdout.strip()

def main():
    if not APK.is_file():
        raise FileNotFoundError(f"Release APK missing: {APK}")
    Path("qa-results").mkdir(exist_ok=True)
    # Emulators used for CI have no user data. Explicitly test a clean
    # installation rather than upgrading a debug APK signed by the same run.
    adb("uninstall", PACKAGE, check=False)
    # pm path returns exit 1 when a package is absent. That is expected here.
    assert not adb("shell", "pm", "path", PACKAGE, check=False), "Core app is still installed"
    install = adb("install", str(APK))
    assert "Success" in install, f"Installer did not confirm success: {install}"
    installed = adb("shell", "pm", "path", PACKAGE)
    assert "base.apk" in installed, f"Installed package not found: {installed}"
    start = adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")
    assert "Status: ok" in start, f"Release APK would not launch: {start}"
    from device_smoke import wait_for, snapshot
    wait_for("Welcome to CodeLab", timeout=30)
    snapshot("release-clean-install")
    api = adb("shell", "getprop", "ro.build.version.sdk")
    Path("qa-results/clean-install-result.txt").write_text(
        "Passed: release APK installed as a new Android package and "
        f"launched on API {api} x86_64 emulator. Phone firmware and "
        "downloaded-asset integrity remain unverified.\n"
    )
    adb("uninstall", PACKAGE)

if __name__ == "__main__":
    main()
