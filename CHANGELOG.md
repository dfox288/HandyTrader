# Changelog

## 2.0.4

### Bug Fixes
- **Villager Trading Plus / "Easier Villager Trading" compatibility — now for 26.1 and 26.1.1 too.** Clicking a favorited trade with "trade all on click" enabled does the bulk trade again instead of vanilla single-trade. This fix shipped in 2.0.3 but was only published for Minecraft 26.1.2; 2.0.4 publishes the same build for 26.1 and 26.1.1 as well. (#20)

## 2.0.3

### Bug Fixes
- **Restore compatibility with other mods that mix into the villager trade screen** — the trade-index remapping no longer cancels and reimplements vanilla `postButtonClick`, so bookmark mods, autoclickers, and any other mod injecting into the same method run normally again

## 2.0.2

- Update to Minecraft 26.1.2 compatibility
- Update Fabric Loader to 0.19.2, Fabric API to 0.146.1

## 2.0.1

- Update to Minecraft 26.1.1 compatibility
- Update Fabric Loader to 0.18.6, Fabric API to 0.145.3, YACL to 3.9.2

## 2.0.0

- Port to Minecraft 26.1 (Java 25, unobfuscated)
- Restore YACL config screen integration

## 2.0.0-beta.1

- Port to Minecraft 26.1-rc-1 (Java 25, unobfuscated)
- Restore YACL config screen integration
- Add CI/CD pipeline with automated Modrinth publishing

## v1.0.1

### Bug Fixes
- **Fix trade selection desync when favorites are sorted** — clicking a sorted trade row now correctly tells the server which trade was selected, fixing the issue where items would flash in/out of payment slots or trades would silently fail
- **Fix selection preservation on bookmark toggle** — toggling a bookmark mid-session no longer desyncs the selected trade; the previously selected trade stays selected at its new sorted position
- **Handle server-side offer refresh** — when a villager restocks or levels up, the mod now detects the new offers and re-applies favorite sorting instead of showing stale data

### Technical
- Replaced non-functional `@ModifyArg` on `ServerboundSelectTradePacket` constructor with `@Inject`-based interception of `postButtonClick` for reliable trade index remapping
- Added `lastKnownOffers` reference tracking to detect server-side offer replacements

## v1.0.0

Initial release for Minecraft 1.21.11 (Fabric).

### Features
- **Favorite trades** — click the top-left corner of any trade row to bookmark it with a gold corner indicator
- **Sort favorites to top** — favorited trades automatically move to the top of the trade list
- **Per-villager persistence** — favorites are stored per villager UUID and saved to disk across sessions
- **Bookmark hover feedback** — ghost bookmark appears when hovering the corner, brightens on favorited rows
- **Amethyst chime** — subtle sound feedback when toggling favorites
- **Config screen** — "Enable Favorites" toggle via YACL + ModMenu (both optional)
- Config persists as `config/handytraders.json` (editable manually without YACL)
- Favorites persist as `config/handytraders-favorites.json`
