# Thalj fixture captures

These seven 1920×1080 Arabic RTL captures render the actual production Thalj
composables and shell through Robolectric API 34 native graphics. They are not
redrawn mockups, screenshots from a physical TV, or proof of real playback.
All titles, lists, metadata, and channel names are fake fixtures. A bundled mountain
photograph is reused as fixture artwork, synchronously decoded before composition
and served through Coil's official FakeImageLoaderEngine. Requests for any
unexpected artwork fail the test. The player surface is a plain fixture background,
not decoded video. The Live TV preview has no player engine. Settings uses the
production navigation rail, its complete nine-entry order, theme index 8, and the
production themes section with a mocked view model. No app/server credentials or
customer data are used.

Run the Thalj screenshots workflow on kat/thalj-screens, or run
:app:testDebugUnitTest --tests com.streamvault.app.shots.ThaljShotsTest.
PNG output is under app/build/outputs/thalj-shots.
The workflow also uploads the JUnit results and HTML report.
No main-branch, release, APK publication, or production UI changes are intended.
