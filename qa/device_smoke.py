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
            for node in nodes():
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


def main():
    OUT.mkdir(exist_ok=True)
    apk = next(Path("app/build/outputs/apk/debug").glob("*.apk"))
    adb("install", "-r", str(apk))
    adb("shell", "pm", "clear", PACKAGE)
    adb("shell", "cmd", "connectivity", "airplane-mode", "enable")
    adb("shell", "svc", "wifi", "disable")
    assert adb("shell", "settings", "get", "global", "airplane_mode_on") == "1"
    adb("logcat", "-c")
    launch()
    wait_for("New project")
    snapshot("01-home")
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
    tap("Editor")
    tap("Add file")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "answer.js")
    tap("Create")
    wait_for("Close answer.js")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", shlex.quote("print(6*7)"))
    wait_for("print(6*7)", contains=True)
    tap("Run")
    wait_for("42", contains=True)
    wait_for("Exit 0", contains=True)
    tap("Editor")
    tap("main.js")
    wait_for("Close main.js")
    tap("answer.js")
    wait_for("Close answer.js")
    adb("shell", "am", "force-stop", PACKAGE)
    launch()
    wait_for("Close answer.js")
    wait_for("print(6*7)", contains=True)
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", "1")
    time.sleep(1)
    wait_for("print(6*7)", contains=True)
    snapshot("05-restored-landscape")

    # Verify the SQL starter and SQL console through the actual Android UI.
    adb("shell", "settings", "put", "system", "user_rotation", "0")
    time.sleep(1)
    tap("Project library")
    wait_for("New project")
    tap("New project")
    tap("SQLite")
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
    tap("Web")
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
    tap("Lua")
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
    tap("Java")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "JavaSmoke")
    tap("Create project")
    wait_for("Main.java", contains=True)
    tap("Run")
    wait_for("Hello from CodeLab!", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("09-java-beta")

    # Cloud-only template is editable in airplane mode, but cannot claim to run offline.
    tap("Project library")
    wait_for("New project")
    tap("New project")
    tap_node(wait_for("Browse", contains=True))
    tap("Search language or extension")
    adb("shell", "input", "text", "Python")
    tap("Python")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "PySmoke")
    # Ensure the software keyboard does not obscure the sheet's Create action.
    adb("shell", "input", "keyevent", "4")
    tap("Create project")
    wait_for("main.py", contains=True)
    tap("Run")
    wait_for("This language needs an online compiler", contains=True)
    tap("Close")
    snapshot("10-online-required")

    (OUT / "result.txt").write_text(
        "Passed: install, create, launch offline JavaScript, interactive input, "
        "computed output, normal exit, file creation, editing, tab switching, autosave, "
        "process restoration, rotation, SQLite execution, rendered HTML preview, offline Lua, " 
        "Java beta execution, and honest offline behavior for cloud-only Python.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        OUT.mkdir(exist_ok=True)
        snapshot("failure")
        (OUT / "logcat.txt").write_text(adb("logcat", "-d", "-t", "2500"))
        raise
