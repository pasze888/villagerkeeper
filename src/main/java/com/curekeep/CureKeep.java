package com.curekeep;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * 治愈守业（CureKeep）——治愈僵尸村民后保留其职业。
 *
 * 原版机制：治愈瞬间 ZombieVillager.finishConversion 会把职业数据复制给新村民，
 * 但新村民的大脑是全新的（没有工作站点记忆），1 tick 后大脑里的
 * ResetProfession 行为（xp==0 且 level<=1 且无 JOB_SITE）就把职业重置为无业。
 * 本模组在治愈瞬间把经验设为 1，让 ResetProfession 的条件不再成立，职业永不自动重置。
 */
@Mod(CureKeep.MODID)
public class CureKeep {
    // 模组的唯一 ID（必须小写）。所有注册名都以它为命名空间。
    public static final String MODID = "curekeep";

    // 日志器：在控制台/日志文件里打印信息的工具
    public static final Logger LOGGER = LogUtils.getLogger();

    // 构造函数：模组加载时第一个被执行的代码
    public CureKeep(IEventBus modEventBus) {
        CureProfessionKeeper.register();
        LOGGER.info("治愈守业模组加载成功！");
    }
}
