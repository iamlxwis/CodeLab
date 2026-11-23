"""Exercise the installed debug build through Android's accessibility tree."""
import shlex
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "site.lxwis.codelab"
OUT = Path("qa-results")


def adb(*args, binary=False, check=True):
    data = subprocess.run(["adb", *args], check=check, capture_output=True,
                          timeout=40).stdout
    return data if binary else data.decode("utf-8", errors="replace").strip()


def launch():
    adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")


def snapshot(label):
    (OUT / f"{label}.png").write_bytes(adb("exec-out", "screencap", "-p", binary=True))


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/window.xml")
    xml = adb("exec-out", "cat", "/sdcard/window.xml")
    (OUT / "last-window.xml").write_text(xml)
    return list(ET.fromstring(xml).iter("node"))


def wait_for(*labels, field=False, contains=False, timeout=28):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        try:
            current = nodes()
            # Google APIs emulator occasionally displays a Pixel Launcher ANR on cold boot.
            # It obscures a healthy CodeLab screen and must not be confused with an app crash.
            if any("Pixel Launcher isn't responding" in n.attrib.get("text", "")
                   for n in current):
                dismiss = next((n for n in current if n.attrib.get("text") == "Close app"), None)
                if dismiss is not None:
                    tap_node(dismiss)
                    time.sleep(1)
                    continue
            for node in current:
                attrs = node.attrib
                if field and attrs.get("class") != "android.widget.EditText":
                    continue
                strings = (attrs.get("text", ""), attrs.get("content-desc", ""))
                if not labels or any(value == label or (contains and label in value)
                                     for label in labels for value in strings):
                    return node
        except (ET.ParseError, subprocess.CalledProcessError):
            pass
        time.sleep(0.35)
    raise AssertionError(f"Timed out waiting for {labels or 'text field'}")


def tap_node(node):
    x1, y1, x2, y2 = map(int, re.findall(r"-?\d+", node.attrib["bounds"]))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))


def tap(*labels):
    tap_node(wait_for(*labels))


def tap_text_result(name):
    """Tap the actual result TextView, never a search EditText with the same text."""
    until = time.monotonic() + 16
    while time.monotonic() < until:
        options = [node for node in nodes()
                   if node.attrib.get("class") == "android.widget.TextView"
                   and node.attrib.get("text") == name]
        if options:
            tap_node(options[0])
            return
        time.sleep(.4)
    raise AssertionError(f"Result text {name} did not appear")


def choose_language(name):
    """Choose from the searchable language browser, not an off-screen quick card."""
    tap("Change")
    field = wait_for(field=True)
    tap_node(field)
    adb("shell", "input", "text", name)
    # Dismiss IME to reveal the result, then select its TextView, not the
    # identically named editable search field.
    adb("shell", "input", "keyevent", "4")
    until = time.monotonic() + 16
    while time.monotonic() < until:
        choices = [node for node in nodes()
                   if node.attrib.get("class") == "android.widget.TextView"
                   and node.attrib.get("text") == name]
        if choices:
            tap_node(choices[0])
            wait_for("New project")
            return
        time.sleep(.4)
    raise AssertionError(f"Language result {name} did not appear")


