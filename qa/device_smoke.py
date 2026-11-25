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
    summary = []
    try:
        summary = [n.attrib.get("text", "") for n in nodes()
                   if n.attrib.get("text", "")][-18:]
    except (ET.ParseError, subprocess.CalledProcessError):
        pass
    raise AssertionError(f"Timed out waiting for {labels or 'text field'}; "
                         f"visible text tail: {summary}")


def tap_node(node):
    x1, y1, x2, y2 = map(int, re.findall(r"-?\d+", node.attrib["bounds"]))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))


def tap(*labels):
    tap_node(wait_for(*labels))


def scroll_to(*labels, attempts=6, direction="down", swipe_x=.5):
    """Reach a real visible UI target on varied screen sizes without fixed pixel coordinates."""
    size = adb("shell", "wm", "size")
    matches = re.findall(r"(\d+)x(\d+)", size)
    w, h = map(int, matches[-1])
    start, end = (.77, .35) if direction == "down" else (.35, .77)
    for _ in range(attempts):
        try:
            return wait_for(*labels, timeout=2.5)
        except AssertionError:
            adb("shell", "input", "swipe", str(int(w*swipe_x)), str(int(h*start)),
                str(int(w*swipe_x)), str(int(h*end)), "350")
    return wait_for(*labels, timeout=4)


def selected(label):
    return any(
        (node.attrib.get("selected") == "true"
         or (node.attrib.get("checkable") == "true" and node.attrib.get("checked") == "true"))
        and any(child.attrib.get("text") == label for child in node.iter("node"))
        for node in nodes()
    )


def select_mode(name):
    tap_node(scroll_to(name))
    assert selected(name), f"{name} appearance did not report its selected state"
    snapshot("appearance-" + name.lower())


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



def open_workspace_action(name):
    """Reach categorized actions through the user-visible accessible search."""
    tap("More tools and settings")
    wait_for("Search actions")
    # Compose places the accessible hint on a child of the editable field.
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", name)
    adb("shell", "input", "keyevent", "4")
    # The search field repeats the query; select the real result TextView.
    tap_text_result(name)


