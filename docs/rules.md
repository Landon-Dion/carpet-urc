# 规则详解

**URC** 的所有规则都和 Carpet 自带规则一样用 `/carpet` 切换。本文记录每条规则的实现细节与注入点；
概览、安装与构建请看 [README](../README.md)。

```
/carpet <rule> <value>
```

规则一览：

```
/carpet noAttackCooldown true          # 移除攻击冷却（含 HUD 指示器）
/carpet noAttackSpeedLimit true        # 移除攻击速度上限（受击无敌帧）
/carpet customNetherPortal true        # 自定义地狱门（任意闭环黑曜石框）
/carpet horizontalNetherPortal true    # 横向的地狱门（地面上的闭环）
/carpet netherPortalSearchRadius 512   # 更大的地狱传送门范围（原版 128）
/carpet higherEnchantmentLevels true   # 更高的附魔等级（铁砧叠到 10 级）
/carpet noAnvilPenalty true            # 移除铁砧附魔惩罚
/carpet villagerFastDiscount true      # 村民快速骨折（一次治疗即最低价）
/carpet villagerSafeDiscount true      # 村民安全打折（100% 转化僵尸村民）
/carpet efficientIronFarm true         # 高效率刷铁机（5t 检测 / 1t 冷却 / 1 人同意）
/carpet whiteLooting true              # 白色掠夺（抢夺对铁傀儡生效）
/carpet superFastFurnace true          # 超光速熔炉（烧制 1t）
/carpet fortuneClay true               # 时运粘土（时运铲挖粘土至少 5 个）
```

## 规则一：`noAttackCooldown`

开启后：

| 行为 | 说明 |
| --- | --- |
| 完全移除攻击冷却 | 每次攻击都按满蓄力结算：满伤害，且始终满足暴击/横扫/击退的蓄力条件 |
| 隐藏冷却指示器 | 准星下方（以及「快捷栏」样式）的攻击指示条/满蓄力图标不再绘制，HUD 上没有任何冷却反馈 |
| 绕过最小攻击蓄力 | 服务端 `handleAttack` 对 `minimum_attack_charge` 物品的拦截一并失效 |
| 盔甲架免疫玩家伤害 | 玩家近战、玩家射出的箭等以玩家为伤害来源的攻击不再作用于盔甲架 |

`/carpet noAttackCooldown false` 立即恢复 vanilla 行为，无需重启。规则值会像其它 Carpet 规则一样写进 `carpet.conf`。

## 规则二：`noAttackSpeedLimit`

原版对**同一个目标**有一条 10 tick 的受击无敌帧：`LivingEntity#hurtServer` 里当 `invulnerableTime > 10` 且本次伤害不高于上次伤害时直接丢弃这次攻击。所以即使冷却已移除，点击再快，同一目标也约每 0.5 秒才掉一次血。

开启该规则后，玩家造成的伤害在结算前会把目标的 `invulnerableTime` 清零，于是**服务器处理到的每个攻击包都会落地**，同一游戏刻内的多次攻击也全部生效，攻击速度不再有上限。配合 `noAttackCooldown` 即为真正意义上的无上限。

> 说明：客户端输入仍按游戏刻采样，一 tick 内能发出的攻击包数量取决于点击速率（自动点击器可以每 tick 发多个包）。服务端会全部处理，不会只取一个。

## 规则三：`customNetherPortal`（自定义地狱门）

原版只认 2~21 宽、3~21 高的**矩形**黑曜石框。开启该规则后：

- 平面**就是黑曜石闭环所在的平面**——门面由框决定，与玩家朝向无关；
- 形状任意：圆、三角、十字、L 形、带内部立柱的环都行，**1×1 也可以**；
- 大小限制：区域内接包围盒 **1~21 格宽、1~21 格高**；
- 用打火石点燃闭环内的空气即可生成地狱门，传送逻辑与原版一致（对面仍按原版规则找门/建门）。

