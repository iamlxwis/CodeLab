"""Fail closed unless the reviewed release acceptance ledger is complete.

Functional/device/signature/upgrade gates remain independent prerequisites.
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
# W09 records publication after it happens; requiring it before upload is circular.
GROUPS = {'W': 8, 'D': 37, 'U': 10}

def problems(text):
    rows = {}
    failures = []
    for line in text.splitlines():
        cells = [cell.strip() for cell in line.strip().strip('|').split('|')]
        if not cells or not re.fullmatch(r'[WDU]\d{2}', cells[0]):
            continue
        key = cells[0]
        if key in rows:
            failures.append(f'{key}: duplicate acceptance entry')
        rows[key] = cells
    for group, count in GROUPS.items():
        for number in range(1, count + 1):
            key = f'{group}{number:02}'
            cells = rows.get(key, [])
            if len(cells) != 5 or cells[2] != 'Accepted' or not cells[4].startswith('Verified: '):
                failures.append(f'{key}: acceptance or verified evidence missing')
    return failures

def main():
    failures = problems((ROOT / 'docs/development/RELEASE_ACCEPTANCE.md').read_text())
    if failures:
        raise SystemExit('Stable publication blocked:\n' + '\n'.join(failures))
    print('All 8 prepublication stabilization, 37 redesign and 10 UI entries have recorded acceptance evidence.')

if __name__ == '__main__':
    main()
