# External Player Chooser Cancel Handoff

## Baseline

- Latest committed work: `144f4ce Fix external playback URL handoff`.
- User validated that commit on device:
  - Copy URL works.
  - Direct VLC playback works.
- Current working tree has uncommitted follow-up changes in:
  - `app/src/main/java/com/streamvault/app/ui/screens/player/PlayerScreen.kt`
  - `app/src/main/java/com/streamvault/app/ui/screens/player/PlayerViewModel.kt`
  - `app/src/main/java/com/streamvault/app/ui/screens/settings/SettingsScreenDialogs.kt`
  - `app/src/main/java/com/streamvault/app/ui/screens/settings/SettingsScreenState.kt`
  - `app/src/main/res/values/strings.xml`

## User Correction

- Real issue is not playback after VLC is selected.
- Real issue:
  1. Setting is `External player`.
  2. User presses Play.
  3. Android chooser opens with VLC option.
  4. Before user taps VLC, StreamVault internal player starts playing behind chooser.
  5. Internal player must stay stopped while chooser is open.
  6. If user selects VLC, keep internal player stopped.
  7. If user dismisses chooser/taps outside/back, then start internal playback as fallback.
- `Ask every time` option in Default playback app should be removed. Existing saved `ask` can behave as External player.

## Current Uncommitted Implementation Attempt

- `PlayerViewModel.kt` was refactored so external mode resolves/probes URL without preparing Media3:
  - Added `prepareStreamInfoForPlayback(...)` helper.
  - `preparePlayer(...)` now prepares internal player only after helper returns resolved `StreamInfo`.
  - Added `prepareExternalPlaybackUrl(...)` to set `currentResolvedPlaybackUrl/currentResolvedStreamInfo`, stop timeshift, and stop player without preparing playback.
  - Added `playResolvedStreamInternally()` fallback that prepares the saved resolved `StreamInfo` inside `viewModelScope.launch`.
  - Main `prepare(...)` path uses external-only resolution when `externalPlaybackMode != INTERNAL_PLAYER`.
- `PlayerScreen.kt` was changed to use chooser-aware launch:
  - Uses `rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())`.
  - Uses `Intent.createChooser(...)` plus `Intent.EXTRA_CHOSEN_COMPONENT_INTENT_SENDER`.
  - Registers a per-screen `BroadcastReceiver` to detect actual external app selection.
  - If Activity Result returns `RESULT_CANCELED` and no chosen callback was received, calls `viewModel.playResolvedStreamInternally()`.
- Settings changes:
  - `SettingsScreenDialogs.kt` lists only `INTERNAL_PLAYER` and `EXTERNAL_PLAYER`.
  - Existing `ASK_EVERY_TIME` selected state displays as External player.
  - `SettingsScreenState.kt` maps `ASK_EVERY_TIME` label to External player.
  - Removed `settings_external_playback_mode_ask` string from default `strings.xml`.

## Verification State

- `gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain` passed after chooser patch.
- Then tests and assemble were accidentally run in parallel. This was wrong for this Gradle project and caused build-output races.
- Afterward, serial targeted tests still failed:
  - Command: `gradlew.bat :app:cleanTestDebugUnitTest :app:testDebugUnitTest --tests com.streamvault.app.player.external.* --no-daemon --console=plain`
  - Failure: all external-player tests fail with `NoClassDefFoundError` at calls to `ExternalPlayerLauncher` / `inferExternalPlayerMimeType`.
  - This looks like stale/corrupt test classpath/build intermediates from the previous parallel Gradle run, not a Kotlin compile error.
- APK assemble after chooser patch has not been successfully re-run after the failed parallel run.

## Recommended Next Steps

1. Do not run Gradle tasks in parallel in this project.
2. First inspect current uncommitted diff:
   - `git status --short`
   - `git diff -- app/src/main/java/com/streamvault/app/ui/screens/player/PlayerScreen.kt app/src/main/java/com/streamvault/app/ui/screens/player/PlayerViewModel.kt app/src/main/java/com/streamvault/app/ui/screens/settings/SettingsScreenDialogs.kt app/src/main/java/com/streamvault/app/ui/screens/settings/SettingsScreenState.kt app/src/main/res/values/strings.xml`
3. Recover build state with one serial clean command:
   - `gradlew.bat clean :app:compileDebugKotlin --no-daemon --console=plain`
4. If compile passes, run targeted tests serially:
   - `gradlew.bat :app:testDebugUnitTest --tests com.streamvault.app.player.external.* --no-daemon --console=plain`
5. If tests still show `NoClassDefFoundError`, inspect generated test report for exact missing class and run full app clean again before code changes.
6. Build APK serially only after tests/compile:
   - `gradlew.bat :app:assembleDebug --no-daemon --console=plain`
7. Device-test exact behavior:
   - External selected, press Play, chooser visible: internal audio/video must not start.
   - Tap VLC: VLC plays, StreamVault stays stopped.
   - Dismiss chooser: StreamVault starts internal fallback.
8. If chooser cancel is not detected reliably by `StartActivityForResult`, keep internal stopped while chooser is visible and implement explicit in-app fallback notice/button after returning to StreamVault.

## Caution

- Do not revert `144f4ce`; user confirmed it works.
- Current uncommitted changes are not yet verified on device.
- The chooser callback approach compiles, but Android resolver behavior must be device-tested.
