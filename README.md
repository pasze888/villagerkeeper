# 治愈守业 CureKeep

NeoForge 1.21.1 模组：**治愈僵尸村民后保留其职业**。

## 功能

- 治愈僵尸村民后，村民**保留职业**（外观、职业名、交易均保留）
- 治愈后**可立即交易**，且带原版治愈折扣
- 附近没有对应工作台时，村民保持职业并自行认领（像"工作台被拆掉的村民"一样）

## 原版问题

原版治愈瞬间 `ZombieVillager.finishConversion` 确实把职业数据复制给了新村民，
但新村民的大脑是全新的（**没有工作站点记忆 JOB_SITE**），1 tick 后大脑里的
`ResetProfession` 行为就会把职业重置为无业。

`ResetProfession` 的触发条件（全部满足才重置）：

1. 职业不是 `none` / `nitwit`
2. `JOB_SITE` 记忆缺失
3. `villagerXp == 0`
4. 等级 ≤ 1

治愈村民恰好全部命中。

## 原理

治愈瞬间（`LivingConversionEvent.Post`，发生在复制数据之后、首个大脑 tick 之前）
把经验设为 1：`villager.setVillagerXp(1)`。

条件 3（`xp == 0`）从此永远不成立，`ResetProfession` 不再触发，职业永久保留。
`setVillagerXp` 是纯 setter 无副作用，1 点经验不影响任何数值。

## 构建

```bash
./gradlew build
```

产物在 `build/libs/curekeep-0.0.1.jar`，放入 `mods` 文件夹即可。

## 游戏内验证

1. 召唤带职业的僵尸村民（以制箭师为例）：
   ```
   /summon zombie_villager ~ ~ ~ {VillagerData:{profession:"minecraft:fletcher"}}
   ```
2. 施加虚弱并喂金苹果治愈：
   ```
   /effect give @e[type=zombie_villager] weakness 100 1
   ```
   （对僵尸村民使用金苹果）
3. 观察：治愈后超过 1 tick，村民仍是**制箭师**（外观 + 交易界面标题），可正常交易
4. 对照：不带本模组时，同一操作 1 tick 后村民变无业（配合 `curevillager`
   诊断模组可在 `logs/curevillager.log` 看到治愈瞬间职业相同、之后才被重置）

## 说明

- 只处理僵尸村民 → 村民的治愈方向；村民 → 僵尸方向原版本来就复制职业数据
- 无配置项，纯事件监听，不引入 Mixin
