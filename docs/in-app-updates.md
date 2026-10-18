# In-app updates

The existing GitHub updater is extended, not replaced. Stable builds use the
repository's `/releases/latest` endpoint. The existing Settings update section
continues to show the current version and offers a manual check. The startup
prompt lives above navigation inside the shared theme, so it does not depend on
which home design is selected. Automatic checks respect the Settings switch.

Selecting Update starts DownloadManager with visible progress and proceeds to
installation. Later dismisses for the current activity session. Android requires
user confirmation. If unknown-source installs are disabled, the startup flow
opens this app's permission settings and continues on return after permission is
granted. A denied permission never triggers repeated settings launches.

Published `<apk-name>.sha256` assets are fetched and validated; an unreadable or
malformed published checksum fails the release check instead of silently skipping
verification. Without a checksum file, GitHub's asset SHA256 digest is used when
available. Releases without either trusted checksum are rejected. The selected checksum is persisted before downloading, so integrity
verification survives process restart and applies to manual Settings installs too.
APK installation stages the FileProvider stream into a PackageInstaller session;
its explicit non-exported result activity opens Android's confirmation screen.
The callback opts into creator-side activity launches on Android 15+ (visible-only
on Android 16+). Denying install permission restores dismissible retry buttons.

Release CI already signs with the required persistent release keystore and
publishes the APK checksum. Do not rotate that signing key. The debug APK uses a
different application ID/signature and is a test artifact, not a replacement for
the installed release. No release is published by this feature branch.

Device validation still required on Android TV: test D-pad focus and Arabic/English,
Update/Later, download interruption, corrupt APK rejection, unknown-app permission
allow/deny, installer cancel/retry, and a successful same-key upgrade preserving
data. Local compilation, JVM tests, and lint do not establish device behavior.
