# Changelog

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
