package com.villagerkeeper;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration (COMMON type, at run/config/villagerkeeper-common.toml).
 */
public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** How long the profession is kept after curing (seconds, default 30) */
    public static final ModConfigSpec.IntValue RESET_DELAY_SECONDS = BUILDER
            .comment(
                    "Profession watch-window duration after curing (seconds, default 30)",
                    "After expiry, villagers revert to vanilla behavior: those who never traded and have no workstation reset to none",
                    "Set to 0 to restore vanilla behavior immediately at the moment of curing"
            )
            .defineInRange("resetDelaySeconds", 30, 0, 3600);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
