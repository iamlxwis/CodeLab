"""Android 13/15 real-UI regression: offline Python requires a deliberate module install.

CI disposable emulator only. This script does not uninstall, clear user data,
download any APK or accept Android's installer prompt.
"""
from pathlib import Path
import device_smoke as ui

PACKAGE = "site.lxwis.codelab"
PYTHON = "site.lxwis.codelab.engine.python"


def main():
    ui.OUT.mkdir(exist_ok=True)
    installed = ui.adb("shell", "pm", "list", "packages")
    assert f"package:{PYTHON}" not in installed, (
        "Missing-module test requires a clean CI emulator with no Python pack")
    apk = next(Path("app/build/outputs/apk/debug").glob("*.apk"))
    assert "Success" in ui.adb("install", "-r", str(apk))
    ui.adb("shell", "pm", "clear", PACKAGE)
    ui.launch()
    ui.wait_for("Welcome to CodeLab", timeout=45)
    for action, expected in (
        ("Next", "Create a project"),
        ("Next", "Choose language and template"),
        ("Next", "Import projects"),
    ):
        ui.tap(action)
        ui.wait_for(expected)
    ui.tap("Finish basics")
    ui.wait_for("New project")
    ui.tap("New project")
    ui.choose_language("Python")
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", "MissingPython")
    ui.tap("Create project")
    ui.wait_for("main.py", contains=True)
    ui.tap("Run")
    ui.wait_for("Python runtime module not installed", contains=True, timeout=35)
    ui.wait_for("Manage Python")
    ui.snapshot("python-module-not-installed")
    ui.tap("Manage Python")
    ui.wait_for("Python runtime")
    ui.wait_for("Download Python")
    ui.snapshot("python-module-download-discovery")
    # Navigating to the module manager may not silently download or install.
    assert f"package:{PYTHON}" not in ui.adb("shell", "pm", "list", "packages")
    api = ui.adb("shell", "getprop", "ro.build.version.sdk")
    (ui.OUT / f"missing-python-api-{api}.txt").write_text(
        "PASS: Offline Python Run without module showed explicit error and "
        "a working Manage Python action, opened in-app Download Python controls; "
        "no silent source upload or module installation. "
        f"Android API {api}. Network transfer, installer approval and module "
        "updates remain separately unverified.\n"
    )


if __name__ == "__main__":
    try:
        main()
    except Exception:
        ui.OUT.mkdir(exist_ok=True)
        try:
            ui.snapshot("missing-python-failure")
            (ui.OUT / "missing-python-window.xml").write_text(
                ui.adb("exec-out", "cat", "/sdcard/window.xml", check=False))
        except Exception:
            pass
        raise