> 正常门框只有唯一合法平面：与门面垂直的那个平面会穿过门洞直接撞到空气，闭环检测不通过。只有门框**嵌在实心黑曜石里**（或空心房间）时，垂直方向那层"1 格宽的切片"也会闭合，才会出现第二个候选。判定顺序是纯粹的几何/交互信息，与玩家朝向无关：
>
> 1. 点火所点击的那一面的平面（点侧面时能唯一钉死平面）；
> 2. **宽度 ≥ 2 的开口**优先于 1 格宽的切片（真正的门洞至少 2 宽，垂直切片恒为 1）；
> 3. 仍分不出时取闭环更小者，再平局取 X 轴。
>
> 出现多候选时会在日志里打印 `Custom portal: both planes enclosed (X 宽x高=面积, Z ...)`，方便排查。

> 门方块**只在服务端创建**。若两端各自预测，客户端世界稍慢一拍就会算出不同的平面/朝向，服务端只会覆盖自己那套，客户端多出来的门方块就会留成"方向不一致"的残块——所以客户端只保留火焰，等服务端同步。

## 规则四：`horizontalNetherPortal`（横向的地狱门）

```
/carpet horizontalNetherPortal true
```

允许**平铺在地面上**的黑曜石闭环被点燃成横向地狱门：

- 闭环所在平面为水平面（`y = 常量`），形状任意，两个水平方向各 **1~21 格**；
- 闭环内部可以是空气，也可以是雪层/草/花这类可替换方块（会被门方块替换掉）——平地上的环通常正是一层雪；
- 填出来的不是原版传送门方块，而是 URC 自带的 `urc:horizontal_nether_portal`——一块朝上/朝下的薄片，复用原版传送门贴图；
- 站上去（进入方块）即开始传送，冷却/音效/目的地选择完全走原版逻辑；
- 原版 `nether_portal` 的 `AXIS` 只有 X/Z，做不出朝地的门面，所以必须新增方块；
- 可单独开启，也可与「自定义地狱门」同时开；`customNetherPortal` 只管竖直平面，`horizontalNetherPortal` 只管水平平面，互不影响。

> 横向门方块和原版传送门一样不可徒手破坏（`strength = -1`）。要拆掉它：敲掉闭环上的任意黑曜石（会自动消失），或直接 `/setblock`/`/fill` 成空气。
>
> 从横向门进入后，对侧如果还没有门，仍由原版规则生成一个**竖直**门——原版 `PortalForcer` 只会建矩形竖门。

## 规则五：`netherPortalSearchRadius`（更大的地狱传送门范围）

```
/carpet netherPortalSearchRadius 512
```

原版在 `PortalForcer#findClosestPortalPosition` 里写死：

```java
int radius = toNether ? 16 : 128;   // 去下界搜 16 格；回主世界搜 128 格
```

该规则替换的就是这个 **128**（回主世界一侧的搜索半径），默认值 128 等于原版行为：

- 调大后，从下界回来时会去更远的地方找**已存在的**传送门并链接过去，而不是在旁边新建一个；
- 取值范围 **16~512**（超出会被规则校验拒绝）；
- 下界一侧的 16 格保持原版不变（那是"下界 1 格 = 主世界 8 格"的链接精度，改大容易乱连门）；
- 搜索走的是 POI（兴趣点）而不是逐方块扫描，但半径越大，`ensureLoadedAndValid` 需要加载的区块越多，512 会有明显的短暂卡顿。

## 规则六：`higherEnchantmentLevels`（更高的附魔等级）

```
/carpet higherEnchantmentLevels true
```

原版铁砧在 `AnvilMenu#createResult` 里写死了一句 `if (level > enchantment.getMaxLevel()) level = getMaxLevel()`，把叠出来的等级压回上限（附魔台/战利品同理）。

规则开启后只把**铁砧这条路径**的上限抬到 10：

- 两本同附魔的书合并可以一路叠到 10 级（10+10 仍是 10）；
- 10 级的书可以正常打到装备上，装备上的等级也保留 10；
- 附魔台、村民交易、战利品表的产出仍受真实 `getMaxLevel()` 限制，所以 **6~10 级只能靠铁砧获得**。

> 部分附魔的原版效果公式对高等级是线性的（锋利、保护等），10 级数值会明显偏强；少数硬编码上限的附魔（如深海探索者、摔落缓冲的减伤）到 10 级后收益会封顶。

