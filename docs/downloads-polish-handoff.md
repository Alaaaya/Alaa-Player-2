# Downloads Polish - Handoff Document

Created: 2026-05-27
Project: StreamVault (Android TV-first IPTV app)
Stack: Kotlin, Jetpack Compose, Hilt, Room, OkHttp, Media3
Base: current Downloads feature working baseline

## Overview

This handoff defines the next Downloads polish work. Scope has three features:

1. Show current download folder path in Downloads section.
2. Stack Download and Copy URL buttons vertically in Series episode rows.
3. Add smart download concurrency with resumable partial downloads.

Execution must be phased. Complete Phase 1 first, compile APK, commit, then proceed to Phase 2.

## Phase 1 - UI Polish

### Feature 1 - Show Current Download Folder Path

Goal: Downloads screen must show the currently active download folder path under the Change download folder button. When user changes folder with the SAF picker, displayed path must update without app restart.

Files:

| File | Purpose |
|------|---------|
| `app/src/main/java/com/streamvault/app/ui/screens/downloads/DownloadsScreen.kt` | Add folder path UI below button |

Current state:

- `DownloadsScreen.kt` has Change download folder button in the top area.
- `DownloadsViewModel` already observes `downloadManager.observeStorageState()`.
- `uiState.storageConfig` already contains `displayName` and `treeUri`.
- `observeStorageState()` updates after `onFolderSelected()` persists new SAF tree URI.

Implementation steps:

1. In `DownloadsScreen`, replace the top button-only row with a layout that can show button plus path text.
2. Keep Change download folder button visible in the same area.
3. Add text directly below the button.
4. Display text priority: `uiState.storageConfig.displayName`, then `uiState.storageConfig.treeUri`, then default folder label.
5. Use restrained styling: small body/label typography, tertiary text color, max 2 lines, ellipsis overflow.
6. Do not add new persistence for this feature. Existing storage state flow is enough.

Acceptance criteria:

- Downloads tab shows current folder path under Change download folder.
- If no folder has been selected, UI shows a default folder label instead of blank text.
- Selecting a new folder updates the displayed path immediately.
- Restarting app still shows persisted selected folder.

### Feature 2 - Stack Series Episode Buttons Vertically

Goal: In Series detail episode rows, Download and Copy URL buttons must remain on the right side of each episode row, but be stacked vertically. Download is on top. Copy URL is below.

Files:

| File | Purpose |
|------|---------|
| `app/src/main/java/com/streamvault/app/ui/screens/series/SeriesDetailScreen.kt` | Update `EpisodeItem` button layout |

Current state:

- `EpisodeItem` uses a horizontal `Row`.
- Episode card takes `Modifier.weight(1f)`.
- Copy URL and Download buttons are currently siblings in the same `Row`, so they appear side-by-side.

Implementation steps:

1. In `EpisodeItem`, keep episode card unchanged.
2. Replace the two trailing button siblings with a `Column`.
3. Place Download button first in the column.
4. Place Copy URL button second in the column.
5. Preserve existing button click handlers.
6. Preserve existing button styling, especially Download button emphasis colors.
7. Keep the column in the same right-side slot of the existing row.

Acceptance criteria:

- Series episode row shows Download above Copy URL.
- Buttons remain right of the episode card.
- Download button still starts download.
- Copy URL button still copies episode URL.
- Layout works on compact and wide screens.

### Phase 1 Completion Gate

Do not proceed to Phase 2 until all items below are complete.

- [x] Feature 1 implemented.
- [x] Feature 2 implemented.
- [x] `./gradlew assembleDebug` succeeds from `projects/streamvalutfork`.
- [x] APK installs or smoke test passes if device/emulator is available. Skipped by user for this coding phase.
- [x] Commit created with message: `Polish Downloads UI`.

## Phase 2 - Smart Download Concurrency + Resumable Downloads

Feature 3 includes resumable partial downloads, queueing, provider concurrency auto-detection, playback coordination, and retry on connectivity restore.

### Feature 3a - Resumable Partial Downloads

Goal: Partial downloads are preserved on transient failure. When network or server connection resumes, download continues from last written byte instead of restarting. User can delete partial downloads from Downloads section with existing delete action.

Files:

| File | Purpose |
|------|---------|
| `data/src/main/java/com/streamvault/data/manager/DownloadManagerImpl.kt` | Resume logic, Range headers, append mode, error classification |
| `data/src/main/java/com/streamvault/data/local/entity/DownloadEntity.kt` | Add resumability/retry fields |
| `data/src/main/java/com/streamvault/data/local/dao/DownloadDao.kt` | Queries for paused/retryable downloads |
| `domain/src/main/java/com/streamvault/domain/model/DownloadModels.kt` | Expose resumability/retry state in domain model |
| `domain/src/main/java/com/streamvault/domain/repository/DownloadManager.kt` | Add resume API |
| `data/src/main/java/com/streamvault/data/local/StreamVaultDatabase.kt` | Room migration for new fields |