def choose_language(name):
    """Choose from the searchable language browser, not an off-screen quick card."""
    tap_node(scroll_to("Change", "Browse all languages"))
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
    # Both optional interpreters must be installed before CodeLab so that
    # Android verifies their signature-permission IPC on a real API 35 device.
    for engine in ("engine-js", "engine-lua", "engine-java", "engine-ts", "engine-sql"):
        pack = next(Path(engine, "build/outputs/apk/debug").glob("*.apk"))
        adb("install", "-r", str(pack))
    adb("install", "-r", str(apk))
    adb("shell", "pm", "clear", PACKAGE)
    packages = adb("shell", "pm", "list", "packages")
    for engine in ("python", "js", "lua", "java", "ts", "sql"):
        assert f"site.lxwis.codelab.engine.{engine}" in packages
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
    # Verify the real app-wide color mode selector and its selected semantics.
    tap("Settings")
    tap("Appearance")
    wait_for("Liquid Glass")
    for mode in ("Light", "Dark", "System", "Dark"):
        select_mode(mode)
    tap("Back to settings")
    tap("Close settings")
    wait_for("New project")
    tap("Explore")
    tap("Offline runtimes")
    wait_for("Search languages")
    wait_for("Installed")
    tap("Downloads")
    wait_for("Wi-Fi only")
    wait_for("Offline runtime packs")
    tap("Back")
    tap("Offline runtimes")
    tap("Available")
    # Validate installed signed companions through their actual detail dialogs.
    for language in ("JavaScript", "Lua", "Java", "TypeScript", "SQLite"):
        tap_node(scroll_to(language))
        wait_for(f"Verified optional {language} module installed", contains=True)
        tap("Back")
    tap("Installed")
    wait_for("languages", contains=True)
    # Searching automatically expands to All instead of hiding an edit-only language.
    search = wait_for(field=True)
    tap_node(search)
    adb("shell", "input", "text", "Python")
    adb("shell", "input", "keyevent", "4")
    tap_text_result("Python")
    wait_for("Python runtime")
    wait_for("Download latest Python")
    wait_for("Remove Python module")
    tap("Remove Python module")
    wait_for("Remove Python runtime?")
    wait_for("Open uninstaller")
    tap("Cancel")
    # A cancelled confirmation must keep the installed signed module intact.
    wait_for("Python runtime")
    tap("Back")
    tap("Back")
    tap("Home")
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
    # Keep the IME open: Send must remain reachable without hiding the keyboard.
    wait_for("Send")
    snapshot("03a-input-keyboard")
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
    # A duplicate file name must not replace or blank the already open file.
    tap("Add file")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "answer.js")
    tap("Create")
    wait_for("That name is already in use.", contains=True)
    tap("Close")
    wait_for("Close answer.js")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", shlex.quote("print(6*7)"))
    snapshot("06-after-input")
    wait_for("print(6*7)", contains=True)
    # Mode controls intentionally collapse during typing; reveal them before choosing.
    ime = adb("shell", "dumpsys", "input_method")
    if re.search(r"(?:mInputShown|mIsInputViewShown|isInputViewShown)=true", ime):
        adb("shell", "input", "keyevent", "4")
    # Primary Run honors the preferred entry file; explicitly choose this tab.
    # Run modes are deliberate, never a silent fallback to cloud.
    wait_for("Ready: local runtime")
    tap("Online")
    # The emulator can retain a validated default network even with its
    # airplane toggle on. Both states are legitimate: no network or no
    # configured cloud provider, or a usable online compiler. Never assert
    # airplane mode implies the app cannot access an online compiler.
    wait_for("Internet connection required", "No online compiler for this file",
             "Online: source upload needs confirmation")
    tap("Offline")
    wait_for("Ready: local runtime")
    tap("Run file and entry options")
    tap("Run active · answer.js")
    wait_for("42", contains=True)
    wait_for("Exit 0", contains=True)
    tap("Editor")
    tap("Quick switch open files")
    tap("main.js")
    wait_for("Close main.js")
    tap("Quick switch open files")
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
    tap("Projects")
    # Favorites must hide other projects, including Smoke.
    tap("Manage PySmoke")
    tap("Add to favorites")
    tap("Favorites")
    wait_for("PySmoke")
    assert not any(n.attrib.get("content-desc") == "Manage Smoke" for n in nodes()), (
        "Favorites filter leaked an unstarred project")
    snapshot("projects-favorites-filter")
    tap("Manage PySmoke")
    tap("Remove from favorites")
    wait_for("No favorites yet")
    tap("Show all projects")
    wait_for("Smoke")
    # Duplicate-name creation must return to the project library with an error.
    tap("New project")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "Smoke")
    tap("Create project")
    wait_for("That name is already in use.", contains=True)
    tap("Close")
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
    # Keep the IME open: Send must remain reachable without hiding the keyboard.
    wait_for("Send")
    snapshot("03a-input-keyboard")
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
    tap("Project library")
    wait_for("New project")
    tap("New project")
    choose_language("TypeScript")
    tap_node(wait_for(field=True))
    adb("shell", "input", "text", "TypeScriptSmoke")
    tap("Create project")
    wait_for("main.ts", contains=True)
    # ProjectStore's Beginner template contains a real TypeScript type annotation;
    # assert the exact on-device file before trusting the runtime output.
    typed_source = adb("shell", "run-as", PACKAGE, "cat",
                       "files/workspace/projects/TypeScriptSmoke/main.ts")
    assert "const message: string" in typed_source, (
        "TypeScript starter must contain a real type annotation")
    (OUT / "typescript-starter.txt").write_text(typed_source)
    tap("Run")
    wait_for("Hello from CodeLab!", contains=True, timeout=30)
    wait_for("Exit 0", contains=True)
    snapshot("11-typescript")


    # Find an actual saved source line from another project in universal search.
    tap("Project library")
    tap("Search")
    wait_for("Search CodeLab")
    search_field = wait_for(field=True)
    tap_node(search_field)
    adb("shell", "input", "text", shlex.quote("print(6*7)"))
    wait_for("answer.js:1")
    snapshot("universal-code-search")
    tap("answer.js:1")
    wait_for("Close answer.js")
    wait_for("print(6*7)", contains=True)
    tap("Project library")
    wait_for("Resume workspace")
    tap("Home")
    tap_node(scroll_to("Developer activity"))
    wait_for("Last 30 days")
    wait_for("Recent executions")
    snapshot("developer-activity")
    tap("Back")

    (OUT / "result.txt").write_text(
        "Passed: install, create, launch offline JavaScript, interactive input, "
        "computed output, normal exit, file creation, editing, tab switching, autosave, "
        "process restoration, rotation, SQLite execution, rendered HTML preview, offline Lua, " 
        "Java beta execution, TypeScript single-file transpilation, real CPython execution in airplane mode, "
        "cross-project code search, workspace resume, and local activity.\n")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        OUT.mkdir(exist_ok=True)
        # Preserve the original failure even if the emulator itself disappeared.
        try:
            snapshot("failure")
        except Exception as capture_error:
            (OUT / "failure-capture-error.txt").write_text(str(capture_error))
        for filename, args in (("logcat.txt", ("logcat", "-d", "-t", "12000")),
                               ("crash-log.txt", ("logcat", "-d", "-b", "crash", "-t", "1000"))):
            try:
                (OUT / filename).write_text(adb(*args, check=False))
            except Exception as capture_error:
                (OUT / filename).write_text(str(capture_error))
        raise
