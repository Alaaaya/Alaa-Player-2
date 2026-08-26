# Theme 7–20 Compliance Gap Audit

## Preserved work

The six completed packages—`cinematic`, `glass`, `minimal`, `neon`, `premium`, and `streaming`—are present and must not be rewritten. Their existing data wiring, D-pad callbacks, preview handoff, player integration, and selector registration remain outside the expansion work.

## Non-compliant draft identified

The initial expansion draft introduced a shared `ExpansionThemeSystem` that uses one Live TV structure and one fullscreen-player structure with conditional parameters. Although it varies spacing, naming, focus scale, palettes, and action labels, it does **not** satisfy the attached no-shared-visual-architecture rule. It must therefore be treated as a temporary routing reference only and replaced before any Theme 7–20 is considered complete.

## Required replacement standard

Each of the fourteen new packages must own independent composables for the app shell/navigation, dashboard, Live TV, movie browse/detail, series browse/detail/seasons/episodes, search, favorites/recently watched, settings/login presentation, dialog and focus treatment, and fullscreen player with an independently structured bottom action bar. Shared ViewModels, domain models, repositories, routes, player engine, and callback contracts remain shared.

## Build status

Source-level implementation can proceed. Runtime compilation is blocked in the current environment because no Android SDK path is configured; the project reports `SDK location not found`. This is an environment limitation, not an application test result.
