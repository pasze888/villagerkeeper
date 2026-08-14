package com.curekeep;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * 治愈瞬间写入观察窗口到期时间，配合 ResetProfessionMixin 延迟重置职业。
 *
 * 到期时间写入村民持久数据（随存档保存，重启/卸载后依然有效），
 * 窗口期内的拦截由 mixin 完成，到期后标记自然失效。
 * 交易过（xp>0）或已认领工作站（有 JOB_SITE）的村民原版本就不重置，无需特殊处理。
 */
public class CureProfessionKeeper {
    /** 持久数据键：观察窗口到期的游戏时间 */
    public static final String WINDOW_END_TAG = "curekeep_window_end";

    public static void register() {
        NeoForge.EVENT_BUS.register(CureProfessionKeeper.class);
    }

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof ZombieVillager) || !(event.getOutcome() instanceof Villager villager)) {
            return;
        }

        // none / nitwit 不会被 ResetProfession 重置，无需开窗口
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            int delaySeconds = Config.RESET_DELAY_SECONDS.get();
            villager.getPersistentData().putLong(WINDOW_END_TAG, serverLevel.getGameTime() + delaySeconds * 20L);
            CureKeep.LOGGER.info("治愈保留职业: {}（{} 秒后恢复原版重置逻辑）", profession, delaySeconds);
        }
    }
}
