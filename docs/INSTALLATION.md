# Install CodeLab

CodeLab supports Android 8.0 and later (API 26+). The Python pack requires a 64-bit ARM or x86 device.

Download [CodeLab.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk), open it in Android Files, and approve installation. Android may ask to allow installation from the browser or file manager used for the download.

The public download is the last published alpha. The Reimagined stabilization candidate is still undergoing acceptance; see the [current release status](development/RELEASE_ACCEPTANCE.md). Do not assume the public APK contains unreleased changes.

## Optional offline packs

In CodeLab, open **Languages**, choose a runtime, and download its pack. Installation and removal require Android confirmation. The editor verifies package identity, signing certificate and version before offering installation. Install CodeLab and its packs from the official release with the same signing identity.

| Pack | Download | Scope |
| --- | --- | --- |
| JavaScript | [CodeLab-JavaScript.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-JavaScript.apk) | JavaScript interpreter; no Node.js runtime |
| Python | [CodeLab-Python.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-Python.apk) | CPython; 64-bit devices |
| Lua | [CodeLab-Lua.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-Lua.apk) | Lua interpreter |
| Java | [CodeLab-Java.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-Java.apk) | BeanShell interpreter beta; no javac toolchain |
| TypeScript | [CodeLab-TypeScript.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-TypeScript.apk) | Single-file transpilation; no Node.js or multi-file imports |
| SQLite | [CodeLab-SQLite.apk](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-SQLite.apk) | Android SQLite |

Download and install packs while connected, then choose **Offline** to run without internet. HTML/CSS/JavaScript preview is local. Recognized file types and starter examples do not imply an available offline compiler. Legacy bundled JavaScript, Lua, Java beta and SQLite fallbacks remain during modular-runtime migration.

**Online** execution requires internet, an available compatible provider, and explicit approval before source is sent. Cancelling stops local monitoring; a submitted remote job may continue on the provider.

## Update safely

Export important projects as ZIP files to storage outside CodeLab before updating. Install the new APK over the existing app; Android requires the same signing certificate and a compatible version code. Do not uninstall or clear app data to work around an update failure until readable backups are confirmed.

The permanent production certificate SHA-256 is `c81d2aa187febd5252ad0369f2b4cfba368b5f16f6b4b049963fa284c9e42392`. Older APKs using rotating Android Debug certificates need a one-time backed-up reinstall to move to this identity. The production key must remain unchanged.

[Release details](https://github.com/iamlxwis/CodeLab/releases/latest) include build information and [SHA-256 checksums](https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab.apk.sha256) for the editor and six packs. Check the actual APK certificate when investigating signing problems; historical metadata alone was unreliable.

If installation fails, download again, check Android version and free space, and check for an existing installation in a work profile or cloned-app space. Keep the exact Android installer error for diagnosis. Signing administration is documented separately in [GitHub Actions signing setup](SIGNING_GITHUB_ANDROID.md); existing production credentials must be reused.
