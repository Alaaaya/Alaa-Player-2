# Android Build Environment Note

The sandbox originally had no Android SDK configured. Android SDK Platform 36, Build Tools 36.0.0, and Java 17 were subsequently provisioned locally for source verification.

The project now gets through SDK discovery and begins dependency/module compilation, but Gradle's single-use daemon exits unexpectedly before it reaches `:app:compileDebugKotlin`. Retrying with one worker and a 1 GB heap produces the same daemon-disappearance result, so this is an environment-capacity failure rather than a confirmed source failure.

After stopping unrelated development processes and retrying with a 1.5 GB heap, Gradle reached `:app:compileDebugKotlin`. Its final daemon log contained no Kotlin `e:` or `error:` diagnostics for the retained Blue Ocean source files before the daemon exited unexpectedly. This supports, but does not replace, a successful full compile in a stable Android build environment.

A final in-process compilation attempt reached `:app:compileDebugKotlin`, lost the Kotlin compiler daemon through an RMI EOF, entered its fallback compilation strategy, and then made no further progress for five minutes. It was stopped to avoid consuming sandbox resources. The process did not emit a source diagnostic before it was stopped.

After reducing the project heap to 1.5 GB and using a single worker, compilation produced three actionable Red Cinema diagnostics. The invalid `weight` imports and a mixed `horizontal`/`bottom` padding call were corrected. A subsequent compile reached Kotlin again, exceeded the available 1.5 GB heap, and stopped making progress after four minutes without a new source diagnostic; it was terminated to protect the sandbox. Full verification remains pending a higher-memory Android build environment.

Increasing the Gradle heap to 2 GB did not produce additional Kotlin diagnostics. The daemon again disappeared while `:app:compileDebugKotlin` was running. Further retries are deferred because they only consume the constrained sandbox without advancing source verification.

After adding the Blue Ocean EPG and settings surfaces, a source check identified and corrected one `Border` argument-order error in `BlueOceanEpgSurface.kt`. The corrected check reached `:app:compileDebugKotlin` without another `e:` diagnostic, then exhausted the configured 1.5 GB heap and remained at 98%; it was stopped after roughly four minutes. This is not a successful build.

After routing Red Cinema through the dashboard, Live TV, EPG, libraries, details, search, favourites, settings, provider setup, dialogs, and fullscreen player, `:app:compileDebugKotlin` again reached 98% without an emitted source diagnostic. The check was stopped at high memory pressure before a final Gradle result, so a successful Android build remains outstanding.

Android's official documentation states that command-line packages are installed under `android_sdk/cmdline-tools/version/bin/` and that `ANDROID_HOME` should identify the SDK directory. The same documentation confirms that Build Tools are required for Android builds.[^tools]

[^tools]: Android Developers, [Command-line tools](https://developer.android.com/tools), accessed 2026-08-26. See also [Android SDK Command-Line Tools release notes](https://developer.android.com/tools/releases/cmdline-tools).
