# Backup and Restore Review

Reviewed: 2026-08-14

This is the living review of the normal file-backup flow and the Google Drive flow. A status is marked resolved only when the implementation and focused regression coverage support it.

## Current status

- **Resolved:** current `Replace Existing` restore replaces affected provider/type scopes, rebuilds group memberships, and previews conflicts using provider and stable content identity.
- **Resolved:** pending Drive credentials are isolated to the matching restore and cleared on unrelated imports, dismissals, sign-out, pull failures, and completed restores.
- **Resolved:** normal exports, Drive downloads, and external import staging enforce bounded I/O with cleanup of rejected partial files.
- **Resolved:** Drive lookup, upload, and download failures classify authorization versus network errors and persist the result in `syncStatus`.
- **Resolved:** Drive sign-out attempts access revocation before clearing the local session and has direct unit coverage.
- **Resolved:** Drive transport operations are serialized to prevent concurrent lookup-then-upload races and duplicate backup files.
- **Resolved:** new Drive bundles preserve the exact exported backup JSON for checksum verification; pulls accept the new v2 bundle, existing v1 bundles, and legacy standalone files.
- **Resolved:** Google Drive retains a bounded history of timestamped v2 bundles, lists snapshots newest-first, and lets the user choose which snapshot enters the existing preview/import flow. The previous fixed-name v2 bundle and legacy standalone files remain readable.
- **Resolved:** Android TV Export Data and Restore Data no longer launch a system document picker; they use the app-managed local path directly. Export publishes to `Downloads/StreamVault` when available, and common TV file-manager MIME types are accepted when opening backups.
- **Resolved:** local backup management lists only files StreamVault created or successfully registered after a SAF export, supports app-managed Downloads, app-private, registered SAF, and Fire TV USB locations, and requires confirmation before deletion. Arbitrary JSON files selected from elsewhere are intentionally left to the user's file manager.
- **Resolved:** Google Drive backup management lists the available snapshots, requires confirmation, validates the selected Drive file before deleting it, refreshes the list afterward, and removes the legacy standalone credentials companion when deleting a legacy standalone backup.
- **Resolved:** release-built backup DTOs now have stable wire field names; imports also accept the obfuscated `a`/`b`/`c` aliases produced by earlier releases, and SHA-256 verification uses the exact exported bytes to avoid false checksum failures.
- **Resolved:** completed restore checkpoints are reusable. A new explicit import reopens the checkpoint and reapplies the selected Keep Existing or Replace Existing behavior, so deleting a provider and importing the same local or Drive backup works again.
- **Still open:** legacy backups containing only local Room IDs cannot be made fully portable because the missing stable metadata was never exported.
- **Partially addressed:** legacy standalone Drive files remain a compatibility fallback whose backup/credentials generation pairing cannot be proven; new v2 bundles are paired.
- **Open:** real Google Play Services integration testing, preference-scope decisions, atomic normal SAF export semantics, and the cleartext-credentials threat-model decision.

## Supported flows

### Normal backup

The normal export contains provider snapshots, selected preferences, favorites/groups, playback history, split-screen presets, protected categories, scheduled recordings, EPG sources, and portable provider-scoped references. It is checksummed and bounded before import. On television devices, Export Data writes through the app-managed local path without invoking a document picker; it uses the public `Downloads/StreamVault` folder when available, and Restore lists visible local JSON backups inside StreamVault. The release DTO wire names are pinned so R8 obfuscation cannot change the backup schema.

Restore uses the existing inspect/preview/import flow with selectable sections and conflict strategy. New local backups include a matching provider-credentials snapshot so a restored provider can sync immediately. Older backups without that field still restore providers without passwords and require the user to re-enter them.

### Google Drive backup

New pushes create timestamped v2 bundles such as `streamvault_backup_bundle_20260814_120000_123_a1b2c3d4.json`. Each contains the exact backup JSON text and matching provider-credentials snapshot, and uploads it to the private Drive `appDataFolder`. The app keeps the ten newest timestamped bundles, lists them newest-first, and lets the user select one before the existing preview/import flow. The original fixed-name v2 bundle, existing v1 bundles, and legacy standalone backup files remain readable for migration.

Google Drive is optional; local backup does not require a Google account or Google Drive.

### Backup management and deletion

Settings provides separate management actions for local and Google Drive backups.
Local management shows only app-owned exports: files in the app-managed
`Downloads/StreamVault` folder, the app-private backup directory, registered
SAF export destinations, and the supported Fire TV USB backup directory. Each
delete requires confirmation. Files that the user merely picked for import, or
files created outside StreamVault, are not offered for deletion.

Drive management lists the same snapshots available to restore. Deletion is
performed by the Drive file ID after the manager revalidates that the ID is
still one of StreamVault's backup files. Deleting a legacy standalone backup
also removes its legacy `streamvault_credentials.json` companion when present;
new v2 bundles already contain their matching credentials in the same artifact.

## Remaining limitations

- The backup is currently a selected-settings backup, not a complete export of every preference.
- Legacy ID-only favorites/history remain compatibility-limited across devices with different catalog IDs.
- Legacy Drive sibling files may represent different generations.
- Real Google Play Services sign-in/revocation still needs device/account validation.
- New local and Drive backup artifacts contain cleartext provider credentials so they can restore and sync across devices. This requires an explicit threat-model decision before release.
- Normal SAF destinations still write directly rather than using a provider-specific atomic rename strategy.

## Verification performed

Focused suites passed:

```text
./gradlew.bat :data:testDebugUnitTest --tests com.streamvault.data.manager.BackupManagerImplTest --tests com.streamvault.data.manager.GoogleDriveBackupSyncManagerTest --no-daemon
./gradlew.bat :app:testDebugUnitTest --tests com.streamvault.app.ui.screens.settings.SettingsBackupActionsTest --tests com.streamvault.app.ui.screens.settings.SettingsDriveBackupActionsTest --tests com.streamvault.app.ui.screens.provider.ProviderSetupViewModelTest --tests com.streamvault.app.backup.BackupFileBridgeTest --no-daemon
```

The Drive tests cover v2 exact-text preservation, v1 bundle compatibility, malformed and oversized payloads, snapshot listing and explicit selection, upload/download failures, revoke-before-clear behavior, and concurrent pushes. The app tests cover settings state transitions, multi-snapshot selection, provider onboarding, bounded external-import staging, and compatibility with the pre-fix obfuscated backup fields. On the connected Android TV emulator, the corrected release APK opened the existing 2,309-byte backup in the import preview without a checksum error.

## Real-device test checklist

1. Export locally on Android TV with and without a system document picker; confirm the file appears under `Downloads/StreamVault` when the picker is absent.
2. Open that JSON from a TV file manager with StreamVault and restore it after deleting the provider.
3. Push to Google Drive, delete the provider, pull the Drive backup, and restore it.
4. Repeat on a fresh install/different catalog and verify favorites/history behavior.
5. Validate Google sign-in, sign-out, revoke, network failure, and retry behavior with a real Google account.
