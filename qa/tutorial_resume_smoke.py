"""Android 13/15 UI proof that trying a feature does not complete onboarding.

Runs only on a disposable CI emulator, using exclusively test project state.
"""
from pathlib import Path
import device_smoke as ui

PACKAGE = "site.lxwis.codelab"


def main():
    ui.OUT.mkdir(exist_ok=True)
    apk = next(Path("app/build/outputs/apk/debug").glob("*.apk"))
    assert "Success" in ui.adb("install", "-r", str(apk))
    ui.adb("shell", "pm", "clear", PACKAGE)
    ui.launch()
    ui.wait_for("Welcome to CodeLab", timeout=40)
    ui.tap("Next")
    ui.wait_for("Create a project")
    ui.tap("Try it in CodeLab")
    ui.wait_for("New project")
    # Exploring the real project creation screen must leave the guide unfinished.
    ui.adb("shell", "am", "force-stop", PACKAGE)
    ui.launch()
    ui.wait_for("Choose language and template", timeout=40)
    ui.snapshot("tutorial-resumes-after-try")
    # Deliberately skipping finishes; it must not reopen on every launch.
    ui.tap("Skip")
    # Prove the guide actually closed before killing the process.
    ui.wait_for("New project", timeout=25)
    assert not any(n.attrib.get("text") == "Choose language and template"
                   for n in ui.nodes()), "Skip did not close the guide"
    ui.adb("shell", "am", "force-stop", PACKAGE)
    ui.launch()
    ui.wait_for("New project", timeout=40)
    assert not any(n.attrib.get("text") == "Welcome to CodeLab"
                   for n in ui.nodes()), "Skipped guide unexpectedly restarted"
    ui.snapshot("tutorial-deliberately-completed")
    api = ui.adb("shell", "getprop", "ro.build.version.sdk")
    (ui.OUT / f"tutorial-resume-api-{api}.txt").write_text(
        f"PASS API{api}: Try real project action -> restart resumes next "
        "guide topic; intentional Skip -> restart no longer opens onboarding.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        ui.OUT.mkdir(exist_ok=True)
        try:
            ui.snapshot("tutorial-resume-failure")
            (ui.OUT / "tutorial-resume-window.xml").write_text(
                ui.adb("exec-out", "cat", "/sdcard/window.xml", check=False))
        except Exception:
            pass
        raise
