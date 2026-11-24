"""Static workflow safety checks: app, packs and reports remain three artifacts."""
import re
import unittest
from pathlib import Path

WORKFLOW = Path(__file__).resolve().parents[1] / ".github/workflows/android.yml"
PACKS = ("Python", "JavaScript", "Lua", "Java", "TypeScript", "SQLite")

class ArtifactLayoutTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = WORKFLOW.read_text(encoding="utf-8")

    def test_exactly_three_download_artifacts(self):
        names = re.findall(r"^\s*name: (CodeLab-[A-Za-z-]+)$", self.source, re.M)
        self.assertEqual({"CodeLab-APK", "CodeLab-Offline-Packs", "CodeLab-CI-Reports"}, set(names))
        self.assertEqual(3, len(names))
        self.assertEqual(3, self.source.count("uses: actions/upload-artifact@v4"))

    def test_main_apk_and_six_named_pack_apks(self):
        self.assertIn("artifact-out/app/CodeLab.apk", self.source)
        for pack in PACKS:
            self.assertIn("artifact-out/packs/CodeLab-" + pack + ".apk", self.source)
            self.assertIn("release/CodeLab-" + pack + ".apk", self.source)
        self.assertIn("SHA256SUMS.txt", self.source)

    def test_release_signing_and_same_commit_device_gate_retained(self):
        self.assertIn("qa/require_device_smoke.py", self.source)
        self.assertIn("Enforce actual production signing certificate", self.source)
        self.assertIn("Audit upgrade signing compatibility", self.source)
        self.assertIn("github.event_name == 'workflow_dispatch'", self.source)
        self.assertIn("gh release upload latest", self.source)
        self.assertIn("gh release create latest", self.source)
        self.assertIn("release/CodeLab.apk", self.source)

if __name__ == "__main__":
    unittest.main()
