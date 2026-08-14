package com.villagerkeeper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * Writes the watch-window expiry time at the moment of curing, working together
 * with ResetProfessionMixin to delay the profession reset.
 *
 * The expiry time is stored in the villager's persistent data (saved with the
 * world; still valid after restarts or chunk unloads). Interception during the
 * window is done by the mixin, and the marker naturally lapses afterwards.
 * Villagers who have traded (xp > 0) or claimed a workstation (have JOB_SITE)
 * are never reset by vanilla, so they need no special handling.
 */
public class CureProfessionKeeper {
    /** Persistent data key: game time when the watch window expires */
    public static final String WINDOW_END_TAG = "villagerkeeper_window_end";

    public static void register() {
        NeoForge.EVENT_BUS.register(CureProfessionKeeper.class);
    }

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof ZombieVillager) || !(event.getOutcome() instanceof Villager villager)) {
            return;
        }

        // none / nitwit are never reset by ResetProfession; no window needed
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            int delaySeconds = Config.RESET_DELAY_SECONDS.get();
            villager.getPersistentData().putLong(WINDOW_END_TAG, serverLevel.getGameTime() + delaySeconds * 20L);
            VillagerKeeper.LOGGER.info("Cured villager keeps profession: {} (vanilla reset logic resumes in {} s)", profession, delaySeconds);
        }
    }
}
