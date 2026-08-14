package com.curekeep;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * 治愈守业（CureKeep）——治愈僵尸村民后延迟重置职业。
 *
 * 原版机制：治愈瞬间 ZombieVillager.finishConversion 会把职业数据复制给新村民，
 * 但新村民的大脑是全新的（没有工作站点记忆），1 tick 后大脑里的
 * ResetProfession 行为（xp==0 且 level<=1 且无 JOB_SITE）就把职业重置为无业。
 *
 * 本模组通过 Mixin 给 ResetProfession 的决策方法加一个新条件：治愈时在村民
 * 持久数据里写入窗口到期时间（默认 30 秒，可配置），窗口期内跳过重置——
 * 玩家可以在这段时间里观察保留的职业并交易（含治愈折扣），
 * 到期后标记失效，原版重置逻辑恢复。
 */
@Mod(CureKeep.MODID)
public class CureKeep {
    // 模组的唯一 ID（必须小写）。所有注册名都以它为命名空间。
    public static final String MODID = "curekeep";

    // 日志器：在控制台/日志文件里打印信息的工具
    public static final Logger LOGGER = LogUtils.getLogger();

    // 构造函数：模组加载时第一个被执行的代码
    public CureKeep(IEventBus modEventBus, ModContainer container) {
        // 注册 COMMON 配置（run/config/curekeep-common.toml）
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CureProfessionKeeper.register();
        LOGGER.info("治愈守业模组加载成功！");
    }
}
