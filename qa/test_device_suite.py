import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from device_suite import run_stages, successful

class DeviceSuiteTest(unittest.TestCase):
    def test_later_success_cannot_erase_failure_and_other_checks_still_run(self):
        calls = []
        def execute(command, **kwargs):
            calls.append(command)
            return SimpleNamespace(returncode=1 if command == ['bad'] else 0)
        with tempfile.TemporaryDirectory() as folder:
            results = run_stages([('first', ['bad']), ('next', ['good'])], Path(folder), execute)
            self.assertEqual([['bad'], ['good']], calls)
            self.assertFalse(successful(results))
            self.assertIn('"status": "failed"', (Path(folder) / 'suite-results.json').read_text())

    def test_unavailable_stage_fails_closed(self):
        def execute(command, **kwargs):
            raise FileNotFoundError()
        with tempfile.TemporaryDirectory() as folder:
            self.assertFalse(successful(run_stages([('missing', ['missing'])], Path(folder), execute)))
        self.assertFalse(successful([]))
