# Release stabilization candidate

Version **0.5.1-alpha** remains a candidate. Stable publication is blocked until the [acceptance ledger](development/RELEASE_ACCEPTANCE.md) and independent signing, upgrade and device gates pass.

## Changes

- Reclaim workspace space during console and preview keyboard input, keeping standard input and Send reachable.
- Reveal newly selected tabs fully and constrain long file names so close controls fit the tab viewport.
- Give runtime input a persistent accessible label.
- Reject insecure TLS settings, alternate push destinations and rewritten Git destinations before credentials can be transmitted.
- Make lint errors block CI and refuse fallback debug signing for public releases.
- Restore Android 15 production upgrade verification and retain source-matched device checks.
- Capture actual activity screenshots in two themes at three sizes, with measured keyboard viewport evidence and toolbar/status regressions.

These source changes have separate build and device evidence in the development tracker. A source change alone is not acceptance.

## Known limitations

- Complete screen-by-screen, gesture, TalkBack, physical-device and sustained-performance acceptance is outstanding.
- The OS starting splash uses the static default; saved light-theme launch behavior still needs resolution and verification.
- Optional runtime network installation/removal journeys and live online execution remain separate from bundled/offline smoke checks.
- Java uses an interpreter beta; TypeScript is single-file transpilation. See [installation and runtime scope](INSTALLATION.md).
- The public latest release may precede this candidate. No stable update is claimed until the guarded publication succeeds.
