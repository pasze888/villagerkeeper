package com.curekeep;

import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * 监听僵尸村民 -> 村民的治愈事件，保住治愈村民的职业。
 *
 * 原理：原版 Villager 大脑中的 ResetProfession 行为（优先级 10，每 tick 检查）
 * 会在同时满足以下条件时把职业重置为无业：
 *   1. 职业不是 none / nitwit（治愈瞬间职业已被复制过来，命中）
 *   2. JOB_SITE 记忆缺失（新村民大脑全新，命中）
 *   3. villagerXp == 0（僵尸村民经验通常为 0，命中）
 *   4. 等级 <= 1（命中）
 * 本模组在治愈瞬间把经验设为 1（纯 setter，无副作用），条件 3 永远不成立，
 * ResetProfession 不再触发，职业从此保留。
 *
 * 附带效果：治愈村民保留职业外观与职业名、可立即交易（含原版治愈折扣），
 * 没有工作台时像"工作台被拆掉的村民"一样保持职业并自行认领附近同职业工作台。
 */
public class CureProfessionKeeper {
    public static void register() {
        NeoForge.EVENT_BUS.register(CureProfessionKeeper.class);
    }

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        // 只关心僵尸村民被治愈成村民的情况
        if (!(event.getEntity() instanceof ZombieVillager) || !(event.getOutcome() instanceof Villager villager)) {
            return;
        }

        VillagerProfession profession = villager.getVillagerData().getProfession();
        // none / nitwit 本来就不会被 ResetProfession 重置，无需干预
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        // 仅当会被 ResetProfession 命中（xp==0）时才干预：设为 1 破坏触发条件
        if (villager.getVillagerXp() == 0) {
            villager.setVillagerXp(1);
            CureKeep.LOGGER.info("治愈保留职业: {}（xp 0→1，跳过 ResetProfession）", profession);
        }
    }
}
