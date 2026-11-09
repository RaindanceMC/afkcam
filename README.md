# RaindanceMC/afkcam

- **`afkcam-module`** — Rain's Flexible Modules — AFK Cam: whoever is away sees their own character from a slow, cinematic camera circling them (close, wide from high up, low looking up, from straight above, gliding from shot to shot), at sunset, with music and drifting sparks, until they press a key. Their arm and HUD step aside while it runs: only their client is told it spectates (never for Bedrock players, only on solid ground). Only their own screen changes: they stay in their game mode where they are, so farms, item pickup and mob spawning around them carry on as before — and so does the performance module's slowdown of away players' farms.
- **`afkcam-standalone`** — Rain's AFK Cam: the module and the standard wrapper, shaded together.

Who is away is Core's `Away` (the essentials module's `/afk` and timeout). The camera is Core's `ui.camera.PlayerCamera` (2.80.0): the game's camera packet sent to the one player, pointed at an item display only they can see.

`/afkcam` switches it for yourself (`on | off | status`), `/afkcam preview` shows it now. Settings page "AFK camera".

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework).

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `afkcam-standalone/target/RainsAfkCam-0.1.0.jar` — needs RainsCore 2.80.0 or newer

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.

End to end: `AfkCamScenarioTest` in [e2e](https://github.com/RaindanceMC/e2e).
