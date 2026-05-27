# Download Feature Plan — StreamVault

Created: 2026-05-26
Updated: 2026-05-26
Project: streamvalutfork (LocalForge project #6)
Status: implemented manually after LocalForge completion review
Features: 13 original LocalForge items (IDs 159–171), completed
Dependency chain: serial (159→160→...→171)

## User Requirements

- Download Movie/Series content to local phone storage
- Default folder: app external downloads storage under `Download/StreamVault`, user-configurable via SAF folder picker
- Download button next to Play and Copy URL on movie detail and per-episode rows
- Background download with progress tracking
- New "Downloads" top-level tab showing all downloaded content
- Downloads section: thumbnail grid, play local file, delete (removes file + DB record)
- Raw stream capture (same pattern as recording, no transcoding)

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Download folder | Separate from recording | Independent config, default "Downloads" |
| Format | Raw stream (.ts) | Reuses capture engine pattern, no FFmpeg needed |
| Series scope | Per-episode only | Users download individual episodes, not full series |
| Navigation | New top-level tab | Most discoverable, consistent with Movies/Series tabs |
| Storage | Android SAF treeUri | Same proven pattern as recording folder |

## Architecture

### Data flow

```
User taps Download → ViewModel resolves URL (resolveCopyStreamUrl)
  → creates DownloadRequest → DownloadManager.enqueueDownload()
  → creates DownloadEntity (PENDING) → starts OkHttp capture coroutine
  → streams to SAF output file or app external downloads fallback
  → updates progress/status in Room → COMPLETED/FAILED/CANCELLED
  → DownloadForegroundService observes Room state and shows notification
```

### New files

| Layer | File | Purpose |
|-------|------|---------|
| domain | `model/DownloadModels.kt` | DownloadStatus, DownloadContentType, DownloadItem, DownloadRequest, DownloadStorageConfig |
| domain | `repository/DownloadManager.kt` | Interface: observe, enqueue, cancel, delete, updateStorageConfig |
| data | `local/entity/DownloadEntity.kt` | Room entity for downloads table |
| data | `local/dao/DownloadDao.kt` | getAll, getById, getActive, insert, update, deleteById |
| data | `manager/DownloadManagerImpl.kt` | OkHttp stream capture to SAF/app external storage, progress tracking |
| app | `di/RepositoryModule.kt` | Hilt @Binds DownloadManager |
| app | `service/DownloadForegroundService.kt` | Background download with notification |
| app | `ui/screens/downloads/DownloadsScreen.kt` | Thumbnail grid, play, delete, folder picker |
| app | `ui/screens/downloads/DownloadsViewModel.kt` | State management, play/delete/folder actions |
| app | `ui/screens/downloads/DownloadsUiState.kt` | UI state model |
| tools | `smoke_download.ps1` | Compile verification script |

### Modified files

| File | Changes |
|------|---------|
| `StreamVaultDatabase.kt` | Add DownloadEntity, DownloadDao, bump v57→v58, migration |
| `DatabaseModule.kt` (or DI module) | Register migration |
| `PreferencesRepository.kt` | Add downloadTreeUri preference |
| `AndroidManifest.xml` | Register service, add permissions |
| `strings.xml` | 16 new string resources |
| `MovieDetailScreen.kt` | Add Download button in hero Row |
| `MovieDetailViewModel.kt` | Add downloadMovie() |
| `SeriesDetailScreen.kt` | Add Download button per EpisodeItem |
| `SeriesDetailViewModel.kt` | Add downloadEpisode() |
| `AppNavigation.kt` | Add DOWNLOADS route |
| `AppShell.kt` | Add Downloads tab to navigation |

## Feature breakdown

### Batch 1 — Data layer (159–163)

| ID | Title | Files |
|----|-------|-------|
| 159 | Download domain model + interface | `DownloadModels.kt` + `DownloadManager.kt` |
| 160 | Room entity + DAO | `DownloadEntity.kt` + `DownloadDao.kt` |
| 161 | DB registration + migration v57→v58 | `StreamVaultDatabase.kt` + DI module |
| 162 | DownloadManagerImpl + Hilt module | `DownloadManagerImpl.kt` + `DownloadModule.kt` |
| 163 | Download folder preference | `PreferencesRepository.kt` |

### Batch 2 — Service + UI integration (164–167)

| ID | Title | Files |
|----|-------|-------|
| 164 | String resources | `strings.xml` |
| 165 | ForegroundService + manifest | `DownloadForegroundService.kt` + `AndroidManifest.xml` |
| 166 | Download button on MovieDetailScreen | `MovieDetailScreen.kt` + `MovieDetailViewModel.kt` |
| 167 | Download button on SeriesDetailScreen | `SeriesDetailScreen.kt` + `SeriesDetailViewModel.kt` |

### Batch 3 — Downloads section (168–171)

| ID | Title | Files |
|----|-------|-------|
| 168 | Downloads tab + route | `AppNavigation.kt` + `AppShell.kt` |
| 169 | DownloadsViewModel + state | `DownloadsViewModel.kt` + `DownloadsUiState.kt` |
| 170 | DownloadsScreen composable | `DownloadsScreen.kt` |
| 171 | Smoke test script | `smoke_download.ps1` |

## Dependencies

```
159 → 160 → 161 → 162 → 163 → 164 → 165 → 166 → 167 → 168 → 169 → 170 → 171
```

Serial chain ensures each LocalForge session starts from a known-complete state.
