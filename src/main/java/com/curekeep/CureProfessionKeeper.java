package com.curekeep;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * 治愈瞬间写入观察窗口标记，配合 ResetProfessionMixin 实现延迟重置职业。
 *
 * 只负责一件事：僵尸村民被治愈成村民时，把"观察窗口到期的游戏时间"
 * 写进村民的持久数据（随存档保存，重启/卸载后窗口依然有效）。
 * 窗口期内的重置拦截完全由 mixin（ResetProfessionMixin）在 ResetProfession
 * 的决策方法里完成，到期后标记自然失效，原版逻辑恢复。
 *
 * 原版语义的两个例外（窗口到期后由原版条件自然保证，无需特殊处理）：
 *   - 窗口期内交易过的村民（xp > 0）：原版对交易过的村民本来就不重置；
 *   - 窗口期内认领了工作站的村民：JOB_SITE 存在，ResetProfession 不会命中。
 */
public class CureProfessionKeeper {
    /** 持久数据键：观察窗口到期的游戏时间（随实体存档持久化） */
    public static final String WINDOW_END_TAG = "curekeep_window_end";

    public static void register() {
        NeoForge.EVENT_BUS.register(CureProfessionKeeper.class);
    }

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        // 只关心僵尸村民被治愈成村民的情况
        if (!(event.getEntity() instanceof ZombieVillager) || !(event.getOutcome() instanceof Villager villager)) {
            return;
        }

        // none / nitwit 本来就不会被 ResetProfession 重置，无需开窗口
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            int delaySeconds = Config.RESET_DELAY_SECONDS.get();
            long delayTicks = delaySeconds * 20L;
            villager.getPersistentData().putLong(WINDOW_END_TAG, serverLevel.getGameTime() + delayTicks);
            CureKeep.LOGGER.info("治愈保留职业: {}（{} 秒后恢复原版重置逻辑）", profession, delaySeconds);
        }
    }
}
