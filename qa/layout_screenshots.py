"""Capture real API35 activity layouts and measure the IME editor viewport.

Only disposable emulators: modifies display overrides, restores them even on failure.
"""
import json
import re
import subprocess
from pathlib import Path

OUT = Path('qa-results/layouts')
APP = 'site.lxwis.codelab'

def adb(*args):
    return subprocess.run(['adb', *args], capture_output=True, text=True, check=True, timeout=120).stdout

def override(kind):
    text = adb('shell', 'wm', kind)
    match = re.search(r'Override ' + kind + r':\s*(\S+)', text, re.I)
    return match.group(1) if match else 'reset'

def main():
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == '1', 'Disposable emulator required'
    OUT.mkdir(parents=True, exist_ok=True)
    for apk in (Path('app/build/outputs/apk/debug/app-debug.apk'),
                Path('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')):
        assert apk.is_file(), f'Missing built instrumentation artifact: {apk}'
        adb('install', '-r', str(apk))
    previous_size, previous_density = override('size'), override('density')
    previous_font = adb('shell', 'settings', 'get', 'system', 'font_scale').strip()
    results = []
    try:
        adb('shell', 'settings', 'put', 'system', 'font_scale', '1.0')
        for name, pixels, density in [('phone', '1080x2400', '480'),
                                       ('landscape', '2400x1080', '480'),
                                       ('tablet', '2560x1600', '320')]:
            adb('shell', 'wm', 'size', pixels)
            adb('shell', 'wm', 'density', density)
            for theme in ('Obsidian', 'Porcelain'):
                result = adb('shell', 'am', 'instrument', '-w', '-r',
                             '-e', 'class', 'site.lxwis.codelab.ReleaseVisualEvidenceTest',
                             '-e', 'visualEvidence', 'true', '-e', 'visualSize', name,
                             '-e', 'visualTheme', theme,
                             APP + '.test/androidx.test.runner.AndroidJUnitRunner')
                (OUT / f'{name}-{theme.lower()}-instrumentation.txt').write_text(result)
                results.append({'size': name, 'theme': theme, 'passed': bool(re.search(r'OK \(1 test\)', result))})
                # Keep all screenshots and measurements even when the acceptance assertion fails.
                subprocess.run(['adb', 'pull', '/sdcard/Android/data/' + APP + '/files/release-evidence/.', str(OUT)],
                               check=True, capture_output=True, timeout=60)
        (OUT / 'matrix.json').write_text(json.dumps(results, indent=2))
        assert all(r['passed'] for r in results), 'Layout/viewport acceptance failed; inspect saved images and metrics'
    finally:
        adb('shell', 'wm', 'size', previous_size)
        adb('shell', 'wm', 'density', previous_density)
        adb('shell', 'settings', 'put', 'system', 'font_scale',
            previous_font if previous_font not in ('', 'null') else '1.0')

if __name__ == '__main__':
    main()
