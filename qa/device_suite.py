"""Run independent device checks, retain every result, fail if any check fails."""
import json
import subprocess
import time
from pathlib import Path

STAGES = [
    ('release-install', ['python3', 'qa/clean_install_smoke.py']),
    ('tutorial-resume', ['python3', 'qa/tutorial_resume_smoke.py']),
    ('missing-runtime', ['python3', 'qa/missing_python_smoke.py']),
    ('functional-smoke', ['python3', 'qa/device_smoke.py']),
    ('instrumentation', ['bash', 'gradlew', ':app:connectedDebugAndroidTest']),
    ('performance', ['python3', 'qa/performance_smoke.py']),
    ('motion', ['python3', 'qa/motion_smoke.py']),
    ('accessibility', ['python3', 'qa/accessibility_audit.py']),
    ('font-scale', ['python3', 'qa/font_scale_smoke.py']),
    ('layouts', ['python3', 'qa/layout_screenshots.py']),
]

def run_stages(stages, output, execute=subprocess.run):
    output.mkdir(parents=True, exist_ok=True)
    results = []
    for name, command in stages:
        entry = {'stage': name, 'status': 'running', 'returncode': None}
        results.append(entry)
        (output / 'suite-results.json').write_text(json.dumps(results, indent=2))
        print(f'::group::Device verification: {name}', flush=True)
        started = time.monotonic()
        try:
            entry['returncode'] = execute(command, check=False).returncode
        except Exception as error:
            entry['returncode'] = 1
            entry['error'] = type(error).__name__
        entry['seconds'] = round(time.monotonic() - started, 2)
        entry['status'] = 'passed' if entry['returncode'] == 0 else 'failed'
        (output / 'suite-results.json').write_text(json.dumps(results, indent=2))
        print(f'{name}: {entry["status"]} (exit {entry["returncode"]})', flush=True)
        print('::endgroup::', flush=True)
    return results

def successful(results):
    return bool(results) and all(entry['returncode'] == 0 for entry in results)

if __name__ == '__main__':
    results = run_stages(STAGES, Path('qa-results'))
    print(json.dumps(results, indent=2))
    raise SystemExit(0 if successful(results) else 1)
