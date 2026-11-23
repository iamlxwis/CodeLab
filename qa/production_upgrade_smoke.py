"""Production-signed APK upgrade acceptance on disposable CI emulators only.

This script NEVER uninstalls packages or clears data. It does install signed
previous/current APKs and creates disposable test projects. Not for personal devices.
"""
import os
import re
import subprocess
from pathlib import Path
import device_smoke as ui

APP = "site.lxwis.codelab"
PY = "site.lxwis.codelab.engine.python"
OUT = Path("qa-results")
MARKER = "UpgradeProof20261123"
APKS = (
    Path("prior-apk/CodeLab.apk"),
    Path("app/build/outputs/apk/release/app-release.apk"),
    Path("prior-apk/CodeLab-Python.apk"),
    Path("engine-python/build/outputs/apk/release/engine-python-release.apk"),
)


def command(*args):
    result = subprocess.run(args, text=True, capture_output=True, timeout=90)
    if result.returncode:
        raise AssertionError(f"{args[0]} failed ({result.returncode}): {result.stdout[-1000:]} {result.stderr[-1000:]}")
    return result.stdout


def tool(name):
    return str(Path(os.environ["ANDROID_HOME"]) / "build-tools" / "35.0.0" / name)


def describe(path):
    assert path.is_file(), f"Missing APK: {path}"
    badge = command(tool("aapt"), "dump", "badging", str(path))
    pkg = re.search(r"package: name='([^']+)' versionCode='(\d+)'", badge)
    assert pkg, f"Cannot identify APK package/version: {path}"
    signed = command(tool("apksigner"), "verify", "--print-certs", str(path))
    cert = re.search(r"Signer #1 certificate SHA-256 digest: ([0-9a-fA-F]+)", signed)
    assert cert, f"Cannot authenticate APK signer: {path}"
    return pkg.group(1), int(pkg.group(2)), cert.group(1).lower()


def validate_apks():
    values = [describe(path) for path in APKS]
    assert values[0][0] == values[1][0] == APP, "Editor applicationId changed"
    assert values[2][0] == values[3][0] == PY, "Python applicationId changed"
    assert values[1][1] > values[0][1], "Editor versionCode did not increase"
    assert values[3][1] > values[2][1], "Python versionCode did not increase"
    assert len({item[2] for item in values}) == 1, "Old/new editor/Python signing identities differ"
    return values


def installed_version(package):
    data = ui.adb("shell", "dumpsys", "package", package)
    found = re.search(r"versionCode=(\d+)", data)
    assert found, f"Installed versionCode unavailable: {package}"
    return int(found.group(1))


def install(path, update=False):
    args = ("install", "-r", str(path)) if update else ("install", str(path))
    assert "Success" in ui.adb(*args), f"PackageManager rejected {path}"


def point(node):
    x1, y1, x2, y2 = map(int, re.findall(r"-?\d+", node.attrib["bounds"]))
    return (x1 + x2) // 2, (y1 + y2) // 2


def verify_clay():
    tree = ui.nodes()
    # Newer APKs expose a contextual, accessible style/selection label on the
    # clickable card; the previous production release used a separate check icon.
    # Verify either representation so the real OLD -> NEW upgrade stays testable.
    if any(n.attrib.get("content-desc") == "Clay style, selected" for n in tree):
        assert any(n.attrib.get("text") == "Clay" for n in tree), (
            "Clay selection label was detached from its visible style")
        return
    labels = [n for n in tree if n.attrib.get("text") == "Clay"]
    old_markers = [n for n in tree if n.attrib.get("content-desc") == "Selected"]
    assert any(abs(point(a)[1] - point(b)[1]) < 80 and point(b)[0] > point(a)[0]
               for a in labels for b in old_markers), "Saved Clay appearance was not selected"


def open_workspace_action(name):
    """Use the searchable action sheet; destinations are nested in categories."""
    ui.tap("More tools and settings")
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", name)
    ui.adb("shell", "input", "keyevent", "4")
    # Search EditText repeats the result name; tap only the result TextView.
    ui.tap_text_result(name)


def create_test_data():
    ui.launch()
    ui.wait_for("Welcome to CodeLab", timeout=45)
    for text in ("Next", "Next", "Next", "Finish basics"):
        ui.tap(text)
    ui.wait_for("New project")
    ui.tap("New project")
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", "UpgradeProof")
    ui.tap("Create project")
    ui.wait_for("main.js", contains=True)
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", MARKER)
    ui.wait_for(MARKER, contains=True)
    ui.tap("Save file")
    open_workspace_action("Appearance")
    ui.wait_for("Choose your style")
    ui.tap("Clay")
    verify_clay()
    ui.tap("Done")
    ui.adb("shell", "am", "force-stop", APP)
    ui.launch()
    ui.wait_for(MARKER, contains=True, timeout=45)
    ui.snapshot("signed-upgrade-before")


def validate_persistence_and_python():
    ui.adb("shell", "am", "force-stop", APP)
    ui.launch()
    ui.wait_for(MARKER, contains=True, timeout=45)
    ui.tap("Project library")
    ui.wait_for("UpgradeProof")
    ui.tap("UpgradeProof")
    ui.wait_for(MARKER, contains=True, timeout=45)
    open_workspace_action("Appearance")
    ui.wait_for("Choose your style")
    verify_clay()
    ui.tap("Done")
    ui.snapshot("signed-upgrade-after")
    open_workspace_action("Languages")
    ui.wait_for("Language Manager")
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", "Python")
    ui.adb("shell", "input", "keyevent", "4")
    ui.tap_text_result("Python")
    ui.wait_for("Python runtime")
    ui.wait_for("Download latest Python")
    ui.tap("Close")
    ui.tap("Close languages")
    ui.tap("Project library")
    ui.tap("New project")
    ui.choose_language("Python")
    ui.tap_node(ui.wait_for(field=True))
    ui.adb("shell", "input", "text", "UpgradePython")
    ui.tap("Create project")
    ui.wait_for("main.py", contains=True)
    ui.tap("Run")
    ui.wait_for("Hello from CodeLab!", contains=True, timeout=50)
    ui.wait_for("Exit 0", contains=True)
    ui.snapshot("signed-upgrade-python")


def main():
    OUT.mkdir(exist_ok=True)
    a, b, c, d = validate_apks()
    for package in (APP, PY):
        assert not ui.adb("shell", "pm", "path", package, check=False), (
            "Requires an empty disposable CI emulator; refusing to touch existing installation")
    install(APKS[2])
    install(APKS[0])
    assert installed_version(APP) == a[1] and installed_version(PY) == c[1]
    create_test_data()
    # Critical: in-place update with NO uninstall and NO pm clear.
    install(APKS[1], update=True)
    install(APKS[3], update=True)
    assert installed_version(APP) == b[1] and installed_version(PY) == d[1]
    validate_persistence_and_python()
    api = ui.adb("shell", "getprop", "ro.build.version.sdk")
    (OUT / f"production-upgrade-api-{api}.txt").write_text(
        f"PASS API{api}: same-certificate editor/Python adb install -r, "
        f"monotonic versions {a[1]}->{b[1]} and {c[1]}->{d[1]}, "
        "persisted project text and Clay setting, trusted companion and real Python execution.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        OUT.mkdir(exist_ok=True)
        try:
            ui.snapshot("signed-upgrade-failure")
            (OUT / "signed-upgrade-crash.txt").write_text(
                ui.adb("logcat", "-d", "-b", "crash", "-t", "1000", check=False))
        except Exception:
            pass
        raise
