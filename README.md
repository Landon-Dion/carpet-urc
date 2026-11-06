# URC

[![Build](https://github.com/URC-Mod/URC/actions/workflows/build.yml/badge.svg)](https://github.com/URC-Mod/URC/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**URC** is a [Carpet Mod](https://modrinth.com/mod/carpet) extension for **Minecraft Java 26.x** on Fabric.
Every feature is a normal Carpet rule, toggled with `/carpet` and persisted in `carpet.conf` like the
built-in ones - no extra commands, no config screens.

> 中文文档见下方 [规则列表](#规则列表)，每条规则的实现细节在 [docs/rules.md](docs/rules.md)。

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 26.1 - 26.3 (any 26.x release) |
| Java | 25+ |
| Fabric Loader | 0.20.1+ |
| Carpet Mod | 26.1+ (**required**) |

## Installation

1. Install Fabric Loader 0.20.1+ and Fabric API.
2. Drop `Carpet` and `URC-<version>.jar` into `mods/`.
3. In game, run `/carpet <rule> <value>` (requires Carpet's command permission).

## 规则列表

| 规则 | 类型 | 说明 |
| --- | --- | --- |
| `noAttackCooldown` | bool | 完全移除攻击冷却（并在 26.1.x 上隐藏 HUD 指示器） |
| `noAttackSpeedLimit` | bool | 移除受击无敌帧，攻击速率不设上限 |
| `customNetherPortal` | bool | 任意竖直闭环黑曜石框都能点燃成地狱门（1~21 格） |
| `horizontalNetherPortal` | bool | 平铺在地面的闭环可点燃成朝上的横向地狱门 |
| `netherPortalSearchRadius` | int 16~512 | 传送门搜索半径（原版 128） |
| `higherEnchantmentLevels` | bool | 铁砧叠附魔可到 10 级，6~10 级只能靠铁砧 |
| `noAnvilPenalty` | bool | 移除"过于昂贵"与修复费用滚雪球 |
| `villagerFastDiscount` | bool | 治疗一次 / 村庄英雄即最低价 |
| `villagerSafeDiscount` | bool | 任意难度 100% 转化僵尸村民 |
| `efficientIronFarm` | bool | 5 tick 检测、1 tick 八卦冷却、单村民即可生成 |
| `whiteLooting` | bool | 抢夺对铁傀儡生效（+1 铁锭/级 + 随机虞美人） |
| `superFastFurnace` | bool | 所有烧制 1 tick |
| `fortuneClay` | bool | 时运铲挖粘土至少 4 + 等级 个粘土球 |

默认全部关闭，用例如下：

```mcfunction
/carpet noAttackCooldown true
/carpet netherPortalSearchRadius 512
```

## Compatibility notes

- Built against **26.1.2**; every server-side mixin target (class + method + descriptor) was verified to be
  byte-identical in the 26.2 / 26.3 jars, so one jar covers the whole 26.x series.
- 26.2 moved the HUD from `net.minecraft.client.gui.Gui` to `net.minecraft.client.gui.Hud`. The client
  mixin that hides the attack indicator is therefore marked optional (`required: false`, `require = 0`):
  on 26.2+ it simply skips instead of crashing, and the crosshair keeps its vanilla indicator.
  The gameplay parts of `noAttackCooldown` still apply on every version.
- 26.x has no obfuscation and no remapping: the project has no `mappings` block and uses `jar` (not `remapJar`).

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/URC-<version>.jar`.

To target another 26.x release, change `minecraft_version` and `fabric_api_version` in
[gradle.properties](gradle.properties) (versions: <https://fabricmc.net/develop>).

## Project layout

```
src/main/java/com/urc/
  URC.java                 mod entrypoint (registers the Carpet extension + blocks)
  URCCarpetExtension.java  Carpet extension: registers rules into Carpet's SettingsManager
  URCSettings.java         rule declarations
  URCBlocks.java           block registration
  portal/                  custom / horizontal nether portal frame logic + block
  mixin/                   server-side mixins (one file per feature area)
  mixin/client/            optional client HUD mixin
src/main/resources/
  fabric.mod.json, urc*.mixins.json
  assets/urc/              lang, icon, block model
docs/rules.md              per-rule implementation notes
```

## Contributing

Pull requests are welcome. Please keep one feature per rule, gate it behind a rule check at the
injection point, and leave vanilla behaviour untouched when the rule is off.

## License

[MIT](LICENSE)
