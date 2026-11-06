# Changelog

All notable changes to this project are documented here.

## [1.0.0] - 2026-11-06

Initial release for Minecraft 26.x on Fabric, as a Carpet Mod extension.

Artifacts are built **per Minecraft version** (`URC-1.0.0+26.1.jar` ... `URC-1.0.0+26.3.jar`), each
pinned to its exact game version.

### Combat
- `noAttackCooldown` - removes the attack cooldown (full-charge hits, minimum attack charge gate bypassed,
  armor stands ignore player damage, HUD indicator hidden on 26.1.x).
- `noAttackSpeedLimit` - clears the 10 tick hurt immunity window for player damage so every attack lands.
- `whiteLooting` - Looting also applies to iron golems.

### Nether portals
- `customNetherPortal` - any closed obsidian loop in a vertical plane (1..21 blocks) can be lit.
- `horizontalNetherPortal` - flat obsidian loops become ground-facing portals (`urc:horizontal_nether_portal`).
- `netherPortalSearchRadius` - configurable portal search radius (vanilla 128).

### Anvil & enchanting
- `higherEnchantmentLevels` - anvil merging up to level 10.
- `noAnvilPenalty` - no "Too Expensive" cap and no compounding repair cost.

### Villagers & iron farms
- `villagerFastDiscount` - one cure or Hero of the Village gives the lowest price.
- `villagerSafeDiscount` - villagers always convert to zombie villagers.
- `efficientIronFarm` - 5 tick golem scan, 1 tick gossip cooldown, single villager spawning.

### Misc
- `superFastFurnace` - every cooking recipe finishes in one tick.
- `fortuneClay` - Fortune shovels always yield at least 4 + level clay balls.
