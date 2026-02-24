# Changelog

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
