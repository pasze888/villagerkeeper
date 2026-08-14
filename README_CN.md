# VillagerKeeper（村民守护者）

[English](README.md) | 简体中文

NeoForge 1.21.1 模组：**治愈僵尸村民后延迟重置职业**。

## 功能

- 治愈僵尸村民后，职业保留一段**观察窗口**（默认 30 秒，可配置）
- 窗口期内村民保留职业（外观、职业名、交易均保留），可立即交易且带原版治愈折扣
- 窗口到期后恢复原版逻辑：未交易过且无工作站的村民重置为无业

## 原版问题

治愈瞬间 `ZombieVillager.finishConversion` 虽把职业数据复制给了新村民，
但新村民大脑是全新的（**没有 JOB_SITE 工作站点记忆**），1 tick 后大脑里的
`ResetProfession` 行为就把职业重置为无业。

`ResetProfession` 的触发条件（全部满足才重置）：

1. 职业不是 `none` / `nitwit`
2. `JOB_SITE` 记忆缺失
3. `villagerXp == 0`
4. 等级 ≤ 1

治愈村民恰好全部命中。

## 原理

**治愈瞬间**（`LivingConversionEvent.Post`）：把「窗口到期时间」写进村民持久数据
（`villagerkeeper_window_end`，随存档保存）。

**窗口期内**：Mixin 给 `ResetProfession` 的决策方法（`lambda$create$0`）加一个条件
——当前游戏时间未到窗口到期时间时，整个行为直接跳过，职业保留；
到期后标记自然失效，原版条件照常生效。

不改动任何实体数据、无需定时器，到期逻辑由条件本身完成。

与原版语义一致的两个例外（窗口到期后由原版条件自然保证）：
- 窗口期内**交易过**的村民（xp > 0）：原版本就不重置
- 窗口期内**认领了工作站**的村民：`JOB_SITE` 存在，`ResetProfession` 不会命中

## 配置

`run/config/villagerkeeper-common.toml`：

```toml
[general]
# 治愈后职业保留的观察窗口时长（秒，默认 30 秒）
# 到期后恢复原版逻辑：未交易过且无工作站的会重置为无业
# 设为 0 表示治愈瞬间就恢复原版逻辑
resetDelaySeconds = 30
```

## 构建

```bash
./gradlew build
```

产物在 `build/libs/villagerkeeper-1.0.0.jar`，放入 `mods` 文件夹即可。

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
3. 观察窗口期（默认 30 秒）：村民保持制箭师职业，可交易（带治愈折扣）
4. 窗口到期：未交易的村民变无业；交易过或已认领工作站的村民保留职业
5. 对照：不带本模组时，同一操作 1 tick 后村民就变无业

## 说明

- 只处理僵尸村民 → 村民的治愈方向；反向（村民 → 僵尸）原版本就复制职业数据
- 事件监听 + 一个 Mixin（`ResetProfessionMixin`），窗口到期时间存持久数据，
  服务器重启、区块卸载后窗口依然有效

## 许可证

[GNU 宽通用公共许可证 v3.0](LICENSE)（LGPL-3.0）。
Copyright (c) 2026 pasze888
