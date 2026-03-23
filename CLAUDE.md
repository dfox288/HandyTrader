# Handy Trader - Fabric Mod

## Project Overview

A Fabric mod that adds star/favorite toggles to the villager trading UI. Favorited trades get a gold highlight overlay so they can be found instantly in long trade lists. Per-villager storage keyed by UUID, persisted as JSON.

## Tech Stack

- **Minecraft**: 26.1-rc-3 (first unobfuscated version — no mappings needed)
- **Fabric Loader**: 0.18.4
- **Fabric API**: 0.143.14+26.1
- **Fabric Loom**: 1.15.5
- **Java**: 25
- **YACL**: 3.9.0+26.1-fabric (soft dependency, via Modrinth Maven `maven.modrinth:yacl`)
- **ModMenu**: 18.0.0-alpha.6 (soft dependency)

## Build

```bash
cd /Users/dfox/Development/minecraft/HandyTrader && ./gradlew build
cd /Users/dfox/Development/minecraft/HandyTrader && ./gradlew runClient
cd /Users/dfox/Development/minecraft/HandyTrader && ./gradlew genSources
```

Always prefix commands with `cd /path &&` so they auto-approve via permission rules.

## Project Structure

Uses `splitEnvironmentSourceSets()`:
- `src/main/` — shared code (client + server)
- `src/client/` — client-only code (UI overlay, config screen)

Package: `net.rezanmb.handytraders`

## Dependencies

YACL and ModMenu are **soft dependencies** — `compileOnly`/`localRuntime` in build.gradle. The mod works without them. Config screen code checks `FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")` at runtime.

Maven repos:
- `https://maven.terraformersmc.com/` — ModMenu
- `https://api.modrinth.com/maven` — YACL (artifact: `maven.modrinth:yacl`)

## Config

YACL config screen with option for:
- Enable/disable favorites

Config class: `HandyTradersConfig` — JSON file at `config/handytraders.json`
