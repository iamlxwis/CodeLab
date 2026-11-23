# CodeLab signing from an Android phone — no Termux or PC

This method requires **GitHub Actions and Chrome only**. It generates a new,
permanent Android signing identity on GitHub's temporary build runner, then
uploads an **encrypted-only backup**. No private keystore is ever committed or
uploaded in cleartext. The encrypted artifact is still visible in a public
repository, so a long, unique passphrase is essential.

> **Do this only once for a new signing identity.** If CodeLab has previously
> shipped with a production key, **reuse that original keystore**, not this
> generator. Creating another signing key makes in-place Android upgrades fail.

## 1. Create one secret on GitHub

Open [CodeLab Actions Secrets](https://github.com/iamlxwis/CodeLab/settings/secrets/actions)
in your Android browser. Under **Repository secrets**, press **New repository secret**.
Enter:

- **Name:** `CODELAB_KEYGEN_PASSPHRASE`
- **Secret:** a **new, unique password of at least 20 characters** that you
  privately save in your password manager. Use letters, numbers and symbols.

Tap **Add secret**. Never give the password to ChatGPT or put it in a workflow
input, issue, README, commit or screenshot. It is the keystore password and
the encryption password for the backup.

## 2. Generate the encrypted backup in GitHub Actions

Open [CodeLab Actions](https://github.com/iamlxwis/CodeLab/actions),
select **Create CodeLab signing key (one time)** from the workflow list.
Tap **Run workflow**, choose `main`, then **Run workflow**. Open the new run
and confirm **Generate an encrypted, recoverable Android keystore** succeeded.

Under the run's **Artifacts**, tap **CodeLab-encrypted-signing-backup** to
download a ZIP. This artifact expires after 7 days: keep your own private
backup of the resulting .jks file.

## 3. Unlock locally on Android

Use Android Files / your phone's file manager to extract the ZIP. It contains:

- `codelab-release.jks.enc.json`: encrypted keystore (safe to keep as encrypted backup)
- `unlock.html`: the standalone browser key unlocker
- `START-HERE.txt`: short directions

Open the extracted **unlock.html** in **Chrome** (as a local file). The page
contains no remote JavaScript, images or external dependencies. Select
`codelab-release.jks.enc.json` with the file chooser. Enter the exact
passphrase you saved in Step 1 and tap **Unlock locally**.

Once it says **Unlocked successfully**:

1. Tap **Save keystore (.jks)** and make at least two private, encrypted
   backups in trusted off-device storage. Do not depend on the GitHub artifact.
2. Tap **Copy Base64** (or **Save Base64 text** if Android blocks clipboard).
   This is the complete content needed for the first GitHub production secret.

If Chrome will not open a local HTML page, try **Open with → Chrome** from
the extracted file in your file manager. You can also use this same static
unlocker through a GitHub Pages deployment you control, but do not upload the
encrypted bundle and passphrase to third-party decryptor websites.

## 4. Add the four release signing secrets

Back at [CodeLab Actions Secrets](https://github.com/iamlxwis/CodeLab/settings/secrets/actions),
tap **New repository secret** four times, filling one entry per name:

| **Name** | **Secret field** |
| --- | --- |
| `CODELAB_KEYSTORE_B64` | Entire Base64 value shown by the unlocker |
| `CODELAB_KEYSTORE_PASSWORD` | Same long passphrase from Step 1 |
| `CODELAB_KEY_ALIAS` | `codelab` |
| `CODELAB_KEY_PASSWORD` | Same long passphrase from Step 1 |

Use **Repository secrets**, not **Variables** or **Environment secrets**.
You should now see **five** secret names (including the generator passphrase).
GitHub hides saved secret values; that's expected.

Delete any plaintext Base64 text file from Downloads and clear the clipboard.
Keep the original `.jks` in two secure backup locations.

## 5. Publish the first production-signed APK

Open **Actions → Android APK → Run workflow → main → Run workflow**.
Wait for the run to finish successfully. On
[CodeLab's latest release](https://github.com/iamlxwis/CodeLab/releases/latest),
open `CodeLab-build-info.txt`. It must report
`Signing mode: production` and a positive `Version code`.
The editor and Python companion are signed with the same keystore.

**Key change:** APKs signed with the old CI fallback-debug key normally require
**one final backed-up uninstall/reinstall** to move to this permanent signing
identity. Export projects before uninstalling; do not uninstall if backups
cannot be recovered. Once using the permanent key, future builds with a
higher version code can update in place and preserve data.

## 6. Future updates

Keep `CODELAB_KEYSTORE_B64`, `CODELAB_KEYSTORE_PASSWORD`,
`CODELAB_KEY_ALIAS`, and `CODELAB_KEY_PASSWORD` unchanged. Normal pushes
to `main` rebuild the APK with the same key and increasing version codes.

**Do not run the key generator again.** Losing the key or changing the key
causes Android to reject in-place updates. Your encrypted backup and original
`.jks` are the recovery method if GitHub secrets need restoring.

## Verification scope

The workflow uses Java `keytool` to create a 4096-bit RSA key in PKCS12
format. Node crypto encrypts the backup with authenticated AES-256-GCM and
600,000 PBKDF2-HMAC-SHA256 iterations, and validates recovery using WebCrypto.
Only encrypted JSON, the local unlocker and text instructions are uploaded.
Android update acceptance still needs a real physical-device upgrade test.
