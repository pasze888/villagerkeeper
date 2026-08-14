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
 * 原版治愈瞬间虽复制了职业数据，但新村民大脑全新（无 JOB_SITE 记忆），
 * 1 tick 后 ResetProfession 行为就把职业重置为无业。
 * 本模组在治愈时写入观察窗口到期时间，窗口期内由 ResetProfessionMixin 跳过重置，
 * 到期后恢复原版逻辑。
 */
@Mod(CureKeep.MODID)
public class CureKeep {
    public static final String MODID = "curekeep";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CureKeep(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CureProfessionKeeper.register();
        LOGGER.info("治愈守业模组加载成功！");
    }
}
