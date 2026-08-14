package com.villagerkeeper;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * VillagerKeeper — delays the profession reset after curing zombie villagers.
 *
 * Vanilla copies profession data at the moment of curing, but the new villager
 * has a brand-new brain (no JOB_SITE memory), so 1 tick later the ResetProfession
 * behavior resets the profession to none.
 * This mod writes a watch-window expiry time when curing; during the window
 * ResetProfessionMixin skips the reset, and vanilla logic resumes afterwards.
 */
@Mod(VillagerKeeper.MODID)
public class VillagerKeeper {
    public static final String MODID = "villagerkeeper";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VillagerKeeper(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CureProfessionKeeper.register();
        LOGGER.info("VillagerKeeper loaded successfully!");
    }
}
