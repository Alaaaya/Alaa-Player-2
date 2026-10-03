#!/usr/bin/env sh

set -u
# Installed application id of this fork (instrumentation classes stay under com.streamvault).
APP_ID="${APP_ID:-com.alaaaya.alaaplayer2.debug}"

artifact_dir="${PLATFORM_SMOKE_ARTIFACT_DIR:-build/platform-smoke-diagnostics}"
device_serial="${ANDROID_SERIAL:-emulator-5554}"
active_suite="${PLATFORM_SMOKE_ACTIVE_SUITE:-unknown}"

mkdir -p "$artifact_dir"
printf '%s\n' "$active_suite" > "$artifact_dir/active-suite.txt"

capture() {
  output_file=$1
  shift
  "$@" > "$artifact_dir/$output_file" 2>&1 || true
}

capture adb-devices.txt adb devices -l
capture device-properties.txt adb -s "$device_serial" shell getprop
capture instrumentation.txt adb -s "$device_serial" shell pm list instrumentation
capture app-package.txt adb -s "$device_serial" shell dumpsys package "$APP_ID"
capture app-processes.txt adb -s "$device_serial" shell dumpsys activity processes "$APP_ID"
capture crash-buffer.log adb -s "$device_serial" logcat -b crash -d -v threadtime
capture recent-logcat.log adb -s "$device_serial" logcat -d -v threadtime -t 4000
capture dropbox-app-crash.txt adb -s "$device_serial" shell dumpsys dropbox --print data_app_crash
capture app-crash-report.txt adb -s "$device_serial" shell run-as "$APP_ID" cat files/diagnostics/crash/latest-crash.txt
