# Installing and upgrading CodeLab

Download **CodeLab.apk** from the [latest GitHub release](https://github.com/iamlxwis/CodeLab/releases/tag/latest). **CodeLab-Python.apk** is an optional companion for offline Python, not the editor itself. CodeLab requires Android 8.0 or newer; the Python companion requires a supported 64-bit device.

## Already have CodeLab installed?

A GitHub Actions fallback-debug build may use a different signing certificate from the previous release. Android **will not** install one APK over another package with the same app ID but a different signer. The generic "package appears to be invalid" error can also indicate other causes; only Android's package-manager failure code can distinguish them reliably.

**Back up before uninstalling.** While the old CodeLab still works, open each important project and use its **Export project** option to save the project ZIP to Downloads or another location outside CodeLab's private app storage. Confirm that the ZIP files are present and readable. Reinstalling or clearing CodeLab storage can permanently remove local project data.

If backup succeeds but updating fails, uninstall the previous CodeLab, download **CodeLab.apk** again from the same release page, install it, and import your saved project ZIPs. Install the matching Python module only if you need offline Python; its certificate must match CodeLab. If you cannot export a backup, **do not uninstall**.

## Failure after uninstalling the old app

If you have already uninstalled CodeLab and Android reports `App not installed as package appears to be invalid`, **a signing conflict with the removed app is not the leading explanation**. Do not repeatedly uninstall. Re-download the editor APK in full through another browser or GitHub's official release page, then open the local download with Android Files. Verify its size/hash against the release asset metadata. If it still fails, collect Android version and the precise installer error; the generic toast does not establish a specific cause.

## First installation fails too

- Make sure the downloaded file is exactly **CodeLab.apk** and is not a browser error page, HTML file, partial download or **CodeLab-Python.apk**. Compare its SHA-256 with the release checksum file.
- Confirm Android is version 8.0 or higher and that your device allows installation from your chosen browser/file manager.
- Delete the damaged download, download again to local device storage, and open the file using Android Files.
- Check that CodeLab is not already installed in another Android user profile, work profile or cloned-app space.
- If the installer still rejects it on a clean device, capture the specific `adb install` error code and device Android version; do not assume a signature conflict.

## Signing policy

The editor and Python runtime must be signed with the **same Android certificate**. A permanent stable signing key is required for reliable updates and companion verification. Configure the four GitHub Actions secrets named `CODELAB_KEYSTORE_B64`, `CODELAB_KEYSTORE_PASSWORD`, `CODELAB_KEY_ALIAS`, and `CODELAB_KEY_PASSWORD`. Never commit the keystore or passwords. Switching from previously distributed fallback-debug keys to a stable key requires a **one-time backed-up reinstall** because Android cannot migrate package signing identities automatically.

The latest release includes `CodeLab-build-info.txt` (source SHA, signing mode, certificate fingerprint and upgrade compatibility) and `CodeLab.apk.sha256` (checksums for both APK assets).

## Permanent signing from an Android phone (one-time setup)

Reliable install-over-existing updates require the **same private keystore** on
every release, plus increasing Android version codes. CI now advances version
codes automatically. If you already have a stable production keystore, reuse
it; creating a second key makes existing production installs incompatible.

To create a new key entirely on your phone with Termux (not an online key generator):

```sh
pkg update
pkg install openjdk-17 coreutils
keytool -genkeypair -keystore codelab-release.jks -storetype PKCS12 \
  -alias codelab -keyalg RSA -keysize 4096 -validity 10000
base64 -w 0 codelab-release.jks > codelab-release.b64
```

Choose a long unique password when prompted. Keep at least two encrypted backups
of the **original .jks** (off-device). Losing it permanently prevents signing
future updates with the same identity. Treat the base64 file as sensitive too;
never commit it to GitHub, paste it in a chat, or share it publicly.

In your mobile browser open
**GitHub → CodeLab → Settings → Secrets and variables → Actions → New repository secret**
(use **Desktop site** if navigation is hidden). Add:

- `CODELAB_KEYSTORE_B64`: full contents of `codelab-release.b64`
- `CODELAB_KEYSTORE_PASSWORD`: keystore password
- `CODELAB_KEY_ALIAS`: `codelab` (or your chosen alias)
- `CODELAB_KEY_PASSWORD`: key-entry password (usually equal to the store password for PKCS12)

Then go to **Actions → Android APK → Run workflow** on `main`. Confirm
the build passes and the latest release's `CodeLab-build-info.txt` says
`Signing mode: production`; record the certificate fingerprint. Keep these
four secrets unchanged in future. A previously fallback-debug-signed install
requires **one backed-up reinstall** when switching to the permanent key.
Subsequent APKs signed with that same key and higher version codes can replace
the old one while preserving app data.
