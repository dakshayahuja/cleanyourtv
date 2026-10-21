# TCL 55Q6C (G08, Google TV, Android 14), India

TCL 55Q6C QD-Mini LED, device `G08`, model "Smart TV Pro", product `G08_4K_GB`, firmware `UTT2.250416.001 AU02`,
MediaTek `tcl9618`, about 2.4 GB RAM. The same board and build as the Q7C, 85RM7L and QM6K lists. Set up on Wi-Fi
in India with a PS5 on HDMI 1. Both halves work: the debloat list and the Clean Your TV launcher as Home, with a
PS5 tile in the dock.

| File | What it is |
|---|---|
| `debloat.txt` | The 27 packages that were disabled, grouped and commented |
| `config.json` | The theme: 7-app dock with PS5 first, other apps hidden behind the top-bar library button, cinematic home, remote and colour buttons. Push it with `tools/theme.sh` |
| `ps5-shortcut/` | A ~20 KB app whose only job is to switch the TV to the PlayStation's HDMI port, so it can sit in the dock |
| `restore-all.sh` | Undoes everything below in one go |

**How much testing is behind it:** one set, 22 October 2026. Three debloat batches with a screenshot after each,
then a reboot; launcherx and setupwraith afterwards; then two more reboots with the launcher as Home. Home, Input,
Settings, YouTube, Netflix, Prime and the four colour buttons were pressed on the real remote. ADB was closed
afterwards and `tools/adb-closed.py` saw "Connection refused".

All commands run from the repo root with the TV's IP in `TV_HOST` and USB debugging on.

## Before you start

- **macOS Local Network privacy.** Homebrew's Python got `No route to host` while the system Python reached the TV.
  Build the venv on the system Python: `/usr/bin/python3 -m venv tools/venv && tools/venv/bin/pip install -r tools/requirements.txt`.
- **Build tools without sudo.** `brew install --cask temurin@17` wants a password; `brew install openjdk@17` and
  `brew install --cask android-commandlinetools` do not. Then
  `sdkmanager --sdk_root=/opt/homebrew/share/android-commandlinetools "platforms;android-34" "build-tools;34.0.0" "platform-tools"`
  and pass `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` and
  `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` to `build-and-install.sh`.
- **Wi-Fi owned by setupwraith.** `tools/tv.py "dumpsys wifi | grep -o 'cname=[^ ]*'"` named setupwraith as the
  creator of the home network. Forget the network in Settings and join it again so `com.android.tv.settings` owns
  it, then check again. Disabling setupwraith deletes the networks it still owns (an old unused one here).

## Install

```bash
# 1. Debloat (launcherx is skipped here by disable.sh's Home guard)
TV_HOST=<tv-ip> tools/disable.sh examples/tcl-55q6c-google-tv/debloat.txt

# 2. PS5 shortcut
(cd examples/tcl-55q6c-google-tv/ps5-shortcut && ./gradlew assembleDebug)
TV_HOST=<tv-ip> tools/venv/bin/python tools/tv.py --push \
  examples/tcl-55q6c-google-tv/ps5-shortcut/app/build/outputs/apk/debug/app-debug.apk /data/local/tmp/ps5.apk
TV_HOST=<tv-ip> tools/venv/bin/python tools/tv.py 'pm install -r /data/local/tmp/ps5.apk; rm /data/local/tmp/ps5.apk'

# 3. Launcher as Home (starts it, checks Home resolves to it, only then disables launcherx)
INPUT_KEY_SERVICE=1 HOME_KEY_SERVICE=1 TV_HOST=<tv-ip> launcher/build-and-install.sh

# 4. Let TCL's boot manager start the key services (see Remote buttons), then the theme
TV_HOST=<tv-ip> tools/venv/bin/python tools/tv.py 'appops set com.gathin.tvhome AUTO_START allow; appops write-settings'
TV_HOST=<tv-ip> tools/theme.sh examples/tcl-55q6c-google-tv
```

