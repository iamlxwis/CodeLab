"""Record measured Android emulator app-start and memory statistics.

Reports evidence; there are deliberately no universal pass/fail performance targets.
"""
import json
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
    apk = next(Path("app/build/outputs/apk/debug").glob("*.apk"))
    adb("install", "-r", str(apk))
    adb("shell", "am", "force-stop", PACKAGE)
    start_output = adb("shell", "am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
    match = re.search(r"TotalTime:\s*(\d+)", start_output)
    memory = adb("shell", "dumpsys", "meminfo", PACKAGE)
    total = re.search(r"TOTAL PSS:\s*(\d+)", memory)
    result = {
        "package": PACKAGE,
        "apk_bytes": apk.stat().st_size,
        "cold_start_milliseconds": int(match.group(1)) if match else None,
        "total_pss_kb": int(total.group(1)) if total else None,
        "device": adb("shell", "getprop", "ro.product.model"),
        "android_api": adb("shell", "getprop", "ro.build.version.sdk"),
        "source": "adb am start -W, dumpsys meminfo and APK file size",
    }
    (OUT / "performance.json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()