def main():
    OUT.mkdir(exist_ok=True)
    apk = next(Path("app/build/outputs/apk/debug").glob("*.apk"))
    companion = next(Path("engine-python/build/outputs/apk/debug").glob("*.apk"))
    adb("install", "-r", str(companion))
    adb("install", "-r", str(apk))
    adb("shell", "pm", "clear", PACKAGE)
    assert "site.lxwis.codelab.engine.python" in adb("shell", "pm", "list", "packages")
    adb("shell", "cmd", "connectivity", "airplane-mode", "enable")
    adb("shell", "svc", "wifi", "disable")
    assert adb("shell", "settings", "get", "global", "airplane_mode_on") == "1"
    adb("logcat", "-c")
    launch()
    wait_for("Welcome to CodeLab")
    tap("Next")
    wait_for("Create a project")
    tap("Next")
    wait_for("Choose language and template")
    tap("Next")
    wait_for("Import projects")
    tap("Finish basics")
    wait_for("New project")
    snapshot("01-home")
    # Verify actual theme settings, not merely preview names.
    tap("More tools and settings")
    tap("Appearance")
    wait_for("Choose your style")
    tap("Glass")
    snapshot("theme-glass")
    tap("Clay")
    snapshot("theme-clay")
    tap("AMOLED")
    snapshot("theme-amoled")
    # Paper is the third row of the lazy appearance gallery.
    adb("shell", "input", "swipe", "420", "1230", "420", "520", "350")
    tap("Paper")
    snapshot("theme-paper")
    adb("shell", "input", "swipe", "420", "520", "420", "1230", "350")
    tap("Minimal")
    snapshot("theme-minimal")
    tap("Done")
    wait_for("New project")
    # The Python pack is installed by this smoke run; verify native in-app
    # management affordances without downloading on the airplane-mode emulator.
    tap("More tools and settings")
    tap("Languages")
    wait_for("Languages & file types")
    search = wait_for(field=True)
    tap_node(search)
    adb("shell", "input", "text", "Python")
    adb("shell", "input", "keyevent", "4")
    tap_text_result("Python")
    wait_for("Python runtime")
    wait_for("Check for updates")
    wait_for("Remove Python module")
    tap("Close")
    tap("Close languages")
    wait_for("New project")
    # Python is now a genuine CPython runtime. Run the starter with airplane mode on.
    tap("New project")
    choose_language("Python")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "PySmoke")
    tap("Create project")
    wait_for("main.py", contains=True)
    tap("Run")
    wait_for("Hello from CodeLab!", contains=True, timeout=40)
    wait_for("Exit 0", contains=True)
    snapshot("10-offline-python")

    tap("Project library")
    wait_for("New project")
    tap("New project")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "Smoke")
    tap("Create project")
    wait_for("main.js", contains=True)
    snapshot("02-editor")
    tap("Run", "Run file")
    wait_for("Waiting for input")
    snapshot("03-input")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "Ada")
    tap("Send")
    wait_for("Hello, Ada!", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("04-output")
    # Exercise the real stacked mobile layout before returning to single-panel editing.
    tap("Split")
    wait_for("Resize split panels")
    snapshot("04a-phone-split")
    tap("Split")
    tap("Editor")
    tap("Add file")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "answer.js")
    tap("Create")
    wait_for("Close answer.js")
    snapshot("05-new-file")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", shlex.quote("print(6*7)"))
    snapshot("06-after-input")
    wait_for("print(6*7)", contains=True)
    # Primary Run honors the preferred entry file; explicitly choose this tab.
    # Run modes are deliberate, never a silent fallback to cloud.
    wait_for("Ready: local runtime")
    tap("Online")
    wait_for("Internet connection required")
    tap("Offline")
    wait_for("Ready: local runtime")
    tap("Run file and entry options")
    tap("Run active · answer.js")
    wait_for("42", contains=True)
    wait_for("Exit 0", contains=True)
    tap("Editor")
    tap("All open tabs")
    tap("main.js")
    wait_for("Close main.js")
    tap("All open tabs")
    tap("answer.js")
    wait_for("Close answer.js")
    # Read the actual project file, not an accessibility node that can disappear
    # when the native EditText viewport collapses under IME or rotation.
    stored = lambda: adb("shell", "run-as", PACKAGE, "cat",
                         "files/workspace/projects/Smoke/answer.js")
    assert "print(6*7)" in stored(), "Answer file did not survive tab switching"
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", "0")
    adb("shell", "am", "force-stop", PACKAGE)
    assert "print(6*7)" in stored(), "Edited source was not durably saved"
    launch()
    wait_for("Close answer.js")
    wait_for("print(6*7)", contains=True)
    adb("shell", "settings", "put", "system", "user_rotation", "1")
    time.sleep(1)
    assert "print(6*7)" in stored(), "Rotation modified the saved source"
    wait_for("Close answer.js")
    snapshot("05-restored-landscape")

    # Verify the SQL starter and SQL console through the actual Android UI.
    adb("shell", "settings", "put", "system", "user_rotation", "0")
    time.sleep(1)
    tap("Project library")
    wait_for("New project")
    tap("New project")
    choose_language("SQLite")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "SqlSmoke")
    tap("Create project")
    wait_for("main.sql", contains=True)
    tap("Run")
    wait_for("2 rows", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("06-sql")

    # Verify the web starter and embedded preview navigation, fully offline.
    tap("Project library")
    wait_for("New project")
    tap("New project")
    choose_language("Web")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "WebSmoke")
    tap("Create project")
    wait_for("index.html", contains=True)
    tap("Preview")
    wait_for("Local preview")
    wait_for("Hello, CodeLab.", contains=True, timeout=20)
    snapshot("07-web-preview")

    # Verify both additional offline interpreters from installed Android APK.
    tap("Project library")
    wait_for("New project")
    tap("New project")
    choose_language("Lua")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "LuaSmoke")
    tap("Create project")
    wait_for("main.lua", contains=True)
    tap("Run")
    wait_for("Waiting for input")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "Ada")
    tap("Send")
    wait_for("Hello, Ada!", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("08-lua")

    tap("Project library")
    wait_for("New project")
    tap("New project")
    choose_language("Java")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "JavaSmoke")
    tap("Create project")
    wait_for("Main.java", contains=True)
    tap("Run")
    wait_for("Hello from CodeLab!", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("09-java-beta")

    (OUT / "result.txt").write_text(
        "Passed: install, create, launch offline JavaScript, interactive input, "
        "computed output, normal exit, file creation, editing, tab switching, autosave, "
        "process restoration, rotation, SQLite execution, rendered HTML preview, offline Lua, " 
        "Java beta execution, and real CPython execution in airplane mode.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        OUT.mkdir(exist_ok=True)
        snapshot("failure")
        (OUT / "logcat.txt").write_text(adb("logcat", "-d", "-t", "12000"))
        (OUT / "crash-log.txt").write_text(
            adb("logcat", "-d", "-b", "crash", "-t", "1000", check=False))
        raise
