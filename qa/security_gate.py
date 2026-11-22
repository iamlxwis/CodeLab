"""Static regression checks for accidental secrets and unsafe configuration.

This simple gate supplements, not replaces, security review and dependency scanning.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SCANNABLE = {".kt", ".java", ".xml", ".yml", ".yaml", ".md", ".properties"}
PATTERNS = {
    "embedded private key": re.compile(r"-----BEGIN (?:RSA|EC|OPENSSH|DSA) PRIVATE KEY-----"),
    "GitHub token": re.compile(r"\bghp_[A-Za-z0-9]{36,}\b"),
    "fine-grained GitHub token": re.compile(r"\bgithub_pat_[A-Za-z0-9_]{60,}\b"),
    "Google API key": re.compile(r"\bAIza[0-9A-Za-z_-]{35}\b"),
}

def inspect():
    errors = []
    for root in (ROOT / "app/src", ROOT / ".github/workflows", ROOT / "docs"):
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file() or path.suffix not in SCANNABLE or path.stat().st_size > 2_000_000:
                continue
            content = path.read_text(encoding="utf-8")
            for label, pattern in PATTERNS.items():
                if pattern.search(content):
                    errors.append(f"{path.relative_to(ROOT)}: possible {label}")
    source = (ROOT / "app/src/main/java/site/lxwis/codelab/WorkspaceModel.kt").read_text()
    git = (ROOT / "app/src/main/java/site/lxwis/codelab/GitWorkspace.kt").read_text()
    manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text()
    if re.search(r'\.putString\s*\(\s*"credential"', source):
        errors.append("WorkspaceModel persists cloud token without encryption")
    if '.addFilepattern(".")' in git:
        errors.append("GitWorkspace stages the entire repository implicitly")
    if 'android:allowBackup="false"' not in manifest:
        errors.append("Android backup policy unexpectedly relaxed")
    if 'android:isolatedProcess="true"' not in manifest:
        errors.append("Script runtime process isolation unexpectedly removed")
    return errors

if __name__ == "__main__":
    errors = inspect()
    if errors:
        for problem in errors:
            print("SECURITY REGRESSION: " + problem)
        sys.exit(1)
    print("Static security invariant checks passed; no comprehensive audit claimed.")
