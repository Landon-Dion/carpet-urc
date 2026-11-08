# Changelog

All notable changes to this project are documented here.

## [1.2] - 2026-11-08

### New rules
- `halfSlabBreak` - breaking a double slab destroys only the aimed half, the other half stays.
- `fastHopper` - anvil can apply Efficiency V to a hopper; enchanted hoppers transfer up to five items per tick.
- `fastElytra` - firework rockets accelerate elytra without the vanilla 1.5 blocks/tick cap.
- `mixedSlab` - two different slabs combine into one full block (custom block entity + renderer), split again when broken, and can be pushed by pistons.
- `simpleObserverPlacement` - placing a block against an observer's face does not make it pulse.
- `silkTouchFortune` - Silk Touch and Fortune are compatible; silk-touch drops use the vanilla fortune formula.
- `noIceMelting` - ice no longer melts naturally, breaking it still leaves water.

### Packaging
- One jar per Minecraft version: `URC-1.2+26.1.jar`, `+26.1.1`, `+26.1.2`, `+26.2`, `+26.3`, each pinned to its exact game version.

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
