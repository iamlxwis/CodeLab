"""Exercise the installed debug build through Android's accessibility tree."""
import os
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
    tap("Create")
    wait_for("main.js", contains=True)
    snapshot("02-editor")
    tap("Run", "Run file")
    wait_for("Waiting for input")
    snapshot("03-input")
    tap("Program input")
    adb("shell", "input", "text", "Ada")
    adb("shell", "input", "keyevent", "KEYCODE_ENTER")
    wait_for("Hello, Ada!", contains=True)
    wait_for("Exit 0", contains=True)
    snapshot("04-output")
    adb("shell", "am", "force-stop", PACKAGE)
    launch()
    wait_for("main.js", contains=True)
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", "1")
    time.sleep(1)
    wait_for("main.js", contains=True)
    snapshot("05-restored-landscape")
    (OUT / "result.txt").write_text(
        "Passed: install, create, launch offline JavaScript, interactive input, "
        "computed output, normal exit, process restoration, and rotation.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        OUT.mkdir(exist_ok=True)
        snapshot("failure")
        (OUT / "logcat.txt").write_text(adb("logcat", "-d", "-t", "2500"))
        raise
