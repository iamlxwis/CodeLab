"""Block public APK publication unless the same commit passed manual Android 15 smoke.

The APK build and full device checks start in separate GitHub Actions workflows.
This script runs on the release job immediately before publishing. An absent,
cancelled, failed or timed-out device run MUST NOT publish a new release.
"""
import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request

WORKFLOW_ID = "device.yml"
POLL_SECONDS = 20
MAX_POLLS = 120  # 40 min, with an independent step timeout.


def classify(payload, sha):
    """Return (state, reason) with state pending, passed or failed."""
    matching = [r for r in payload.get("workflow_runs", [])
                if r.get("head_sha") == sha and r.get("event") == "workflow_dispatch"]
    if not matching:
        return "pending", "Device workflow not registered yet"
    latest = max(matching, key=lambda r: (
        int(r.get("run_number") or 0), int(r.get("run_attempt") or 0)))
    status = latest.get("status")
    conclusion = latest.get("conclusion")
    run_id = latest.get("id", "unknown")
    if status != "completed":
        return "pending", f"Device workflow {run_id}: {status}"
    if conclusion == "success":
        return "passed", f"Device workflow {run_id}: passed all Android jobs"
    return "failed", f"Device workflow {run_id}: {conclusion or 'no conclusion'}"


def main():
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    sha = os.environ.get("GITHUB_SHA", "")
    token = os.environ.get("GH_TOKEN", "")
    if not (repo == "iamlxwis/CodeLab" and len(sha) == 40 and token):
        raise SystemExit("Missing or unexpected repository, SHA or GitHub token")
    query = urllib.parse.urlencode({"event": "workflow_dispatch", "head_sha": sha, "per_page": 20})
    url = f"https://api.github.com/repos/{repo}/actions/workflows/{WORKFLOW_ID}/runs?{query}"
    request = urllib.request.Request(url, headers={
        "Authorization": "Bearer " + token,
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "CodeLab-Release-Device-Gate"})
    for attempt in range(MAX_POLLS):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                payload = json.load(response)
        except (urllib.error.URLError, TimeoutError, ValueError) as error:
            print("Device CI status unavailable; refusing to publish:",
                  type(error).__name__, flush=True)
            raise SystemExit(1)
        state, reason = classify(payload, sha)
        print(reason, flush=True)
        if state == "passed":
            print("PASS: same-source Android 15 manual device workflow succeeded", flush=True)
            return
        if state == "failed":
            raise SystemExit("FAIL: device workflow failed; refusing APK publication")
        if attempt != MAX_POLLS - 1:
            time.sleep(POLL_SECONDS)
    raise SystemExit("FAIL: device workflow timed out; refusing APK publication")


if __name__ == "__main__":
    main()
