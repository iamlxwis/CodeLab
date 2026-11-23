# Approved product and engineering decisions

Updated 2026-11-23. These are user requirements, not evidence that code already implements them.

| ID | Decision | Status in source |
| --- | --- | --- |
| D-001 | CodeLab is a native Android code editor with a light core APK and optional language execution components | Target; core still bundles runtimes |
| D-002 | Users can intentionally choose **Run Online** or **Run Offline**. No automatic upload/source transfer without explicit user consent | Partial cloud confirmation exists; full choice flow pending |
| D-003 | Offline execution requires download/install of the chosen supported language runtime. **Python is optional**, just like JS and other modules | Not implemented; Python currently bundled via Chaquopy |
| D-004 | Provide offline editing and starter code for every catalog entry, with Beginner Example, Hello World, Blank choices. A file type need not have an executable engine | Implemented in source |
| D-005 | The user installs/uninstalls/updates runtimes individually; status and availability reflect actual installation, not planned support | Planned |
| D-006 | Online mode can rely on configurable cloud compiler/provider; disclose source transfer, internet requirement and unavailability. No silent fallback from offline to cloud | Partially implemented; requires final UX and tests |
| D-007 | No emojis in the APK; use a coherent, professional vector icon system (Material Symbols or Lucide as appropriate) | New rule for all future UI/code work; audit existing UI |
| D-008 | Offer five well-executed visual styles: Minimal, Glass, Clay, AMOLED and Paper; prioritize editor legibility, simple navigation, accessibility and performance | Implemented foundations, not complete visual QA |
| D-009 | Commit verified incremental work **directly to `main`**; avoid PR review unless the user changes this instruction | Approved |
| D-010 | Maintain a GitHub-based **Second Brain for development**, not an end-user AI assistant: AGENTS.md + status, roadmap, decisions and known issues. Every meaningful implementation commit includes updated memory/next steps | Established by this documentation work |
| D-011 | Protect user projects and source: isolated execution, bounded resources, signed trustworthy module distribution, explicit network permission, no arbitrary downloaded native executable loading | Engineering requirement; full module architecture pending |
| D-012 | Each new user thought or development requirement is assigned a persistent ID and recorded immediately. Pending and in-progress ideas remain in the repository until verified complete; completed and superseded ideas retain history and supporting evidence rather than being silently removed | Idea ledger established; ongoing discipline required |
| D-014 | First-ever app launch shows a concise, skippable tutorial explaining project creation, Run/online/offline modules and settings, with ability to replay later | Implemented in source, awaiting device acceptance |
| D-015 | Five themes must use materially distinct **real UI treatments** instead of only preview colors. Prioritize actual Glass background blur/transparency, Clay depth, true-black AMOLED, Paper light surfaces, professional Minimal styling | Implemented in source, awaiting visual/device acceptance |
| D-013 | In-app language module download with progress, install/status, update and removal. Users remain in CodeLab for the full experience; external browser/GitHub download links are NOT the finished UX. On GitHub APK distribution, Android-required user confirmation for executable companion packages must be respected; evaluate Play on-demand delivery separately | Approved, not implemented; supersedes previous browser-link UX |

## Implementation notes

- Android-companion runtime APKs are a candidate for GitHub release distribution, provided authentication/signing, IPC isolation and package compatibility are proven. Play dynamic feature delivery is an alternative for that separate distribution channel. **No runtime marketplace has been validated yet.**
- Built-in SQLite/WebView are Android platform capabilities, not equivalent to a separately downloadable SQL or browser engine. Interpret D-003 as *optional language execution capability*: decide how to expose platform-native functionality without inaccurately claiming that engine binaries were downloaded.
- Existing bundled engine source should be migrated only after a working opt-in replacement exists, with a supported upgrade path for old projects.
- If a user changes an architectural decision, revise this file first (or in the same commit) and explicitly note migration impact.

## 2026-11-23 — stable update policy

D-023: A dependable install-over-existing update requires the **same Android signing certificate** and a nondecreasing version code (our release automation increments it); both editor and Python companion must use the same key. Refuse silent fallback to debug publishing after production is established. Signing keys are kept only in owner-managed GitHub Actions secrets and encrypted offline backups; never in source or chat. Initial fallback-to-production migration needs data backup and one-time reinstall.

## 2026-11-23 — GitHub Actions-only signing provisioning

D-024: User explicitly chose GitHub Actions instead of requiring Termux or a PC. Generate a permanent key on a temporary runner only on manual dispatch. Protect the key via an owner-supplied 20+ character `CODELAB_KEYGEN_PASSPHRASE` GitHub **repository secret**, encrypt authenticated key backup before public Actions artifact upload, and supply a local browser-based decryptor. No cleartext keystore or Base64 key is committed, logged, attached or sent in chat. Users own and secure original keystore backups. The four existing production secrets still must be populated by owner.
