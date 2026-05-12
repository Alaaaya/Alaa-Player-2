# StreamVault Plugin API

StreamVault plugins are companion Android APKs. They do not inject code into the
main application. Instead, StreamVault discovers installed APKs that expose a
bound service with the action `com.streamvault.plugin.API` and talks to them
through Android `Messenger` IPC.

This keeps the plugin boundary installable, removable, and compatible with
Android package isolation while still allowing plugins to add provider, playback,
and Cast capabilities.

## Package Discovery

The host queries services for:

```xml
<action android:name="com.streamvault.plugin.API" />
```

The host app must declare the same action in `<queries>`. Plugin APKs should
declare an exported service:

```xml
<service
    android:name=".MyPluginService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.streamvault.plugin.API" />
    </intent-filter>
</service>
```

Plugins can be installed from:

- A direct HTTP/HTTPS APK URL.
- A local APK selected with the system file picker.
- Any manual Android package install. StreamVault detects it after refreshing the
  Plugins screen.

## Manifest

StreamVault first asks the plugin service for its manifest. As a fallback, it
reads `com.streamvault.plugin.MANIFEST_JSON` service metadata, then individual
metadata fields if the JSON is missing or invalid.

```json
{
  "schemaVersion": 1,
  "id": "com.example.plugin",
  "name": "Example Plugin",
  "versionName": "1.0.0",
  "versionCode": 1,
  "description": "Adds external capabilities to StreamVault.",
  "providerName": "Example Provider",
  "configurationActivityAction": "com.example.plugin.CONFIGURE",
  "capabilities": [
    "provider.m3u",
    "playback.prepare",
    "cast.rewriteUrl",
    "configuration.activity"
  ]
}
```

Recommended fallback metadata:

```xml
<meta-data android:name="com.streamvault.plugin.ID" android:value="com.example.plugin" />
<meta-data android:name="com.streamvault.plugin.NAME" android:value="Example Plugin" />
<meta-data android:name="com.streamvault.plugin.VERSION_NAME" android:value="1.0.0" />
<meta-data android:name="com.streamvault.plugin.VERSION_CODE" android:value="1" />
<meta-data android:name="com.streamvault.plugin.DESCRIPTION" android:value="Adds external capabilities to StreamVault." />
<meta-data android:name="com.streamvault.plugin.PROVIDER_NAME" android:value="Example Provider" />
<meta-data android:name="com.streamvault.plugin.CONFIGURATION_ACTIVITY_ACTION" android:value="com.example.plugin.CONFIGURE" />
<meta-data android:name="com.streamvault.plugin.CAPABILITIES" android:value="provider.m3u,playback.prepare,cast.rewriteUrl,configuration.activity" />
```

Capability names:

- `provider.m3u`: the plugin can expose an M3U URL that StreamVault imports as a
  provider when enabled.
- `playback.prepare`: the plugin can prepare a stream URL before playback starts.
- `cast.rewriteUrl`: the plugin can rewrite a playback URL before Google Cast
  loads it.
- `configuration.activity`: the plugin exposes an Android activity that
  StreamVault can open.

## IPC Messages

Every request includes:

- `api_version`: current value is `1`.
- `request_id`: opaque ID copied back by the plugin response.

Every response should include:

- `api_version`
- `request_id`
- `success`
- Optional `message`

Messages:

| ID | Name | Purpose |
| --- | --- | --- |
| 1 | `MSG_GET_MANIFEST` | Return `manifest_json`. |
| 2 | `MSG_SET_ENABLED` | Start or stop plugin functionality using `enabled`. |
| 3 | `MSG_GET_STATUS` | Return a short `status_label` and optional `message`. |
| 4 | `MSG_GET_PROVIDER_URL` | Return `url` and optional `provider_name`. |
| 5 | `MSG_PREPARE_PLAYBACK` | Prepare `input_url`; set `handled` when relevant. |
| 6 | `MSG_REWRITE_CAST_URL` | Rewrite `input_url`; return `output_url` when relevant. |

For `playback.prepare` and `cast.rewriteUrl`, plugins should set `handled=false`
when the URL is not theirs. StreamVault then continues with other enabled
plugins or the original URL.

## Configuration Activity

If `configuration.activity` is present, StreamVault starts the action in
`configurationActivityAction` with the plugin package set explicitly. The plugin
activity should be exported and include the `DEFAULT` category.
