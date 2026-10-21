# My TV: TCL 55Q6C

This fork belongs to Dakshay Ahuja. **The TV it is set up for is a TCL 55Q6C**, and everything for it lives in
[`examples/tcl-55q6c-google-tv/`](examples/tcl-55q6c-google-tv/). When asked to change "my TV", "the TV" or "the
launcher" in this fork, that is the set and that is the folder: start from its README, not from another TCL list.

## The set

| | |
|---|---|
| Model | TCL 55Q6C QD-Mini LED, 55" |
| Board / product | `G08`, model "Smart TV Pro", `G08_4K_GB`, MediaTek `tcl9618`, ~2.4 GB RAM |
| OS / firmware | Google TV, Android 14 (SDK 34), `UTT2.250416.001 AU02` |
| Network | Wi-Fi, IP `192.168.0.15`, ADB on port 5555 (closed when not in use) |
| HDMI | PS5 on HDMI 1 (CEC name "PlayStation 5", input `com.tcl.tvinput/.TvPassThroughService/HW15`) |
| Mac | macOS; use the **system Python** for `tools/venv` (Homebrew's Python is blocked by Local Network privacy) |

## Current state (22 October 2026)

- **Debloated:** the 27 packages in `examples/tcl-55q6c-google-tv/debloat.txt`, all `pm disable-user --user 0`,
  none uninstalled. Includes `launcherx` (Google TV home) and `setupwraith`.
- **Home:** the Clean Your TV launcher (`com.gathin.tvhome`), theme `examples/tcl-55q6c-google-tv/config.json`:
  dock PS5 · YouTube · Spotify · Netflix · Hotstar · Twitch · Prime, 7 columns, other apps hidden behind the
  top-bar library button, cinematic home, greeting "Dakshay", `compact_inputs`, `wrap_dock`.
- **PS5 tile:** `tv.cleanyourtv.ps5`, source in `examples/tcl-55q6c-google-tv/ps5-shortcut/`.
- **Remote:** YouTube 190, Netflix 4062, Prime 4099, Red 183 → YouTube, Green 184 → Spotify, Yellow 185 →
  Hotstar, Blue 186 → PS5. Needs `appops set com.gathin.tvhome AUTO_START allow; appops write-settings`, or the key
  services never bind.
- **Kept on purpose:** all streaming apps, Play Store / Services, Chromecast (`mediashell`), voice (`katniss`),
  AirPlay, Miracast, Game Bar / Game Master (`com.tcl.gamebar`), every `com.tcl.tv*`, `com.mediatek.*`, `com.dolby.*`.

## Working on it

1. Ask the owner to switch **USB debugging** on (Settings → System → Developer options) and accept the prompt.
2. `TV_HOST=192.168.0.15 tools/venv/bin/python tools/tv.py --probe`
3. Every reinstall of the launcher: `INPUT_KEY_SERVICE=1 HOME_KEY_SERVICE=1` with the JDK/SDK paths from the
   example README, then `tools/theme.sh examples/tcl-55q6c-google-tv`.
4. When done, ask the owner to switch USB debugging off, revoke the authorisations and reboot, then
   `tools/venv/bin/python tools/adb-closed.py 192.168.0.15` must say "Connection refused".

The owner's rules: show exact package names and get approval before disabling anything, `pm disable-user` only
(never uninstall, root, flash or factory reset), keep a rollback for every change, and don't touch the stock
launcher until the replacement is tested. Undo everything: `TV_HOST=192.168.0.15 examples/tcl-55q6c-google-tv/restore-all.sh`.