Current state:

- `DownloadManagerImpl.captureDownload()` starts each HTTP request from byte 0.
- On any non-cancellation error, entity becomes `FAILED`.
- Partial output file may remain on disk, but no code reuses it.
- Existing delete action deletes output and DB row; preserve this behavior.

Implementation steps:

1. Add `supportsResume: Boolean` to download entity and domain item.
2. Add `retryCount: Int` to download entity and domain item.
3. Add Room migration with default values: `supportsResume = false`, `retryCount = 0`.
4. Add `resumeDownload(id: String)` to `DownloadManager`.
5. In initial download response, detect `Accept-Ranges: bytes` and store `supportsResume`.
6. When resuming and `bytesWritten > 0`, add HTTP header `Range: bytes=<bytesWritten>-`.
7. When resuming and output URI exists, append to existing output instead of creating a new file.
8. For SAF output, use append mode: `contentResolver.openOutputStream(uri, "wa")`.
9. For regular file output, use append mode file stream.
10. If server replies `206 Partial Content`, continue append resume.
11. If server replies `200 OK` to a range request, treat server as non-resumable and restart from byte 0 safely.
12. On transient error, mark download `PAUSED`, preserve output URI/path and `bytesWritten`.
13. On permanent error, mark download `FAILED`.
14. On delete, keep current behavior: remove partial output file and DB row.

Transient errors:

- Network unavailable.
- Socket disconnect.
- Socket timeout.
- HTTP 5xx.
- VOD contention detected by scheduler.

Permanent errors:

- HTTP 400.
- HTTP 404.
- Invalid URL.
- Unrecoverable output creation failure.

Acceptance criteria:

- Network drop during download changes status to `PAUSED`, not `FAILED`.
- Partial file remains present.
- Resume continues from saved byte offset when server supports ranges.
- Resume safely restarts when server does not support ranges.
- Delete removes partial file and DB row.

### Feature 3b - Concurrency-Aware Download Scheduler

Goal: Downloads run through a scheduler that respects learned/provider stream capacity. Excess downloads queue instead of all starting immediately.

Files:

| File | Purpose |
|------|---------|
| `data/src/main/java/com/streamvault/data/manager/DownloadManagerImpl.kt` | Queue, active stream tracking, slot management |
| `data/src/main/java/com/streamvault/data/preferences/PreferencesRepository.kt` | Persist max concurrent streams |
| `domain/src/main/java/com/streamvault/domain/repository/DownloadManager.kt` | Expose scheduler controls if needed |

Implementation steps:

1. Add persisted `maxConcurrentStreams` preference. Default: `2`.
2. Clamp `maxConcurrentStreams` to range `1..4`.
3. Track active stream count in `DownloadManagerImpl`. Count includes active downloads and active playback.
4. Replace direct fire-and-forget launch in `enqueueDownload()` with scheduler entry point.
5. If active count is below max, start download immediately.
6. If active count is at max, keep item `PENDING` and queue it.
7. When a download completes, pauses, fails permanently, or cancels, free its slot.
8. After freeing a slot, start next queued `PENDING` or `PAUSED` item.

Acceptance criteria:

- With max stream count 1, three downloads run serially.
- With max stream count 2, first two downloads run and third waits.
- Queued downloads show existing pending/paused status in Downloads screen.
- Cancelling or deleting active download starts next queued item.

### Feature 3c - Auto-Detect Provider Concurrency

Goal: App learns whether VOD provider allows parallel streams. It downgrades when contention is detected and upgrades after stable parallel success.

Files:

| File | Purpose |
|------|---------|
| `data/src/main/java/com/streamvault/data/manager/DownloadManagerImpl.kt` | Collision detection, downgrade/upgrade logic |
| `data/src/main/java/com/streamvault/data/preferences/PreferencesRepository.kt` | Persist learned limit |

Implementation steps:

1. Start optimistic with `maxConcurrentStreams = 2` unless persisted value exists.
2. Treat HTTP 403 or HTTP 429 while another stream is active as probable VOD concurrency collision.
3. Treat connection reset during parallel activity as possible VOD concurrency collision only if network is otherwise available.
4. On collision, decrement `maxConcurrentStreams` by 1, minimum 1.
5. Persist downgraded value.
6. Mark failed/collided download `PAUSED` or `PENDING`, not `FAILED`.
7. If active downloads exceed new limit, pause most recently started downloads until active count fits limit.
8. Track consecutive successful parallel downloads.
9. After enough parallel success, increment `maxConcurrentStreams` by 1, maximum 4.
10. Reset success counter after any downgrade.

Acceptance criteria:

- Provider allowing one stream causes downgrade to 1 after collision.
- Collided download queues/resumes instead of failing permanently.
- Provider allowing multiple streams keeps or increases parallel limit after successful runs.
- Learned limit survives app restart.

### Feature 3d - Playback-Aware Coordination

Goal: Playback counts as an active VOD stream. If playback starts and provider limit would be exceeded, downloads pause and later resume automatically.

