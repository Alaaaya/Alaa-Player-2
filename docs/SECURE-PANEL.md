# Android panel protocol v2

Backend remains the original jytjcdlmygwqizgydeyp project. The bundled anon key is public, not a management/service credential. Only v2 RPCs are called. Tokens never enter URLs, logs, display codes, analytics or screenshots. The dedicated HTTP client has no provider interceptors, logging, HTTP cache, unsafe TLS or redirects; response size is bounded.

## Storage and identity

Six SecureRandom bytes form the uppercase 12-hex display code; 32 independent bytes form the lowercase 64-hex token. One encrypted SharedPreferences record commits code, token, protocol history, backoff and cached provider configuration together before registration. Android Keystore-backed AES encryption uses the already-declared AndroidX security dependency. No plaintext fallback. A nonsecret initialized marker detects a missing secure record; bad Keystore/decryption/integrity fails closed. Android backup is already disabled by the existing manifest; this patch does not change it. Clearing app data/uninstall destroys the identity and requires a new activation.

## Registration and polling

Fresh installations automatically register `alaa_player`, then poll pending every minute. Registration retries always use the committed identity. A collision/unavailable reply requires operator attention, never silently rotates or takes over a binding. HTTP/network errors and SQL rate limits persist exponential backoff with jitter; forced refresh cannot bypass it. `retry_after` is honored up to seven days. Active devices keep the previous 30-minute background refresh and refresh on activity resume subject to the request/backoff guard.

A pending registration can renew only after the backend specifically returns `registration_expired`, and local history proves successful registration plus a pending response, with no active observation, legacy identity or ambiguous/inactive/unauthorized history. No other status rotates a bound token.

## Offline and denial policy

A successfully authorized encrypted cache may be used offline until its explicit subscription expiration. A null expiration has no local time limit, matching the existing offline policy; remote revocation cannot be learned without network connectivity. Every app open/resume refreshes silently, respecting backoff. On a known expiration, inactive/expired/unauthorized reply, or invalid/missing secure identity, cached authorization is not used. Panel-managed providers are removed; manually added providers are not deleted.

Every DI-provided main, preview and multiview engine is guarded. Denial stops current engines before provider deletion, clears preloads/timeshift, and blocks previously authorized provider origins. A current stream may be a CDN redirect, so stopping all current engines is deliberate and conservative; manually added providers can be selected again. Origin denial is also conservative: a manually added playlist on the same origin may be blocked until the panel authorizes that origin again. A secure-storage failure leaves all playback blocked until operator recovery. The guard does not claim to revoke provider credentials outside this app or prevent manually entering them elsewhere.

## Existing code-only devices

The old code is retained exactly, with a new random token saved securely. The old code-only provider cache is not accepted as v2 authorization and stale panel providers are removed even when offline. Legacy installations never call public registration and never automatically claim a new pending row. They call authenticated-by-token config only, which returns unauthorized until physically verified admin pairing.

An authenticated operator must physically verify this TV and transfer its generated token privately through local trusted tooling to `admin_pair_legacy_device`. Do not put the token in the display-code field, screenshot, email or public log. This release deliberately does not add a public token-export screen, QR transfer or automatic legacy takeover. A secure operator transfer UI remains a separate deployment prerequisite for migrating existing TVs; until that exists these legacy installations stay fail-closed. Never work around this by re-enabling code-only RPCs. Lost token or revoked binding requires admin-assisted re-pair, not a fresh automatic claim.

## Validation

Pure protocol/parser/diff tests exercise identity entropy/format, persistence-before-request, retry identity stability, pending discovery, activation history, expired pending proof, legacy lockout, cache denial, HTTP 401/403/429 and malformed responses. CI additionally compiles the complete app, runs targeted panel unit tests and packages the APK. No real Android TV or Keystore hardware test is claimed by these host tests.
