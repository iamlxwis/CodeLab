"""Record measured Android emulator app-start and memory statistics.

Reports evidence; there are deliberately no universal pass/fail performance targets.
"""
import json
import os
import re
import subprocess
from pathlib import Path

OUT = Path("qa-results")
OUT.mkdir(parents=True, exist_ok=True)
PACKAGE = "site.lxwis.codelab"

def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=True,
                          timeout=40).stdout.strip()

def main():
    variant = os.environ.get("CODELAB_APK_VARIANT", "debug")
    if variant not in ("debug", "release"):
        raise ValueError("CODELAB_APK_VARIANT must be 'debug' or 'release'")
    apk = next(Path(f"app/build/outputs/apk/{variant}").glob("*.apk"))
    adb("install", "-r", str(apk))
    adb("shell", "am", "force-stop", PACKAGE)
    start_output = adb("shell", "am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
    match = re.search(r"TotalTime:\s*(\d+)", start_output)
    memory = adb("shell", "dumpsys", "meminfo", PACKAGE)
    total = re.search(r"TOTAL PSS:\s*(\d+)", memory)
    gfx = adb("shell", "dumpsys", "gfxinfo", PACKAGE)
    rendered = re.search(r"Total frames rendered:\s*(\d+)", gfx)
    janky = re.search(r"Janky frames:\s*(\d+)", gfx)
    percentiles = {}
    for percentile in (50, 90, 95, 99):
        found = re.search(rf"{percentile}th percentile:\s*(\d+)\s*ms", gfx)
        percentiles[str(percentile)] = int(found.group(1)) if found else None
    frame_total = int(rendered.group(1)) if rendered else None
    frame_janky = int(janky.group(1)) if janky else None
    latency_lines = adb("logcat", "-d", "-s", "CodeLabLatency:D", "*:S").splitlines()
    tap_latencies = []
    for line in latency_lines:
        hit = re.search(r"CodeLabLatency:.*?([^:]+?) next-frame:\s*([\d.]+) ms", line)
        if hit:
            tap_latencies.append({"action": hit.group(1).strip(), "milliseconds": float(hit.group(2))})
    result = {
        "package": PACKAGE,
        "apk_variant": variant,
        "apk_bytes": apk.stat().st_size,
        "cold_start_milliseconds": int(match.group(1)) if match else None,
        "total_pss_kb": int(total.group(1)) if total else None,
        "frames_rendered": frame_total,
        "janky_frames": frame_janky,
        "janky_percentage": (round(100.0 * frame_janky / frame_total, 2)
                             if frame_total and frame_janky is not None else None),
        "frame_time_percentiles_ms": percentiles,
        "debug_tap_next_frame_samples": tap_latencies[-40:],
        "device": adb("shell", "getprop", "ro.product.model"),
        "android_api": adb("shell", "getprop", "ro.build.version.sdk"),
        "source": "adb am start -W, dumpsys meminfo/gfxinfo, logcat (debug only), APK bytes",
    }
    (OUT / "performance.json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()
