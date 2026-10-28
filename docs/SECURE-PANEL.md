# Android panel protocol v2

Backend remains the original jytjcdlmygwqizgydeyp project. The bundled anon key is public, not a management/service credential. Only v2 RPCs are called. Tokens never enter URLs, logs, display codes, analytics or screenshots. The dedicated HTTP client has no provider interceptors, logging, HTTP cache, unsafe TLS or redirects; response size is bounded.

## Storage and identity

Six SecureRandom bytes form the uppercase 12-hex display code; 32 independent bytes form the lowercase 64-hex token. One encrypted SharedPreferences record commits code, token, protocol history, backoff and cached provider configuration together before registration. Android Keystore-backed AES encryption uses the already-declared AndroidX security dependency. No plaintext fallback. A nonsecret initialized marker detects a missing secure record; bad Keystore/decryption/integrity fails closed. Android backup is already disabled by the existing manifest; this patch does not change it. Clearing app data/uninstall destroys the identity and requires a new activation.

## Registration and polling

Fresh installations automatically register `alaa_player`, then poll pending every minute. Registration retries always use the committed identity. A collision/unavailable reply requires operator attention, never silently rotates or takes over a binding. HTTP/network errors and SQL rate limits persist exponential backoff with jitter; forced refresh cannot bypass it. `retry_after` is honored up to seven days. Active devices keep the previous 30-minute background refresh and refresh on activity resume subject to the request/backoff guard.

A pending registration can renew only after the backend specifically returns `registration_expired`, and local history proves successful registration plus a pending response, with no active observation, legacy identity or ambiguous/inactive/unauthorized history. No other status rotates a bound token.

## Offline and denial policy

A successfully authorized encrypted cache may be used offline until its explicit subscription expiration. A null expiration has no local time limit, matching the existing offline policy; remote revocation cannot be learned without network connectivity. Every app open/resume refreshes silently, respecting backoff. On a known expiration, inactive/expired/unauthorized reply, or invalid/missing secure identity, cached authorization is not used. Panel-managed providers are removed; manually added providers are not deleted.

Every DI-provided main, preview and multiview engine is guarded. Panel grant deletion/replacement or denial stops only affected provider engines before provider mutation and clears their preloads/timeshift. Provider IDs and monotonic process-local authorization generations travel through stream resolution and plugin copies, so a redirected CDN stream stays attributable and stale streams cannot restart after reauthorization. Generations are captured before resolver network activity. Manual accounts on the same host remain independent; empty pending refreshes never stop manual playback. Denied provider IDs persist in encrypted storage. A secure-storage failure leaves all playback blocked until operator recovery. The guard does not revoke provider credentials outside this app or prevent manually entering them elsewhere.

Onboarding reserves a unique inert provider identity, persists its panel ownership, then uses the repository edit path. This path checks real-account collisions before modifying configuration and refuses collisions with manual providers instead of adopting them. Partial setup stays denied; only a successfully matching configuration fingerprint restores its grant.

## Existing code-only devices

The old code is retained exactly, with a new random token saved securely. The old code-only provider cache is not accepted as v2 authorization and stale panel providers are removed even when offline. Legacy installations never call public registration and never automatically claim a new pending row. They call authenticated-by-token config only, which returns unauthorized until physically verified admin pairing.

An authenticated operator must physically verify this TV and transfer its generated token privately through local trusted tooling to `admin_pair_legacy_device`. Do not put the token in the display-code field, screenshot, email or public log. This release deliberately does not add a public token-export screen, QR transfer or automatic legacy takeover. A secure operator transfer UI remains a separate deployment prerequisite for migrating existing TVs; until that exists these legacy installations stay fail-closed. Never work around this by re-enabling code-only RPCs. Lost token or revoked binding requires admin-assisted re-pair, not a fresh automatic claim.

## Validation

Pure protocol/parser/diff tests exercise identity entropy/format, persistence-before-request, retry identity stability, pending discovery, activation history, expired pending proof, legacy lockout, cache denial, HTTP 401/403/429 and malformed responses. CI additionally compiles the complete app, runs targeted panel unit tests and packages the APK. No real Android TV or Keystore hardware test is claimed by these host tests.