## 规则七：`noAnvilPenalty`（移除铁砧附魔惩罚）

```
/carpet noAnvilPenalty true
```

对应原版两处惩罚：

| 原版逻辑 | 规则效果 |
| --- | --- |
| `if (cost >= 40 && !creative) result = EMPTY`（"过于昂贵"） | 不再清空产物，超过 40 级的操作也能执行 |
| `calculateIncreasedRepairCost` 让每次修理/重命名后基准费用按 `2x+1` 增长 | 返回 0，费用不再滚雪球 |

产物价格还会被压到最高 39 级，避免出现"能合成但要 100 级"的尴尬。

## 规则八：`villagerFastDiscount`（村民快速骨折）

```
/carpet villagerFastDiscount true
```

原版治疗折扣是跟着 gossip 声望慢慢叠加、村庄英雄按等级给 30%~55%，要反复治疗。

规则开启后，满足任一条件就把该玩家看到的**所有报价直接压到最低价（1 个）**：

- 该村民的声望 ≥ 5（治疗一次就会 +5，即"治疗过一次"）；
- 玩家身上有「村庄英雄」效果（任意等级）。

注入点在 `Villager#updateSpecialPrices` 的末尾，也就是每次打开交易界面时生效。

## 规则九：`villagerSafeDiscount`（村民安全打折）

```
/carpet villagerSafeDiscount true
```

原版 `Zombie#killedEntity` 对村民的转化是这样判定的：

```java
if (difficulty == NORMAL || difficulty == HARD) {
    if (difficulty != HARD && random.nextBoolean()) return;  // 普通难度 50% 失败
    convertVillagerToZombieVillager(...);
}
```

规则开启后，该方法里对 `getDifficulty()` 的三次读取都返回 `HARD`，于是：**任何难度下僵尸杀死村民都会 100% 转化成僵尸村民**，方便自己造僵尸村民再治疗拿折扣，不必把世界切成困难。

实现上有三处必须同时处理，否则会出现"能点着但门瞬间消失"：

| 注入点 | 作用 |
| --- | --- |
| `BaseFireBlock#canBePlacedAt` | 原版在这里用矩形检测决定打火石能否放火，非矩形框会被直接拒绝 |
| `BaseFireBlock#onPlace` | 真正创建传送门方块的地方 |
| `NetherPortalBlock#updateShape` | 原版门方块每次收到邻块更新都会用矩形检测自检，非矩形门会被判为非法并还原成空气 |

`updateShape` 的放行条件是"该门块所在平面仍构成黑曜石闭环"，所以**拆掉框上的黑曜石后门仍会正常消失**，不会留下悬空传送门方块。

## 规则：`hopperIdleCooldown`（漏斗优化）

```
/carpet hopperIdleCooldown 4
```

原版每个漏斗每 tick 都会尝试转移；只有成功移动才会设置 8 tick 冷却。因此空闲漏斗会**每 tick** 做一次容器查找，上方没有容器时还会做两次 AABB 实体查询（`getEntityContainer` 找容器实体、`getItemsAtAndAbove` 找掉落物）。双箱子是 54 格，而 `CACHED_SLOTS` 只缓存 0~53 格，导致每次 `getSlots` 都新建 `int[54]`。

| 行为 | 说明 |
| --- | --- |
| 54 格容器槽位缓存 | 始终开启；双箱子等 54 格容器的 `int[54]` 只创建一次，纯分配优化，不改变行为 |
| 空闲冷却 | `hopperIdleCooldown` 控制空闲漏斗未移动物品后等待的 tick 数；`0` 保持原版每 tick 重试，建议 `4~8` |

`hopperIdleCooldown > 0` 会把物品拾取延迟最多 `N-1` tick（默认 `0`，开启后属于有意的性能取舍）。

## 环境要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | 26.1.2 |
| Java | 25+ |
| Fabric Loader | 0.20.1+ |
| Carpet Mod | 26.1（`fabric-carpet-26.1+v260402`）**必需前置** |

## 安装

1. 安装 Fabric Loader 0.20.1+ 与 Fabric API。
2. 把 Carpet Mod 和 `URC-1.0.0.jar` 一起放进 `mods/`。
3. 进游戏执行 `/carpet noAttackCooldown true`（需要 Carpet 命令权限）。

