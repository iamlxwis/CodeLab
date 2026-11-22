"""Capture and check key controls with Android's 200% font-scale setting.

This is automated evidence, not a full manual TalkBack or WCAG audit.
"""
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path("qa-results")
OUT.mkdir(exist_ok=True)
PACKAGE = "site.lxwis.codelab"

def adb(*args, binary=False):
    value = subprocess.run(["adb", *args], capture_output=True, check=True,
                           timeout=45).stdout
    return value if binary else value.decode("utf-8", "replace")

def main():
    previous = adb("shell", "settings", "get", "system", "font_scale").strip()
    try:
        adb("shell", "settings", "put", "system", "font_scale", "2.0")
        adb("shell", "am", "force-stop", PACKAGE)
        adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")
        adb("shell", "uiautomator", "dump", "/sdcard/font.xml")
        contents = adb("exec-out", "cat", "/sdcard/font.xml")
        (OUT / "font-200-hierarchy.xml").write_text(contents)
        (OUT / "font-200-screen.png").write_bytes(
            adb("exec-out", "screencap", "-p", binary=True))
        root = ET.fromstring(contents)
        values = [
            node.attrib.get("text", "") + " " + node.attrib.get("content-desc", "")
            for node in root.iter("node")
        ]
        assert any("CodeLab" in value or "Projects" in value or
                   "New project" in value or "Project library" in value
                   for value in values), "No recognizable navigation at 200% font"
        (OUT / "font-200-result.txt").write_text(
            "200% font scale: app launched, navigation present, screenshot captured.\n"
            "Manual TalkBack, focus-order and clipping review still required.\n")
    finally:
        adb("shell", "settings", "put", "system",
            "font_scale", previous if previous not in ("", "null") else "1.0")

if __name__ == "__main__":
    main()
