# Alaa Control Center — Android TV Setup

## What is included

The Android TV client has a secure device-link route available from the first-run screen. It generates a P-256 key in Android Keystore, sends a public key and a keyed server-side device-identity digest for enrollment, displays a short-lived pairing code, and retrieves only the configuration assigned to that TV ID after approval.

The Android TV app stores its TV ID and device session tokens using encrypted preferences. The private signing key remains in Android Keystore. Deleting and reinstalling the app can retain the Android ID on supported Android 8.0+ devices with the same package and signing key, but the Keystore key is re-created. The dashboard therefore requires a new administrator approval rather than silently treating an reinstall as authenticated.

## Release configuration

The dashboard must be published before a release APK can contact it. When manually running the **Build and publish Alaa Player 2** workflow, set the optional `device_control_api_base_url` input to the public HTTPS URL of the published control-center dashboard. Do not add a path, credential, password, access token, or trailing slash.

For a local debug build, set the non-versioned property below in `local.properties`:

```properties
device.control.url=https://your-control-center.example.com
```

The default release value is deliberately empty. It prevents a development preview URL from being embedded in a public APK.

## Pairing flow

1. The user chooses **Link this TV to Alaa Control Center**.
2. The app displays a short-lived `ALAA-…` pairing code.
3. An administrator enters the code in the dashboard, names the device, and assigns an expiry date.
4. The app checks approval, exchanges the code for encrypted session tokens, and syncs only the configured sources for that TV.
5. Later syncs update existing sources through the same provider setup use cases already used by the app; no alternative player, database, or playback stack is introduced.

## Provider compatibility

The control-center API supports an encrypted generic provider envelope. The current Android synchronizer applies `xtream`, `m3u`, `stalker`, and `jellyfin` provider kinds by delegating to the app's existing `ValidateAndAddProvider` use case. Unsupported kinds remain blocked and report a non-sensitive failure to the TV UI.
