"""Pure release-gate checks. No network or GitHub secrets needed."""
import unittest
from require_device_smoke import classify

SHA = "a" * 40
OTHER = "b" * 40


def run(sha=SHA, event="workflow_dispatch", status="completed", conclusion="success",
        num=100, attempt=1):
    return {"head_sha": sha, "event": event, "status": status,
            "conclusion": conclusion, "run_number": num,
            "run_attempt": attempt, "id": num}


class ReleaseGateTest(unittest.TestCase):
    def test_only_exact_commit_authorizes_release(self):
        self.assertEqual("pending", classify({"workflow_runs": [run(OTHER)]}, SHA)[0])
        self.assertEqual("pending", classify(
            {"workflow_runs": [run(event="push")]}, SHA)[0])

    def test_manual_api35_run_only(self):
        self.assertEqual("pending", classify({"workflow_runs": [run(event="push")]}, SHA)[0])
        self.assertEqual("passed", classify({"workflow_runs": [run(event="workflow_dispatch")]}, SHA)[0])

    def test_success_requires_completed_workflow(self):
        self.assertEqual("pending", classify(
            {"workflow_runs": [run(status="in_progress", conclusion=None)]}, SHA)[0])
        self.assertEqual("passed", classify({"workflow_runs": [run()]}, SHA)[0])

    def test_fail_cancel_timeout_and_skip_block_publication(self):
        for value in ("failure", "cancelled", "timed_out", "skipped", None):
            self.assertEqual("failed", classify(
                {"workflow_runs": [run(conclusion=value)]}, SHA)[0])

    def test_latest_retry_and_latest_run_win(self):
        runs = [run(conclusion="failure", num=12, attempt=1),
                run(num=12, attempt=2)]
        self.assertEqual("passed", classify({"workflow_runs": runs}, SHA)[0])
        self.assertEqual("failed", classify({"workflow_runs":
            list(reversed(runs)) + [run(num=13, conclusion="failure")]}, SHA)[0])


if __name__ == "__main__":
    unittest.main()