## 构建

```bash
./gradlew build
```

产物：`build/libs/URC-1.0.0.jar`。

## 实现要点

- 26.1 起 Minecraft 官方 jar 不再混淆，项目不使用 mappings，也不需要 `remapJar`。
- 拓展通过 `CarpetServer.manageExtension(...)` 在 `ModInitializer` 中注册（Carpet 不扫描自定义入口点）。
- 规则直接注册进 Carpet 自己的 `SettingsManager`，所以命令是 `/carpet <rule>`，而不是独立的 `/urc`。
- `Player#getAttackStrengthScale(float)` 恒返 `1.0F`：这是伤害、暴击、横扫、击退共用的蓄力系数。
- `Player#cannotAttackWithItem(ItemStack, int)` 恒返 `false`：解除服务端对最小攻击蓄力的拦截。
- `Gui#extractCrosshair` / `Gui#extractItemHotbar`（仅客户端）：规则开启时强制 `scale = 1`、`attackDelay = 0`，两条分支都不绘制，HUD 上不留任何指示器。
- `LivingEntity#hurtServer(...)`：规则开启且伤害来源是玩家时，把 `invulnerableTime` 清零，让每个攻击包都结算。
- `ArmorStand#hurtServer(...)` 在伤害来源为玩家时取消。
- 规则关闭时所有注入立即放行，行为与 vanilla 完全一致。

## 许可

MIT

## 规则十：`efficientIronFarm`（高效率刷铁机）

```
/carpet efficientIronFarm true
```

原版铁傀儡生成的三个时间/人数闸门，规则逐个改掉：

| 原版 | 规则 |
| --- | --- |
| `GolemSensor` 每 **200 tick** 扫描一次附近铁傀儡 | 改成 **5 tick** 一次 |
| 村民八卦冷却 `GOSSIP_COOLDOWN = 1200` | 改成 **1 tick** |
| `spawnGolemIfNeeded(level, time, 5)` 需要 **5 名**村民同意 | 只需要 **1 名** |
| 只有 `gossip()`（需要两名村民八卦）才会触发生成尝试 | 每 5 tick 主动调用一次 `spawnGolemIfNeeded(..., 1)` |
| `wantsToSpawnGolem` 要求村民 24000 tick 内睡过觉 | 不再要求睡觉 |
| 每次生成后 `GOLEM_DETECTED_RECENTLY` 记忆持续 **599 tick**（约 30 秒），期间不再生成 | 缩短到 **20 tick**（1 秒），只有铁傀儡真的在附近时才继续压制 |

原版里**单个村民永远不会尝试生成铁傀儡**——生成尝试只发生在 `gossip()` 里，而 gossip 需要另一个村民；光把"同意人数"改成 1 是不够的。这版补了 `customServerAiStep` 里的 5 tick 轮询，并放开睡觉条件，单村民即可持续尝试。配合 `whiteLooting` 收益更高。

## 规则十一：`whiteLooting`（白色掠夺）

```
/carpet whiteLooting true
```

原版铁傀儡的战利品表（`data/minecraft/loot_table/entities/iron_golem.json`）**完全没有 `looting_enchant` 函数**，所以抢夺附魔对铁傀儡毫无作用。

规则开启后，玩家击杀铁傀儡时按抢夺等级额外掉落：

- **每级 +1 铁锭**（固定）；
- **额外 0~等级 个虞美人**（随机）。

掉落走的是 `LivingEntity#dropFromLootTable` 的尾部注入，只在铁傀儡、且击杀者是生物时生效，不影响其它实体。

## 规则十二：`superFastFurnace`（超光速熔炉）

```
/carpet superFastFurnace true
```

直接改 `AbstractCookingRecipe#cookingTime()` 返回 **1**，而不是去改熔炉的 tick：

- 熔炉首次放入物品时用 `getTotalCookTime()` 初始化总时长；
- 每烧完一次又会把 `cookingTotalTime` 重置成 `recipe.cookingTime()`。

