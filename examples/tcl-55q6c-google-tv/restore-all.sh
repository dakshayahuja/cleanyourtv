#!/usr/bin/env bash
# Undo everything done on the TCL 55Q6C: bring Google TV's home back, re-enable every disabled package, reset the
# launcher theme, remove the PS5 shortcut and give the launcher's AUTO_START op back to TCL's default.
# Run from the repo root with USB debugging on:  TV_HOST=192.168.0.15 examples/tcl-55q6c-google-tv/restore-all.sh
set -euo pipefail
D="$(dirname "$0")"
tools/restore.sh "$D/debloat.txt" com.google.android.apps.tv.launcherx
tools/theme.sh --reset || true
tools/venv/bin/python tools/tv.py 'pm uninstall tv.cleanyourtv.ps5' || true
tools/venv/bin/python tools/tv.py 'appops set com.gathin.tvhome AUTO_START default; appops write-settings' || true
