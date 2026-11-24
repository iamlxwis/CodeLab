"""Exercise the publication guard's actual no-key branch without any credentials."""
import subprocess
import unittest
from pathlib import Path

class ProductionSigningGateTest(unittest.TestCase):
    def test_missing_production_key_aborts_publication(self):
        text = (Path(__file__).resolve().parents[1] / '.github/workflows/android.yml').read_text()
        step = text.split('- name: Enforce actual production signing certificate', 1)[1].split('      - name:', 1)[0]
        script = step.split('        run: |\n', 1)[1]
        script = '\n'.join(line[10:] for line in script.splitlines())
        result = subprocess.run(['bash', '-c', script], env={'CODELAB_SIGNING_MODE': 'fallback-debug'},
                                capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('debug-signed artifacts cannot be published', result.stdout)

    def test_upgrade_gate_runs_before_publication(self):
        text = (Path(__file__).resolve().parents[1] / '.github/workflows/android.yml').read_text()
        self.assertLess(text.index('script: python3 qa/production_upgrade_smoke.py'),
                        text.index('- name: Publish latest APK'))
        self.assertIn('test -s qa-results/production-upgrade-api-35.txt', text)

if __name__ == '__main__':
    unittest.main()
