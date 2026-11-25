# CodeLab — contributor and agent guidance

Read [current development records](docs/development/README.md), [Reimagined 2.1](docs/development/REIMAGINED_2_1.md), [decisions](docs/development/DECISIONS.md) and inspect current source and CI before editing. Historical records may be stale.

- Keep the public README user-facing. Do not restore a development tracker, session status or AI handoff there.
- Record meaningful changes, evidence, outstanding issues and handoffs in the existing development records. Preserve approved requirements and history; distinguish verified behavior from plans.
- Use a PR for application changes unless the owner explicitly authorizes direct commits. Keep changes scoped and preserve project data.
- Maintain native Kotlin/Compose, neutral Light/Dark appearance (System follows Android), professional icons and accessible, responsive controls.
- Choose Offline or Online at Run/Rerun; require explicit revision-bound consent before remote source uploads. Preserve runtime verification and isolation. Do not conflate editing support with executable languages.
- Preserve the production signing identity and release gates. Never regenerate the key or expose secrets.
- Verify actual builds, tests and device evidence before claiming completion. Keep physical-device and emulator results distinct.
- Keep documentation-only changes separate from source changes where practical.

[Historical handoffs](docs/development/AGENT_HANDOFF_ARCHIVE.md) are archived, not active instructions.