Change `greeting_name` in `config.json` to your own name, or remove it.

## Undo everything

```bash
TV_HOST=<tv-ip> examples/tcl-55q6c-google-tv/restore-all.sh
```

## Remote buttons

Every button arrives as scancode 240 and TCL's `TclKeyLayoutMap` turns it into a keycode by HID usage, so map by
keycode. Found with `logcat -d -v time | grep TIDD` after pressing the real buttons.

| Button | Keycode | Without Google TV's home | Here |
|---|---|---|---|
| Home | 3 | works | |
| Input | 178 | works: TCL's own source panel | |
| Settings (gear) | 83 | handled by TCL | `settings_key_keycodes: [83]` |
| YouTube | 190 | does nothing | `app_keys` → YouTube |
| Netflix | 4062 | does nothing | `app_keys` → Netflix |
| Prime Video | 4099 | does nothing | `app_keys` → Prime Video |
| (app button) | 4166 | does nothing | unmapped |
| TCL Channel | 4020 | tries to open the disabled `com.tcl.waterfall.overseas` | unmapped |
| (app button) | 4159 | does nothing | unmapped |
| Red / Green / Yellow / Blue | 183 / 184 / 185 / 186 | | YouTube / Spotify / Hotstar / PS5 |

**The app buttons did nothing at first even though `build-and-install.sh` said both services were enabled.**
`dumpsys accessibility` showed `Bound services:{}` and `appops get com.gathin.tvhome` showed
`AUTO_START: ignore`: TCL's boot manager refuses to bind sideloaded services. `appops set com.gathin.tvhome
AUTO_START allow; appops write-settings` fixed it, and it survived two reboots (the same fix as the C89B note in
`docs/TROUBLESHOOTING.md`). Re-pass both `*_KEY_SERVICE=1` flags on every reinstall.

## PS5 tile

`ps5-shortcut/` is a separate app, not a launcher change: one translucent activity that looks for the TV input whose
label contains "playstation" (the CEC name), switches to its parent HDMI port with `ACTION_VIEW` on
`TvContract.buildChannelUriForPassthroughInput`, and finishes. If the PS5 is off and CEC has forgotten it, it falls
back to `com.tcl.tvinput/.TvPassThroughService/HW15`, HDMI 1 on this set; change `FALLBACK` for another port.
The banner is the PlayStation app icon from Wikimedia Commons (`PlayStation_App_Icon.jpg`), placed on a
640×360 field with its own gradient; it is Sony's trademark, fine on your own TV.

## Launcher options added for this set

Three launcher changes in `launcher/`, each off by default and switched on in `config.json`:

- `compact_inputs`: Inputs becomes a round icon-only button (`ic_input_source`, a screen with an arrow going in)
  just left of the library button, instead of the labelled pill at the start of the top bar. The top bar's
  left-edge guard now uses whichever button is leftmost.
- `wrap_dock`: Right on the last dock app goes to the first, Left on the first to the last.
- With `library_button` on, the "App library" pill after the last row is left out: the top-bar button is the way in.

## Notes for this set

- **Game Master** (`com.tcl.gamebar`) is untouched. Its settings are only offered while an HDMI game source is on
  screen, so they look missing from Home; with the PS5 tile pressed they are there. `GAME_MODE_HDMI_1=1` and
  `tcl_sys_hdmi_144vrr_config_hdmi1=1` stayed as they were.
- **Continue watching rows** are not possible with a third-party launcher here: the shared `watch_next_program` and
  `preview_program` tables were empty, because Netflix, YouTube, Prime and Hotstar feed Google TV's home through
  Google's servers.
- The UI runs at a 1920×1080 override on the 3840×2160 panel; that is stock.
- Screenshots of HDMI and Live TV come back empty; check `dumpsys tv_input` for the session's `inputId` instead.