Files:

| File | Purpose |
|------|---------|
| `domain/src/main/java/com/streamvault/domain/repository/DownloadManager.kt` | Add playback lifecycle methods |
| `data/src/main/java/com/streamvault/data/manager/DownloadManagerImpl.kt` | React to playback start/stop |
| Player screen/ViewModel integration point | Call playback lifecycle methods |

Implementation steps:

1. Add `onPlaybackStarted()` to `DownloadManager`.
2. Add `onPlaybackStopped()` to `DownloadManager`.
3. On playback start, increment active stream count.
4. If active count exceeds learned max, pause most recently started active download.
5. Paused download must keep partial file and byte offset.
6. On playback stop, decrement active stream count.
7. After playback stop, resume next queued/paused download if slot is available.
8. Wire calls from player lifecycle where playback actually starts/stops, not merely screen navigation.

Acceptance criteria:

- With max stream count 1, active download pauses when playback starts.
- Download resumes automatically when playback stops.
- With max stream count 2, one download plus one playback can run together.
- With max stream count 2 and two active downloads, starting playback pauses one download.

### Feature 3e - Auto-Retry On Connectivity Restore

Goal: Downloads paused due to network/server transient errors retry automatically when connection returns, with backoff for repeated failures.

Files:

| File | Purpose |
|------|---------|
| `data/src/main/java/com/streamvault/data/manager/DownloadManagerImpl.kt` | Connectivity callback and retry scheduling |
| `data/src/main/java/com/streamvault/data/local/entity/DownloadEntity.kt` | Retry count field |
| `data/src/main/java/com/streamvault/data/local/dao/DownloadDao.kt` | Query retryable paused downloads |

Implementation steps:

1. Register `ConnectivityManager.NetworkCallback` in `DownloadManagerImpl`.
2. On network available, query paused retryable downloads.
3. Resume retryable paused downloads through scheduler, not directly.
4. On transient error, increment `retryCount` and schedule exponential backoff.
5. Suggested backoff: 5s, 15s, 45s, then cap around 5 minutes.
6. On successful completion, reset `retryCount` to 0.
7. After max retries, mark `FAILED` and stop automatic retries.
8. Keep VOD contention separate from network retry. Contention waits for stream slot, not network callback.

Acceptance criteria:

- Airplane mode during download pauses download.
- Turning network back on resumes download automatically.
- Repeated failures increase retry delay.
- After max retry threshold, download becomes `FAILED`.
- User can delete failed or paused partial downloads.

## Phase 2 Completion Gate

Do not commit Phase 2 until all items below are complete.

- [x] Partial file preserved on transient failure.
- [x] Resume uses HTTP Range when supported.
- [x] Resume safely restarts if server does not support ranges.
- [x] Delete removes partial output and DB row.
- [x] Queue respects learned `maxConcurrentStreams`.
- [x] Provider contention downgrades concurrency.
- [x] Stable parallel success can upgrade concurrency.
- [x] Playback start pauses downloads when limit would be exceeded.
- [x] Playback stop resumes queued/paused downloads.
- [x] Connectivity restore resumes network-paused downloads.
- [x] `./gradlew assembleDebug` succeeds from `projects/streamvalutfork`.
- [x] APK installs or smoke test passes if device/emulator is available. Skipped by user for this coding phase.
- [x] Commit created with message: `Add smart resumable download scheduler`.

## Work Order

Use this exact sequence in the next session:

1. Implement Feature 1.
2. Implement Feature 2.
3. Run `./gradlew assembleDebug` from `projects/streamvalutfork`.
4. If build passes, commit Phase 1.
5. Implement Feature 3a.
6. Implement Feature 3b.
7. Implement Feature 3c.
8. Implement Feature 3d.
9. Implement Feature 3e.
10. Run `./gradlew assembleDebug` from `projects/streamvalutfork`.
11. Smoke test if device/emulator is available.
12. Commit Phase 2.

## Important Implementation Notes

- Do not mark transient download errors as `FAILED` unless retry budget is exhausted or error is clearly permanent.
- Do not delete partial files on pause, retry, or provider collision.
- Use existing delete button as the user-controlled cleanup path for partial files.
- All resume/retry work must go through scheduler so concurrency rules stay consistent.
- Playback stream and download streams must share the same active stream accounting.
- Keep UI changes in Phase 1 small and separate from scheduler work.

## Known Risks

- SAF append mode (`"wa"`) may vary by provider. Implement safe fallback: restart full download if append/resume cannot be guaranteed.
- Some VOD providers may return 403/429 for auth expiration, not concurrency. Error classification should check whether another stream is active before treating as contention.
- Some servers omit `Accept-Ranges` but still support ranges. Prefer response behavior (`206 Partial Content`) over header alone when deciding if resume works.
- Player lifecycle wiring must use actual playback start/stop signals, not only screen enter/exit, to avoid pausing downloads unnecessarily.
