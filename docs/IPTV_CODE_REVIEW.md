# StreamVault IPTV Player Technical Review

## Review status

| Phase | Status | Last updated |
|---|---|---|
| 1. Architecture and flow mapping | **Complete** | 2026-07-11 |
| 2. Shared infrastructure review | **Complete** | 2026-07-11 |
| 3. Provider reviews | **Complete** | 2026-07-11 |
| 4. Feature flow reviews | **Complete** | 2026-07-11 |
| 5. Edge cases and failure scenarios | **Complete** | 2026-07-12 |
| 6. Consolidated remediation plan | **Complete** | 2026-07-12 |

This is a living report. Findings are added and refined in this file as each phase is completed. A finding marked **Confirmed defect** is directly demonstrated by the source or a focused build/test. **Highly likely defect** has a concrete failing path but still needs a runtime/provider fixture to reproduce. **Suspected risk** needs further verification. **Architectural concern** describes a structural condition that makes correctness or maintenance materially harder. **Simplification opportunity** identifies unnecessary complexity without asserting a current failure.

## Table of contents

- [Scope and method](#scope-and-method)
- [Phase 1 — Architecture and flow mapping](#phase-1--architecture-and-flow-mapping)
  - [System map](#system-map)
  - [Provider boundaries](#provider-boundaries)
  - [Application and feature flows](#application-and-feature-flows)
  - [Phase 1 findings summary](#phase-1-findings-summary)
  - [Detailed Phase 1 findings](#detailed-phase-1-findings)
  - [Phase 1 open questions](#phase-1-open-questions)
  - [Next review targets](#next-review-targets)
- [Phase 2 — Shared infrastructure review](#phase-2--shared-infrastructure-review)
- [Phase 3 — Provider reviews](#phase-3--provider-reviews)
- [Phase 4 — Feature flow reviews](#phase-4--feature-flow-reviews)
- [Phase 5 — Edge cases and failure scenarios](#phase-5--edge-cases-and-failure-scenarios)
- [Phase 6 — Consolidated remediation plan](#phase-6--consolidated-remediation-plan)

## Scope and method

The review covers all four Gradle modules (`app`, `data`, `domain`, and `player`) and all provider/feature families visible in the application. It intentionally excludes a security/privacy assessment. Authentication, tokens, cookies, credentials, and device identity are in scope only where they affect correctness, reliability, networking, persistence, or playback.

Phase 1 used the repository's existing graphify graph and wiki, then verified the graph against startup, DI, provider setup, synchronization, persistence, playback, EPG, recording, backup, and plugin entry points. The graph contains thousands of nodes and identifies `PreferencesRepository`, `SettingsViewModel`, `OkHttpStalkerApiService`, `StalkerProvider`, `Media3PlayerEngine`, and `XtreamProvider` as load-bearing abstractions. The graph's community labels are generic and its cohesion is low, so graph relationships were treated as navigation hints, not proof.

Focused verification performed in this phase:

- `./gradlew.bat :data:compileDebugKotlin --rerun-tasks --no-daemon` — passed on 2026-07-11. This confirms the inspected data-module source compiles, while also producing a large warning set that will be triaged in Phase 2/4.
- Static size/coupling measurements at the original review baseline: `SyncManager` was 6,048 lines/169 functions; `OkHttpStalkerApiService` 2,767/124; `Media3PlayerEngine` 2,482/122; `PreferencesRepository` 2,324/169; `PlayerViewModel` 1,776 lines; `StalkerProvider` 1,611/88; `ProviderRepositoryImpl` 1,308 lines. After the current ARCH-003 slices, `SyncManager` is approximately 2,451 lines; implementation is substantially complete, with device-backed validation and further compatibility cleanup remaining.

# Phase 1 — Architecture and flow mapping

**Status: Complete.** This phase maps responsibilities and control/data flow. It identifies architectural faults and directly visible correctness problems; it does not yet claim a line-by-line infrastructure or provider audit.

## System map

### Module boundaries

| Module | Intended responsibility | Observed responsibility and dependencies |
|---|---|---|
| `domain` | Platform-neutral models, repository/manager contracts, use cases | Pure Kotlin/JVM and depends only on coroutines/injection. `Provider` is now stable identity/status; sealed `ProviderConfiguration`, `ProviderSnapshot`, generation-bound `StalkerPortalLearning`, and explicit capability contracts form the provider boundary. `LegacyProvider` remains only as a staged compatibility envelope for legacy UI/revision/backup callers (ARCH-001/ARCH-002 implementation, 2026-08-11). |
| `data` | Room, DataStore, networking, provider clients, parsers, sync, backup/recording implementations | Android library depending on `domain`. It owns the Room database and DAOs, Xtream/Stalker/Jellyfin clients, M3U/XMLTV parsing, synchronization workers, repositories, backup/Drive, downloads, recordings, reminders, and URL resolution. `SyncManager` centralizes much of the application's provider policy (ARCH-003). |
| `player` | Reusable playback engine and timeshift | Android library depending on `domain`. It wraps Media3/ExoPlayer, networking/data sources, decoder policy, media session, timeshift, and diagnostics. Provider URL resolution occurs outside this module in `data`/`app`, so the engine receives a resolved URL plus request metadata. |
| `app` | Android entry points, Compose UI/navigation, DI, TV integration, plugins, cast, updates | Depends on all other modules and is the composition root. Several ViewModels directly depend on `data` implementation types (`SyncManager`, `PreferencesRepository`, `XtreamStreamUrlResolver`, Stalker URL helpers), weakening the domain boundary (ARCH-005). |

### Runtime composition

Hilt modules under `app/.../di` bind domain repository/manager interfaces to `data` implementations and construct the shared networking, database, and player objects. `StreamVaultDatabase` is the durable catalog/state store. `PreferencesRepository` is the DataStore-backed global preference/session facade. UI consumes Room/DataStore flows through repositories and directly through a few implementation services.

The primary runtime path is:

```text
Android components / Compose UI
        |
        v
ViewModels + domain use cases
        |
        +------> domain repository/manager contracts ------> data implementations
        |                                                     |
        |                                                     +--> Room / DataStore
        |                                                     +--> provider HTTP/parsers
        |                                                     +--> WorkManager / alarms/services
        |
        +------> direct data implementation dependencies (architectural leak)
        |
        +------> PlayerEngine contract ------> Media3PlayerEngine
```

### Persistence

`StreamVaultDatabase` declares the provider/catalog, EPG, favorites, history, recording, download, sync staging/index, and compatibility tables and exposes their DAOs. It also contains a long in-file migration chain. Provider catalog sync uses staging and transaction helpers (`SyncCatalogStore`, `DatabaseTransactionRunner`) before committing normalized entities. Preferences and cross-feature flags live in `PreferencesRepository`, while plugin enablement/mappings use a separate `SharedPreferences` store in `StreamVaultPluginManager`.

This is a hybrid state system: Room owns durable relational content and job metadata; DataStore owns global/user settings and some generation/session markers; WorkManager owns background job state; services/alarms own recording execution; in-memory maps/mutexes own active provider and playback state. Phase 2 must verify consistency and recovery across these stores.

## Provider boundaries

### Declared boundary

The former monolithic `IptvProvider` contract has been removed. The domain now declares `ProviderAuthenticator`, `LiveCatalogSource`, `VodCatalogSource`, `SeriesCatalogSource`, `GuideSource`, `PlaybackResolver`, and `CatchUpSource`, resolved through one complete typed registry keyed by `ProviderType`.

### Original boundaries (historical review baseline)

| Provider/source | Setup and validation | Catalog/sync path | Playback resolution | EPG path |
|---|---|---|---|---|
| Xtream Codes | `ValidateAndAddProvider` → `ProviderRepositoryImpl` → Xtream API | `SyncManager` branches into Xtream onboarding, live sync, staged index jobs, and `XtreamIndexWorker`; `XtreamProvider` implements `IptvProvider` | `XtreamStreamUrlResolver` parses internal tokens/direct URLs and rebuilds credentialed URLs | Provider API short/full EPG plus optional XMLTV/external sources |
| Stalker Portal | `ValidateAndAddProvider` → `ProviderRepositoryImpl` → `StalkerProvider`/`StalkerApiService` bootstrap | `SyncManager` branches into Stalker catalog staging/index jobs; `StalkerIndexWorker`; `StalkerProvider` implements `IptvProvider` | The Xtream-named resolver constructs/caches `StalkerProvider`, calls `resolvePlaybackInfo`, and persists learned playback behavior | Portal EPG bulk/per-channel strategies plus optional XMLTV/external sources |
| M3U/M3U8 | `ValidateAndAddProvider.addM3u` → `ProviderRepositoryImpl.validateM3u` | `SyncManagerM3uImporter` and `M3uParser`; no `IptvProvider` implementation | Pass-through URL with provider request profile | Playlist/provider URL and manually assigned XMLTV sources |
| Jellyfin/local media | Jellyfin login/quick-connect paths in setup/repository | Dedicated `JellyfinProvider`, but dispatched directly by `SyncManager`; no `IptvProvider` implementation | Direct Jellyfin URL plus authorization header assembled by the shared resolver | No provider guide lookup; catalog metadata is server-derived |
| Plugin-based | Android service discovery and Messenger protocol in `StreamVaultPluginManager` | Plugin catalogs are imported/mapped into an M3U provider record rather than represented as a provider capability | Playback delegates through the plugin manager or the mapped M3U path depending on manifest action | No first-class provider EPG contract identified |
| Recordings/local output | `RecordingManagerImpl`, Room recording entities, alarms, foreground service/capture engine | Local database/files/document URIs, reconciled on launch and periodically | Local `RecordingItem` enters player/navigation as content | EPG is used to schedule/name recordings; recordings do not supply EPG |

The registry is now the shared execution boundary for synchronization, detail hydration, guide lookup, playback, catch-up, recording-source resolution, and setup. Remaining coordinator decomposition and Stalker playback side-effect removal belong to ARCH-003/ARCH-004 in WP5.

### Current provider boundary (2026-08-11)

| Provider/source | Typed execution boundary | Capability notes |
|---|---|---|
| Xtream Codes | Typed setup and client factories; registry-selected sync, guide, detail, playback, and catch-up adapters | All seven capabilities available |
| Stalker Portal | `StalkerConfig` plus generation-bound `StalkerPortalLearning`; registry-selected adapters | All capabilities are potential and become typed restricted/unsupported from validated portal learning |
| M3U/M3U8 | `M3uConfig`; registry-selected live/VOD import and playback/catch-up adapters | Live, VOD, playback, and catch-up available; guide available only when XMLTV is configured; authentication/series unsupported |
| Jellyfin/local media | `JellyfinConfig`; registry-selected authentication, VOD/series sync/detail, and playback adapters | Authentication, VOD, series, and playback available; live, native guide, and catch-up unsupported |

## Application and feature flows

### Startup

1. Android creates `StreamVaultApp` (`AndroidManifest.xml`, `StreamVaultApp.onCreate`).
2. Crash/runtime diagnostics start; stale timeshift directories and cached app-update checks are launched in an application-scoped coroutine.
3. WorkManager schedules daily maintenance, periodic/launch provider sync, periodic/launch Xtream indexing, and periodic/one-shot recording reconciliation.
4. `MainActivity` installs Compose and enters `AppNavigation`, whose start destination is always `WELCOME`.
5. `WelcomeViewModel` observes whether providers exist. Existing providers proceed to the configured landing route; an empty database shows setup. Deep links, search, playback-history navigation, and imported M3U/backup intents are coordinated by `MainActivity`/`AppNavigation`.

### Provider setup and first synchronization

1. Welcome/setup UI builds a provider-specific command (`XtreamProviderSetupCommand`, `M3uProviderSetupCommand`, `StalkerProviderSetupCommand`, or Jellyfin command).
2. `ValidateAndAddProvider` calls `ProviderSetupInputValidator`, then delegates provider-specific login/add behavior to `ProviderRepository`.
3. `ProviderRepositoryImpl` validates/authenticates, encrypts/persists provider data, and calls `SyncManager` for the initial catalog. A provider can be persisted even when sync fails, represented by `ProviderSavedWithSyncErrorException`/`SavedWithWarning`.
4. `SyncManager.sync` acquires a provider lock, decrypts the entity, dispatches on `ProviderType`, stages/commits catalog data, updates sync metadata/status, publishes a `SyncState`, and may queue follow-up provider-specific workers.
5. UI observes Room catalog flows and the separate sync state/progress facilities.

### Refresh and incremental synchronization

Manual refresh flows through `SyncProvider` or UI classes that call `ProviderRepository.refreshProviderData`/`SyncManager` directly. Background refresh is split across `ProviderSyncWorker`, `XtreamIndexWorker`, `StalkerIndexWorker`, and `BackgroundEpgSyncWorker`. Each uses its own unique-work naming, policies, retries, and stale-job rules; per-provider mutexes in `SyncManager` are the in-process serialization mechanism. Process death relies on Room job/staging metadata and WorkManager re-entry.

### Live TV playback

1. UI selects a normalized `Channel`; navigation constructs a player request.
2. `PlayerViewModel` loads content/EPG/context and calls `XtreamStreamUrlResolver` even though the content may be Xtream, Stalker, M3U, or Jellyfin.
3. The resolver reads/decrypts the provider entity, resolves internal tokens or direct URLs, attaches headers/user agent/proxy data, and for Stalker may authenticate/create a link and persist learned playback settings.
4. `PlayerViewModel` prepares the `PlayerEngine`; `Media3PlayerEngine` constructs Media3 sources/renderers, media session, retry/recovery, token renewal, and optional timeshift.
5. Recovery, alternate streams, catch-up variants, cast, recording, history, watch-next, plugin actions, and UI messages are coordinated across `PlayerViewModel` plus many extension/support files.

### VOD, series, and resume

Movie/series repositories expose Room-backed catalog flows. Xtream and Stalker can lazily hydrate details/categories through provider-specific branches embedded in `MovieRepositoryImpl` and `SeriesRepositoryImpl`; M3U/Jellyfin mostly use persisted records. Player preparation resolves movie/episode URLs, reads history, and periodically persists progress. Completion updates watched state, recommendations, and optional next-episode behavior.

### EPG and manual XMLTV

Provider EPG can be fetched during provider sync, in `BackgroundEpgSyncWorker`, or on demand. External/manual XMLTV sources and provider assignments are managed by `EpgSourceRepositoryImpl`; provider-owned refresh is also exposed by `EpgRepositoryImpl`. Channel-to-guide resolution and priorities are stored in separate EPG source/program/mapping tables. UI guide composition combines cached provider data and assigned sources through `EpgViewModel` and repository helpers.

### Recording and scheduled recording

UI uses `ScheduleRecording`/`RecordingManager`. `RecordingManagerImpl` persists schedule/run entities, schedules exact start/stop alarms, and starts `RecordingForegroundService`, which invokes the capture engine and updates run status/output. A boot/package restore receiver and `RecordingReconcileWorker` re-establish or reconcile schedules; startup also triggers an immediate reconciliation. Manual recording is initiated from `PlayerViewModel` but uses the same manager/service pipeline.

### Backup and restore

`BackupManagerImpl` serializes preferences, providers, mappings, user state, and scheduled recordings through the domain backup contract. File/document bridging happens in `app`; optional Google Drive orchestration is in `GoogleDriveBackupSyncManager`. Restore recreates provider and feature records and delegates recording re-scheduling to `RecordingManager`. Migration/identity matching behavior requires full Phase 4 review.

### Plugins

`StreamVaultPluginManager` discovers Android services implementing the plugin action, reads a manifest over Messenger or service metadata, enables/disables plugins in its own preferences, invokes plugin operations, imports plugin-provided catalog data into an M3U provider mapping, and refreshes TV input/catalog state. Discovery currently performs synchronous `runBlocking(Dispatchers.IO)` Messenger calls (ARCH-006).

### App upgrades and database migrations

App update checking starts from `StreamVaultApp` and stores cached release metadata in `PreferencesRepository`; install UI/service logic lives under `app/update`. Room database upgrades are handled by the large explicit migration chain in `StreamVaultDatabase`, with schema JSON emitted by the Room Gradle plugin. Migration correctness is deferred to Phase 4, but its concentration is recorded in ARCH-007.

## Phase 1 findings summary

| ID | Classification | Severity | Summary | Fix scope |
|---|---|---:|---|---|
| ARCH-001 | Implemented in code; API 25 migration evidence pending (2026-08-11) | High | Explicit provider capabilities and complete typed registries now drive synchronization and runtime provider execution. | Architectural |
| ARCH-002 | Implemented in code; API 25 migration evidence pending (2026-08-11) | High | Stable provider identity, typed encrypted configuration/runtime storage, and generation-bound Stalker learning replace the authoritative union row. | Architectural |
| ARCH-003 | Implemented in code; device validation pending (2026-08-12) | High | Provider plans, activation receipts, durable continuation scheduling, state publication, index execution, and workflow admission are separated; the remaining `SyncManager` compatibility façade is approximately 2,451 lines. | Architectural |
| ARCH-004 | Confirmed defect + architectural concern | Medium | The all-provider playback resolver contains contradictory dispatch and mixes URL resolution with Stalker session construction and persistence. | Local first; architectural follow-up |
| ARCH-005 | Architectural concern | High | `PlayerViewModel` is a 25+ dependency feature orchestrator that crosses domain/data/player boundaries. | Architectural |
| ARCH-006 | Highly likely defect | High | Plugin discovery performs multiple blocking IPC calls and maps plugin providers into M3U records. | Local + architectural |
| ARCH-007 | Architectural concern | Medium | Database schema and the full migration history are concentrated in one multi-thousand-line class. | Architectural |
| ARCH-008 | Resolved in code; device validation pending | Medium | Startup launches several overlapping background pipelines with independent work identities and state stores. | Architectural |

## Detailed Phase 1 findings

### ARCH-001 — Shared provider abstraction is partial and not used by the main sync pipeline

- **Classification:** Implemented in code; API 25 migration evidence pending (2026-08-11)
- **Severity:** High
- **Where:** `domain/.../provider/ProviderCapabilities.kt`; `data/.../provider/DefaultProviderCapabilityRegistry.kt`; `data/.../provider/ProductionProviderCapabilityFactories.kt`; `data/.../sync/ProviderSyncAdapterRegistry.kt`; `data/.../sync/SyncManager.kt`.
- **Current behavior:** `IptvProvider` claims to be the shared interface for all providers and includes authentication, every catalog type, EPG, playback, and catch-up. Only Xtream and Stalker implement it. M3U and Jellyfin bypass it. More importantly, `SyncManager` does not dispatch through this interface; it repeatedly switches on `ProviderType` and constructs/calls provider-specific helpers.
- **Why this is wrong/fragile:** There are two competing provider architectures: a polymorphic domain contract and a central type switch. Adding or correcting a capability requires edits to multiple switches, repositories, URL resolution, setup, workers, and UI. The oversized interface also assumes every source supports authentication, live, VOD, series, EPG, and catch-up, which is false for M3U, Jellyfin, plugins, and recordings. This encourages dummy behavior and exceptions rather than explicit capabilities.
- **Concrete failure scenario:** A new provider or a Jellyfin live-TV capability is added to one path (for example catalog sync) but omitted from one of `retrySection`, guide lookup, playback resolution, or background-worker dispatch. It compiles because `ProviderType` switches are distributed, then fails only in the omitted feature. Existing code already throws “retry unavailable” for Jellyfin in section-specific paths and handles Jellyfin separately in playback.
- **Recommended correction:** Replace the monolithic interface with explicit capabilities (`ProviderAuthenticator`, `LiveCatalogSource`, `VodCatalogSource`, `SeriesCatalogSource`, `GuideSource`, `PlaybackResolver`, `CatchUpSource`) and a registry/factory keyed by provider type. Make `SyncManager` orchestrate capability results rather than provider types. M3U, Jellyfin, plugins, and local sources should implement only supported capabilities.
- **Fix scope:** Architectural; can be introduced incrementally behind adapters.
- **Required tests:** A provider capability contract test suite; registry completeness test for every `ProviderType`; unsupported-capability tests returning typed results rather than exceptions; end-to-end sync/playback tests using one fake per capability combination.
- **Implementation (2026-08-11):** Removed `IptvProvider`; introduced the seven explicit capability interfaces, typed `Available`/`Unsupported`/`Restricted`/`ConfigurationError` resolution, one validated factory per provider type, and the documented capability matrix. Full sync, section retry, and guide orchestration resolve through `ProviderSyncAdapterRegistry`; section implementations no longer re-dispatch on `ProviderType`. Detail hydration, on-demand guide, playback, catch-up, recording-source resolution, and setup resolve through the same capability boundary. Jellyfin movie/series retry is supported, configured M3U XMLTV exposes a cache-backed guide, and absent features return typed results. Architecture tests reject direct protocol-client construction outside `TypedProviderClientFactory`, reintroduction of `IptvProvider`, provider-type redispatch in `SyncManager`, and provider-type branching in the runtime playback/recording/guide boundaries. Stalker playback cache/learning side effects deliberately remain behind its adapter for ARCH-004/WP5.

### ARCH-002 — Core domain model is polluted by Stalker transport, fingerprint, and learned state

- **Classification:** Implemented in code; API 25 migration evidence pending (2026-08-11)
- **Severity:** High
- **Where:** `domain/.../model/Provider.kt`; `domain/.../model/ProviderConfiguration.kt`; provider snapshot persistence/codecs; Room migrations 72→73→74; backup schema v11.
- **Current behavior:** One `Provider` data class stores generic identity plus Stalker MAC, device profile, timezone/locale, serial/device IDs/signature, advanced JSON, auth mode, portal profile/fingerprint/MAG preset/bootstrap recipe, endpoint/cookie/playback preferences, learned playback mode, requirement flags, fallback use, and rediscovery attempts. Xtream and M3U flags are mixed into the same record.
- **Why this is wrong/fragile:** Transport configuration, durable user configuration, portal-learning output, and cross-provider identity are conflated. Every layer and backup/migration must understand fields irrelevant to most providers. Defaults silently create plausible Stalker state, making “unset”, “learned”, and “user-selected” hard to distinguish. It also causes provider-specific policy to leak into UI and shared data paths.
- **Concrete failure scenario:** A Stalker bootstrap attempt updates a learned recipe/fingerprint but a concurrent provider edit saves an older copied `Provider`, reverting learned state or overwriting user configuration. Alternatively, a new Stalker strategy requires more fields and forces domain model, Room schema, backup format, UI commands, and every mapper to change together.
- **Recommended correction:** Keep `Provider` as stable identity/status. Store typed provider configurations (`XtreamConfig`, `M3uConfig`, `StalkerDeviceIdentity`, `StalkerPortalLearning`, `JellyfinConfig`) behind a sealed configuration model or dedicated tables. Separate user intent from observed/learned portal state with version/timestamp/source metadata.
- **Fix scope:** Architectural and migration-sensitive.
- **Required tests:** Mapping round trips for each provider subtype; migrations from the union record; concurrent edit-vs-learning update tests; backup compatibility tests; explicit unset/default/learned state tests.
- **Implementation (2026-08-11):** Canonical `Provider` now contains only stable identity/status and lifecycle timestamps. `ProviderSnapshot` combines that identity with sealed `XtreamConfig`, `M3uConfig`, `StalkerConfig`, or `JellyfinConfig`, account/runtime state, configuration generation, and optional generation-matched `StalkerPortalLearning`. Stalker user intent and observations are separate; observations carry generation, source, and timestamp, and compare-and-set persistence rejects stale writes. Room 72→73 creates authoritative encrypted `provider_configs`, runtime storage, and typed generation-bound learning and backfills every provider type; 73→74 rebuilds the stable `providers` table while preserving IDs/FKs and checking `PRAGMA foreign_key_check`. Pending revisions use a versioned typed envelope with legacy decoding. Backup v11 stores stable provider plus typed configuration and excludes sessions, learning, playback hints, TLS pins, and transport consent; versions 0–10 remain readable. `LegacyProvider` is retained only as an explicitly named compatibility envelope while presentation and old backup inputs complete their staged cutover; it is no longer the authoritative model or persistence row.

### ARCH-003 — `SyncManager` is the application's provider-policy and persistence god object

- **Classification:** Implemented in code; device/instrumentation execution pending (2026-08-12)
- **Severity:** High
- **Where:** `data/.../sync/SyncManager.kt` (approximately 2,451 lines after the current extraction slices), plus `SyncCoordinator`, the provider catalog executors, `ProviderContinuationScheduler`, `ProviderWorkflowRunner`, and the provider workers.
- **Current behavior:** Provider-neutral full/section/guide dispatch goes through `SyncCoordinator` and typed `CatalogSyncPlan` implementations. M3U, Jellyfin, Xtream section/full, Stalker section/full, and provider EPG execution have dedicated executors. Plans return validated activation receipts and the coordinator hands typed continuations to `ProviderContinuationScheduler`; provider executors no longer enqueue WorkManager directly. State transitions are guarded by `SyncStateTracker`/`SyncStateMachine`, and app callers now consume role-specific sync ports rather than `SyncManager` directly. `SyncManager` remains the composition/compatibility façade for lower-level provider helpers.
- **Extraction update (2026-08-12):** `StalkerIncrementalIndexExecutor` now owns the summary-category and wildcard page loops, parallel-fetch downgrade, retry/anomaly handling, page checkpoint progression, partial-result state selection, and catalog commit handoff. `StalkerIncrementalIndexOperationsAdapter` keeps that executor at one dependency while the remaining low-level recovery/checkpoint helpers are still exposed by the compatibility façade.
- **Current behavior correction (2026-08-12):** Xtream and Stalker incremental index orchestration, metadata/status publication, and state/progress session ownership now live in dedicated coordinators. `SyncManager` retains compatibility wrappers and remaining provider admission, compatibility conversion, error policy, and hydration entry-point responsibilities.
- **Why this is wrong/fragile:** Correctness depends on many implicit invariants across one class: which lock is held, which staging rows exist, which status is published, whether a worker was queued, and whether provider-specific partial success is safe to commit. Repeated `ProviderType` branches create divergent full-sync and repair behavior. Unit tests must mock a huge dependency graph and cannot isolate protocol policy from transaction policy.
- **Concrete failure scenario:** A fix to Stalker full sync commits a new staging invariant but `syncStalkerLiveCatalogStaged` or section repair uses the old invariant. A repair reports success and updates sync time while leaving a stale/partial catalog that the normal path would reject. Similar divergence can occur between Xtream initial onboarding and periodic index workers.
- **Recommended correction:** Extract a provider-neutral `SyncCoordinator`, per-provider `CatalogSyncPlan` implementations, section-level transactional importers, and a separate durable job scheduler. Each plan should return a typed result containing staged mutations, warnings, continuation work, and activation criteria. Centralize status transitions in one state machine.
- **Fix scope:** Architectural, high risk; refactor by seams and preserve fixtures before behavior changes.
- **Status extraction update (2026-08-12):** `SyncStatusPublicationCoordinator` now owns provider metadata/status writes, aggregate state exposure, and state/progress session fencing. The coordinator preserves partial-summary count/staleness semantics while `SyncManager` keeps only compatibility wrappers for existing callers.
- **Validation update (2026-08-12):** `XtreamIncrementalIndexExecutorTest` covers successful full-stream completion and stream-failure fallback into priority/cursor category slices. The full data suite now has 961 tests with zero failures/errors/skips; Xtream incremental orchestration is extracted, while metadata/status ownership, compatibility conversion, façade reduction, release recovery matrices, concurrency coverage, and device validation remain open.
- **Validation supersession (2026-08-12):** The full `:data:testDebugUnitTest` suite now passes with 964 tests and zero failures/errors/skips. Xtream/Stalker incremental orchestration and metadata/status publication are extracted; compatibility conversion, façade reduction, full-sync-vs-repair equivalence, persisted-state recovery matrices, provider activation coverage, concurrent manual/background scenarios, and device validation remain open.
- **Required tests:** State-machine transition tests; full-sync vs repair equivalence tests; cancellation before/during/after commit; process-death recovery from each persisted job state; concurrent manual/background sync serialization; partial-result activation rules for every provider.
- **Latest validation (2026-08-12):** After moving the Xtream index-rebuild metadata write into `SyncStatusPublicationCoordinator`, the complete `:data:testDebugUnitTest` suite passes with 965 tests and zero failures/errors/skips.

**Implementation evidence (2026-08-12, orchestration and lock-ownership slice):**

- `SyncCoordinator` resolves full sync, section repair, and guide work through four typed catalog plans. Production executors populate `SyncOutcome` with accepted staging mutations, warnings, typed continuation work, and a truthful post-execution activation receipt (`ACTIVATED_CATALOG`, `NO_CATALOG_CHANGE`, `PRESERVED_ACTIVE_CATALOG`, or `DEFERRED_TO_FOLLOW_UP`). Validation rejects impossible receipts before continuation handoff.
- `M3uCatalogSyncExecutor`, `JellyfinCatalogSyncExecutor`, `XtreamCatalogSyncExecutor`, `StalkerCatalogSyncExecutor`, the section executors, and `ProviderEpgSyncExecutor` now own provider-specific ingestion seams. `ProviderSyncWorkScheduler` owns WorkManager entry-point construction.
- `ProviderWorkLockRegistry` is the sole provider-wide execution lane. `ProviderSyncLockRegistry` owns only narrower category/summary/index locks; the duplicate `SyncManager` mutex maps and their lock-under-admission path were removed. This removes duplicated lock ownership and keeps cleanup ownership outside the manager.
- `CatalogIndexJobStore` now owns Xtream index-job patch/merge semantics, Stalker legacy-schema routing, priority clearing, and summary refresh eligibility. `SyncManager` keeps only the compatibility façade used by existing executors and workers.
- `VodCategoryHydrationCoordinator` now owns unified and split Stalker VOD paging, hydration checkpoints, page-budget/truncation handling, and the native-Series-vs-derived-Series commit guard. `StalkerIndexPolicy` owns section selection, retry timing, page continuation, anomaly detection, deduplication, and visible-category filtering.
- `StalkerIndexContinuationCoordinator` now owns persisted section-state evaluation, startup reconciliation, successor/retry scheduling, and independent EPG enqueueing. Work is serialized by the durable provider workflow; the coordinator does not itself prove the catalog is idle before enqueueing EPG.
- `SyncStatusPublicationCoordinator` now owns provider metadata/status publication, state/progress session fencing, aggregate state exposure, and partial-summary count/staleness policy. `SyncStatusPublicationCoordinatorTest` covers partial preservation, successful publication, and stale-session fencing.
- `XtreamIncrementalIndexExecutor` now owns full-stream indexing, category-slice fallback, priority-category ordering, cursor continuation, durable job updates, metadata publication, and follow-up scheduling. `XtreamIncrementalIndexOperationsAdapter` keeps that executor at one dependency while existing fetch/DAO helpers remain behind the compatibility façade.
- `StalkerIncrementalIndexExecutor` now owns the summary-category and wildcard page loops, parallel-fetch downgrade, retry/anomaly handling, page checkpoint progression, partial-result state selection, and catalog commit handoff. `StalkerIncrementalIndexOperationsAdapter` keeps the executor at one dependency while the remaining low-level recovery/checkpoint helpers are still exposed by the compatibility façade.
- Focused coordinator, activation-policy, state-machine, lock, cursor, recovery, and architecture tests pass. `CatalogSyncEquivalenceIntegrationTest` runs the production streaming M3U importer and Room DAOs to prove full import converges with separate Live/Movie repair activation. `ProviderWorkManagerSerializationTest` enqueues the real provider, Xtream-index, Stalker-index, and EPG worker APIs and verifies one provider-scoped WorkManager chain. These instrumentation tests compile; execution remains pending because no device or emulator is connected.

### ARCH-004 — Playback resolver has contradictory provider dispatch and hidden side effects

- **Classification:** Confirmed defect and architectural concern
- **Severity:** Medium
- **Where:** `data/.../remote/xtream/XtreamStreamUrlResolver.kt:45-227`, especially `resolveWithMetadata`; Stalker provider cache and learning helpers in the same class.
- **Current behavior:** A class named `XtreamStreamUrlResolver` resolves Xtream, Stalker, M3U, and Jellyfin. In its exhaustive provider `when`, `ProviderType.M3U` is handled at lines 217-224 and then listed again with Jellyfin at lines 225-226. Kotlin accepts this, and the forced data-module compile passed, but the second M3U label contradicts the preceding pass-through behavior. The resolver also decrypts provider state, constructs/caches a full `StalkerProvider`, authenticates/resolves a link, and persists learned Stalker playback behavior.
- **Why this is wrong/fragile:** A URL resolver is expected to be a mostly deterministic transformation. Here it is a provider session factory and state-mutating learning component. The duplicate branch is direct evidence that provider routing has become difficult to reason about. The cached provider configuration can also diverge from persisted edits unless every relevant field participates in cache replacement correctly.
- **Concrete failure scenario:** A future edit changes the combined M3U/Jellyfin branch believing it handles both, while runtime M3U continues taking the earlier branch. More seriously, a playback retry can mutate learned Stalker settings, and a later retry/provider edit observes a cached session built from a different configuration, producing inconsistent headers, cookies, or endpoint choice.
- **Recommended correction:** Immediately remove the duplicate M3U label and add exhaustive provider-routing tests. Rename/split this into a provider-neutral `PlaybackResolverRegistry` with independent Xtream, Stalker, M3U, and Jellyfin resolvers. Return explicit `PlaybackResolution` plus optional observations; persist observations in a separate coordinator with compare/version semantics rather than inside resolution.
- **Fix scope:** Duplicate-label fix is local; side-effect/cache correction is architectural.
- **Required tests:** One routing test per provider for internal and direct URLs; blank/missing provider cases; Stalker cache invalidation after every configuration field changes; concurrent resolve/edit; temporary-link expiry/renewal; observation persistence conflict tests.

### ARCH-005 — `PlayerViewModel` crosses every layer and is a feature-level god object

- **Classification:** Architectural concern
- **Severity:** High
- **Where:** `app/.../ui/screens/player/PlayerViewModel.kt:88-118` and the many `Player*Actions.kt`/`Player*Support.kt` extensions; direct imports of `SyncManager`, `StalkerUrlFactory`, `XtreamStreamUrlResolver`, `PreferencesRepository`, `OkHttpClient`, and concrete Media3 types.
- **Current behavior:** The ViewModel has more than 25 constructor dependencies spanning repositories, recording, TV recommendations, cast, plugins, URL resolution, synchronization, raw HTTP, preferences, and playback. Its behavior is distributed across dozens of extension files that share `internal` mutable state. It coordinates live/VOD/series resolution, EPG, history, autoplay, token renewal, alternate streams, recovery, timeshift, cast, recording, downloads, plugins, diagnostics, and UI notices.
- **Why this is wrong/fragile:** Splitting methods into files reduces file size but does not create ownership boundaries; all extensions still operate on one mutable object. UI lifetime becomes the owner of network/session policy and background sync. Testing one playback behavior requires a broad fixture and can inadvertently exercise unrelated collectors or timers. Concrete data imports bypass domain contracts and prevent isolated player-feature tests.
- **Concrete failure scenario:** Channel zapping cancels/replaces a preparation job while a token-renewal, EPG refresh, alternate-stream recovery, or plugin callback from the prior item completes and mutates shared current state. The code may pass unit tests for each helper but fail under the combined event ordering.
- **Recommended correction:** Introduce a `PlaybackSessionCoordinator` with an immutable session ID/state machine and scoped child jobs. Separate content resolution, provider playback resolution, player control/recovery, guide timeline, history, cast, and recording into explicit collaborators. ViewModel should translate coordinator state to UI and dispatch user intents. Depend on domain/app contracts rather than `data` concrete classes.
- **Fix scope:** Architectural; migrate one session concern at a time.
- **Required tests:** Stale-event/session-ID tests; rapid zap and content-switch tests; cancellation at every preparation stage; token renewal racing stop/release; recovery/cast/recording interactions; ViewModel lifecycle destruction while work is active.

### ARCH-006 — Plugin discovery blocks callers and plugins are not first-class providers

- **Classification:** Highly likely defect
- **Severity:** High
- **Where:** `app/.../plugins/StreamVaultPluginManager.kt`, especially `resolvePlugin`, `readManifestFromService`, `refreshTvInputCatalogInBackground`, and provider mapping/import methods.
- **Current behavior:** Discovery resolves each service and uses `runBlocking(Dispatchers.IO)` for status and manifest Messenger calls with 2.5s and 3s timeouts. These calls are sequential per plugin and are invoked from non-suspending discovery helpers. Background TV refresh creates an unowned `CoroutineScope(Dispatchers.IO)`. Plugin catalog identity is persisted as a mapping to an M3U provider, so plugin lifecycle and provider lifecycle are coupled indirectly by names/IDs and preferences.
- **Why this is wrong/fragile:** `runBlocking` blocks the calling thread even though work runs on IO; if discovery runs from the main thread, one unresponsive plugin can freeze UI for up to 5.5 seconds, multiplied by installed plugins. The unowned refresh scope cannot be cancelled or observed. Treating a plugin as M3U erases its runtime/capability boundary and risks orphaned or colliding provider records.
- **Concrete failure scenario:** Three installed but unresponsive plugins make opening the plugin screen block for roughly 16.5 seconds. The user disables/uninstalls a plugin while its fire-and-forget TV refresh or catalog import is running, leaving an enabled mapping or M3U catalog that no longer has a source service.
- **Recommended correction:** Make discovery/status loading suspending and concurrent with a bounded global timeout, expose per-plugin loading/error state, and run it in a lifecycle-owned scope. Model plugins as a provider/source capability with stable package/service/manifest identity and explicit uninstall/disable cleanup. Replace the ad-hoc scope with injected application work coordination or WorkManager if durable.
- **Fix scope:** Local for blocking calls/scope; architectural for provider modeling.
- **Required tests:** Main-thread discovery test; multiple slow/dead plugin services; timeout/cancellation; uninstall during import; duplicate manifest IDs; mapping cleanup and TV catalog consistency; process death during catalog import.

### ARCH-007 — Database definition and migration history are too concentrated to audit safely

- **Classification:** Architectural concern
- **Severity:** Medium
- **Where:** `data/.../local/StreamVaultDatabase.kt` and generated `data/schemas`.
- **Current behavior:** One multi-thousand-line Room database class declares a large entity set and a long sequence of handwritten migrations. The forced compile emitted dozens of migration override warnings and two warnings about inferred intersection types in migration code, in addition to schema-related warning noise.
- **Why this is wrong/fragile:** Schema declaration, migration SQL, data backfills, and compatibility helpers in one file make it difficult to review whether every historical upgrade path preserves invariants. Warning volume hides newly introduced warnings. Large provider-union entities and feature tables amplify migration coupling.
- **Concrete failure scenario:** An upgrade from an older production version takes a different migration chain than tests normally cover; a backfill infers/coerces the wrong type or omits a newly required provider state column, producing a database that opens but later fails catalog/playback logic.
- **Recommended correction:** Split migrations into versioned files grouped by feature, introduce reusable migration assertions/backfill helpers, and maintain an explicit schema-invariant checklist. Treat warnings as errors after cleaning the current baseline. Keep Room schema exports under version control and test every supported historical start version.
- **Fix scope:** Architectural organization; individual warning fixes are local.
- **Required tests:** Room MigrationTestHelper coverage from every shipped schema to current; multi-hop and direct-chain equivalence; data-preservation fixtures for every provider, EPG assignment, recording, history, and plugin mapping; downgrade/restore incompatibility messaging.

### ARCH-008 — Background work is fragmented across independently scheduled pipelines

- **Classification:** Implementation complete; device integration validation pending
- **Severity:** Medium
- **Implementation status (2026-08-09):** Complete in code; device integration validation pending. The implementation provides one provider work identity, one in-process provider execution lane, a central startup-work registry, and a durable Room-backed workflow/phase coordinator with generation-fenced catalog and status commits. Configuration revisions explicitly supersede older work; ordinary sync, index, and EPG requests join the provider chain. Stale revision input after provider deletion/supersession is rejected before a workflow row can be created.
- **Where:** `StreamVaultApp.onCreate:74-97`; `ProviderSyncWorker`; `XtreamIndexWorker`; `StalkerIndexWorker`; `BackgroundEpgSyncWorker`; `RecordingReconcileWorker`; scheduling methods inside `SyncManager`.
- **Current behavior:** Every process start enqueues maintenance, provider stale-check, Xtream index stale-check, and recording reconciliation, while periodic equivalents also exist. Provider sync can queue provider-specific index and EPG workers. Each class has separate unique names, `KEEP`/`REPLACE`/`APPEND_OR_REPLACE` policy, retry classification, delays, and persisted job metadata. In-process provider mutexes serialize some `SyncManager` calls but do not by themselves define a durable cross-worker state machine.
- **Why this is wrong/fragile:** WorkManager uniqueness prevents duplicates only within an identical unique name. Different worker types can target the same provider and network/database concurrently. Status is distributed across WorkManager, Room job tables, provider status, sync metadata, and in-memory `SyncStateTracker`, so cancellation/process death may leave contradictory state.
- **Concrete failure scenario:** Shortly after launch, `ProviderSyncWorker` refreshes an Xtream provider while `XtreamIndexWorker` processes queued categories and `BackgroundEpgSyncWorker` writes guide data. A manual refresh replaces one unique work chain but not the others; UI sees success from one state source while another worker later marks the provider partial or overwrites staging data.
- **Recommended correction:** Define a durable per-provider work coordinator/state machine with named phases and one serialized chain. Worker entry points should claim persisted leases/epochs and reject stale work. Manual refresh should explicitly supersede or join background work. Consolidate retry/error/status publication rules.
- **Fix scope:** Architectural.
- **Required tests:** Simultaneous launch workers; manual refresh during each worker phase; process death and restart; stale WorkManager input after provider edit/delete; network loss and retry across phases; verify one authoritative user-visible status.

**Implementation evidence (2026-07-30, slice 1):**

- `StartupWorkRegistry` is now the sole durable-work registration entry point called by `StreamVaultApp`; data maintenance uses periodic `UPDATE` so changed constraints are applied without creating a second schedule.
- Provider resume, Xtream index, Stalker index, background EPG, and provider-configuration recovery now resolve to one `provider-workflow-{providerId}` unique chain. Ordinary phases use `APPEND_OR_REPLACE`; configuration recovery uses `REPLACE` as an explicit superseding operation.
- `ProviderWorkLockRegistry` replaces the separate full-sync, Stalker-summary, and Stalker-EPG mutex ownership paths. All `SyncManager` provider operations now share one per-provider execution lane while different providers can still execute concurrently.
- This slice closes same-process overlap and centralizes scheduling policy.

**Implementation evidence (2026-07-30, slice 2):**

- Room schema version 71 adds `provider_workflows`, the authoritative current generation/state/lease row for each provider, plus `provider_workflow_phases`, a per-generation phase ledger for prepare, catalog, index, EPG, and finalize work.
- `ProviderWorkflowDao.request` gives ordinary startup/periodic/manual work explicit join semantics and gives configuration changes explicit supersede semantics. Supersession atomically increments the generation, clears the prior lease, and marks unfinished older phases `SUPERSEDED`.
- Lease claim, renewal, completion, and failure all require provider ID, generation, phase, and an opaque lease token. A worker from an older generation therefore cannot renew or publish completion after a provider edit supersedes it.
- Lease expiry and heartbeat recovery handle expired, missing, stale, invalid, and future-clock state. Retryable failures preserve phase checkpoints and return the phase to the durable pending workflow.
- `ProviderWorkflowDaoTest` covers concurrent claim exclusion, join/phase serialization, generation fencing, retry, and restart candidate selection. `MIGRATION_70_71` and its `MigrationTestHelper` case validate both new tables, indices, and cascade ownership for upgraded installations.

**Implementation evidence (2026-07-30, slice 3):**

- `ProviderWorkflowRunner` is now the common execution boundary for full provider sync, configuration-revision recovery, Xtream indexing, Stalker indexing, and background EPG. It claims an opaque generation-bound lease before entering `SyncManager`, renews that lease during long calls, and publishes success, partial, retryable failure, or permanent failure through the phase ledger.
- `ProviderWorkFailureClassifier` consolidates the duplicated network/SQLite retry rule used by those workers. Worker result mapping now follows the durable disposition: busy or retryable work retries, permanent failure fails, and a superseded generation exits without overwriting the newer workflow result.
- A restarted request can atomically reclaim a `RUNNING` phase only when its lease is expired, missing, stale, or has a future-clock heartbeat. A live lease is left untouched. This closes the process-death loop for WorkManager's persisted retries without requiring an in-memory owner to release its mutex.
- `ProviderWorkflowRunnerTest` covers durable success and retry publication, overlapping-owner admission, and supersession during execution. `ProviderWorkflowDaoTest` additionally covers stale-process reclaim while proving that a live owner is not disturbed.

**Implementation evidence (2026-07-30, slice 4):**

- `ProviderRepositoryImpl.refreshProviderData` now executes through `ProviderWorkflowRunner`. A forced manual refresh explicitly supersedes background work at priority 50; a non-forced refresh joins the current generation and reports a busy workflow instead of starting a second sync.
- Workflow admission is priority-aware inside the Room transaction. Configuration changes (priority 100) cannot be displaced by manual refresh (50), and manual work cannot be displaced by periodic/startup work (0). Rejected lower-priority requests do not mutate the generation, reason, or phase ledger.
- `ProviderWorkflowExecutionContext` carries the claimed generation/token through the sync coroutine. Every `SyncCatalogStore` apply path now checks that token inside the same Room transaction that applies staged categories/channels/movies/series. Supersession and catalog commit are therefore serialized: whichever transaction commits first determines whether the old apply is accepted or rejected.
- `ProviderWorkflowCommitFenceTest` proves that a superseded generation cannot replace committed catalog rows and that the current owner can apply normally. Manual-refresh tests verify explicit join versus supersede arguments, and DAO tests cover configuration-over-manual priority inversion.
- `ProviderSyncStateReaderImpl` treats the durable workflow as authoritative whenever a workflow row exists; the legacy Xtream job table remains only as a migration fallback for providers without a workflow row. This prevents stale legacy rows from resurrecting a completed-work spinner after process recreation.
- Provider sync timestamps, sync metadata, discovered M3U EPG URLs, manual status reconciliation, and targeted-worker status reconciliation are now checked inside generation-owned transactions before publication. Stale or deleted configuration-revision input is rejected before workflow creation. ARCH-008 remains open only for device-backed process-kill and manual-refresh-during-each-phase integration tests.

**Implementation evidence (2026-07-30, slice 5):**

- `ProviderWorkflowIntegrationTest` adds file-backed Room instrumentation coverage for the remaining coordinator boundaries. It closes and reopens the database to model process recreation, then proves that an expired durable phase lease can be reclaimed without relying on in-memory ownership.
- The integration matrix forces a manual refresh while each background phase owns the workflow (`PRIMARY_CATALOG`, `CONTENT_INDEX`, `MOVIE_INDEX`, `SERIES_INDEX`, `EPG`, and `FINALIZE`). In every case the old generation loses commit authority and the manual generation can claim the phase. A separate case proves that manual priority cannot supersede an active configuration migration.
- The data module now declares `AndroidJUnitRunner`; both the new integration suite and the 70-to-71 migration test compile and package successfully with `:data:assembleDebugAndroidTest`.
- A targeted `:data:connectedDebugAndroidTest` run was attempted on 2026-07-30, but ADB reported no attached device and Gradle stopped with `DeviceException: No connected devices!`. These cases are implemented but must still be executed on an Android device/emulator; this is why the finding remains **Implemented; device integration validation pending**.
- After device execution, the remaining architectural cleanup is to remove or explicitly demote redundant legacy status stores once UI compatibility has been verified.

## Phase 1 open questions

1. Which historical app/database versions are actually in the supported upgrade window? Phase 4 migration coverage depends on this.
2. Is Jellyfin intended as a permanent first-class provider? It appears throughout setup/catalog/playback but is omitted from the requested provider-review list and the shared provider contract.
3. Are plugins intended to supply only M3U-compatible catalogs, or arbitrary provider operations and playback resolution? The current Messenger contract and M3U mapping suggest both models.
4. Which Stalker fields are user configuration versus learned observations, and which must survive edits/backups? The current union record does not make this distinction explicit.
5. What is the intended source of truth for a sync's visible state after process death: provider status, sync metadata, job tables, WorkManager, or `SyncStateTracker`?
6. Should provider sync and index work be allowed during playback/recording on low-powered TV devices, or should a resource coordinator throttle them?
7. Is the app expected to support multiple Jellyfin providers? `JellyfinImageAuthInterceptor` performs provider lookup by type, which may imply a singleton assumption requiring Phase 3 verification.
8. Are direct M3U/Jellyfin playback URLs expected to be renewable, or only Xtream/Stalker URLs? Expiring signed M3U/Jellyfin URLs need an owner-specific renewal contract.

## Next review targets

Phase 2 should review these files/modules first, in this order:

1. Networking profiles, clients, limits, cookies, timeouts, retries, and logging:
   - `app/.../di/NetworkModule.kt`
   - `data/.../remote/http/*`
   - `OkHttpXtreamApiService.kt`
   - `OkHttpStalkerApiService.kt`
   - `JellyfinProvider.kt`
2. Coroutine/lifecycle ownership and concurrency:
   - `SyncManager.kt`, `SyncStateTracker.kt`, all sync workers
   - repository-owned scopes in movie/series/provider/history/EPG repositories
   - `Media3PlayerEngine.kt`, `LiveTimeshiftManager.kt`
   - plugin, recording, receiver, and application scopes
3. Persistence and transaction boundaries:
   - `StreamVaultDatabase.kt`, all migrations and schema exports
   - `SyncCatalogStore.kt`, staging/index DAOs, provider deletion
   - preference/DataStore and plugin preference stores
4. Shared playback infrastructure:
   - `PlayerEngine`, `Media3PlayerEngine`, data source factories, retry/recovery, URL renewal, session release
   - `XtreamStreamUrlResolver` (to be renamed/split later)
5. Error/result/logging semantics and observability across repositories, workers, player, recording, and plugins.

# Phase 2 — Shared infrastructure review

**Status: Complete.** This phase reviewed shared networking, coroutine/cancellation ownership, WorkManager policy, catalog staging, Room/DataStore use, playback/recording lifecycle, cache bounds, application update checks, and error/result propagation. Provider-specific protocol behavior remains for Phase 3; end-to-end feature behavior and migrations remain for Phase 4.

## Phase 2 findings summary

| ID | Finding | Classification | Severity |
|---|---|---|---|
| INFRA-001 | Cancellation is broadly converted into normal failure/retry results | Resolved; five-worker adverse-case matrix and no-post-cancellation publication checks pass (2026-08-09) | High |
| NET-001 | Suspend call paths use blocking, non-cancellable OkHttp `execute()` | Resolved; MockWebServer never-header/first-byte/mid-body matrix passes (2026-08-09) | High |
| SYNC-001 | M3U staging has no effective response or catalog-size bound | Resolved (2026-07-26) | High |
| SYNC-002 | A global progress bus corrupts concurrent per-provider sync progress | Resolved (2026-08-01) | High |
| LIFE-001 | Every player engine permanently registers a timeshift component callback | Implemented; lifecycle stress verification pending (2026-07-27) | High |
| LIFE-002 | Player reset launches unowned cleanup that can stop a new timeshift session | Implemented; lifecycle stress verification pending (2026-07-27) | High |
| REC-001 | Recording stop/cancel neither cancels the HTTP call nor joins the writer | Implemented; capture matrix verification pending (2026-07-27) | High |
| PERF-001 | Playback snapshots synchronously rewrite a file every second on main | Resolved (2026-07-26) | High |
| PERF-002 | VOD progress triggers DB and Android TV rewrites every five seconds | Resolved (2026-07-26) | High |
| WORK-001 | Local database maintenance is incorrectly gated on connectivity | Resolved (2026-08-01) | Medium |
| WORK-002 | Recording reconciliation retries every error without classification/cap | Resolved (2026-07-29) | Medium |
| UPDATE-001 | Failed automatic update checks suppress another attempt for 24 hours | Resolved (2026-07-29) | Medium |
| MEMORY-001 | Process-lifetime maps grow with provider/category/host/media identities | Architectural concern | Medium |
| PERSIST-001 | The central preferences DataStore has no explicit corruption recovery | Implemented; boundary verification pending (2026-07-30) | Medium |
| TEST-001 | The player suite asserts the opposite MPEG-TS extractor policy from production | Resolved (2026-07-26) | Medium |

The urgent shared fixes are cancellation-safe HTTP, recording/timeshift lifecycle ownership, M3U admission limits, and removal of main-thread/per-five-second persistence churn. These can cause work after cancellation, contradictory terminal state, retained resources, storage exhaustion, and playback jank independently of provider protocol correctness.

## Detailed Phase 2 findings

### INFRA-001 — Cancellation is broadly converted into normal failure/retry results

- **Classification:** Resolved (2026-08-09)
- **Severity:** High
- **Where:** `data/.../sync/{SyncWorker,BackgroundEpgSyncWorker,ProviderSyncWorker,StalkerIndexWorker,XtreamIndexWorker}.kt`; representative paths in `EpgSourceRepositoryImpl.kt:218-420`, `DownloadManagerImpl.kt:110-203`, `RecordingManagerImpl.kt:134-473`, and `SyncManager.kt:1423`. Static inventory found 289 `runCatching` sites and 234 broad `Exception`/`Throwable` catches.
- **Current behavior:** All five coroutine workers end in `catch (Exception)` and translate the throwable to WorkManager failure/retry without first rethrowing `CancellationException`. Many suspending repository/manager operations use Kotlin `runCatching` or broad catches. EPG refresh can turn cancellation into persisted failure and continue cleanup/status work. `SyncManager.sync:712-714` correctly rethrows cancellation, but this is not consistent.
- **Why this is wrong/fragile:** Cancellation is control flow. Swallowing it lets superseded UI work and stopped workers publish errors, schedule retries, perform cleanup, or commit state after their owner has gone away. Standard `runCatching` catches `CancellationException`.
- **Concrete failure scenario:** WorkManager stops a provider worker because constraints change. The worker reports retry/failure and may write error state even though it was deliberately stopped. Switching provider can similarly let a cancelled old refresh mark its source failed after the new one is active.
- **Recommended correction:** Add a shared `runSuspendCatching`/`catchNonCancellation` helper that immediately rethrows cancellation; put an explicit cancellation catch before every broad worker/repository catch; prohibit raw `runCatching` around suspend calls with Detekt/review policy. Translate to domain `Result` only at an explicit owner boundary.
- **Fix scope:** Cross-cutting but mechanical; start with workers, sync, recording, and EPG.
- **Required tests:** Cancel every worker mid-call and assert no retry/failure or error metadata; cancel imports at parse/transaction boundaries; cancel download/recording/backup; prove child cancellation propagates while genuine `IOException` remains retryable.
- **Resolution (2026-08-09):** Shared `runSuspendCatching`/`rethrowIfCancellation` helpers protect the WP1 recording, EPG, download, backup, and sync boundaries touched in this package. All five workers catch and rethrow cancellation before failure/retry classification. `Wp1CancellationPolicyTest`, the provider-worker phase matrix in `ProviderWorkflowRunnerTest`, and `SyncWorkerPolicyTest` cover cancellation and no post-cancellation failure/status/snapshot publication. Genuine I/O remains retryable through the existing workflow classifier.

### NET-001 — Suspend call paths use blocking, non-cancellable OkHttp `execute()`

- **Classification:** Resolved (2026-08-09)
- **Severity:** High
- **Where:** `SyncManagerM3uImporter.kt:236-267`; `EpgSourceRepositoryImpl.kt:256`; `EpgRepositoryImpl.kt:288`; `OkHttpStalkerApiService.kt:975,1082,1221`; `JellyfinProvider.kt:305-308`; `RecordingCaptureEngine.kt:80,238,248`; Google Drive/update/download/resolver paths. A correct reference bridge exists at `OkHttpXtreamApiService.executeCancellable:377-392`.
- **Current behavior:** Suspending functions move to `Dispatchers.IO` and call synchronous `Call.execute()`. Coroutine cancellation does not call `Call.cancel()`; the thread remains blocked until data, timeout, or server closure. EPG reads are long and recording streams can remain open indefinitely.
- **Why this is wrong/fragile:** `withContext(IO)` prevents main-thread blocking but does not make I/O cancellation-aware. It consumes threads/sockets and can continue into parsing/persistence after cancellation. INFRA-001 compounds the damage.
- **Concrete failure scenario:** The user replaces an M3U provider while a server sends headers then stalls. The obsolete import remains blocked, later resumes, and stages rows for superseded work. A stopped recording can remain in `read()` and retain its output.
- **Recommended correction:** Standardize an OkHttp coroutine adapter using `suspendCancellableCoroutine`, `enqueue`, and `invokeOnCancellation { call.cancel() }`. Streaming operations need a cancellable handle that closes body and call. Apply operation-specific deadlines.
- **Fix scope:** Shared adapter plus local migrations.
- **Required tests:** MockWebServer never-headers, never-first-byte, and mid-body stalls; cancellation must promptly cancel the server call, close the body, avoid persistence, and release the dispatcher thread. Retain status/timeout/body-limit coverage.
- **Resolution (2026-08-09):** `Call.useCancellableResponse` now owns the response for the full consumer lifetime, not just until headers arrive. It asynchronously enqueues the call, cancels it before headers and during body reads, converts cancellation-triggered read failures back to coroutine cancellation, and closes the response/body on every exit. M3U, EPG, Jellyfin, recording capture/source probing, downloads, Drive backup, Stalker, and plugin APK download paths use the owned adapter. `CancellableOkHttpTest` proves pre-header, never-first-byte, and mid-body MockWebServer cancellation, dispatcher release, blocked-body closure, and closure when parsing throws, all under five-second bounds.

### SYNC-001 — M3U staging has no effective response or catalog-size bound

- **Classification:** Resolved (2026-07-29)
- **Severity:** High
- **Where:** `SyncManagerM3uImporter.kt:60-220,236-267,314-320`; `SyncCatalogStore.kt:331-379`; compare `NetworkTimeoutConfig` and `CatalogSizeLimits` used by non-staging paths.
- **Current behavior:** The importer does not bound `Content-Length`, decompressed bytes/lines, entries, or categories. It retains unbounded live/movie identity sets and category accumulators. M3U uses `stageChannelBatch`/`stageMovieBatch` then `finalizeStagedImport`, bypassing catalog limits applied by other store methods.
- **Why this is wrong/fragile:** Streaming avoids retaining every object, but identity/category sets and Room staging still scale with the complete input. An accidental giant playlist, endless chunked body, or decompression expansion can exhaust heap, disk, and transaction time.
- **Concrete failure scenario:** A provider returns millions of chunked entries without declared size. The app stages beyond the nominal 100,000-channel limit, grows hash sets, fills storage, and may leave a huge staging epoch after process death.
- **Recommended correction:** Enforce decompressed bytes, lines, per-type entries, categories, field length, and parse-error ratio. Put count invariants in staging/database admission so every caller shares them; prefer database uniqueness to full-catalog identity sets. Abort with a typed oversize result and atomically clear staging.
- **Fix scope:** Architectural at catalog admission; parser counters are local.
- **Required tests:** Chunked/no length, compressed expansion, every catalog maximum, duplicate-heavy input, overlong lines, cancellation/space exhaustion, and preservation of the active catalog.
- **Resolution:** M3U imports now enforce declared response size, decompressed bytes, raw-line length, total entries, per-type entries, categories, field length, and invalid-entry ratio through `CatalogSizeLimits`. The importer raises `CatalogAdmissionExceeded` before finalization; its existing `finally` clears the staged session, preserving the active catalog. `SyncCatalogStore` independently rejects channel/movie batches that would exceed the durable stage limit, so non-M3U callers cannot bypass it. `M3uParser` reports malformed candidates to support ratio enforcement; its focused unit suite passes.
- **2026-08-01 hardening:** Field admission now covers every persisted M3U/header string, and malformed candidates replaced by another `#EXTINF` or left pending at EOF contribute to the invalid ratio. The ratio is evaluated after both valid and invalid candidates, closing the malformed-prefix threshold bypass. Channel/movie count-and-insert admission and per-type category admission now run inside the shared Room transaction boundary. Provider/session cleanup is atomic, and session cleanup runs in a non-cancellable context so cancellation cannot skip staging disposal.
- **Verification:** `SyncManagerM3uImporterTest` covers unknown-length exact-byte admission, declared length, gzip expansion, raw-line and metadata-field bounds, total/live/movie/category limits, duplicate-heavy input, invalid-ratio threshold ordering, storage failure, cancellation, no catalog apply on rejection, and staging disposal. Parser and catalog-store tests cover replaced/unterminated candidates, transactional admission, category limits, and atomic cleanup. The complete `:data:testDebugUnitTest` suite passed on 2026-08-01: 722 tests, zero failures, errors, or skips.

### SYNC-002 — A global progress bus corrupts concurrent per-provider sync progress

- **Classification:** Resolved (2026-08-01)
- **Severity:** High
- **Where:** `SyncProgressBus.kt:21-36`; `SyncManager.withProviderLock:296-306`; `SyncManager.sync:659-732` and `retrySection:845-898`; consumers such as `WelcomeViewModel`.
- **Current behavior:** Different providers have different mutexes and can sync concurrently. State is provider-scoped, but `SyncProgressBus` contains one unkeyed `StateFlow<SyncProgress?>`. Every sync emits into it and every sync's `finally` resets the same value.
- **Why this is wrong/fragile:** Independent events interleave; completion/cancellation of one provider clears progress for another still working. A late event can be attributed to the wrong screen/session.
- **Concrete failure scenario:** Providers A and B sync together. B publishes 70%, A finishes and resets the bus, and B's UI returns to “no sync” until another emission; labels and counters alternate between providers.
- **Recommended correction:** Key progress by provider ID and monotonic sync epoch, or return a per-session flow. Reset only the matching epoch; derive aggregate progress separately.
- **Fix scope:** Shared sync API and consumers.
- **Required tests:** Two interleaved providers; one completes/cancels without clearing the other; stale epoch after replacement; concurrent manual/background entry points.
- **Resolution:** `SyncProgressBus` now owns an active session per provider, identified by a monotonic epoch. It exposes keyed provider progress and a separately derived aggregate; only the matching session can publish or finish. `SyncManager` opens and closes that session for full syncs, section retries, and Xtream index rebuilds, while M3U and Xtream emitters report through the provider-scoped bridge. Focused bus tests cover two interleaved providers, independent completion and cancellation, stale-session replacement with increasing epochs, aggregate derivation, and genuinely concurrent manual/background lifecycles.
- **Verification:** The complete data-module suite passed on 2026-08-01 with 778 tests and no failures, errors, or skips. App test compilation and `DataMaintenanceSchedulingTest` also passed after removing the ambiguous defaulted injectable constructor from `ProviderWorkflowCommitFence`.

### LIFE-001 — Every player engine permanently registers a timeshift component callback

- **Classification:** Implemented; lifecycle stress verification pending (2026-07-27)
- **Severity:** High
- **Where:** `DefaultLiveTimeshiftManager` in `LiveTimeshiftManager.kt:74-91`; construction at `Media3PlayerEngine.kt:272`; release/reset at `Media3PlayerEngine.kt:847-914`; auxiliary engines in multiview/home/EPG preview.
- **Current behavior:** Each manager calls `context.registerComponentCallbacks(this)` in its initializer. There is no matching unregister or terminal manager close. Engine release resets player state but does not release the manager/scope.
- **Why this is wrong/fragile:** The application context retains every manager ever created, including scope, state, and client references. Auxiliary-engine churn becomes a process-lifetime leak.
- **Concrete failure scenario:** Hundreds of preview/multiview sessions leave hundreds of managers receiving memory callbacks until process death.
- **Recommended correction:** Add idempotent terminal `close()` to stop the session, cancel calls/scope, clear state, and unregister. Invoke it from terminal engine release while keeping reusable reset distinct.
- **Fix scope:** Local lifecycle API with all factories audited.
- **Required tests:** Registration count returns to baseline after hundreds of cycles; double release; active HLS/DASH/TS release; reusable reset retains exactly one callback.
- **Implementation (2026-07-27):** Terminal release now unregisters the component callback synchronously and idempotently, then performs joined session close in the engine-owned terminal cleanup scope. Reusable reset does not unregister. This remains unresolved until callback registration returns to baseline across the required repeated create/release and active-format stress matrix.

### LIFE-002 — Player reset launches unowned cleanup that can stop a new timeshift session

- **Classification:** Implemented; lifecycle stress verification pending (2026-07-27)
- **Severity:** High
- **Where:** `Media3PlayerEngine.resetEngineState:861-914`, particularly scope recreation at `907-910` and anonymous `CoroutineScope(Dispatchers.IO).launch` at `913`.
- **Current behavior:** Reset cancels/recreates the engine scope, then launches `stopSession()` in an unrelated IO scope. Cleanup cannot be joined and has no generation check.
- **Why this is wrong/fragile:** Reuse can start a new session before old cleanup gets the manager mutex; the old cleanup can then stop the new session. Release returns before resources close and failures are unobserved.
- **Concrete failure scenario:** Channel A resets, channel B starts timeshift, and delayed A cleanup stops B or removes B's local manifest.
- **Recommended correction:** Serialize reset/start under a session generation, or make reset/release awaitable and join cleanup. At minimum retain an owned cleanup job that start must await. Never use anonymous scopes for object lifecycle.
- **Fix scope:** Player/timeshift lifecycle contract.
- **Required tests:** Immediate restart after reset, delayed stop, release in every capture state, repeated reset, cleanup failure, and stale generation isolation.
- **Implementation (2026-07-27):** Reset cleanup is now an engine-owned serialized job chain; every new timeshift start joins the latest chain before creating a session, and cleanup jobs are no longer cancelled/overwritten. `TimeshiftOwnershipTest` proves joined writer cleanup precedes deletion. The finding remains unresolved until delayed-cleanup restart and repeated reset/release integration tests pass.

### REC-001 — Recording stop/cancel neither cancels the HTTP call nor joins the writer

- **Classification:** Implemented; capture matrix verification pending (2026-07-27)
- **Severity:** High
- **Where:** `RecordingCaptureEngine.kt:74-109,234-251`; `RecordingManagerImpl.startCapture:564-585`, `stopRecording:195-213`, `cancelRecording:216-249`, `cancelActiveJob:1080-1082`.
- **Current behavior:** Capture creates local synchronous OkHttp calls. The manager tracks only a coroutine `Job`; stop/cancel calls `job.cancel()` without `join`, immediately marks terminal state, and returns. A blocked body read cannot observe job cancellation.
- **Why this is wrong/fragile:** UI/DB terminal state can precede writer termination. A “finished” file may remain open/growing, and delete/backup/replace can race it. Stalled reads also defeat coroutine stall checks.
- **Concrete failure scenario:** A TS body stalls. The user cancels then deletes. Metadata/output are removed while the capture still owns the stream and may later write/finalize.
- **Recommended correction:** Track an `ActiveCapture` owning job, calls, responses, and output. Cancel/close calls then `cancelAndJoin` before terminal state and destructive operations. Use bounded segment/read deadlines and one idempotent finalizer.
- **Fix scope:** Recording capture/manager contract.
- **Required tests:** Never-returning/mid-body stalls, stop/cancel/delete blocked TS and HLS, SAF/file outputs, service stop, and no write after terminal state.
- **Implementation (2026-07-27):** `RecordingManagerImpl` now registers a lazy `ActiveCapture` before starting its job. Stop, cancellation, deletion, and foreground-service timeout cancel and join that owner before terminal persistence or destructive output work; capture cancellation is rethrown. `ActiveCaptureTest` proves repeated cancellation waits for non-cancellable resource finalization. The finding remains unresolved until the complete TS/HLS and file/SAF adverse matrix passes.

### PERF-001 — Playback snapshots synchronously rewrite a file every second on main

- **Classification:** Confirmed defect
- **Severity:** High
- **Where:** `Media3PlayerEngine.kt:164-170,341-404`; `PlaybackSupportSnapshotStore.kt:13-21`.
- **Current behavior:** Engine collectors run on `Dispatchers.Main.immediate`; each second they call a store whose `write()` synchronously executes `File.writeText`. Failures are silently swallowed.
- **Why this is wrong/fragile:** Continuous flash I/O on the UI dispatcher creates frame-time variance and storage churn during playback, and silent failure undermines the diagnostic.
- **Concrete failure scenario:** Slow TV storage makes each rewrite coincide with controls/guide rendering, causing recurring dropped frames or input lag.
- **Recommended correction:** Send state to a single IO actor, coalesce, write only on material transitions or a much lower cadence, use atomic replace, and record rate-limited failures.
- **Fix scope:** Local.
- **Required tests:** Injected dispatcher/writer proves no main I/O; stable-playback cadence cap; coalescing; atomicity; unavailable-storage observability.

### PERF-002 — VOD progress triggers DB and Android TV rewrites every five seconds

- **Classification:** Confirmed defect
- **Severity:** High
- **Where:** `PlayerLifecycleActions.kt:12-38`; `PlaybackHistoryRepositoryImpl.kt:42-55,185-214,266-280`; `WatchNextManager.kt:33-77`; `LauncherRecommendationsManager.kt:57-103`.
- **Current behavior:** Every five seconds non-live playback performs a Room transaction/denormalized update, buffers the same row, refreshes Watch Next, and requests launcher recommendations. Watch Next scans platform entries and updates/inserts up to 40 rows every call. The 30-second “batch” flush writes the history again. Recommendations throttle themselves; Room and Watch Next do not.
- **Why this is wrong/fragile:** The buffer duplicates instead of batches. Binder/launcher/Room/flash work on this cadence competes directly with playback and background sync.
- **Concrete failure scenario:** A two-hour film causes about 1,440 history transactions plus denormalized updates, roughly 1,440 Watch Next scans/rewrites, and duplicate 30-second flushes.
- **Recommended correction:** Keep progress in memory, debounce durable writes (for example 30-60 seconds), force flush on background/stop/content switch, and remove persisted pending entries. Refresh Watch Next only on meaningful thresholds/lifecycle events and update one affected row when possible.
- **Fix scope:** Shared persistence policy plus TV integration optimization.
- **Required tests:** Virtual two-hour playback with DAO/ContentResolver upper bounds; forced lifecycle flush; crash tolerance; incognito; watched transition; provider switch; no duplicate flush.

### WORK-001 — Local database maintenance is incorrectly gated on connectivity

- **Classification:** Resolved (2026-07-26)
- **Severity:** Medium
- **Where:** `StreamVaultApp.kt:74-90`; `SyncWorker.kt:27-64`; `DatabaseMaintenanceManager.kt:32-104`.
- **Current behavior:** Daily maintenance requires `NetworkType.CONNECTED`, battery-not-low, and idle, yet performs only local pruning, statistics, checkpoint/VACUUM, and file cleanup.
- **Why this is wrong/fragile:** Offline/local-network TVs can indefinitely skip maintenance and accumulate expired/orphan rows and reclaimable database/WAL space.
- **Concrete failure scenario:** A local-playlist device without internet never satisfies the constraint despite being idle/charging, so months of EPG churn grow storage.
- **Recommended correction:** Remove network. Consider separate light pruning/checkpoint and expensive idle-only vacuum jobs; persist last success.
- **Fix scope:** Local scheduling.
- **Required tests:** Request constraints, offline execution, battery/idle variants, locked/full DB retry, and safe skip during active sync.
- **Resolution (2026-08-01):** Daily local maintenance requires no network connection while retaining battery-not-low and device-idle constraints. The complete prune/statistics/checkpoint/VACUUM operation now holds the same no-provider-sync admission gate, so it cannot race catalog/EPG writes. If any provider work is active or queued, maintenance is deferred as retryable work without writing a false successful-maintenance snapshot; only a completed operation records the persisted success snapshot.
- **Verification:** `DataMaintenanceSchedulingTest` asserts offline, battery, and idle constraints. `ProviderWorkLockRegistryTest` verifies both active-provider rejection and that provider work cannot enter while admitted maintenance is running. `SyncWorkerPolicyTest` verifies completed work persists one snapshot, deferred work persists none, and locked/busy/full database failures retry. The focused data tests and app-level `DataMaintenanceSchedulingTest` passed on 2026-08-01 after correcting the unrelated Hilt constructor ambiguity.

### WORK-002 — Recording reconciliation retries every error without classification or cap

- **Classification:** Resolved (2026-07-29)
- **Severity:** Medium
- **Where:** `RecordingReconcileWorker.kt`; `RecordingManagerImpl.reconcileRecordingState`; typed contract in `RecordingModels.kt` and `RecordingManager.kt`.
- **Original behavior:** Every domain `Error` and even `Loading` mapped to `Result.retry()`, without explicit backoff, attempt ceiling, or category. A preliminary correction added a three-attempt ceiling and exponential backoff, but still classified every error as transient, returned only `Result<Unit>`, swallowed cancellation through `runCatching`, emitted no diagnostic metadata, and used startup `REPLACE`, which reset the unfinished one-shot and its attempt history.
- **Why this is wrong/fragile:** Permanent row/configuration failures retry indefinitely. Startup replacement can reset the history of a repeatedly failing one-shot and diagnostics cannot distinguish causes.
- **Concrete failure scenario:** A malformed legacy recording row fails deterministically; each launch replaces/restarts work and WorkManager repeatedly scans state without quarantining it.
- **Recommended correction:** Return typed transient/permanent/per-row outcomes, quarantine/report bad rows, cap one-shot attempts with exponential backoff, and make `Loading` impossible as a terminal return.
- **Fix scope:** Worker plus result refinement.
- **Required tests:** Transient then success, permanent/partial row failure, ceiling/backoff, cancellation, startup replacement, and diagnostic metadata.
- **Resolution:** Recording reconciliation now returns the closed `Complete`, `Partial`, `TransientFailure`, or `PermanentFailure` contract; `Loading` is not representable. Deterministic row failures are marked `FAILED` with a persisted quarantine reason and accumulated into a partial report without blocking healthy rows. I/O and locked/busy database failures remain retryable, other global failures terminate, and `CancellationException` is always rethrown without a status write. The one-shot uses 30-second exponential backoff, stops after three attempts, and startup/boot enqueue uses unique `KEEP` ownership so another launch cannot reset an unfinished attempt chain. Periodic transient work remains retryable independently. Success/failure output and retry progress carry bounded outcome, attempt, inspected, repaired, quarantined, and reason diagnostics.
- **Verification:** `RecordingReconcileWorkerTest` covers transient-then-success, permanent and partial outcomes, the one-shot ceiling/backoff, periodic independence, startup `KEEP`, and diagnostic fields. `RecordingManagerImplTest` covers row quarantine/persisted reason, transient/permanent classification, cancellation propagation, stale capture repair, and exact-alarm reconciliation. The focused 17-test data matrix and full domain test task passed, and `:app:assembleDebug` passed on 2026-07-29.

### UPDATE-001 — Failed automatic update checks suppress another attempt for 24 hours

- **Classification:** Resolved (2026-07-29)
- **Severity:** Medium
- **Where:** `AppUpdateCheckWorker.kt`; `GitHubReleaseChecker.kt:39-68`; `PreferencesRepository.kt`; `SettingsAppUpdateActions.kt:42-55`.
- **Original behavior:** Startup wrote the last-check timestamp before fetching. Only success refreshed cache; error was ignored. The timestamp gated checks for 24 hours, treating transient failure as success.
- **Why this is wrong/fragile:** Last attempt and last success require different retry semantics; update discovery depends on being online at one startup and stale cache has no observable reason.
- **Concrete failure scenario:** The TV starts before Wi-Fi, records the attempt, fails, then stays online all day without another automatic check.
- **Recommended correction:** Store separate attempt/success/outcome. Rate-limit success for 24 hours, use short bounded failure backoff, and preferably use network-constrained unique WorkManager work. Preserve valid cache on failure.
- **Fix scope:** Local/shared startup-settings policy.
- **Required tests:** Offline then reconnect, 429/500, malformed response, cached success then failure, restart, force check, and clock changes.
- **Resolution:** Successful checks retain the 24-hour cadence; attempts, outcomes, and failures are persisted separately; failed automatic checks run as unique network-constrained WorkManager work with exponential 15-minute backoff, including when connectivity returns after startup. Manual checks use the same markers, and failures never overwrite valid cached release data.
- **Verification:** `GitHubReleaseCheckerTest` covers HTTP 429/5xx and malformed successful responses; `SettingsAppUpdateActionsTest` proves failed refreshes preserve cached release data; `AppUpdateCheckPolicyTest` covers success cadence, failure backoff, and backward clock changes. The focused 8-test app matrix and `:app:compileDebugKotlin` passed on 2026-07-30.

### MEMORY-001 — Process-lifetime maps grow with provider/category/host/media identities

- **Classification:** Architectural concern
- **Severity:** Medium
- **Where:** `PlayerDataSourceFactoryProvider.clientsByKey:42-78`; `PlayerAddressHealthStore`; `Media3PlayerEngine.promotedLiveHlsBufferReasonsByMediaId:225,1764`; `AudioCompatibilityMemoryStore`; movie/series category caches and locks; `EpgSourceRepositoryImpl.sourceRefreshMutexes:81,218`; URL resolver Stalker-provider registry.
- **Current behavior:** Singleton/long-lived maps, sets, and per-media preference keys have no maximum, comprehensive TTL sweep, or deletion hook. Keys are data/network driven; player client keys include address/proxy variations. `SyncManager.onProviderDeleted` does not invalidate these other registries.
- **Why this is wrong/fragile:** Deleted providers and one-off category/media/host keys remain reachable for the process. Per-key OkHttp clients cost more than metadata due to pools/dispatchers/configuration.
- **Concrete failure scenario:** Provider churn and large catalogs create thousands of stale locks/freshness keys; rotating proxy/media identities add clients, health entries, compatibility keys, and buffer reasons until process death.
- **Recommended correction:** Set ownership/bounds for every registry: provider-delete invalidation, LRU/TTL with proactive sweep, removal of unused mutexes, and a small fixed set of client profiles. Version/cap persisted compatibility memory.
- **Fix scope:** Shared cache policy plus local hooks.
- **Required tests:** 100,000 unique keys remain within fixed bounds; delete/re-add; TTL without same-key lookup; concurrent eviction/use; active calls survive client policy.

### PERSIST-001 — The central preferences DataStore has no explicit corruption recovery

- **Classification:** Implemented; boundary verification pending (2026-07-30)
- **Severity:** Medium
- **Where:** `PreferencesRepository.kt:62-64` and its direct `.data.map/first` consumers; startup consumers in application/welcome/settings/player.
- **Original behavior:** `preferencesDataStore(name = "user_preferences")` had no `ReplaceFileCorruptionHandler`, and no central read-I/O fallback/diagnostic policy was present. This graph god node backs onboarding, provider/source choices, playback configuration, updates, plugin mappings, and feature flags.
- **Why this is wrong/fragile:** DataStore surfaces corruption/read failure unless recovery is configured. One damaged file can repeatedly fail unrelated startup flows. Blindly defaulting every error is also unsafe, so the policy must reconcile Room-backed state explicitly.
- **Concrete failure scenario:** Storage/power damage corrupts preferences; eager collectors and `.first()` calls fail every process start even though the Room catalog remains usable.
- **Recommended correction:** Add a documented corruption handler that preserves a diagnostic copy when possible and replaces known defaults; distinguish corruption from transient I/O; expose recoverable UI/telemetry; centralize policy.
- **Resolution:** `PreferencesRepository` now creates the preferences DataStore through a single `PreferenceDataStoreFactory` policy. `ReplaceFileCorruptionHandler` snapshots the damaged file when possible, writes a bounded recovery marker outside DataStore, logs the corruption for diagnostics, and returns `emptyPreferences()` so known repository defaults can restore service. Snapshots are capped at 4 MiB and three retained copies. Transient I/O exceptions and coroutine cancellation remain outside the corruption handler and are not converted into defaults.
- **Verification:** `PreferencesCorruptionRecoveryTest` proves corrupt input produces empty defaults, persists a recovery marker, and retains one diagnostic snapshot. `:data:testDebugUnitTest --tests com.streamvault.data.preferences.PreferencesCorruptionRecoveryTest` and `:data:compileDebugKotlin` passed on 2026-07-30. The broader runtime matrix below remains pending.
- **Fix scope:** Shared persistence policy, possibly recovery UX.
- **Required tests:** Corrupt checksum/file, transient unreadable file, one-time handler, Room/provider reconciliation, restart, backup/restore, and cancellation not treated as corruption.

### TEST-001 — The player suite asserts the opposite MPEG-TS extractor policy from production

- **Classification:** Resolved (2026-07-26)
- **Severity:** Medium
- **Where:** `PlayerMediaSourceFactory.kt:40-51`; `PlayerMediaSourceFactoryTest.kt:8-17`.
- **Current behavior:** Production explicitly configures direct live MPEG-TS with `TsExtractor.MODE_SINGLE_PMT` and explains that HLS mode is inappropriate for a long-lived raw TS input. The only focused test is named “uses hls mode” and asserts `MODE_HLS`. The Phase 2 run failed with expected `2` (`MODE_HLS`) but actual `1` (`MODE_SINGLE_PMT`); the other 190 player tests passed.
- **Why this is wrong/fragile:** Either the documented production policy regressed or its regression test was not updated. In both cases the suite is red and cannot guard the intended live-TS behavior. Reflection on a private Media3 field also makes the test sensitive to dependency internals.
- **Concrete failure scenario:** CI blocks unrelated changes on a stale assertion, encouraging the test to be ignored; alternatively, a developer changes production back to HLS merely to make CI green and reintroduces finite-duration/continuity behavior without a real playback fixture.
- **Recommended correction:** Decide the supported direct-TS contract using a raw long-lived TS fixture and the historical stuck-window validation. Encode the mode as an app-owned policy value that can be asserted without reflection, update the stale name/assertion, and add a media-source/playback behavior test rather than relying only on an internal integer.
- **Fix scope:** Local test/policy clarification; runtime change only if fixture evidence rejects `MODE_SINGLE_PMT`.
- **Required tests:** Long-lived raw MPEG-TS beyond two minutes; discontinuity/multiple-PMT samples; duration remains live/unknown; reconnect behavior; keep the existing Android emulator 90-120 second live validation for any production-mode change.
- **Resolution:** Verified the focused test now names and asserts the supported `MODE_SINGLE_PMT` policy. No runtime playback policy changed, so live-device validation is not applicable to this resolution.

## Phase 2 verification and evidence

- The existing graph report/wiki were read first. Graph communities identified `Media3PlayerEngine`, `PreferencesRepository`, `SyncManager`, and provider HTTP services as load-bearing; graph queries traced engine/timeshift and provider-sync relationships, then source was treated as authoritative.
- Static inventory found 289 `runCatching` sites, 234 broad catches, five workers with broad terminal catches, and synchronous OkHttp calls across sync, EPG, Stalker, Jellyfin, recording, backup, update, download, and resolver paths. Reported defects were individually traced through callers/owners rather than inferred from counts.
- Focused baseline command: `./gradlew.bat :data:testDebugUnitTest --tests "com.streamvault.data.sync.SyncProgressBusTest" --tests "com.streamvault.data.repository.PlaybackHistoryRepositoryImplTest" :app:testDebugUnitTest --tests "com.streamvault.app.tv.WatchNextManagerTest" :player:testDebugUnitTest --no-daemon`. The selected data/app tests passed. The player suite ran 191 tests: 190 passed and `PlayerMediaSourceFactoryTest` failed as documented in TEST-001. Existing tests do not cover the adverse cancellation/concurrency scenarios specified above.
- Documentation only changed; no application code was modified, so `graphify update .` was not required.

## Phase 2 open questions

1. Should providers intentionally sync concurrently, or should catalog work be globally resource-limited during playback/recording?
2. What hard limits are approved for decompressed M3U/XMLTV bytes, entries, categories, and field sizes on TV devices?
3. Is playback snapshot persistence intended continuously in production or only for opt-in diagnostics?
4. What maximum VOD progress loss after process death is acceptable (for example 30 seconds)?
5. Must `Media3PlayerEngine.resetForReuse()` remain synchronous? Awaitable lifecycle would simplify correctness.
6. Which preferences are safe to reset after corruption, and which must be reconstructed from Room or force recovery UI?
7. Should recording stop be immediate with a partial segment or wait to a segment boundary within a fixed deadline?

## Next review targets

Phase 3 should keep these shared defects separate from provider-specific behavior and proceed in this order:

1. **Stalker Portal (deepest):** `OkHttpStalkerApiService.kt`, `StalkerProvider.kt`, token/session/device state, handshake, profiles, pagination, command/link resolution, learned capabilities, retry, and time handling.
2. **Xtream Codes:** API/provider, adaptive/index workers, pagination/category state, URL renewal, response limits, error mapping, and live/VOD/series distinctions.
3. **M3U/M3U8 and XMLTV/manual EPG:** grammar/encoding/identity, duplicates/categories, EPG assignment/time, compression, merge/refresh, and staging recovery building on SYNC-001.
4. **Jellyfin/local media/recordings:** session renewal, multi-provider selection, paging/identity, image auth, URL expiry, outputs, and REC-001.
5. **Plugin providers:** Messenger lifecycle/timeouts/cancellation, manifest identity/capabilities, M3U mapping, uninstall cleanup, catalog ownership, and process death.

# Phase 3 — Provider reviews

**Status: Complete.** Providers were reviewed independently across authentication/session behavior, catalog completeness, identity mapping, protocol compatibility, EPG time handling, playback URL resolution, lifecycle ownership, and provider-specific test coverage. Stalker received the deepest inspection. Shared defects already established in Phase 2 are cross-referenced rather than counted again unless a provider amplifies them in a distinct way.

## Phase 3 provider assessment

| Provider | Strong existing behavior | Principal gaps found |
|---|---|---|
| Stalker Portal | Extensive MAG recipes/fingerprints, bounded response parsing, staged/indexed catalog paths, link-mode adaptation, replay harness | Global cookie state, incomplete token-expiry recognition, cancellation-driven fallback storms, silent page cap, timezone loss, collision-prone synthetic IDs |
| Xtream Codes | Cancellable OkHttp bridge, response byte budgets, streaming catalog decode, typed HTTP/parsing errors, compatibility fallbacks | Provider facade swallows cancellation; transient category failure is cached as authoritative adult classification |
| M3U/M3U8 | Streaming parser, defensive attribute parser, URL policy, stable 64-bit identity, staging swap | Fixed UTF-8 decode, only one header EPG URL, query-bearing VOD extensions misclassified; Phase 2 also found missing import bounds |
| XMLTV/manual EPG | XML encoding autodetection, offset-aware dates, staged atomic source swap, channel/program batching | Size limit is applied before decompression; no deterministic timezone when one source is shared across differing providers |
| Jellyfin/local media | Stable SHA-256-derived remote IDs, bounded verifiable catalog pagination with durable resume, bearer request profiles for playback, Quick Connect support | Image authorization device/migration verification remains pending for multiple accounts on one server |
| Plugin providers | Explicit Messenger request IDs, bind/unbind cleanup, capability contract, M3U provider integration | Name-based provider ownership, unscoped manifest IDs, orphan cleanup gap, sequential multi-minute playback chain |

## Phase 3 findings summary

| ID | Finding | Classification | Severity |
|---|---|---|---|
| STALKER-001 | All Stalker providers share and globally clear one cookie jar | Implemented; provider-deletion/device matrix pending (2026-08-10) | High |
| STALKER-002 | Expired sessions reauthenticate only for one error phrase | Implemented; concurrent-refresh/retry-ceiling matrix pending (2026-08-10) | High |
| STALKER-003 | Cancellation can continue through the Stalker recipe search | Resolved; discovery-stage, stream-body, cookie, and fallback matrix passes (2026-08-09) | High |
| STALKER-004 | Catalog pagination silently declares page 200 complete | Resolved; aggregate-limit, resume, cycle, metadata, and persistence coverage passes (2026-08-11) | High |
| STALKER-005 | Portal-local dates are parsed as UTC | Implemented; full DST/archive matrix pending (2026-08-10) | High |
| STALKER-006 | Non-numeric remote IDs collapse to a 31-bit Java hash | Implemented; migration/stress matrix pending (2026-08-10) | Medium |
| XTREAM-001 | Most Xtream provider operations swallow cancellation | Resolved; complete public-facade and compatibility matrix passes (2026-08-09) | High |
| XTREAM-002 | A transient category failure is cached as an empty adult-category set | Resolved; recommended-standard cache policy verified (2026-08-01) | Medium |
| M3U-001 | Playlist decoding is fixed to UTF-8 and only the first header EPG URL is retained | Resolved; hardened and fully verified (2026-08-01) | Medium |
| M3U-002 | Query-bearing VOD file URLs are classified as live streams | Resolved; hardened and fully verified (2026-08-01) | Medium |
| XMLTV-001 | The 200 MB EPG limit measures compressed, not decompressed, bytes | Resolved; hardened and fully verified (2026-08-01) | High |
| XMLTV-002 | Shared no-offset XMLTV sources fall back to the device timezone | Implemented; migration/device verification pending (2026-07-30) | Medium |
| JELLYFIN-001 | Jellyfin catalog APIs buffer unpaginated libraries without a byte bound | Resolved; hardened and fully verified (2026-08-01) | High |
| JELLYFIN-002 | Images from two accounts on one server use whichever provider matches first | Implemented; migration/device verification pending (2026-07-30) | High |
| PLUGIN-001 | Plugin provider fallback can overwrite/delete a user-created M3U provider | Implemented; ownership lifecycle matrix pending (2026-07-30) | High |
| PLUGIN-002 | Manifest IDs are not package-scoped and absent plugins are not reconciled | Implemented; package lifecycle matrix pending (2026-07-30) | High |
| PLUGIN-003 | Playback preparation is a sequential, multi-minute, first-handler chain | Implemented; IPC lifecycle verification pending (2026-07-27) | High |

## Stalker Portal deep review

### STALKER-001 — All Stalker providers share and globally clear one cookie jar

- **Classification:** Implemented; provider-deletion/device matrix pending (2026-08-10)
- **Severity:** High
- **Where:** `OkHttpStalkerApiService.kt:63-89,309-357,842-843,1455-1467,1504-1517,1581-1585,2728-2765`.
- **Current behavior:** The Hilt-singleton API service owns one `InMemoryStalkerCookieJar`. Every direct/proxy client uses it, keyed only by registrable domain/host. Every authentication recipe calls `cookieJar.clear()`, and current playback cookies are read back by host. Provider ID, MAC, account, and auth epoch are absent from cookie ownership.
- **Why this is wrong/fragile:** Concurrent or alternating providers mutate the same session state. Authenticating provider B clears provider A's cookies; two accounts on the same portal overwrite same-name cookies. A's `currentCookieHeader` can then return B's cookie instead of the snapshot stored in A's session.
- **Concrete failure scenario:** Provider A begins playback on `portal.example`; a background sync authenticates provider B on the same or another Stalker host and globally clears the jar. A's create-link renewal or archive request loses/replaces its session cookie and fails or resolves under B's portal session.
- **Recommended correction:** Scope cookie storage to a provider/auth-session key. Construct a session-owned client/jar or pass an explicit immutable cookie snapshot; never globally clear unrelated sessions. Remove expired session jars on invalidation/provider deletion.
- **Fix scope:** Stalker API/session architecture.
- **Required tests:** Two providers on different hosts and two MAC/accounts on the same host; concurrent authentication; B fallback clears only B; A playback renewal retains A cookies; provider deletion and token refresh cleanup.
- **Implementation (2026-08-10):** Session scope keys now include provider identity and a monotonically increasing authentication epoch. Recipe reconstruction preserves both fields, direct test profiles alias to the latest authenticated scope, and `invalidateSessionScopes(providerId)` removes that provider's cookie jars, HTTP clients, resolved endpoints, and aliases. Provider auth invalidation and successful provider deletion invoke the hook. Focused tests cover same-host isolation, scope ownership, and reauthentication after explicit invalidation.

### STALKER-002 — Expired sessions reauthenticate only for one error phrase

- **Classification:** Implemented; concurrent-refresh/retry-ceiling matrix pending (2026-08-10)
- **Severity:** High
- **Where:** `StalkerProvider.runWithAuthorizedSession:743-763`, `isAuthorizationFailure:1579-1584`; HTTP mapping in `OkHttpStalkerApiService.executeJsonRequest:974-1000`.
- **Current behavior:** A provider retries authentication only when a domain message or exception contains the literal phrase `authorization failed`. The HTTP layer reports expired/rejected sessions as `Portal request failed with HTTP 401.` or `HTTP 403`; portals also use values such as `not_valid_token`, `access denied`, or empty/HTML login responses. These do not invalidate cached authentication.
- **Why this is wrong/fragile:** The shared auth cache has no expiry timestamp, so correct recovery depends on recognizing every auth/session failure. Unrecognized expiry remains cached and every later catalog/playback request repeats with the dead token.
- **Concrete failure scenario:** A portal expires a token and answers 401. `getLiveCategories` returns an error without clearing session state. Retrying the screen, background worker, and playback resolver reuse the same token until process restart or a separate explicit invalidation.
- **Recommended correction:** Map HTTP/payload failures to typed `StalkerAuthenticationException`/`StalkerSessionExpiredException`; classify 401, applicable 403, known portal error codes, and login/invalid-token bodies centrally. Add session age/expiry and single-flight refresh. Do not infer auth from free-form UI text.
- **Fix scope:** Stalker error/session contract.
- **Required tests:** 401, token-specific 403, `not_valid_token`, `authorization failed`, HTML login redirect, genuine non-auth 403, concurrent refresh, and one retry ceiling.
- **Implementation (2026-08-10):** Cached sessions now carry authentication time and a bounded 30-minute maximum age, with earlier account expiry respected. HTTP 401 and token-specific 403 responses map to typed `SessionExpired`; generic 403 responses remain `BlockedOrConfiguration`. Existing mutex/single-retry behavior remains the refresh owner. Focused tests cover the 401/token-403/genuine-403 matrix and session deadlines.

### STALKER-003 — Cancellation can continue through the Stalker recipe search

- **Classification:** Resolved (2026-08-09)
- **Severity:** High
- **Where:** `OkHttpStalkerApiService.authenticate:71-383`, especially suspend calls inside `runCatching` at `96,133,157,173,200,219,236,260`; `runApiCall:1408-1415`; synchronous requests at `975,1082,1221`.
- **Current behavior:** Authentication explores auth-mode, recipe, preset, and endpoint candidates. Suspend network calls are wrapped in standard `runCatching`, which captures `CancellationException` and often `continue`s to the next candidate. The API also uses blocking `execute()`, delaying cancellation. Public operations turn any `Exception` into a domain error.
- **Why this is wrong/fragile:** Cancelling setup/sync can cause additional handshakes/profile/account/module requests instead of termination. The fallback search is intentionally broad, so one swallowed cancellation is multiplied into network and cookie mutations across later recipes.
- **Concrete failure scenario:** The user leaves Stalker setup during a slow first handshake. Once the blocking call returns/cancels, the catch marks that attempt failed and continues trying other recipes/endpoints even though the ViewModel/worker is gone.
- **Recommended correction:** Use the shared cancellable OkHttp adapter, explicitly rethrow cancellation at every fallback boundary, and place the complete discovery operation under an overall deadline/request budget. Fallback should continue only for typed compatibility failures.
- **Fix scope:** Stalker authentication implementation, using Phase 2 shared primitives.
- **Required tests:** Cancel during handshake, credential auth, profile, modules, and streamed page; assert no later recipe request, cookie clear, persisted learning, or error state. Test overall attempt/time budget separately.
- **Resolution (2026-08-09):** Stalker JSON and streaming requests use the owned cancellable response adapter, authentication attempts rethrow cancellation, and each authentication operation runs under an injectable 45-second/24-request budget. Recipe and credential fallback now continue only for `StalkerCompatibilityException`; authentication rejection, malformed accepted responses, cancellation, and budget exhaustion terminate discovery. `OkHttpStalkerCancellationTest` covers handshake, credentials, profile, modules, streamed catalog cancellation, request exhaustion, overall deadline cancellation, body closure, no fallback request, no emitted catalog item, and no response-cookie learning after cancellation.

### STALKER-004 — Catalog pagination silently declares page 200 complete

- **Classification:** Resolved; aggregate-limit, resume, cycle, metadata, and persistence coverage passes (2026-08-11)
- **Severity:** High
- **Where:** `OkHttpStalkerApiService.fetchPagedItems` and `fetchPagedItemPage`; `StalkerPagedItems`/`StalkerPagedResult`; Stalker movie, series, unified-VOD, split-VOD, and index hydration state; Room schema version 75.
- **Previous failure mode:** The aggregate safety ceiling was also used as a semantic total. Page 200 could be marked complete, requested pages could be clamped to 200, and the sync/index layer could persist success while titles after the cap remained undiscoverable.
- **Implemented behavior:** Bulk `getVodStreams`/`getSeries`/live aggregation now either reaches a verified end or returns typed `CatalogTruncated`; it never returns a partial success. Single-page requests preserve the requested cursor, including page 201+, and retain advertised item/page totals. Missing totals are not treated as complete when a non-empty page is returned. Empty-before-end pages, changing totals, and repeated page payloads become typed/anomaly failures.
- **Resume and persistence:** Native, unified-VOD, and split-VOD hydration enforce a 200-page work budget, persist `TRUNCATED` plus advertised totals and the last successful page, and resume from the next page on a later request. Repeated-page detection prevents cycles. Index hydration records truncation and exposes it through catalog/search status instead of reporting success; Room migration 74→75 preserves the new totals.
- **Why this matters:** A safety budget remains a safety budget rather than silently becoming catalog completeness. Users can see that a catalog is partial, and a later bounded run can continue from the durable cursor without reloading the first 200 pages or losing the already indexed rows.
- **Fix scope:** Stalker API, hydration/index state, persistence, resume orchestration, and status reporting.
- **Verification:** Focused boundary/resume/metadata tests pass, including 201-page bulk truncation, page-201 cursor preservation, missing totals, empty-before-end rejection, provider completion semantics, and SyncManager continuation. The full `:data:testDebugUnitTest` suite passes (2026-08-11); both data and app debug Kotlin compilation pass.

### STALKER-005 — Portal-local dates are parsed as UTC

- **Classification:** Implemented; full DST/archive matrix pending (2026-08-10)
- **Severity:** High
- **Where:** `OkHttpStalkerApiService.toProgramRecord:1325-1349`, `toProgramRecords:2077-2099`, `parseExpirationDate/parseDateTime:2640-2663`; configured timezone in `buildStalkerDeviceProfile:2467-2547`.
- **Current behavior:** Numeric epochs and offset-bearing timestamps are handled correctly, but date/time strings without an offset are converted using `ZoneOffset.UTC`. The parser does not receive `profile.timezone`, even though that timezone is sent to the portal in cookies/profile data. Account expiry strings use the same UTC assumption. Current Stalker tests overwhelmingly use `timezone = "UTC"`.
- **Why this is wrong/fragile:** MAG/Stalker payloads commonly return portal-local wall times. Interpreting them as UTC shifts EPG, catch-up/archive windows, now-playing state, reminders, and expiry by the portal offset, including DST changes.
- **Concrete failure scenario:** A Europe/Amsterdam portal returns `2026-07-11 20:00:00` without an offset. The app stores 20:00 UTC instead of 18:00 UTC in summer, displaying the program and generating archive URLs two hours late.
- **Recommended correction:** Parse local timestamps using the authenticated/configured portal `ZoneId`; keep offset/epoch precedence. Separate account-date and EPG parsers if their portal conventions differ, and record which interpretation was used for diagnostics.
- **Fix scope:** Stalker DTO/parsing signatures and tests.
- **Required tests:** UTC, positive/negative zones, DST summer/winter and transition ambiguity, explicit offset overriding profile zone, numeric epochs, expiry date-only semantics, and archive URL alignment.
- **Implementation (2026-08-10):** Catalog `added` timestamps on buffered, paged, validation, series-detail, and streamed item paths now receive the configured portal `ZoneId`; numeric epochs and explicit offsets retain precedence. Existing EPG and account-expiry parsing already use the same zone-aware parser. A non-UTC catalog regression test now verifies the wall-time conversion.

### STALKER-006 — Non-numeric remote IDs collapse to a 31-bit Java hash

- **Classification:** Implemented; migration/stress matrix pending (2026-08-10)
- **Severity:** Medium
- **Where:** `StalkerProvider.stableItemId/syntheticCategoryId:1495-1501`; use for channels, movies, series, episodes, programs, and categories around `696,1240,1284,1339,1374,1404,1465`.
- **Current behavior:** Positive numeric IDs are preserved. Every non-numeric ID is mapped with Kotlin/Java `String.hashCode()`, masked to 31 positive bits. The provider/type prefix reduces cross-domain collisions but cannot prevent equal Java hashes; equal-length colliding suffixes such as `Aa`/`BB` remain collisions under the same prefix.
- **Why this is wrong/fragile:** 31 bits is not a stable uniqueness space for large catalogs, and collisions are deterministic. Room uniqueness by provider/remote numeric ID then merges, skips, or replaces distinct content; navigation/history can target the wrong item.
- **Concrete failure scenario:** Two non-numeric episode/channel identifiers hash identically. The second staging insert conflicts with the first, leaving one missing or associating metadata/playback with the other.
- **Recommended correction:** Persist canonical remote IDs as strings and use composite provider/type/remote-ID keys. Where a Long is unavoidable, use the existing 63-bit SHA-256-style stable hashing plus collision detection/resolution, never a bare 31-bit hash.
- **Fix scope:** Provider identity/schema contract; migration implications belong to Phase 4.
- **Required tests:** Known Java-hash collisions; 100k-1m generated remote IDs; stable across restart; collision detection; history/favorites/episodes preserve both records; migration from existing IDs.
- **Implementation (2026-08-10):** The persistent `StalkerRemoteIdentityResolver` and non-persistent fallback now derive candidates from 63-bit SHA-256 material, with collision allocation/resolution retained. Category, channel, movie, series, episode, and projected-category paths use the collision-safe resolver/fallback; known `Aa`/`BB` Java-hash collisions are explicitly covered.

## Xtream Codes review

### XTREAM-001 — Most Xtream provider operations swallow cancellation

- **Classification:** Resolved (2026-08-09)
- **Severity:** High
- **Where:** `XtreamProvider.kt:90-529`, including broad catches at `131,144,173,186,215,251,315,328,357,393,508,528`; compatibility `runCatching` at `1023,1037`. `getSeriesInfo:487-490` is the lone consistent cancellation-first example.
- **Current behavior:** `OkHttpXtreamApiService` correctly cancels `Call` through `suspendCancellableCoroutine`, but the facade catches the resulting `CancellationException` as `Exception` and returns `Result.Error` for authentication, categories, catalogs, details, streaming indexes, and EPG. Series details explicitly rethrow, demonstrating the desired behavior.
- **Why this is wrong/fragile:** The transport cancellation works, but its semantic signal is lost one layer later. Sync/UI code can publish failure, attempt compatibility fallbacks, or cache empty derived data for deliberately cancelled requests.
- **Concrete failure scenario:** A category streaming sync is replaced. The HTTP call cancels promptly, but `streamVodSummaries` returns a domain error; the worker records failure/retry instead of ending cancellation cleanly.
- **Recommended correction:** Apply the cancellation-first pattern used by `getSeriesInfo` to every suspend method and compatibility fallback, ideally through the shared helper from Phase 2.
- **Fix scope:** Local/mechanical across `XtreamProvider`.
- **Required tests:** Cancellation for auth, each catalog stream, category prefetch, VOD/series detail, EPG, and primary-to-legacy compatibility; genuine parsing/network failures remain typed.
- **Resolution (2026-08-09):** Every public Xtream suspend operation now rethrows `CancellationException`, and series-info compatibility attempts use `runSuspendCatching`, so cancellation cannot reach the legacy request. `XtreamProviderTest` covers authentication, category prefetch, every catalog/detail/EPG operation, both streaming facades, primary-to-legacy compatibility, and genuine network-error typing.

### XTREAM-002 — A transient category failure is cached as an empty adult-category set

- **Classification:** Resolved; recommended-standard cache policy verified (2026-08-01)
- **Severity:** Medium
- **Where:** `XtreamProvider.loadAdultCategoryIds:665-697`; long-lived provider caches in `MovieRepositoryImpl:138,1680-1713` and `SeriesRepositoryImpl:113,1660-1693`.
- **Current behavior:** Category fetch failure is caught, logged, converted to `emptyList`, then transformed to an empty set and cached by content type. Subsequent item mapping never retries category loading for that provider instance. Repository provider instances can live for the process.
- **Why this is wrong/fragile:** “No adult categories” and “category request failed” are distinct states. Caching failure as authoritative classification can leave content incorrectly unmarked after connectivity recovers and suppress policy/UI handling.
- **Concrete failure scenario:** The first movie detail/list load occurs during a brief 500 response. The process-lifetime provider caches an empty movie adult set; all later movies rely only on optional text classification until process restart/provider signature change.
- **Recommended correction:** Cache only successful category responses, or store success/error with TTL and retry backoff. Invalidate on sync/provider update and expose degraded classification state where policy requires it.
- **Fix scope:** Local provider cache policy plus repository invalidation.
- **Required tests:** First failure then recovery; cancellation does not cache; genuine successful empty list does cache; TTL/update invalidation; concurrent callers single-flight; explicit item adult flag still wins.
- **Resolution (2026-08-01):** Failed category prefetches return an uncached empty set, while successful responses (including a genuinely empty response) remain cacheable. Cancellation is rethrown. A mutex makes a successful prefetch single-flight, and a successful direct category refresh replaces the cached classification; provider configuration changes create a replacement provider through the repository signature cache. Focused coverage verifies recovery after failure, cancellation without caching, successful-empty caching, concurrent single-flight behavior, refresh invalidation, and explicit item-level adult flags.

## M3U/M3U8 review

### M3U-001 — Playlist decoding is fixed to UTF-8 and only the first header EPG URL is retained

- **Classification:** Resolved (hardened 2026-08-01)
- **Severity:** Medium
- **Where:** `M3uParser.parse/parseStreaming:56-134`, `M3uHeader:25-28`, `extractHeaderEpgUrl:220-230`; `SyncManagerM3uImporter` header handling at `93-99`.
- **Current behavior:** Both parser paths construct `InputStreamReader(..., UTF_8)` with no BOM/charset fallback. Header attributes such as `x-tvg-url` are split on commas and only the first non-empty URL survives in the singular `tvgUrl` field. XMLTV itself supports declaration-based encoding, so behavior differs across the paired formats.
- **Why this is wrong/fragile:** Legacy/provider playlists in Windows-1252/ISO-8859-1 produce replacement characters in names/groups/EPG IDs, breaking display and matching. Multiple XMLTV URLs are a common header convention for split regional guides; discarding all but one creates systematic missing EPG.
- **Concrete failure scenario:** A Latin-1 playlist contains accented channel names and two `x-tvg-url` sources. Names are corrupted, auto matching fails, and channels covered only by the second guide remain without programs.
- **Recommended correction:** Detect BOM, honor a validated HTTP charset when available, and use a documented UTF-8/fallback policy. Model header guide URLs as a bounded deduplicated list and assign/import each valid source with explicit priority.
- **Fix scope:** M3U parser/header model and importer assignment.
- **Required tests:** UTF-8 BOM, UTF-16 BOM if supported, Windows-1252/ISO-8859-1, invalid byte sequences, accented EPG matching, multiple/comma-spaced/duplicate guide URLs, and per-URL failure isolation.
- **Resolution:** M3U parsing now gives UTF-8/UTF-16 BOMs precedence, honors allow-listed HTTP charsets, and otherwise decodes strict UTF-8 with a deterministic Windows-1252 fallback for legacy/invalid lines. Both buffered and streaming paths share this policy. Headers retain up to eight trimmed, distinct guide URLs in source order. The importer validates, imports, and assigns every accepted guide with stable priority; a returned error or exception from one guide is reported as a warning without preventing later guides from being processed. Regression coverage includes every encoding, matching, URL-list, and failure-isolation case listed above.

### M3U-002 — Query-bearing VOD file URLs are classified as live streams

- **Classification:** Resolved (hardened 2026-08-01)
- **Severity:** Medium
- **Where:** `M3uParser.isVodEntry:432-444`; classification branch in `SyncManagerM3uImporter:126-190`; tests cover bare `.mp4/.mkv` only.
- **Current behavior:** VOD classification checks `lowercaseUrl.endsWith(".mp4"/".mkv"/".avi")`, literal `/movie/`, or English group substrings. A tokenized URL such as `film.mp4?token=...` does not end with the extension and is imported as live unless its path/group happens to match another heuristic.
- **Why this is wrong/fragile:** Tokenized/expiring URLs are explicitly supported elsewhere. Misclassified VOD enters live channel UI, loses movie metadata behavior, uses live playback/retry semantics, and may be omitted from VOD entirely.
- **Concrete failure scenario:** A playlist group named `Cinema` supplies `https://cdn/x/123.mp4?token=abc`. With VOD classification enabled, it becomes a live channel because `Cinema` is not one of the three English keywords.
- **Recommended correction:** Parse the URI path and inspect its final extension independent of query/fragment. Make group/name heuristics configurable and localized, treat classification as confidence/evidence, and allow overrides.
- **Fix scope:** Local classifier plus tests/UI explanation.
- **Required tests:** Extensions with query/fragment and uppercase; encoded paths; extensionless VOD; localized groups; misleading live group names; explicit user override and stable reclassification on refresh.
- **Resolution:** M3U classification now returns a deterministic media kind, confidence, and evidence. It decodes the URI path before matching a configurable extension/path rule set, recognizes localized group and title hints, gives strong live-stream evidence precedence over weak text labels, and supports an explicit override. The provider setting uses that override when classification is disabled. Successful full-playlist refreshes commit both Live and Movies even when one side becomes empty, so changing the setting reliably removes stale rows from the previous classification. The UI explains the evidence order. Regression coverage includes query/fragment/case variants, encoded and extensionless paths, localized groups, name hints, misleading live labels, custom rules, explicit override, deterministic results, importer routing, and both directions of empty-section pruning.

## XMLTV/manual EPG review

### XMLTV-001 — The 200 MB EPG limit measures compressed, not decompressed, bytes

- **Classification:** Resolved (hardened 2026-08-01)
- **Severity:** High
- **Where:** `EpgSourceRepositoryImpl.refreshSource:316-371`; `XmltvParser.maybeDecompressGzip:523-556`; `MAX_EPG_SIZE_BYTES` policy.
- **Current behavior:** A `FilterInputStream` counting up to 200 MB wraps the raw HTTP/file stream, then `maybeDecompressGzip` wraps that counted stream in `GZIPInputStream`. The parser consumes unlimited decompressed XML as long as the compressed input stays below 200 MB. Staged rows also grow with the expanded document.
- **Why this is wrong/fragile:** Compression ratios can be very high for repetitive XML. The advertised response ceiling therefore does not bound CPU, decompressed bytes, program count, Room staging, or disk use.
- **Concrete failure scenario:** A 20 MB gzip expands to several gigabytes of XML/program rows. It passes the raw limit, spends prolonged CPU parsing, and fills staging/database storage before failure.
- **Recommended correction:** Count decompressed bytes after decompression and retain a smaller raw/network bound before it. Add channel/program/field/depth limits and abort/clean staging with a typed oversize outcome.
- **Fix scope:** EPG source ingestion boundary.
- **Required tests:** Small gzip with expansion beyond 200 MB, chunked gzip, nested/very long text, maximum channel/program counts, cleanup after overflow, and active-source atomic preservation.
- **Resolution:** XMLTV downloads now request identity transfer encoding so the 64 MB raw-wire ceiling is measured before application-controlled gzip detection, while the parser-facing stream retains an independent 200 MB decompressed ceiling. Exact-limit inputs are accepted and the first excess byte raises a typed `XmltvLimitExceeded` identifying raw bytes, decompressed bytes, channels, programmes, field length, categories per programme, or XML depth. Streaming parsing additionally caps channels, programmes, persisted text/attributes, per-programme categories, and nesting. Any typed overflow removes the negative-ID staging rows and returns a typed repository error without deleting or moving active-source rows. Regression coverage includes gzip expansion, chunked gzip, the raw boundary, exact byte limits, long text, nesting, channel/programme/category maxima, cleanup, and active-source atomic preservation.

### XMLTV-002 — Shared no-offset XMLTV sources fall back to the device timezone

- **Classification:** Implemented; migration/device verification pending (2026-07-30)
- **Severity:** Medium
- **Where:** `EpgSourceRepositoryImpl.resolveSourceTimezoneId:584-605`; `XmltvParser.resolveParsingZoneId:643-652`; unique URL/source and many-provider assignment entities.
- **Current behavior:** The repository derives timezone only from assigned providers' `stalkerDeviceTimezone`. Zero timezones or more than one distinct timezone returns `null`; the parser then uses `ZoneId.systemDefault()`. A unique EPG source is parsed once, so it cannot represent different no-offset interpretations per provider.
- **Why this is wrong/fragile:** The same guide produces different instants on devices in different zones, and multi-provider assignment silently chooses neither provider. M3U/Xtream/Jellyfin have no equivalent source-specific timezone ownership here. Offset-bearing XMLTV is safe; local timestamps are not.
- **Concrete failure scenario:** One manual guide with local timestamps is assigned to UTC and Europe/Amsterdam providers. The app logs a warning and parses using the TV's Asia/Jerusalem timezone, wrong for both; moving/restoring the database on another device changes all guide instants.
- **Recommended correction:** Store an explicit timezone/interpretation policy on each EPG source. If assignments require different zones, create provider-scoped parsed instances or reject the ambiguous assignment. Never use device timezone as a silent durable-data default.
- **Fix scope:** EPG source model and assignment UX; schema work in Phase 4.
- **Required tests:** Same source on devices with different system zones; one/multiple provider zones; explicit source override; DST; offset-bearing input unaffected; backup/restore preserves interpretation.

**Resolution:** XMLTV interpretation is now owned by the EPG source through an explicit `REQUIRE_OFFSET`, `UTC`, or `EXPLICIT_ZONE` policy. Parsing no longer consults the Android/JVM default timezone or derives a shared source's interpretation from assigned providers. Existing sources migrate to the safe `REQUIRE_OFFSET` policy, while settings allow users to choose UTC or a validated IANA timezone and immediately reparse the source. The policy and timezone are included in backup format v10 and restored transactionally with other Room-backed configuration. Focused parser, repository, backup, compile, and migration-compilation coverage passes; on-device execution of the 71→72 migration remains pending.

## Jellyfin, local media, and recordings review

### JELLYFIN-001 — Jellyfin catalog APIs buffer unpaginated libraries without a byte bound

- **Status:** Fixed; hardened and fully verified (2026-08-01).
- **Classification:** Resolved (2026-08-01)
- **Severity:** High
- **Where:** `JellyfinProvider.fetchMovies/fetchSeries/fetchEpisodes:82-119`, `fetchItems:186-198`, `fetchSeriesEpisodes:200-220`, `executeRequest:303-315`, DTOs at `363-369`.
- **Current behavior:** `/Items` and `/Shows/{id}/Episodes` requests omit `StartIndex`/`Limit`. Responses are read fully with `body.string()` and decoded into a complete list. There is a 60-second call timeout but no content-length/decompressed-byte/item limit. DTOs do not retain `TotalRecordCount`/`StartIndex`, so pagination cannot be verified or resumed.
- **Why this is wrong/fragile:** Large libraries require a full response, String, JSON tree/object graph, mapped entity list, and later persistence at once. Timeout does not cap memory. Server defaults/version differences can also make a seemingly successful response incomplete without detection.
- **Concrete failure scenario:** A server with tens of thousands of movies returns one large JSON body; the TV OOMs during `body.string()`/Gson. If a server imposes a default page size, sync reports success with only the first page.
- **Recommended correction:** Use explicit bounded pagination with `StartIndex`/`Limit`, verify `TotalRecordCount`, stream/decode page responses under byte/item limits, stage each page, and persist a partial/resume state. Reuse the cancellable HTTP adapter.
- **Fix scope:** Jellyfin API/catalog sync architecture.
- **Required tests:** Multiple pages, server default truncation, changing totals, empty/repeated pages, huge single item/body, cancellation mid-page, resume/process death, and catalog limits.

**Resolution:** Jellyfin movie, series, and episode endpoints send explicit `StartIndex` and `Limit=100`, validate the echoed start index and stable `TotalRecordCount`, and reject oversized, empty-early, repeated, or non-advancing pages. Responses are decoded directly from a bounded stream with a 4 MiB decompressed/read budget per page; page item counts, individual string fields, nested collections, and overall catalog sizes are independently bounded. Episode hydration uses the same contract and detects repeated IDs and changing totals.

Movie and series pages are staged incrementally under separate session IDs. A versioned workflow checkpoint records the phase, next indexes, totals, and session IDs after every committed page. Retryable failure and coroutine cancellation preserve that checkpoint and its matching staged rows across worker/process loss; resume verifies the staged counts before continuing, while force syncs and corrupt/torn checkpoints restart safely. Lease-token fencing prevents an obsolete worker from changing a checkpoint. Movies and series are published together in one database transaction only after both snapshots are complete, so a partial or invalid fetch cannot replace either active catalog. Cancellation is rethrown through both HTTP and sync boundaries.

Focused JVM coverage now exercises multi-page/default-truncated responses, changing totals, mismatched starts, empty and repeated pages/items, oversized bodies and single items, mid-page cancellation, checkpoint serialization and process-loss reclaim, torn-stage rejection, stale lease fencing, atomic dual-catalog promotion, episode and catalog ceilings, and cancellation propagation. The full data-module unit suite also passes.

### JELLYFIN-002 — Images from two accounts on one server use whichever provider matches first

- **Classification:** Implemented; migration/device verification pending (2026-07-30)
- **Severity:** High
- **Where:** `JellyfinImageAuthInterceptor.kt:13-76`; image URLs built without provider identity at `JellyfinProvider.kt:223-331`; provider lookup is `firstOrNull` by scheme/host/port/base path.
- **Current behavior:** Image requests carry no provider/account ID. The singleton interceptor loads all Jellyfin providers and selects the first whose base URL matches, then attaches that provider's token. Two users/providers on the same server/base path are indistinguishable. Provider order has no documented ownership rule.
- **Why this is wrong/fragile:** Tokens and media visibility are account-scoped. The wrong token yields 401/404, wrong library art, or intermittent behavior as provider ordering/cache changes. Playback avoids this by resolving with a known provider ID, but image loading does not.
- **Concrete failure scenario:** Household accounts A and B share `https://media.local`. A catalog item visible only to B has an identical-host image URL; the interceptor chooses A first and the image fails despite B being the active provider.
- **Recommended correction:** Carry provider ID in the app's image request model (tag/header/custom internal URL) and have the interceptor resolve that exact provider. Alternatively generate account-scoped authenticated image requests at repository/UI boundaries. Invalidate immediately on provider changes.
- **Fix scope:** Image URL/request contract across data/app/Coil.
- **Required tests:** Two accounts same base URL, different base paths, active-provider switch, delete/edit within cache TTL, parallel image requests, and token-specific visibility.

**Implementation (updated 2026-07-30):** Every generated Jellyfin image URL carries an internal provider ID, which the interceptor validates against the URL before attaching that exact provider's token. It never guesses an account from host/path. The interceptor now queries the exact provider on every request so an edit/delete takes effect immediately, and strips the app-internal marker on every marked request, including malformed, mismatched-path, missing-provider, and caller-authenticated cases. Database migration 64→65 backfills the marker on existing Jellyfin movie, series, and episode artwork URLs. Focused tests cover parallel accounts on one base URL, different base paths, immediate token edit/delete, and marker stripping. The finding remains open until the populated migration and device/Coil cache behavior are verified.

The Phase 2 recording cancellation defect REC-001 remains the principal provider-adjacent recording fault. Local recordings themselves use persisted file/SAF outputs rather than a separate catalog provider; their end-to-end scheduling, reconciliation, and playback flows remain in Phase 4.

## Plugin-based provider review

### PLUGIN-001 — Plugin provider fallback can overwrite/delete a user-created M3U provider

- **Classification:** Resolved (2026-07-26)
- **Severity:** High
- **Where:** `StreamVaultPluginManager.syncPluginProvider:423-500`, `removePluginProvider:503-517`, `trackedProvider:531-536`.
- **Current behavior:** If the SharedPreferences provider ID is absent, ownership is inferred by finding the first M3U provider whose `name == plugin.manifest.providerName`. Enabling then overwrites that provider's name/URL/settings and refreshes it; later disabling deletes the stored provider ID.
- **Why this is wrong/fragile:** Display names are not ownership identifiers. A user-created provider or a second plugin can legitimately share the name. The destructive update/delete path has no proof the provider was created by this plugin.
- **Concrete failure scenario:** The user has an M3U provider named `Sports`. A new plugin declares providerName `Sports`; enabling it replaces the user's URL/catalog, and disabling the plugin deletes the user's provider.
- **Recommended correction:** Persist immutable plugin ownership (`packageName`, service component, manifest ID/version) on a provider/source record or dedicated mapping table. Never infer ownership by name; missing mapping should create a new provider or require explicit adoption confirmation.
- **Fix scope:** Plugin-provider persistence model and migration.
- **Required tests:** Same name as user provider, two plugins same provider name, lost preferences with existing mapping, disable/delete, restore, provider rename, and atomic mapping/provider creation.

**Implementation:** Plugin-created M3U providers receive a durable Room ownership record keyed by the plugin package, service class, and manifest ID. Plugin synchronization and disable cleanup resolve only through that record; the unsafe display-name fallback was removed. Existing ambiguous legacy mappings are intentionally not auto-adopted. The finding remains open until the same-name, lost-preference, disable/delete, restore, rename, and atomic provider-plus-mapping creation matrix passes.

### PLUGIN-002 — Manifest IDs are not package-scoped and absent plugins are not reconciled

- **Classification:** Implemented; package lifecycle matrix pending (2026-07-30)
- **Severity:** High
- **Where:** `discoverPlugins:55-60`, enabled/provider keys at `90,496,504,532,680-681`, `resolvePlugin:545-575`; no package-removal or installed-set reconciliation path was found.
- **Current behavior:** Discovery deduplicates solely by plugin-supplied `manifest.id`. Enable state and provider mapping are also keyed only by that ID, not package/service. Two installed packages with the same ID share state and one disappears from discovery. If a package is uninstalled externally, its preferences and M3U provider/catalog remain because cleanup only occurs through `setPluginEnabled(false)` on an installed plugin object.
- **Why this is wrong/fragile:** Package/service identity is the Android lifecycle identity. A manifest ID collision redirects state/mapping to whichever service PackageManager returns first, while uninstall leaves enabled orphan data and active/combined-source references.
- **Concrete failure scenario:** Plugin A is enabled and creates provider 12. It is uninstalled; provider 12 remains active. Plugin B later installs with the same manifest ID and inherits A's enabled flag/provider mapping, then updates or deletes provider 12.
- **Recommended correction:** Namespace identity by package + service + manifest ID, validate manifest ID uniqueness only within that owner, and reconcile installed components against mappings at startup/package-change. Orphan cleanup should be transactional and preserve user-adopted providers explicitly.
- **Fix scope:** Plugin identity/lifecycle architecture.
- **Required tests:** Duplicate IDs, multiple services in one package, uninstall/replace/reinstall/signature/version change as product policy allows, active/combined source cleanup, and user adoption preservation.

**Implementation (updated 2026-07-30):** Plugin enablement and provider ownership use the package/service/manifest triple, so manifest-ID collisions across packages or services cannot share state or provider mappings. Reconciliation now uses the installed Android package/service component as the deletion authority and never treats transient manifest IPC failure or a manifest-ID rename as uninstall. Package broadcasts trigger immediate reconciliation with a `goAsync` lifetime, startup remains the durable retry, and a sole mapping for a renamed manifest ID is re-keyed atomically; ambiguous component mappings are never guessed between. Legacy enabled state is migrated only when the manifest ID is unique among installed services. The finding remains open pending uninstall/replace/reinstall, signature/version policy, active/combined-source, and explicit user-adoption device/integration coverage.

### PLUGIN-003 — Playback preparation is a sequential, multi-minute, first-handler chain

- **Classification:** Implemented; IPC lifecycle verification pending (2026-07-27)
- **Severity:** High
- **Where:** `preparePlaybackStreamInfo:268-293`, `discoverPlugins/resolvePlugin:55-60,545-590`, `rewriteCastUrl:296-318`, `PluginMessengerClient.send:28-100`.
- **Current behavior:** Every preparation rediscovers plugins sequentially. For each service, manifest and status calls can consume 3.0 + 2.5 seconds. Then every enabled playback plugin is queried sequentially with a 120-second timeout until one says handled; cast rewrite similarly uses 10 seconds each. Precedence is sorted display name/PackageManager deduplication, not declared routing or priority. Standard `runCatching` can also swallow caller cancellation and continue.
- **Why this is wrong/fragile:** Startup latency scales with installed plugins and one dead plugin blocks every later handler. A generic early handler can capture a URL intended for another plugin; there is no ownership predicate, parallel discovery cache, total deadline, or deterministic capability routing.
- **Concrete failure scenario:** Three enabled playback plugins are dead/unresponsive. Discovery can cost 16.5 seconds and preparation up to six minutes before normal playback proceeds; leaving the screen can still advance to later plugins because cancellation is caught.
- **Recommended correction:** Cache discovery with package-change invalidation, fetch status concurrently under a small total deadline, require plugins to declare URL schemes/hosts/content ownership and priority, and route only matching candidates. Use one strict overall preparation deadline and propagate cancellation; preserve per-plugin diagnostics.
- **Fix scope:** Plugin capability/routing architecture.
- **Required tests:** Multiple dead/slow plugins, cancellation at bind/response, total latency bound, two matching handlers/priority, generic non-handler, package changes, cast parity, and service disconnect fail-fast.

**Implementation (2026-07-27):** Discovery results are cached and invalidated on package changes; manifest/status calls run concurrently with bounded deadlines. Playback preparation and cast rewriting route only to plugins that explicitly declare matching URL scheme/host ownership, execute candidates concurrently, apply deterministic priority ordering, and share one five-second end-to-end deadline. Caller cancellation propagates through manager boundaries, and Messenger cleanup always unbinds. Routing/priority and total-deadline tests pass; the finding remains unresolved until bind/response cancellation, disconnect, loser-unbind, and package-cache invalidation integration tests pass.

## Phase 3 verification and evidence

- Graph navigation was used for Stalker authentication → provider persistence → sync → playback-resolution relationships, Xtream indexing/renewal, and the M3U/XMLTV/Jellyfin/plugin boundaries. Source and focused tests remained authoritative because graph cohesion is low.
- Reviewed primary provider implementations and their callers: `OkHttpStalkerApiService` (2,767 lines), `StalkerProvider` (1,611), Stalker recipes/support/adapters/traffic coordination, `XtreamProvider` (1,350), `OkHttpXtreamApiService` (615), URL factories/resolver, M3U/XMLTV parsers/importers, EPG source repository, `JellyfinProvider`/image interceptor, and plugin manager/Messenger contract.
- Focused provider test command: `./gradlew.bat :data:testDebugUnitTest --tests "com.streamvault.data.remote.stalker.*" --tests "com.streamvault.data.remote.xtream.*" --tests "com.streamvault.data.remote.jellyfin.*" --tests "com.streamvault.data.parser.M3uParserTest" --tests "com.streamvault.data.parser.XmltvParserTest" --no-daemon` — **passed** on 2026-07-11. The adverse multi-provider, oversize, cancellation, non-UTC, and ownership cases above are absent from current coverage, so the green baseline does not contradict the source-demonstrated defects.
- No application code was changed. Documentation-only changes do not require `graphify update .` under the project rule.

## Phase 3 open questions

1. What is the supported maximum Stalker catalog size, and should exceeding a budget fail sync or expose a deliberately partial library?
2. Are two Stalker MAC/accounts on the same portal and two Jellyfin accounts on one server supported product scenarios? Current provider modeling permits both, while session/image infrastructure does not.
3. Which timezone convention do supported Stalker portals and Xtream catch-up endpoints use for offset-free timestamps? Fixtures should record server/profile timezone and expected instant.
4. Which non-UTF-8 M3U encodings and multiple `x-tvg-url` semantics are officially supported?
5. Should a manual XMLTV source be globally shared by URL, or provider-scoped when timestamp interpretation differs?
6. Is plugin identity controlled by StreamVault (globally issued IDs), or may third-party plugins choose arbitrary IDs? Android component identity should remain part of the key either way.
7. Can a plugin-created M3U provider be adopted/retained by the user after plugin removal, or must uninstall always remove it?
8. Is Jellyfin intended to import all libraries/items or only selected libraries? Explicit selection would change paging and category design.

## Next review targets

Phase 4 should trace feature flows end to end in this order:

1. Provider setup/edit/delete and initial sync for every provider, including saved-with-warning and rollback behavior.
2. Live playback/zapping/preload/retry/renewal/timeshift/catch-up, with Stalker session refresh and plugin preparation races.
3. Catalog refresh/index/detail hydration for VOD/series, partial state, process death, and provider removal.
4. EPG source discovery/assignment/refresh/matching/timezone, followed by reminders and catch-up.
5. Recording schedule → alarm/service → resolution → capture → stop/reconcile → playback/delete, including REC-001.
6. Resume/history/Watch Next/recommendations, downloads/local playback, cast, backup/restore, updates, and plugin lifecycle.
7. Database migration chains and invariant preservation for provider identity, plugin ownership, synthetic IDs, EPG source timezone, and in-flight job state.

# Phase 4 — Feature flow reviews

**Status: Complete.** This phase traced setup/edit/delete, catalog navigation, EPG/reminders/catch-up, playback/resume/download/cast/multiview, recording, backup/restore, plugins/updates, and migrations across UI, domain, persistence, scheduling, and recovery boundaries. Shared and provider findings remain in Phases 2 and 3 rather than being counted twice.

## Phase 4 findings summary

| ID | Finding | Classification | Severity |
|---|---|---|---|
| SETUP-001 | Editing an active provider deactivates it before replacement sync succeeds | Resolved (2026-08-10) | High |
| BACKUP-001 | Restore is non-atomic across Room, preferences, and recording alarms | Resolved (2026-07-29) | High |
| BACKUP-002 | Provider-scoped restored preferences retain obsolete database IDs | Resolved (2026-07-29) | High |
| DELETE-001 | Post-delete alarm/sync cleanup failures are never reconciled | Resolved (2026-07-29) | Medium |
| EPG-001 | Editing a reminder can persist a time whose alarm replacement failed | Resolved (2026-07-29) | Medium |
| PLAY-001 | Failed live-history persistence suppresses same-channel retry for the session | Confirmed defect | Medium |
| MIGRATION-001 | The current 61→62 migration and full supported chain are untested | Needs improvement (2026-07-26 audit) | Medium |

The main feature risks are cross-store transitions: provider edits expose partially committed setup state; restore can report failure after changing Room, DataStore, or alarms; and backup treats database row IDs as portable identities.

## Feature-flow observations

- **Setup/catalog/navigation:** Authentication/save converges in `ProviderRepositoryImpl`; activation follows initial sync. Xtream/Stalker require committed content while M3U/Jellyfin need not contain live channels. Candidate revisions now retain the committed provider/catalog through replacement sync (SETUP-001). Browsing, search, favorites, protection, and virtual groups are provider-scoped; backup v9 now carries that identity model into portable preference restore (BACKUP-002).
- **EPG/catch-up/zapping:** Source refresh/matching, cached lookup, catch-up construction, and launch have clear owners. Phase 3 covers source/parser faults. Reminder creation, edit, cancellation, and startup repair now use compensating alarm/Room ordering (EPG-001). Zap resolution uses request generations to reject stale work; live-history de-duplication is optimistic (PLAY-001).
- **Playback/download/cast/multiview:** Logical URLs become `StreamInfo`, followed by preparation, renewal, and recovery. Phase 2 lifecycle/timeshift/progress/cancellation findings remain controlling. Downloads correctly restart when a server ignores `Range` and returns `200`. Cast eligibility/media creation is centralized and tested for unsupported headers. Auxiliary multiview engines inherit LIFE-001/LIFE-002.
- **Recording/backup/plugins/updates/migrations:** Scheduling validates storage, conflicts, connection limits, and exact alarms; alarm failure leaves an observable failed run. Capture inherits REC-001. Only provider/library/history restore is one Room transaction (BACKUP-001). Plugins retain PLUGIN-001–003 and updates UPDATE-001. Room 62 defines and production-registers every adjacent migration; the latest boundary lacks tests (MIGRATION-001).

## Detailed Phase 4 findings

### SETUP-001 — Editing an active provider deactivates it before replacement sync succeeds

- **Classification / severity:** Resolved (2026-07-29) — High
- **Where:** `ProviderRepositoryImpl.kt:285-333` (Xtream), `381-420` (M3U), `467-485` (Jellyfin), `635-700` (Stalker), and `707-764` (shared completion).
- **Current behavior:** Existing-provider paths stage the replacement configuration separately. The committed provider row and catalog remain authoritative while authentication and replacement catalog work run; activation/promotion is fenced by the provider workflow generation.
- **Why / scenario:** Replacement authentication and catalog promotion are separate commits. A transient failure after editing the active account's name or HTTP profile disables Home/Live TV despite a previously usable catalog.
- **Recommended correction:** Keep the last active provider/catalog until a pending configuration revision and sync epoch commit atomically. At minimum restore prior active state on failure and delay timestamp reset.
- **Fix scope:** Shared provider state-transition architecture.
- **Required tests:** Active/inactive edit for every provider; sync failure, cancellation, process death, success, old-catalog availability, and stale-epoch isolation.
- **Resolution (2026-08-10):** Provider edits now create an encrypted candidate configuration revision and keep the last committed provider/catalog active until a forced replacement sync reaches its first fenced catalog commit. Promotion, catalog publication, and Stalker layout side effects share the Room transaction; failed/cancelled replacements retain the prior configuration and active catalog, while stale/deleted WorkManager revisions become no-ops. Rollback cleanup is cancellation-safe and fenced to the still-current uncommitted revision, so an older edit cannot restore over a newer edit or over a candidate that already committed. Stalker authentication failure caches are cleared before the explicit save-with-verification-pending retry. Focused repository, revision-DAO, catalog-fence, and worker coverage exercises successful promotion, failure, cancellation, process-recovery fencing, and retry isolation.

### BACKUP-001 — Restore is non-atomic across Room, preferences, and recording alarms

- **Classification / severity:** Resolved (2026-07-29) — High
- **Where:** `BackupManagerImpl.kt:344-434,693-778,995-1080`.
- **Current behavior:** Provider/library/history commit in Room, followed by DataStore preferences, presets, and schedules/alarms. Later exceptions return generic failure without rollback. `REPLACE_EXISTING` cancels the old recording before the replacement is known to schedule.
- **Why / scenario:** One result hides irreversible commits. Room and several preferences can change before a later write fails, yet UI reports total failure; failed schedule replacement can leave neither schedule.
- **Recommended correction:** Use a durable sectioned restore with validated plan, checkpoints, and complete/partial/failed-before-commit outcomes. Snapshot/compensate preferences and restore the prior schedule if replacement promotion fails.
- **Fix scope:** Cross-store restore protocol and result contract.
- **Required tests:** Failure/cancellation/process death at every Room/DataStore/alarm boundary; idempotent retry; replacement compensation; exact section outcomes; no duplicates.
- **Resolution:** Backup restore now records durable section checkpoints and returns explicit `complete`, `partial`, or `failed-before-commit` outcomes. The Room checkpoint commits with the Room section; preference/preset changes use a pre-import snapshot for retry/compensation; failed recording replacement compensates the new alarm without removing the prior schedule. Replaying the same backup and plan is idempotent, and partial UI state retains the source/plan for retry. Focused manager and settings-action tests cover outcome reporting, cancellation, compensation, checkpoint replay, and retry state.

### BACKUP-002 — Provider-scoped restored preferences retain obsolete database IDs

- **Classification / severity:** Resolved (2026-07-29) — High
- **Where:** Export `BackupManagerImpl.kt:118-137`; restore `558-690`; Room mapping `714-810`.
- **Current behavior:** `guideDefaultCategoryId`, `promotedLiveGroupIds`, `hiddenChannels_<providerId>`, and `hiddenCategories_<providerId>_<type>` contain source row IDs and are restored directly. Provider/group mappings never reach preferences; channel/category IDs can also change after resync.
- **Why / scenario:** Auto-generated IDs are not portable. If source provider 2 maps to target 7, `hiddenChannels_2` affects no intended channel and can affect unrelated provider 2; group/category references similarly drift.
- **Recommended correction:** Back up semantic provider/content/group identities, resolve them after Room import, then write target IDs; explicitly report unresolved references.
- **Fix scope:** Backup schema/version and identity contract.
- **Required tests:** Empty/populated targets with shifted IDs; keep/replace conflicts; changed catalog IDs; duplicate group names; unresolved references; semantic round trip.
- **Resolution:** Backup schema v9 exports semantic provider, active-provider, category, virtual-group, and channel references while retaining the legacy preference map for v8-and-older imports. Restore resolves providers and content only when the target match is unambiguous, supports changed category IDs through a unique semantic-name fallback, writes target provider/group/channel/category IDs, and reports both source-side stale references and target-side unresolved/duplicate matches as a truthful partial outcome without applying raw v9 IDs. Unresolved portable references remain retryable through the durable preference checkpoint. Focused coverage exercises semantic export/JSON round trip, shifted provider/Room/catalog IDs, empty and populated targets, keep/replace provider conflicts, duplicate group names, unresolved references, and explicit v8 compatibility.

### DELETE-001 — Post-delete alarm and sync cleanup failures are never reconciled

- **Classification / severity:** Resolved (2026-07-29) — Medium
- **Where:** `ProviderRepositoryImpl.kt:131-213`; behavior is explicit in `ProviderRepositoryImplTest`.
- **Current behavior:** Room deletion commits, then alarm cancellation and `syncManager.onProviderDeleted` use a catch-and-log helper. Success is returned without a retry marker.
- **Why / scenario:** A transient external failure becomes permanent after its provider record is gone. Orphan alarms can fire against deleted rows and provider work/cache can survive.
- **Recommended correction:** Persist a deletion tombstone, make cleanup idempotent, and enqueue unique reconciliation until all steps succeed; distinguish library-deleted from cleanup-pending diagnostics.
- **Fix scope:** Deletion workflow and external cleanup hooks.
- **Required tests:** Failure/process death after each step, reboot repair, repeated cleanup, concurrent alarm, and tombstone completion.
- **Resolution:** Provider deletion re-reads recording and reminder alarm identities inside the same Room transaction that inserts per-action tombstones and removes the provider, closing the concurrent-scheduling orphan-alarm window. The returned typed outcome truthfully reports that the library is deleted while cleanup remains pending, including the pending-action count and whether reconciliation was requested. Cleanup drains bounded batches until empty, retains tombstones on every external or database failure, propagates cancellation, and removes each tombstone only after its idempotent side effect succeeds. Unique work uses `APPEND_OR_REPLACE`, so a deletion committed while a drain is running cannot be lost; persisted work and the application-start enqueue repair pending tombstones after process restart or reboot.
- **Verification:** Focused repository and worker fixtures cover tombstone-write rollback, alarm identities committed immediately before the deletion transaction, enqueue failure, recording/reminder/sync failures, database read/diagnostic failures, process restart after each side effect but before tombstone deletion, tombstones added during a drain, repeated cleanup, cancellation, append-safe unique work, and final tombstone completion. Settings coverage verifies that cleanup-pending is surfaced distinctly from complete deletion. The focused tests and `:app:assembleDebug` passed on 2026-07-29.

### EPG-001 — Editing a reminder can persist a time whose alarm replacement failed

- **Classification / severity:** Resolved (2026-07-29) — Medium
- **Where:** `ProgramReminderManagerImpl.kt:81-127`; tests omit existing-row scheduler failure.
- **Current behavior:** Existing reminder rows update before alarm scheduling. Failure deletes only a new row, so an edit remains stored although the alarm was not replaced; cancellation also deletes first.
- **Why / scenario:** Changing lead time from 10 to 30 minutes can show/store 30 while the old 10-minute alarm remains after scheduler failure.
- **Recommended correction:** Keep the old entity, schedule replacement, then commit; compensate with the prior alarm when necessary and reconcile on startup.
- **Fix scope:** Local reminder compensation protocol.
- **Required tests:** Existing schedule/cancel/rollback failure, process death between boundaries, permission changes, startup repair.
- **Resolution:** Reminder mutations are serialized. Existing rows remain the source of truth while a replacement alarm is scheduled, and Room commits the new time only after scheduling succeeds. A Room commit failure reschedules the prior alarm; failed compensation marks the persisted row unarmed so startup repair can retry truthfully. Cancellation now cancels AlarmManager before deleting Room, restores the prior alarm when deletion fails, and verifies ambiguous delete failures before deciding whether cancellation completed. New rows are persisted unarmed until scheduling succeeds. Every application process start now reconciles persisted reminders in addition to boot, package-replacement, and exact-alarm permission broadcasts.
- **Verification:** Focused fixtures cover existing replacement failure, Room failure after replacement scheduling, rollback success/failure, cancellation failure, Room failure after cancellation, ambiguous delete completion, permission loss between capability check and scheduling, unarmed new rows, process restart between replacement/cancel side effects and Room commit, startup repair, permission-unavailable repair, and continued reconciliation after one reminder fails. `ProgramReminderManagerImplTest` and `:app:assembleDebug` passed on 2026-07-29.

### PLAY-001 — Failed live-history persistence suppresses same-channel retry for the session

- **Classification / severity:** Resolved (2026-07-29) — Medium
- **Where:** `PlayerZapActions.kt:298-321`; state `PlayerViewModel.kt:268`; candidate tests omit repository failure.
- **Current behavior:** `lastRecordedLivePlaybackKey` is assigned before asynchronous `recordPlayback`. Failure is logged without clearing it; later same-channel events return early.
- **Why / scenario:** A transient first insert failure prevents every later retry, so last-watched/recent state remains absent even after staying on or returning to the channel.
- **Recommended correction:** Separate in-flight and last-successful keys; mark success only after `Result.Success`, clear on failure/cancellation, and serialize stale completions.
- **Fix scope:** Local player/history coordination.
- **Required tests:** Failure then success, A→B→A, cancellation, concurrent duplicates, stale completion.
- **Resolution:** Live-history persistence now runs through a serialized coordinator with distinct latest-request, in-flight-attempt, and last-successful markers. Only `Result.Success` advances the successful marker; failure and cancellation clear the matching attempt in `finally`, including cancellation swallowed into a repository result. Uninterrupted same-channel duplicates are coalesced, while A→B→A creates a new A attempt so a failed first A cannot suppress the return. Obsolete queued writes are skipped before repository entry. If a stale write was already inside the repository when the channel changed, the latest-channel attempt runs after it, making the latest channel the final durable recent-history write rather than merely ignoring the stale marker completion.
- **Verification:** `PlayerZapActionsLivePlaybackTest` now drives the production coordinator across failure-then-success, successful de-duplication, A→B→A with the first A failing, concurrent duplicate events, cancellation followed by retry, a queued stale write, and a stale write already inside the repository followed by the latest-channel repair write. The focused test task and `:app:assembleDebug` passed on 2026-07-29.

### MIGRATION-001 — The current 61→62 migration and full supported chain are untested

- **Classification / severity:** Resolved (2026-07-26) — Medium
- **Where:** version `StreamVaultDatabase.kt:53`; migration `2687-2703`; registration `DatabaseModule.kt:44-105`; tests `StreamVaultDatabaseMigrationTest.kt:27-1287`.
- **Current behavior:** Production correctly defines/registers `MIGRATION_61_62`. Tests stop at 60→61/57→61; earliest full chain stops at 42 and another at 51. Nothing migrates 61→62 or historical schemas to current 62.
- **Why / scenario:** A default/entity mismatch can pass clean installs and unit tests but fail Room validation or populate wrong policies for upgrading users. This concretizes ARCH-007.
- **Recommended correction:** Test populated 61→62 and every supported release→62 using the exact production registry; make CI compare DB version, exported schema, registration, and coverage.
- **Fix scope:** Migration test infrastructure/CI; no production migration omission was found.
- **Required tests:** 61/60/57/oldest-supported→62, every provider type, defaults/FKs/indexes, registry parity, and downgrade if supported.
- **Resolution:** Added an Android Room migration test for the 61→62 production migration and verifies both newly added provider policy columns.
- **2026-07-27 implementation:** A populated v60 provider fixture now migrates through the current v66 schema and verifies retained provider data plus v62 policy defaults; v65→66 has its own schema fixture. Coverage for every supported upgrade origin and production-registry parity remains required. **Status: Needs improvement.**

## Phase 4 verification and evidence

- Read `graphify-out/GRAPH_REPORT.md` and the wiki first; graph queries traced setup, playback/recording/cast, EPG, and backup/plugin/update/migration flows before source/test verification.
- Directly verified `MIGRATION_61_62` in production `DatabaseModule`; this is a test gap, not a missing migration.
- Focused command: `./gradlew.bat :data:testDebugUnitTest :app:testDebugUnitTest --tests "com.streamvault.data.repository.ProviderRepositoryImplTest" --tests "com.streamvault.data.manager.ProgramReminderManagerImplTest" --tests "com.streamvault.data.manager.BackupManagerImplTest" --tests "com.streamvault.app.ui.screens.player.PlayerZapActionsLivePlaybackTest" --tests "com.streamvault.app.cast.CastMediaRequestFactoryTest" --no-daemon` — passed (`BUILD SUCCESSFUL`, 2026-07-11).
- Only Markdown changed. Emulator/live-TV validation and `graphify update .` do not apply because no application code changed.

## Phase 4 fix order

1. BACKUP-002 portable identities.
2. BACKUP-001 checkpointed restore and safe replacement.
3. SETUP-001 last-committed activation.
4. EPG-001 and PLAY-001 compensation markers.
5. DELETE-001 durable cleanup.
6. MIGRATION-001 release-gated fixtures.

## Phase 4 open questions

1. Should provider activation change only after a new catalog revision commits?
2. Must import be all-or-nothing, or is a durable section-level partial result acceptable?
3. What is the oldest officially supported direct upgrade?
4. Must hidden selections survive catalog row-ID changes? If so, runtime preferences also need stable identities.
5. Is cleanup-pending user-visible or diagnostics-only?
6. What bounded retry policy is acceptable for live-history writes?

## Next review targets

Phase 5 should stress these boundaries: process death/cancellation during commits and handoffs; disk full/corrupt persistence/revoked grants/clock jumps/reboot/upgrades; rapid switching and stale completion; malformed/huge data and shifted backup IDs; and runtime HLS/raw-TS/catch-up/VOD matrices across multiple channels for the required 90–120 second stuck window plus background/PiP/multiview/cast and constrained devices.

# Phase 5 — Edge cases and failure scenarios

**Status: Complete.** This phase stress-reviewed process death, reboot/package replacement, Android background-execution limits, permission revocation, storage/malformed input, clock changes, rapid switching, and stale completion. It used graph traversal, source/manifest inspection, current official Android behavior, and focused unit/lint execution. No application code was changed.

## Phase 5 findings summary

| ID | Finding | Classification | Severity |
|---|---|---|---|
| PLATFORM-001 | Boot recovery starts a prohibited `dataSync` foreground service before enqueuing durable work | Needs improvement (2026-07-26 audit) | High |
| PLATFORM-002 | Recording and downloads share the six-hour `dataSync` quota without timeout handling | Needs improvement (2026-07-26 audit) | High |
| DOWNLOAD-001 | Process-killed downloads remain permanently `DOWNLOADING` | Resolved (2026-07-29) | High |
| ALARM-001 | Exact-alarm permission revocation cancels schedules with no grant-time restoration | Needs improvement (2026-07-26 audit) | High |
| BACKUP-003 | Backup inspection/import has unbounded parsing and quadratic conflict scans | Resolved (2026-07-29) | High |
| REMINDER-001 | Notification failure is swallowed and the reminder is marked delivered | Resolved (2026-07-29) | Medium |
| CLOCK-001 | A future-dated `RUNNING` Xtream index job can suppress recovery indefinitely | Resolved (2026-07-29) | Medium |
| TEST-002 | The download manager/service lacks complete boundary coverage | Needs improvement (2026-07-29 audit) | Medium |
| COMPAT-001 | Legacy-route decoding calls an API-33 overload on API 25–32 | Needs improvement (2026-07-26 audit) | High |

## Adversarial scenario matrix

| Scenario | Observed handling | Assessment |
|---|---|---|
| Process death during staged catalog import | Epoch/staging state is persisted and active catalog promotion is transactional | Good baseline; size/cancellation defects remain in Phase 2 |
| Rapid player prepare/zap | Request generations reject stale URL resolution/preload completion | Good baseline |
| Clock moves backward for cache TTL | `ContentCachePolicy` explicitly treats negative elapsed time as stale | Good baseline |
| Process death during download | Startup reconciles the durable owner and validates the partial output before completion, resume, or safe restart | Resolved (DOWNLOAD-001) |
| Reboot with scheduled recording | Boot receiver starts a forbidden FGS before WorkManager enqueue | PLATFORM-001 |
| Exact-alarm permission revoked/re-granted | System cancels alarms; app has no grant receiver | ALARM-001 |
| Long/cumulative recording and download work | Both services consume one six-hour `dataSync` budget; neither handles timeout | PLATFORM-002 |
| Notification permission/channel changes after reminder creation | Delivery is blocked truthfully, persisted with a reason, surfaced on resume, and retried without duplicate ownership | Resolved (REMINDER-001) |
| Huge/malformed backup | Full Gson object graph is allocated before validation; preview conflict checks are nested scans | BACKUP-003 |
| Wall clock is corrected behind a persisted running-job timestamp | Future/invalid persisted event timestamps are stale; orphaned index/config work is recovered while a genuinely fresh owner is retained | Resolved (CLOCK-001) |
| Encoded legacy route on API 25–32 | Unconditionally calls an API-33 URL-decoder overload | COMPAT-001 |
| Recurring recording across DST | `ZonedDateTime.plusDays/plusWeeks` preserves local start time and fixed duration | Good baseline |
| Range server returns `200` to resume request | Partial output is discarded and restarted from zero | Good baseline |

## Detailed Phase 5 findings

### PLATFORM-001 — Boot recovery starts a prohibited data-sync foreground service before durable work

- **Classification / severity:** Confirmed defect — High
- **Where:** target SDK 36 in `app/build.gradle.kts:54-59`; `data/src/main/AndroidManifest.xml:17-25`; `RecordingRestoreReceiver.kt:7-15`; `RecordingForegroundService.kt:233-237`; service type at `data/src/main/AndroidManifest.xml:4-7`.
- **Current behavior:** On `BOOT_COMPLETED`, the receiver first calls `RecordingForegroundService.requestReconcile()`, which invokes `startForegroundService()` for a `dataSync` service. Only the next statement enqueues the durable reconciliation worker; there is no catch/finally around the foreground-service start.
- **Why / scenario:** Apps targeting Android 15+ cannot launch a `dataSync` FGS from `BOOT_COMPLETED`; Android throws `ForegroundServiceStartNotAllowedException`. StreamVault targets 36, so a reboot can abort the receiver before WorkManager enqueue. Reboot also clears alarms, allowing scheduled recordings to be missed until the user launches the app and startup reconciliation runs. This restriction is documented by [Android's foreground-service changes](https://developer.android.com/about/versions/15/changes/foreground-service-types).
- **Recommended correction:** Make WorkManager the boot entry point and enqueue it first. Do not launch a `dataSync` FGS from boot; let reconciliation schedule alarms and use the exact-alarm delivery exemption only when capture actually starts. Catch platform start rejection at every service-launch boundary and preserve a durable pending state.
- **Fix scope:** Android scheduling/service architecture.
- **Required tests:** API 35/36 boot broadcast with FGS restriction enabled; verify no receiver crash, worker enqueued, future alarms restored, overdue windows classified, and app never needs to be opened.
- **2026-07-26 audit:** The receiver now enqueues `RecordingReconcileWorker` instead of starting the service, which is the required direction. It still lacks the API 35/36 boot-restriction fixture and evidence that every service-start boundary rejects safely while preserving the pending recording. **Status: Needs improvement.**

### PLATFORM-002 — Recording and downloads share the six-hour data-sync quota without timeout handling

- **Classification / severity:** Confirmed defect — High
- **Where:** recording service `data/src/main/AndroidManifest.xml:4-7`; download service `app/src/main/AndroidManifest.xml:158-160`; `RecordingForegroundService.kt:30-107`; `DownloadForegroundService.kt:33-103`; no `Service.onTimeout()` implementation exists.
- **Current behavior:** Both foreground services declare `dataSync`. On Android 15+, that type has a shared per-app six-hour budget per 24 hours. Neither service implements `onTimeout(int,int)` or coordinates remaining quota/terminal persistence.
- **Why / scenario:** A long recording, several recordings, or recording plus downloads can exhaust the shared budget. The system calls `onTimeout`; if the service does not promptly stop, Android raises a fatal `RemoteServiceException`. Capture/download state and partial output can be left contradictory. The quota and required timeout behavior are documented in [Android 15 behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15).
- **Recommended correction:** Implement timeout handling that cancels and joins active I/O, checkpoints partial output, writes an explicit recoverable/terminal reason, and stops the service. Re-evaluate service types/APIs: user-initiated transfer jobs for downloads and an appropriate recording execution model. Coordinate one application-level quota owner.
- **Fix scope:** Recording/download execution architecture and platform compatibility.
- **Required tests:** Reduced quota via device config; one >6h recording; cumulative recording/download use; timeout during read/write/finalization; restart/reconcile; no ANR/crash and truthful partial state.
- **2026-07-26 audit:** Both services now override `Service.onTimeout`, cancel/join their owned work, persist an explicit timeout outcome, and stop themselves. A shared quota owner, reduced-quota device fixtures, and restart/finalization coverage are still absent. **Status: Needs improvement.**

### DOWNLOAD-001 — Process-killed downloads remain permanently DOWNLOADING

- **Classification / severity:** Resolved (2026-07-29) — High
- **Where:** `DownloadManagerImpl.kt:74-88,207-239,251-383`; `DownloadDao.kt:23-30`; `DownloadForegroundService.kt:50-100`.
- **Original behavior:** Capture persisted `DOWNLOADING` before network/file work. Process death prevented catch/finally from changing it. Startup/connectivity scheduling selected only `PENDING`/`PAUSED`; `getActive()` observed but did not repair `DOWNLOADING`. Although a valid service start returned `START_STICKY`, a system restart supplied no download ID, causing the service to stop without reconciliation.
- **Why / scenario:** Killing the process mid-download leaves a row and partial document that no queue path owns. Reopening Downloads or reconnecting does not resume/fail it; only manual resume restarts from zero.
- **Recommended correction:** Persist an owner generation/heartbeat and reconcile orphan `DOWNLOADING` rows at application/service initialization. If the output grant and byte count remain valid, resume with `Range`; otherwise atomically mark paused/restartable. Use durable unique work or a service command journal rather than in-memory jobs as ownership proof.
- **Fix scope:** Download state machine and durable execution ownership.
- **Required tests:** Kill after row transition, target creation, progress checkpoint, final byte, and before completion update; sticky null intent; reboot; revoked SAF grant; server `206/200/416`; exactly one resumed owner.
- **Resolution:** Download rows now persist `owner_id`, monotonic `owner_epoch`, and `heartbeat_at` through migration 68-to-69. Every scheduling entry point serializes orphan reconciliation before queue admission, and a conditional DAO update grants exactly one process owner. Recovery validates the real target length: a fully written known-length target is completed, an exact resumable checkpoint is queued with `Range`, and missing, revoked, zero-length, or mismatched output is deleted and restarted safely. Resume handling validates `Content-Range`, appends only an aligned `206`, replaces output on `200`, recognizes an exact-length `416`, and rejects malformed fresh partial responses without a restart loop. A sticky service restart with no download ID now enters the same reconciliation path instead of stopping.
- **Verification:** `DownloadRecoveryStateMachineTest` covers interruption after row transition, target creation, an exact progress checkpoint, bytes beyond the checkpoint, final bytes before the completion commit, revoked SAF access, request headers, aligned/misaligned/malformed `206`, `200`, and exact/mismatched `416`. `DownloadDaoOwnershipTest` uses Room with concurrent schedulers to prove a single claim and verifies that a new process sees the prior owner as orphaned. `DownloadForegroundServicePolicyTest` covers null, blank, and explicit sticky commands. The focused data and app tests passed; the 68-to-69 Android migration test sources compiled; and `:app:assembleDebug` passed on 2026-07-29.

### ALARM-001 — Exact-alarm permission revocation cancels schedules with no grant-time restoration

- **Classification / severity:** Resolved (2026-07-26) — High
- **Where:** permission `app/src/main/AndroidManifest.xml:16`; boot-only receivers in `data/src/main/AndroidManifest.xml:17-35`; schedulers under `data/.../manager/{recording/RecordingAlarmScheduler,reminder/ProgramReminderAlarmScheduler}.kt`; no `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` receiver exists.
- **Current behavior:** Scheduling checks `canScheduleExactAlarms()`, but restoration listens only for boot/package replacement. When permission is later granted, no receiver rechecks permission and restores persisted recording/reminder alarms.
- **Why / scenario:** Android stops the app and cancels all future exact alarms when `SCHEDULE_EXACT_ALARM` is revoked. On re-grant it broadcasts `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` specifically so apps can restore alarms. StreamVault misses that event; recording repair waits for app startup and reminders have no startup restoration at all. See [Android's exact-alarm guidance](https://developer.android.com/develop/background-work/services/alarms).
- **Recommended correction:** Add one manifest receiver for the grant action, recheck capability, and enqueue durable idempotent restoration for both subsystems. Persist/display “permission missing / schedule not armed” separately from `SCHEDULED`, and reconcile on app startup as a fallback.
- **Fix scope:** Shared exact-alarm lifecycle and user-visible schedule state.
- **Required tests:** Revoke (alarms disappear), grant broadcast, rapid grant/revoke race, app stopped, reboot, package replace, overdue reminder/recording, duplicate restoration, and permission denied after data restore.
- **Resolution:** Both persisted schedule restore receivers now handle Android's exact-alarm permission-state broadcast and reuse their existing idempotent recording/reminder restoration paths.
- **2026-07-27 implementation:** Recording runs and reminders now persist whether their exact alarm is armed. Restoration explicitly rechecks the capability, marks eligible entries unarmed when unavailable, and marks them armed only after rescheduling succeeds. Revoke/grant race and stopped-app device coverage remain required. **Status: Needs improvement.**

### BACKUP-003 — Backup inspection/import has unbounded parsing and quadratic conflict scans

- **Classification / severity:** Resolved (2026-07-29) — High
- **Where:** `BackupManagerImpl.kt:232-340,344-359,513-518`; nested preview scans at `271-317`; no size/count/depth limits are defined.
- **Current behavior:** Inspection and import deserialize the entire stream into `BackupData` with Gson before version, structure, or checksum validation. Preview then performs incoming-list `count` operations containing linear `any` scans over existing groups/favorites/history/recordings. `OutOfMemoryError` is not caught by the surrounding `catch (Exception)`.
- **Why / scenario:** A mistakenly selected huge JSON file or an oversized legitimate backup can allocate an unbounded object graph, perform quadratic work, freeze/kill a TV process, and never return the intended corruption result. Checksum validation happens too late to protect admission.
- **Recommended correction:** Enforce compressed/uncompressed byte, nesting, per-section count, and field-length limits while streaming. Validate header/version and compute checksum incrementally. Build hash-indexed conflict keys for O(n+m) preview and reject oversized sections with typed diagnostics.
- **Fix scope:** Backup format reader/admission architecture.
- **Required tests:** Oversized seekable/non-seekable URI, deep JSON, million-entry/duplicate-heavy sections, long strings, malformed/truncated input, cancellation, low-heap fixture, and linear-time conflict benchmark.
- **Resolution:** Inspection and import now share a bounded streaming reader that admits the version header first, limits the input to 16 MiB, rejects nesting beyond 64 levels, caps every materialized section, and rejects strings beyond 8,192 characters before Gson can allocate the corresponding object graph. Admission failures carry typed reasons, cancellation is propagated, trailing/duplicate/malformed input is rejected, and canonical SHA-256 verification is streamed through a digest output. Preview conflict detection uses hash-indexed keys, including recording channel/URL alternatives, for O(n+m) behavior.
- **Verification:** `BackupManagerImplTest` covers oversized seekable and non-seekable streams, early unsupported/missing headers, depth, million-entry section rejection, duplicate fields, long strings, malformed/truncated JSON, cancellation, and a 20,000-by-20,000 duplicate-heavy recording benchmark. `:data:lowHeapBackupAdmissionTest` runs the focused suite in a 128 MiB test JVM. Both focused tasks passed on 2026-07-29.

### REMINDER-001 — Notification failure is swallowed and the reminder is marked delivered

- **Classification / severity:** Resolved (2026-07-29) — Medium
- **Where:** `ProgramReminderNotifier.kt:20-42`; `ProgramReminderManagerImpl.kt:153-163`; scheduling-time UI gate in `NotificationPermissionGate.kt:36-103`.
- **Original behavior:** `notify()` was wrapped in `runCatching` and returned no outcome. `deliverReminder()` unconditionally wrote `notifiedAt` after the call. The UI checked permission when creating a reminder, but permission/channel state could change later.
- **Why / scenario:** If notifications are revoked, the channel is disabled, or notify throws at delivery time, the user sees nothing while the row permanently records successful delivery and cannot be retried or diagnosed.
- **Recommended correction:** Return a typed notification result, check runtime/channel state at delivery, and commit `notifiedAt` only after accepted delivery. Persist a blocked/failed reason and surface it when the app resumes; avoid blind repeated alerts.
- **Fix scope:** Reminder delivery result/state model.
- **Required tests:** Permission revoked after scheduling, channel disabled, notify exception, process death between notify and DB update, retry/deduplication, stale reminder, and reboot.
- **Resolution:** Reminder delivery now uses typed `Accepted`, `Blocked`, and `Failed` outcomes and checks both app-level notification permission and the reminder channel's importance. Migration 69-to-70 persists delivery state, attempt token/time/count, and failure reason. A conditional DAO claim grants one delivery owner; terminal writes require the same attempt token, so concurrent or stale completions cannot overwrite a newer attempt. `notifiedAt` is committed only after `NotificationManager` accepts the stable-tag notification. If the process dies after posting but before that commit, startup checks the app's active notification and completes the interrupted attempt without reposting; if no notification exists, it retries safely with the same stable tag. Blocked/failed reminders are retried during startup, reboot, and guide foreground reconciliation, remain visible through the stale grace period, and surface their persisted reason in the guide. Stale reminders are dismissed without notifying.
- **Verification:** `ProgramReminderManagerImplTest` covers permission loss, channel blocking, notifier exceptions, cancellation, stale reminders, already-delivered deduplication, process death before and after posting, due-reminder retry, and reboot restoration. `ProgramReminderNotifierPolicyTest` verifies app and channel admission. `ProgramReminderDeliveryDaoTest` proves one concurrent attempt owner and rejects a stale completion token. `ProgramReminderIssueMessageTest` verifies persisted failures are surfaced, and the complete `EpgViewModelTest` suite remains green. Focused tests passed, migration 69-to-70 Android test sources compiled, and `:app:assembleDebug` passed on 2026-07-29.

### CLOCK-001 — A future-dated running Xtream index job can suppress recovery indefinitely

- **Classification / severity:** Resolved (2026-07-29) — Medium
- **Where:** `ProviderSyncWorker.kt`; `ContentCachePolicy.kt`; shared policy in `PersistedTimestampPolicy.kt`; related persisted-event gates in `ProviderConfigRevisionDao.kt`, `EpgSourceRepositoryImpl.kt`, `SettingsProviderActions.kt`, and `AppUpdateCheckPolicy.kt`.
- **Original behavior:** A `RUNNING` job was treated as fresh when `(now - updatedAt) < 15 minutes`. The earlier local correction rejected future timestamps, but then fell through to the previous successful-index TTL; an orphaned `RUNNING` job could therefore still be skipped when `lastSuccessAt` was recent. Durable `SYNCING` provider revisions and several persisted “last event” gates also used raw wall-clock comparisons that extended suppression after a rollback.
- **Why / scenario:** A process dies with a running job, then the device clock moves backward by hours/days. Launch/periodic stale checks repeatedly skip the orphan job and its VOD/series index remains partial.
- **Recommended correction:** Treat negative age as stale, matching `ContentCachePolicy`, or centralize persisted-age calculation with bounded clock-skew rules. Use monotonic time only for in-process durations and wall time plus explicit skew handling across restarts.
- **Fix scope:** Local gate plus shared timestamp policy audit.
- **Required tests:** Future timestamp, backward/forward jumps, process death, exact threshold, zero/invalid timestamps, and no duplicate live owner.
- **Resolution:** `PersistedTimestampPolicy` now defines one wall-clock freshness rule: missing, non-positive, future, and threshold-expired timestamps are stale. `ContentCachePolicy` and the Xtream worker use it. A `RUNNING` index row now returns directly from the owner-freshness decision, so every stale/invalid orphan is recoverable regardless of an older `lastSuccessAt`; a genuinely fresh row remains the live owner and suppresses duplicate recovery. Provider-revision recovery admits future/invalid `SYNCING` rows after process death, while retaining a recent owner. The shared audit moved persisted EPG refresh, provider auto-sync, and app-update event backoff gates onto the same rule. Absolute schedule timestamps remain wall-clock semantics; in-process elapsed timing is not represented by this persisted policy.
- **Verification:** `PersistedTimestampPolicyTest`, `ProviderSyncWorkerTest`, `ContentCachePolicyTest`, `ProviderConfigRevisionClockRecoveryTest`, `EpgRefreshClockPolicyTest`, `SettingsProviderActionsTest`, and `AppUpdateCheckPolicyTest` cover future/backward and forward jumps, process-death rows with a recent prior success, exact threshold, zero/negative/missing values, Room recovery admission, and retention of a fresh owner. The focused 33-test matrix passed with zero failures, and `:app:assembleDebug` passed on 2026-07-29.

### TEST-002 — The download manager/service lacks complete boundary coverage

- **Classification / severity:** Needs improvement (2026-07-29 audit) — Medium
- **Where:** `DownloadManagerImpl.kt`, `DownloadForegroundService.kt`, `DownloadDao.kt`; focused recovery, ownership, and service-policy tests now exist, but the wider execution matrix remains incomplete.
- **Current behavior:** DOWNLOAD-001 now has executable state-machine, Room concurrency, and sticky-command regression coverage. Disk-full writes, cancellation at every I/O/commit boundary, retry/backoff ceilings, timeout execution, and concurrent enqueue/playback still lack end-to-end adapter or device coverage.
- **Why / scenario:** DOWNLOAD-001 and platform timeout behavior can ship while broad builds and unrelated UI tests remain green.
- **Recommended correction:** Extract a deterministic download state machine with injected clock/dispatcher/output/network adapters, add Room/MockWebServer unit tests, and add API 35/36 service/process-death instrumentation.
- **Fix scope:** Testability refactor plus unit/instrumentation suite.
- **Required tests:** All DOWNLOAD-001 cases, disk full, permission loss, 200/206/416, unknown length, cancellation at each boundary, retry ceiling/backoff, service null intent, timeout, and concurrent enqueue/playback.
- **2026-07-29 audit:** The DOWNLOAD-001 subset is covered and passing. The remaining failure and platform boundaries above are still required, so this broader finding is not resolved.

### COMPAT-001 — Legacy-route decoding calls an API-33 overload on API 25–32

- **Classification / severity:** Resolved (2026-07-26) — High
- **Where:** minimum SDK 25 in `app/build.gradle.kts:58`; `ExternalNavigationRequest.kt:80-90`; entry from `MainActivity.kt:342-357`; lint report `app/build/reports/lint-results-debug.html`.
- **Current behavior:** Query parsing unconditionally calls `URLDecoder.decode(String, Charset)`, an overload available only from API 33. Legacy external routes can reach it on every supported Android version. Existing route tests contain only unencoded/blank values and run on the host JVM.
- **Why / scenario:** On an API 25–32 TV, an external provider/detail route containing `importUri` or `returnRoute` query data invokes a method absent from the platform and can fail before navigation. Lint reported 16 `NewApi` errors in total; some other sites have indirect guards or catch failures, but this path has neither.
- **Recommended correction:** Use the API-compatible `URLDecoder.decode(value, Charsets.UTF_8.name())` overload (with malformed-escape handling), or isolate versioned decoding behind an annotated implementation. Make lint a required release gate after triaging the existing backlog rather than accepting a permanently red task.
- **Fix scope:** Local decoder plus minimum-SDK compatibility/testing policy.
- **Required tests:** API 25, 26, 28, 32, and 33 instrumented route parsing; encoded UTF-8, `+`, malformed `%`, duplicate keys, long values, provider import URI, and return route; lint must have zero unsuppressed runtime/API errors.
- **Resolution:** Legacy routes now use the API-compatible string-charset decoder and discard malformed query values rather than crashing navigation.
- **2026-07-26 audit:** The local API-compatibility change and a JVM malformed-value test are present. API-25--32 device smoke coverage and the required zero-unsuppressed-runtime/API-lint gate are still absent. **Status: Needs improvement.**

## Phase 5 verification and evidence

- Read the graph report/wiki and queried recovery, storage, clock/alarm, and rapid-switch relationships before source inspection.
- Verified target SDK 36, merged service types, receiver ordering, absence of exact-alarm grant handling and `Service.onTimeout`, DAO queue predicates, and absence of download-focused tests.
- Focused unit command covering cache skew, provider stale checks, catalog staging, recording/reminders/backup, and player identity/zapping passed (`BUILD SUCCESSFUL`, 2026-07-12).
- `:app:lintDebug` failed after 4m10s with 221 errors, 1,326 warnings, and 11 hints. The first failure was COMPAT-001; lint reported 16 `NewApi` errors plus manifest, resource, Compose, opt-in, and compatibility findings. The full HTML/text reports were generated under `app/build/reports/` and `app/build/intermediates/lint_intermediate_text_report/`.
- Official Android documentation was used only for version-dependent platform behavior; local source establishes that the affected code paths apply.
- Only this report changed, so `graphify update .` and live-playback emulator validation were not applicable. Runtime platform fixtures remain required before claiming fixes.

## Phase 5 fix order

1. PLATFORM-001, ALARM-001, and COMPAT-001 before the next target-36/min-SDK-25 release.
2. PLATFORM-002 quota/timeout-safe capture and transfer ownership.
3. DOWNLOAD-001 durable orphan reconciliation, developed with TEST-002.
4. BACKUP-003 bounded streaming admission and indexed preview.
5. REMINDER-001 truthful delivery outcomes.
6. CLOCK-001 shared skew-safe age handling and systematic lint triage.

## Phase 5 open questions

1. What maximum continuous/cumulative recording duration must be supported on Android 15/16?
2. May interrupted downloads retain partial SAF documents, or must recovery restart from zero for compatibility?
3. What hard backup byte/count/depth limits fit the lowest supported TV memory class?
4. Should reminders/recordings remain visibly `SCHEDULED` when exact-alarm permission is absent, or use a distinct unarmed state?
5. What clock skew should invalidate persisted running/cooldown state?
6. Which API 35/36 physical/emulator devices are part of release qualification for boot, quota, and permission tests?

## Next review target

Phase 6 should consolidate all Phase 1–5 findings into one deduplicated remediation program: release blockers first, then shared lifecycle/cancellation and data-integrity work, provider correctness, performance/bounds, migration/test gates, ownership by module, sequencing dependencies, and concrete acceptance criteria.

# Phase 6 — Consolidated remediation plan

**Status: Complete.** This phase consolidates all 56 findings from Phases 1–5 into eight deduplicated work packages, with dependency order, module ownership, acceptance gates, and release criteria. It is a remediation plan, not an implementation: no application code was changed.

## Executive conclusion

The reviewed build should be treated as **not ready for an unrestricted target-SDK-36/min-SDK-25 production release** until Work Package 0 is complete. The immediate blockers are missed recording restoration after reboot, unhandled Android `dataSync` service limits, exact alarms that are not restored after permission changes, a confirmed minimum-SDK API call, and an untested latest database migration boundary.

The next risk tier is systemic rather than provider-specific: cancellation does not reliably stop network/file work, terminal recording/timeshift state can precede resource closure, and several multi-store workflows report outcomes that do not match committed state. Provider/session identity and bounded ingestion follow closely because they can cross-contaminate accounts, overwrite user providers, misrestore data, or exhaust TV memory/storage.

The codebase has solid foundations worth preserving: explicit Room migrations, catalog staging/atomic promotion, request-generation guards in player preparation, provider-scoped persistence in ordinary browse flows, focused Stalker/Xtream tests, range-response handling, and recurring-recording DST arithmetic. The remediation program builds around those mechanisms.

## Final finding inventory

| Measure | Count |
|---|---:|
| Total findings | 56 |
| High severity | 35 |
| Medium severity | 21 |
| Phase 1 — architecture/flow | 8 |
| Phase 2 — shared infrastructure | 15 |
| Phase 3 — providers | 17 |
| Phase 4 — feature flows | 7 |
| Phase 5 — edge/failure scenarios | 9 |

No Critical-severity finding was assigned. “High” includes confirmed defects, highly likely defects, and architectural concerns; suspected/architectural findings must be validated at their stated boundary before a broad rewrite.

## Dependency order

```text
WP0 Platform release safety ───────────────┐
WP7 Test and release gates ────────────────┼── continuous verification
                                           │
WP1 Cancellation and resource ownership ───┼──> WP2 Durable workflows
                 │                         ├──> WP4 Bounded ingestion/provider correctness
                 └─────────────────────────┘

WP3 Stable provider/session identity ─────────> WP2 backup/plugin/state portability

WP1 + WP2 + WP3 behavior contracts ──────────> WP5 orchestration decomposition

WP1 player lifecycle + WP2 persistence policy > WP6 playback performance
```

WP0 and the initial WP7 harness can start immediately. WP1 establishes the cancellation/resource contract used by WP2 and WP4. WP3 defines durable identities before backup/plugin migration. WP5 decomposition starts after behavior is characterized and protected by tests. WP6 then applies the settled lifecycle/persistence policy to performance work.

## Consolidated work packages

### WP0 — Platform release safety

- **Findings:** PLATFORM-001, PLATFORM-002, ALARM-001, COMPAT-001, MIGRATION-001.
- **Objective:** Make the declared API 25–36 support window and Android 15/16 background execution reliable before release.
- **Primary ownership:** `app`/`data` manifests; recording/download services and receivers; WorkManager scheduling; navigation compatibility; Room migration tests.
- **Deliverables:** WorkManager-first boot recovery; exact-alarm grant restoration and unarmed state; `Service.onTimeout`/quota-safe terminal handling or replacement APIs; API-compatible route decoding; populated 61→62 and supported-version→62 migration fixtures.
- **Exit criteria:** API 25 route/deep-link smoke passes; API 35/36 boot restriction and reduced `dataSync` quota fixtures pass; revoke/re-grant restores exactly one alarm; Room production registry parity and every supported upgrade fixture pass; no unsuppressed runtime/API lint error.

### WP1 — Cancellation and resource ownership

- **Findings:** INFRA-001, NET-001, REC-001, LIFE-001, LIFE-002, STALKER-003, XTREAM-001, PLUGIN-003.
- **Objective:** Make cancellation terminate owned I/O and prevent stale work from publishing state.
- **Primary ownership:** shared HTTP adapter in `data`; provider clients; recording capture; player/timeshift lifecycle; plugin Messenger preparation.
- **Deliverables:** one cancellable OkHttp coroutine/stream adapter; `runSuspendCatching` that rethrows cancellation; owned `ActiveCapture` and engine/timeshift close contracts; request/session generations for provider/plugin chains; bounded per-handler plugin deadlines with parallel eligibility discovery.
- **Exit criteria:** cancellation of never-header/mid-body calls closes call/body/output within a fixed test deadline; no retry/error/status write after deliberate cancellation; recording stop/cancel joins writer before returning; engine release unregisters callbacks and no stale cleanup stops a new session; plugin/provider cancellation terminates the active IPC/network operation.

**Implementation evidence (2026-07-27):**

- Shared cancellation: `CancellableOkHttpTest` covers pre-header cancellation, a blocked body read, and parser failure/response closure. `Wp1CancellationPolicyTest` checks cancellation-first handling in all five WP1 workers and rejects the known raw suspend/network `runCatching` regressions.
- Providers: the existing Stalker and Xtream service suites remain green. `OkHttpStalkerCancellationTest` proves handshake cancellation issues no later recipe request and that request-budget exhaustion issues no request beyond the configured maximum.
- Recording/player: `ActiveCaptureTest` proves repeated cancellation joins terminal cleanup; `TimeshiftOwnershipTest` proves writer cleanup precedes file deletion. The implementation registers lazy captures before start, serializes engine cleanup jobs, unregisters callbacks synchronously on terminal release, and closes timeshift calls before joining capture.
- Plugins: `PluginPlaybackRoutingTest` proves explicit ownership/priority and one five-second discovery-plus-handler deadline; `StreamVaultPluginOwnerTest` retains package/service/manifest isolation. `PluginMessengerClient` unbinds in `finally` and no longer converts an ancestor timeout into a plugin-owned timeout.
- **Session completion (2026-08-10):** INFRA-001, NET-001, STALKER-003, and XTREAM-001 are resolved against their recommended cancellation matrices. `OkHttpStalkerCancellationTest` covers profile, credential, module, streamed-catalog, body, cookie, and fallback cancellation; `ProviderWorkflowRunnerTest` and `SyncWorkerPolicyTest` cover cancellation without durable failure/success publication; and `XtreamProviderTest` covers every public facade operation, compatibility fallback, streaming facade, and genuine network-error typing.
- The full `:data:testDebugUnitTest` suite now runs 924 tests with zero failures (2026-08-11), including the repaired Stalker replay fixtures.
- REC-001, LIFE-001, LIFE-002, and PLUGIN-003 retain their separate capture, lifecycle-stress, and bound-service cancellation matrices.

### WP2 — Durable and truthful workflows

- **Findings:** SETUP-001, BACKUP-001, BACKUP-002, BACKUP-003, DELETE-001, EPG-001, PLAY-001, DOWNLOAD-001, REMINDER-001, CLOCK-001, WORK-002, UPDATE-001, PERSIST-001, ARCH-008.
- **Objective:** Ensure persisted state, external side effects, and returned results describe the same committed outcome across failure, process death, and retry.
- **Primary ownership:** provider repository/setup; backup manager/schema; downloads; reminders/recordings; preferences; update/startup workers.
- **Deliverables:** pending provider revisions and commit-time activation; checkpointed section-level backup import; stable-ID mapping from WP3; bounded streaming backup reader; deletion tombstones; alarm/reminder compensation; in-flight/success history markers; orphan-download reconciliation; typed retry/permanent outcomes; skew-safe timestamps; DataStore corruption policy; one startup-work registry.
- **Exit criteria:** fault injection at every DB/DataStore/alarm/file boundary produces `complete`, `partial`, or `failed-before-commit` truthfully; retry is idempotent; previous active provider/schedule survives failed replacement; process-killed downloads become resumable/failed rather than stuck; update/reconciliation backoff distinguishes attempt from success; corrupt preferences reach a documented recovery state.

**Session completion (2026-08-10):**

- **SETUP-001 — Resolved:** encrypted candidate revisions, forced replacement validation, fenced first-catalog promotion, cancellation-safe rollback, stale/deleted revision no-ops, and Stalker candidate-state restoration are implemented and covered by focused tests.
- **ARCH-008 — Resolved in code; device validation pending:** one durable per-provider workflow/lease lane now fences generations and commits, manual/background work shares the provider chain, stale revision input is rejected before workflow creation, and durable workflow state is authoritative with legacy status only as a migration fallback. Instrumentation packaging passes; connected-device process-kill/manual-refresh execution remains outstanding.
- The remaining WP2 findings are unchanged and are not being marked complete by this session.

### WP3 — Provider, account, and content identity isolation

- **Findings:** ARCH-001, ARCH-002, STALKER-001, STALKER-002, STALKER-005, STALKER-006, JELLYFIN-002, PLUGIN-001, PLUGIN-002, XMLTV-002.
- **Objective:** Prevent one provider/account/plugin/source from mutating or resolving another's state.
- **Primary ownership:** domain provider contracts; Stalker/Jellyfin HTTP/session factories; plugin registry; EPG source model; provider/content persistence.
- **Deliverables:** capability-based provider interfaces and provider-specific configuration/runtime records; per-provider Stalker cookie/session containers; typed authentication-expiry handling; provider-zone date parsing; collision-resistant remote identity; account-specific Jellyfin image resolution; package/component-scoped plugin IDs and explicit ownership; stable source timezone policy.
- **Exit criteria:** concurrent same-host/different-account fixtures prove cookie/image/session isolation; nonnumeric IDs remain unique across restart/migration; plugin uninstall cannot alter user-owned M3U data; absent/renamed plugins reconcile deterministically; shared XMLTV sources require explicit timezone or preserve per-assignment semantics.

**Implementation evidence (2026-07-30):**

- Plugins: reconciliation deletion is now package/service-component scoped and runs both at startup and on package lifecycle broadcasts. Manifest retrieval failure cannot masquerade as uninstall; a sole manifest-ID rename retains and atomically re-keys its provider ownership, while ambiguous mappings are left untouched. Focused ownership-policy tests cover cross-package ID reuse, multiple services, rename retention, ambiguity, and component absence.
- Jellyfin: image authentication now performs an exact provider-ID lookup without a stale account cache and always removes internal routing metadata before network dispatch. Parallel same-server account, base-path, edit/delete, and pre-authenticated request tests pass.
- Status policy: PLUGIN-001 and PLUGIN-002 were changed from “resolved” to implemented-but-open. Their complete ownership creation, restore, uninstall/replace/reinstall, active/combined-source, and adoption-policy matrices are still required for WP3 closure.

**ARCH-001/ARCH-002 staged closure evidence (2026-08-11):**

- Domain and execution boundary: stable `Provider`, sealed typed configurations, immutable `ProviderSnapshot`, generation-bound Stalker observations, seven explicit capability interfaces, complete/unique provider registries, typed capability absence/restriction, and deletion of `IptvProvider`.
- Runtime routing: setup, full/section/native-guide sync, movie/series hydration, on-demand guide, playback, catch-up, and recording source resolution route through registry adapters. `SyncManager` retains lock/progress/commit-fence ownership for WP5, but no longer switches on `ProviderType` for full, section, or guide dispatch. Jellyfin VOD/series retry and optional configured M3U guide behavior are covered.
- Persistence/compatibility: Room 72→73 adds encrypted typed configuration, account/runtime, and generation-bound learning storage; 73→74 rebuilds stable providers and validates all foreign keys. Typed pending revisions retain legacy recovery decoding. Backup v11 is config-only for Stalker transient state and v0–10 imports remain supported.
- Verification: domain 125/125, data 924/924, and app 279/279 JVM tests pass; Android-test sources compile. Focused Room 72→73 and 72→74 migration tests pass on the available API 36 emulator with all four provider types, encrypted credentials, pending revisions, Stalker learning, active state, dependent catalog rows, stable IDs, and zero FK violations.
- Remaining evidence: API 25 migration execution is unavailable in the installed Android SDK/emulator set. The broader historical migration instrumentation class is not yet a clean release gate because 16 older schema-fixture tests still fail independently of the passing 72→74 cases. ARCH-001 and ARCH-002 are therefore implemented in code, with this device/migration evidence explicitly pending rather than claimed complete.

### WP4 — Bounded ingestion and provider correctness

- **Findings:** SYNC-001, STALKER-004, XTREAM-002, M3U-001, M3U-002, XMLTV-001, JELLYFIN-001.
- **Objective:** Make catalog/EPG ingestion finite, resumable, and semantically correct for valid provider variations.
- **Primary ownership:** sync admission/store; M3U/XMLTV parsers; Stalker/Xtream/Jellyfin paging and caches.
- **Deliverables:** shared decompressed-byte/row/category/field/error-ratio limits enforced at admission; Stalker pagination with continuation/cycle detection instead of a silent page-200 success; failure-aware adult-category cache; BOM/charset-aware M3U parsing and complete header EPG handling; path-based extension classification; XMLTV decompressed limits; paged/bounded Jellyfin libraries.
- **Exit criteria:** chunked/compressed expansion and exact-limit fixtures terminate predictably while preserving the active catalog; pagination never declares arbitrary truncation complete; transient cache failures retry; query-bearing VOD classifies correctly; memory/disk use stays within approved low-TV budgets.

### WP5 — Orchestration and ownership decomposition

- **Findings:** ARCH-003, ARCH-004, ARCH-005, ARCH-006, ARCH-007, SYNC-002, WORK-001, MEMORY-001.
- **Objective:** Reduce god-object blast radius after behavior contracts are stable.
- **Primary ownership:** `SyncManager`, playback resolver, `PlayerViewModel`, plugin discovery, database migration organization, worker registry, long-lived caches.
- **Deliverables:** provider-neutral sync coordinator with provider strategies and section runners; split playback resolution/session construction/persistence; feature-scoped player coordinators; async cached plugin discovery; version-grouped migration files/registry; provider+epoch progress flows; correct maintenance constraints; bounded caches with deletion hooks.
- **Exit criteria:** no unkeyed global progress; independent providers cannot clear each other's state; major coordinators have explicit dependency budgets and no data-layer construction; migration registry remains complete; 100,000-key stress remains bounded; local maintenance runs offline; architecture tests enforce module boundaries.

### WP6 — Playback persistence and performance

- **Findings:** PERF-001, PERF-002.
- **Objective:** Remove continuous main-thread/file/Room/launcher churn during playback.
- **Primary ownership:** `Media3PlayerEngine`, playback snapshot store, playback history repository, Watch Next/recommendations.
- **Deliverables:** coalescing IO actor for optional diagnostics; debounced in-memory VOD progress with forced lifecycle/content-switch flush; incremental Watch Next update; material-transition recommendation refresh.
- **Exit criteria:** no file I/O on main; proposed steady-state ceiling of one durable progress write per 30 seconds (subject to product loss tolerance); forced background/stop flush; two-hour virtual playback meets explicit DAO/ContentResolver/write-count budgets with no duplicate flush.

### WP7 — Test and release gates

- **Findings:** TEST-001, TEST-002.
- **Objective:** Turn the failure scenarios in this review into release-blocking automated and device tests.
- **Primary ownership:** all modules plus CI/release engineering.
- **Deliverables:** resolve MPEG-TS policy test against a real long-lived fixture; deterministic download state-machine tests; migration matrix; cancellation/fault-injection utilities; provider replay fixtures; API 25/35/36 platform suite; lint triage/baseline policy; performance counters.
- **Exit criteria:** intended MPEG-TS policy and test agree; download process-death/range/SAF/quota matrix passes; zero test task failures; lint has zero unsuppressed errors and warnings cannot increase; release artifacts publish test evidence described below.

## Finding coverage audit

Every finding appears in exactly one primary work package:

| Work package | Finding count | IDs |
|---|---:|---|
| WP0 | 5 | PLATFORM-001, PLATFORM-002, ALARM-001, COMPAT-001, MIGRATION-001 |
| WP1 | 8 | INFRA-001, NET-001, REC-001, LIFE-001, LIFE-002, STALKER-003, XTREAM-001, PLUGIN-003 |
| WP2 | 14 | SETUP-001, BACKUP-001, BACKUP-002, BACKUP-003, DELETE-001, EPG-001, PLAY-001, DOWNLOAD-001, REMINDER-001, CLOCK-001, WORK-002, UPDATE-001, PERSIST-001, ARCH-008 |
| WP3 | 10 | ARCH-001, ARCH-002, STALKER-001, STALKER-002, STALKER-005, STALKER-006, JELLYFIN-002, PLUGIN-001, PLUGIN-002, XMLTV-002 |
| WP4 | 7 | SYNC-001, STALKER-004, XTREAM-002, M3U-001, M3U-002, XMLTV-001, JELLYFIN-001 |
| WP5 | 8 | ARCH-003, ARCH-004, ARCH-005, ARCH-006, ARCH-007, SYNC-002, WORK-001, MEMORY-001 |
| WP6 | 2 | PERF-001, PERF-002 |
| WP7 | 2 | TEST-001, TEST-002 |
| **Total** | **56** | All Phase 1–5 findings |

Cross-package references are expected—for example, WP2 backup identity depends on WP3—but the table assigns one accountable primary owner to prevent duplicate tracking.

## Recommended delivery waves

### Wave A — Release safety and characterization

- Complete WP0.
- Establish WP7 platform, migration, download, cancellation, and live-playback harnesses needed by subsequent waves.
- Freeze new background-service types, provider identity formats, and migration version changes until their contract tests exist.

### Wave B — Stop and ownership semantics

- Complete WP1 shared cancellation adapter and recording/player ownership first.
- Migrate Stalker, Xtream, recording, EPG, downloads, backup, and plugin calls in bounded slices.
- Require cancellation/fault-injection evidence for each migrated subsystem.

### Wave C — Durable state and identity

- Define WP3 provider/account/plugin/content stable identity schema.
- Implement WP2 provider edit, backup, delete, alarm, reminder, history, and download state machines against that schema.
- Add migrations and round-trip/process-death fixtures before enabling new backup format or ownership behavior.

### Wave D — Bounded provider ingestion

- Complete WP4 shared admission policy, then provider/parser-specific correctness.
- Validate on low-memory/storage profiles and retain the last committed catalog on every rejection/failure.

### Wave E — Decomposition and performance

- Complete WP5 coordinator splits one behavior-preserving seam at a time.
- Complete WP6 after player lifecycle and persistence contracts from WP1/WP2 are stable.
- Enforce module/complexity/cache bounds in CI so god objects and unbounded registries do not regrow.

## Release acceptance gates

### Build and compatibility

- `assembleDebug`, all unit tests, migration instrumentation, and selected device tests pass.
- Lint contains zero unsuppressed errors; an owned warning baseline may only decrease.
- API 25, 28, 33, 35, and 36 smoke covers startup, Settings, external routes/imports, provider setup, playback, PiP where supported, downloads, and alarms.

### Lifecycle and failure behavior

- Network/file/IPC cancellation stops owned resources promptly and produces no post-cancellation commit.
- Process death is injected at every multi-store transition; restart yields one deterministic owner and an idempotent outcome.
- Boot, package replacement, exact-alarm revoke/grant, notification revoke, SAF revoke, disk full, clock jump, and reduced FGS quota have explicit expected states and no crash/ANR.

### Provider and ingestion matrix

- At least two providers/accounts of each supported type run concurrently without cookie/image/progress/identity crossover.
- Stalker replay covers auth/session expiry, recipe fallback, timezone/DST, nonnumeric IDs, and pagination beyond historical limits.
- Xtream/M3U/XMLTV/Jellyfin fixtures cover compressed/chunked/oversized/malformed/duplicate/paged inputs at and beyond every configured limit.
- Plugin install/update/remove/crash/timeout cannot mutate user-owned provider records.

### Playback and recording

- Live TV validation follows the repository protocol on more than one channel: preferably 61 screenshots at two-second cadence, continued frame-hash progression, media session `PLAYING` with `error=null`, and no fatal/stuck/unintended MPEG-TS fallback logs.
- Raw MPEG-TS and HLS each run beyond the historical 90–120 second stuck window; catch-up/VOD test seek, renewal, background, PiP, multiview, cast, and alternate-stream recovery.
- Recording stop/cancel/timeout joins all I/O before terminal state; boot/alarm/quota/process-death fixtures preserve or truthfully fail schedules and outputs.

### Persistence, backup, and performance

- Every supported Room schema migrates to current with representative provider/content state and registry parity.
- Backup round trip uses stable identities on shifted-ID targets, enforces hard limits, reports partial sections truthfully, and is idempotent after interruption.
- No continuous playback file I/O occurs on main; progress/launcher/database counters stay within approved budgets.
- Long-lived maps/caches remain within fixed bounds and provider deletion evicts owned entries.

## Initial implementation batch

The first code batch should be small enough to review independently while removing immediate release hazards:

1. Change recording boot recovery to enqueue WorkManager first and remove boot-time `dataSync` FGS launch.
2. Add exact-alarm grant receiver plus shared idempotent recording/reminder restoration.
3. Add timeout-safe terminal handling for recording/download services and reduced-quota device tests.
4. Replace the API-33-only URL-decoder overload and add API-25 route fixtures.
5. Add `MIGRATION_61_62` and oldest-supported→62 populated tests using the production registry.
6. Add orphan-`DOWNLOADING` reconciliation and its first focused download tests.

This batch does not require the large architecture split and creates the safety harness needed for WP1–WP6.

## Governance and ownership

- Assign one technical owner per work package and one reviewer from an affected neighboring module.
- Track findings by their stable IDs; a finding closes only when its required tests and work-package exit criteria pass.
- Any scope change must record which acceptance criterion replaces the original one.
- Architectural/suspected findings close through measured evidence or an implemented boundary, not file movement alone.
- Each release candidate publishes: supported API/provider matrix, migration range, lint/test status, live-playback evidence, and unresolved accepted risks.

## Residual questions requiring product decisions

1. Oldest supported direct app/database upgrade version.
2. Approved hard limits for M3U, XMLTV, Jellyfin, backup sections, fields, and decompressed bytes on the lowest-memory TV.
3. Maximum acceptable VOD progress loss and durable-write cadence.
4. Maximum supported continuous/cumulative recording duration on Android 15/16 and partial-output behavior at quota/space loss.
5. Whether backup import must be all-or-nothing or may expose durable section-level partial success.
6. Stable identity authority for third-party plugins and whether plugin-created providers may be adopted by users.
7. Whether shared XMLTV sources require one explicit timezone or support provider-specific interpretation.
8. Whether provider sync concurrency remains supported and what global resource budget applies during playback/recording.

## Phase 6 verification and final status

- Re-read the graph report/wiki and queried cancellation, provider identity, Android scheduling, and player lifecycle clusters. The graph confirmed that `PreferencesRepository`, `SettingsViewModel`, provider services, `Media3PlayerEngine`, and sync metadata are the main cross-package change surfaces; source-backed phase findings remain authoritative.
- Parsed every Phase 1–5 summary and assigned all 56 stable IDs exactly once: 5 + 8 + 14 + 10 + 7 + 8 + 2 + 2 = 56.
- Severity totals reconcile to 35 High and 21 Medium.
- No code or build configuration changed in Phase 6. Only this Markdown report changed, so `graphify update .`, application builds, and emulator validation were not required for this consolidation step.
- **Review status:** all six planned phases are complete. Implementation and remediation remain future work.

### ARCH-003 implementation status addendum (2026-08-12)

Compatibility projection is isolated in `SyncProviderSnapshotAdapter`/`SyncManagerPlanDelegate`; workers and repositories consume role-specific sync ports, with distinct Hilt entry-point names for commands and lifecycle cleanup. Stalker hydration checkpoint reads/writes and Xtream/Stalker index failure classification are outside `SyncManager`. Added coverage exercises cancellation before/during/after Room catalog commits, all persisted Xtream/Stalker index states and Stalker legacy-state round trips, every provider plan's supported/unsupported repair sections and typed partial-activation contract, and same-provider manual/background serialization. The complete `:data:testDebugUnitTest` suite and `:app:assembleDebug :app:testDebugUnitTest` pass.

The former full-sync-vs-repair and WorkManager coverage gaps now have instrumentation fixtures: the real streaming M3U importer converges through production Room DAOs for full versus section repair, and all four provider worker enqueue APIs share one serialized WorkManager chain. The fixtures compile, but real device/emulator execution remains pending because this environment has no connected device or installed emulator/AVD.

### ARCH-003 post-review corrections (2026-08-12)

Review of the complete dirty tree found both inherited defects and extraction regressions. Xtream category slices now advance their durable cursor only after a category succeeds or returns an accepted empty result; a failed category stops the slice at its own cursor, and cancellation coverage verifies that a preselected slice cannot skip unprocessed categories. Legacy `PARTIAL` jobs whose old cursor already passed failed work restart a repair sweep. Stalker summary indexing now enters `ProviderWorkLockRegistry` before its narrower summary lock, so database-maintenance admission observes the work. Progress sessions retain the exact paired state-session token, preventing a stale run from finishing its replacement. Unsupported/restricted/configuration section and guide resolutions now publish terminal failure state and guide-job status instead of leaving `Syncing`/`RUNNING` snapshots behind.

The same review corrected adjacent dirty-tree regressions: plugin discovery sorting again applies to both normal and timeout results; TV-input refresh replacement uses identity-checked, lazily-started job ownership; Stalker authentication mutexes use stable provider-scoped identities and are no longer cache-evicted; and the Room annotation plus migration registry share one compile-time database-version constant. Focused state, cursor/cancellation, legacy-partial repair, lock-architecture, migration, and unsupported-section tests pass. The complete validation run passed 978 data tests, 279 app tests, and 196 player tests with zero failures/errors/skips, and rebuilt the debug APK.

### ARCH-003 activation, scheduling, and equivalence closure slice (2026-08-12)

Production M3U, Jellyfin, Xtream, and Stalker executors now report accepted staging mutations and truthful catalog activation receipts. Movie/series shell repairs report deferred activation with typed durable index continuations; full syncs report committed catalog activation independently from background index/guide work, so queued follow-up no longer falsely marks a healthy active catalog partial. Xtream Live repair now also queues the Live index backfill, closing a stale-search regression discovered while wiring the contract.

`SyncCoordinator` validates receipts and owns continuation handoff to `ProviderContinuationScheduler`; provider executors no longer construct or enqueue durable work. Catalog plans consume request snapshots through one delegate instead of re-projecting compatibility providers or bypassing the delegate for selected providers. App startup, Home, Dashboard, Settings, and Player no longer inject the concrete `SyncManager`; they consume lifecycle, command, or state ports, and the unused Player dependency was removed.

`CatalogSyncEquivalenceIntegrationTest` exercises the real streaming M3U importer, staging tables, activation transactions, and production Room DAOs for a full import versus separate Live/Movie repairs. `ProviderWorkManagerSerializationTest` exercises the real enqueue functions for provider refresh, Xtream index, Stalker index, and background EPG and verifies that they occupy one provider-scoped serial chain. Both Android fixtures compile. The complete JVM validation now passes 981 data tests, 279 app tests, and 196 player tests with zero failures/errors/skips and rebuilds the debug APK. One initial aggregate run saw three `StalkerTransportFactoryTest` MockWebServer timeouts; the class passed immediately in isolation and the complete rerun passed. Device/emulator execution of the Room/WorkManager instrumentation tests is the remaining ARCH-003 closure evidence; `adb` finds no connected device and no emulator/AVD is installed here.
