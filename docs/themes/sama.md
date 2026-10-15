# Sama (سما)

Sama is an independent selectable presentation built on the existing BespokeThemeUi contracts. It keeps the data layer, channel ordering, favorites, playback engine, recording and provider handling unchanged. Existing themes are untouched.

The presentation uses a fullscreen live preview, charcoal translucent channel drawer, top navigation, crossfading artwork, red broadcast badges/progress, white circular transport buttons and soft animated atmosphere. Home, movie and series browsing/details, search, saved library/history, settings and both live/VOD overlays have theme-local implementations.

There is deliberately no program-guide grid or timeline. A legacy guide route renders a simple selectable channel list. No guide button is exposed in Sama.

Genre tiles request a matching actual provider category using English, German and Arabic keywords. If a provider has no matching category the category chooser remains available rather than inventing channels.

The clock uses the device's time and locale. Weather is not shown because the app has no weather data contract. Rewind/forward use the existing real seek actions; the host does not expose previous/next-channel callbacks through PlayerOverlayParams. Channel changes use Channels.

Validation must distinguish compilation/APK inspection from device testing. No Android device or emulator was attached during implementation. Playback stability and remote-control navigation are not certified by a successful build.
