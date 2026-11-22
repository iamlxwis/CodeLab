"""Measure emulator frame stats with system animations explicitly enabled.

The functional smoke workflow disables animations for determinism. This extra
experiment uses 1x system animator duration, scrolls the current screen,
collects gfxinfo and restores the previous system setting on completion.
It does not certify FPS on physical devices or every transition.
"""
from pathlib import Path
import json
import re
import subprocess
import time

OUTPUT = Path("qa-results")
OUTPUT.mkdir(exist_ok=True)
PACKAGE = "site.lxwis.codelab"

def adb(*args):
    return subprocess.run(["adb", *args], text=True, capture_output=True,
                          check=True, timeout=40).stdout.strip()

def main():
    old = adb("shell", "settings", "get", "global", "animator_duration_scale")
    stats = {
        "package": PACKAGE,
        "animation_scale": "1x",
        "scenario": "three vertical scrolling gestures, emulator at 1x system animations",
        "frames": None,
        "janky_frames": None,
        "janky_percentage": None,
    }
    try:
        adb("shell", "settings", "put", "global", "animator_duration_scale", "1")
        adb("shell", "am", "force-stop", PACKAGE)
        adb("shell", "am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
        time.sleep(1)
        adb("shell", "dumpsys", "gfxinfo", PACKAGE, "reset")
        for _ in range(3):
            adb("shell", "input", "swipe", "440", "1100", "440", "360", "300")
            adb("shell", "input", "swipe", "440", "360", "440", "1100", "300")
        time.sleep(1)
        raw = adb("shell", "dumpsys", "gfxinfo", PACKAGE)
        frames = re.search(r"Total frames rendered:\s*(\d+)", raw)
        jank = re.search(r"Janky frames:\s*(\d+)", raw)
        if frames:
            stats["frames"] = int(frames.group(1))
        if jank:
            stats["janky_frames"] = int(jank.group(1))
        if stats["frames"] and stats["janky_frames"] is not None:
            stats["janky_percentage"] = round(
                100.0 * stats["janky_frames"] / stats["frames"], 2)
        stats["device"] = adb("shell", "getprop", "ro.product.model")
    finally:
        if old in ("null", ""):
            adb("shell", "settings", "delete", "global", "animator_duration_scale")
        else:
            adb("shell", "settings", "put", "global", "animator_duration_scale", old)
        (OUTPUT / "motion-performance.json").write_text(json.dumps(stats, indent=2) + "\n")
        print(json.dumps(stats, indent=2))

if __name__ == "__main__":
    main()