只改其中一处会出现"第一块 1t、后面又变回原速"的问题，改配方本身两处就都覆盖了，烟熏炉/高炉同样生效。

## 规则十三：`fortuneClay`（时运粘土）

```
/carpet fortuneClay true
```

原版粘土固定掉 4 个粘土球，战利品表里没有时运函数。

规则开启后，用**附有时运的铲子**挖粘土：

- 粘土球数量至少为 **4 + 时运等级**（时运 I → 5 个，II → 6 个，III → 7 个）；
- 保留原版掉落列表，只在不足时补齐，所以数据包对粘土战利品的改动依然有效；
- 非铲子工具、无时运、非玩家破坏（活塞/爆炸）都走原版。

> 如果你想要更夸张的倍率（例如 4 × (等级+1) 或最低固定 5 且每级再 +2），告诉我数值即可，逻辑只有一处。

## 规则十四：`halfSlabBreak`（更好的半砖放置）

```
/carpet halfSlabBreak true
```

破坏双层半砖时不再整块消失：

- 用视线与方块包围盒求交，判断被瞄准的是上半还是下半；
- 只移除被瞄准的那一半，掉落 1 个对应半砖；
- 方块原地保留为另一半的单层半砖。

## 规则十五：`fastHopper`（高速漏斗）

```
/carpet fastHopper true
```

- 铁砧允许把效率 V 附魔书打到漏斗物品上（`Enchantment#canEnchant` 对漏斗放行）；
- 放置后 `BlockItem` 会把物品的组件（含 `ENCHANTMENTS`）复制到方块实体；
- 带效率 V 的漏斗每 tick 反复调用原版 `tryMoveItems`，直到没有物品可搬，因此 5 个槽位可以在同一 tick 内并行吸取/输出。

## 规则十六：`fastElytra`（高速鞘翅）

```
/carpet fastElytra true
```

原版烟花把速度插值到 `视线 × 1.5`；规则改为 `当前速度 + 视线 × 0.1`，每 tick 累加，可无限叠加。

## 规则十七：`mixedSlab`（混合半砖）

```
/carpet mixedSlab true
```

- 手持一种半砖，右键另一种**单层**半砖 → 两种材质合成一个完整方块（上方/下方各一种）；
- 方块本身不可见（`RenderShape.INVISIBLE`），由 `MixedSlabRenderer` 用两个 `MovingBlockRenderState` 分别绘制上、下两半；
- 破坏时按视线命中点只拆掉被瞄准的一半，另一半还原成对应的单层半砖并留在原地；
- 爆破解体等非玩家破坏由 `getDrops` 返回两块半砖。

## 规则十八：`simpleObserverPlacement`（简易放置侦测器）

```
/carpet simpleObserverPlacement true
```

原版侦测器在 `updateShape` 里发现"朝向方向上的方块发生变化"就会 `scheduleTick` 并输出脉冲，因此贴脸放方块也会触发一次。

规则开启后，玩家通过 `BlockItem#place` 放置方块期间会打上标记；这段时间内侦测器的 `updateShape` 直接返回原状态，不再调度脉冲。放置之外的方块变化（挖掉、活塞、流体等）仍按原版检测。

## 规则十九：`silkTouchFortune`（精准采集可附魔时运）

```
/carpet silkTouchFortune true
```

两部分：

1. **解除冲突**：`Enchantment.areCompatible` 对"精准采集 + 时运"直接返回 true，所以铁砧可以把两本书合到同一把工具上，附魔台也有机会同时给出。
2. **精准采集吃时运**：原版战利品表在精准采集成立时走 `silk_touch` 分支，时运函数根本不会执行。规则在 `Block#dropResources` 里对"掉落物 == 方块本身"的那一项乘以 `时运等级 + 1`，并按堆叠上限拆分；其它掉落项保持原样。

## 规则二十：`noIceMelting`（冰？水？）

```
/carpet noIceMelting true
```

原版冰块在 `randomTick` 里检测光照，亮度大于 11 就 `melt` 成水。规则直接在 `IceBlock#randomTick` 入口取消，于是自然融化消失；`playerDestroy` 的"破坏后变成水源"逻辑不经过 randomTick，保持原版，精准采集也照常掉冰块。
