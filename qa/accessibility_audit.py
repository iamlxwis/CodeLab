"""Inspect an actual Android accessibility hierarchy and save factual findings.

This is a report, not a complete WCAG/TalkBack certification.
"""
import json
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path("qa-results")
OUT.mkdir(exist_ok=True)

def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True, check=True, text=True,
                          timeout=40).stdout.strip()

def labeled(node):
    if node.attrib.get("text") or node.attrib.get("content-desc"):
        return True
    return any(child.attrib.get("text") or child.attrib.get("content-desc")
               for child in node.iter("node"))

def main():
    adb("shell", "uiautomator", "dump", "/sdcard/accessibility.xml")
    root = ET.fromstring(adb("exec-out", "cat", "/sdcard/accessibility.xml"))
    density_output = adb("shell", "wm", "density")
    match = re.search(r"(\d+)", density_output)
    factor = int(match.group(1)) / 160.0 if match else 1.0
    clickable = []
    unlabeled = []
    small = []
    for node in root.iter("node"):
        if node.attrib.get("clickable") != "true":
            continue
        clickable.append(node)
        bounds = re.findall(r"-?\d+", node.attrib.get("bounds", ""))
        if len(bounds) == 4:
            x1, y1, x2, y2 = map(int, bounds)
            w = (x2 - x1) / factor
            h = (y2 - y1) / factor
            if w < 48 or h < 48:
                small.append({"text": node.attrib.get("text", ""),
                              "description": node.attrib.get("content-desc", ""),
                              "width_dp": round(w, 1), "height_dp": round(h, 1)})
        if not labeled(node):
            unlabeled.append({"class": node.attrib.get("class", ""),
                              "bounds": node.attrib.get("bounds", "")})
    result = {
        "density": density_output,
        "interactive_count": len(clickable),
        "potentially_unlabeled_count": len(unlabeled),
        "potentially_small_touch_target_count": len(small),
        "unlabeled_examples": unlabeled[:30],
        "small_touch_target_examples": small[:30],
        "disclaimer": "Automated semantics report only; manual TalkBack audit required."
    }
    (OUT / "accessibility.json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()
