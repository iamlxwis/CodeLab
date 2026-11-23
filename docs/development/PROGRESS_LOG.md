# CodeLab — task progress audit

Short, append-only development checkpoints. The [README tracker](../../README.md#development-tracker) shows the current summary. **Every meaningful change to a task's status or implementation progress must be committed with an update to both this log and the README in the same commit.** Never mark a task complete without verifiable test/build evidence.

### Initial baseline — 2026-11-23

| Task | State | Evidence / remaining issue |
| --- | --- | --- |
| `T01` Done | Production-signed release confirmed; upgrade test is separate · [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919) |
| `T02` In progress | Android 15 online-run smoke failed; Android 13 passed · [CI](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470) |
| `T03` Queued | Workstream pending end-to-end verification |
| `T04` Queued | Workstream pending end-to-end verification |
| `T05` Queued | Workstream pending end-to-end verification |
| `T06` Queued | Workstream pending end-to-end verification |
| `T07` Queued | Workstream pending end-to-end verification |
| `T08` Queued | Workstream pending end-to-end verification |
| `T09` Queued | Workstream pending end-to-end verification |

### Subsequent updates (append entries; never overwrite historical events)

| Date (UTC) | Task | New state | What changed / proof |
| --- | --- | --- | --- |
| 2026-11-23 | T01–T09 | Baseline recorded | Initial tracker created; earlier milestones summarized rather than inventing historical event logs |

#### Required update format

With each meaningful task start, commit checkpoint, failure, retry, or verified completion, append a row with the date, task ID, correct state, actual change and evidence link. Update README task row and counts **in the same commit**, plus `PROJECT_MEMORY.md`. In-progress checkpoints remain In progress until done. If a test fails, preserve Failed/Blocked evidence rather than declaring completion. Keep entries short and factual.

| 2026-11-23 | T02 | In progress | Confirmed API35 failure in [device CI 45191732470](https://github.com/iamlxwis/CodeLab/actions/runs/45191732470): XML shows `Online: source upload needs confirmation`, a valid state. Smoke now accepts all three legitimate status messages; API33/35 rerun pending. 1/9 done, 8 remaining, 1 active, 0 blocked. |

| 2026-11-23 | T04 | In progress | Added pre-publish API33/API35 same-signer production `adb install -r` acceptance: project/theme retention, Python module trust and real execution. CI pending; physical handset unverified. 1/9 done, 8 remaining, 2 active, 0 blocked. |

| 2026-11-23 | T01 | Reopened / In progress | Examined APK v2 certificates from [a11](https://github.com/iamlxwis/CodeLab/actions/runs/45194290919), [7acd](https://github.com/iamlxwis/CodeLab/actions/runs/45200242402), [ee24](https://github.com/iamlxwis/CodeLab/actions/runs/45201489255): distinct Android Debug signers despite metadata `production`. Corrected missing Gradle signing env and added true keystore-to-APK signer check; CI pending; key untouched. Counts: 0/9 done, 9 remain, 3 active, 0 blocked. |

| 2026-11-23 | T01 | In progress, CI failure fixed in source | [APK workflow 45204023605](https://github.com/iamlxwis/CodeLab/actions/runs/45204023605) failed at `:app:packageRelease`: `SigningConfig "production" is missing required property "storePassword"`. Root cause: Kotlin DSL `storePassword = keyPassword`, `keyAlias = keyAlias` resolved RHS to uninitialized `SigningConfig` receiver properties, not outer local values. Renamed outer values to `releaseStorePassword`, `releaseAlias`, `releaseEntryPassword`, with static guard; retest pending. Counts unchanged: 0/9 done, 9 remain, 3 active, 0 blocked. |

| 2026-11-23 | T01 | Done | [APK workflow 45205309835](https://github.com/iamlxwis/CodeLab/actions/runs/45205309835) passed build/JVM/lint/signature verification AND actual editor/Python signer comparison against permanent keystore at source 7e04d47. Independently extracted editor certificate SHA-256 `c81d2aa187febd5252ad0369f2b4cfba368b5f16f6b4b049963fa284c9e42392`. Published latest; old debug APKs still need backed-up migration. T04 upgrade skipped for signer mismatch, not passed. |
| 2026-11-23 | T03 | In progress | Found stale online-run confirmation could be accepted after editing, switching mode or target. Bound approval to exact file, revision, mode, endpoint and compiler language ID; consumed once, canceled on network loss/mode switch. Added JVM regression; source CI pending. Counts 1/9 done, 8 remain, 3 active (T02/T03/T04). |
