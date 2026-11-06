# URC

[![Build](https://github.com/Landon-Dion/carpet-urc/actions/workflows/build.yml/badge.svg)](https://github.com/Landon-Dion/carpet-urc/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**URC** is a [Carpet Mod](https://modrinth.com/mod/carpet) extension for **Minecraft Java 26.x** on Fabric.
Every feature is a normal Carpet rule, toggled with `/carpet` and persisted in `carpet.conf` like the
built-in ones - no extra commands, no config screens.

> 中文文档见下方 [规则列表](#规则列表)，每条规则的实现细节在 [docs/rules.md](docs/rules.md)。

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 26.1 / 26.1.1 / 26.1.2 / 26.2 / 26.3 (one jar per version) |
| Java | 25+ |
| Fabric Loader | 0.20.1+ |
| Carpet Mod | 26.1+ (**required**) |

## Installation

1. Install Fabric Loader 0.20.1+ and Fabric API.
2. Pick the jar that matches your game version, e.g. `URC-1.0.0+26.1.2.jar` for Minecraft 26.1.2,
   and drop it together with `Carpet` into `mods/`.
3. In game, run `/carpet <rule> <value>` (requires Carpet's command permission).

## 规则列表

| 规则 | 类型 | 说明 |
| --- | --- | --- |
| `noAttackCooldown` | bool | 完全移除攻击冷却并隐藏 HUD 指示器 |
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

## Version support

Every Minecraft version gets its **own jar**, with an exact `minecraft` dependency
(`~26.1.2`, `~26.2`, ...) so the wrong build cannot be installed by accident:

| Minecraft | Jar | Fabric API used for the build |
| --- | --- | --- |
| 26.1 | `URC-<ver>+26.1.jar` | 0.155.3+26.1.2 |
| 26.1.1 | `URC-<ver>+26.1.1.jar` | 0.155.3+26.1.2 |
| 26.1.2 | `URC-<ver>+26.1.2.jar` | 0.155.3+26.1.2 |
| 26.2 | `URC-<ver>+26.2.jar` | 0.161.0+26.2 |
| 26.3 | `URC-<ver>+26.3.jar` | 0.165.0+26.3 |

The CI workflow (`.github/workflows/build.yml`) builds all five in parallel as a matrix and uploads
one artifact per version.

26.2 moved the HUD from `net.minecraft.client.gui.Gui` to `net.minecraft.client.gui.Hud`, so the
build compiles one of two client mixins depending on the target (`src/client26_1`, `src/client26_2`)
- the attack-indicator hiding works on every version.

## Building

```bash
./gradlew build              # default target: 26.1.2 (gradle.properties)
./gradlew build -Pmc=26.3    # build the 26.3 jar
```

Supported `-Pmc` values: `26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3`. The jar lands in
`build/libs/URC-<mod version>+<mc>.jar`; its `fabric.mod.json` is pinned to that version
automatically, and the matching client mixin (`Gui` vs `Hud`) plus Fabric API version are selected
from the table above.

## Project layout

```
src/main/java/com/urc/
  URC.java                 mod entrypoint (registers the Carpet extension + blocks)
  URCCarpetExtension.java  Carpet extension: registers rules into Carpet's SettingsManager
  URCSettings.java         rule declarations
  URCBlocks.java           block registration
  portal/                  custom / horizontal nether portal frame logic + block
  mixin/                   server-side mixins (one file per feature area)
src/client26_1/java/       HUD mixin for 26.1.x (net.minecraft.client.gui.Gui)
src/client26_2/java/       HUD mixin for 26.2+ (net.minecraft.client.gui.Hud)
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
