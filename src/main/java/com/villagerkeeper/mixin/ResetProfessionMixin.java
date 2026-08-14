package com.villagerkeeper.mixin;

import com.villagerkeeper.CureProfessionKeeper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a condition to ResetProfession's decision method: skip the reset while the watch window is active.
 *
 * The vanilla decision logic is compiled into the synthetic lambda method
 * lambda$create$0(ServerLevel, Villager, long), which resets the profession when
 * the profession is not none/nitwit, there is no JOB_SITE memory, xp == 0 and
 * level <= 1. This mixin intercepts at the method head: while the persistent-data
 * window expiry time has not been reached, it returns false immediately, skipping
 * the whole behavior and keeping the profession; once expired the marker naturally
 * lapses and vanilla logic resumes.
 */
@Mixin(ResetProfession.class)
public abstract class ResetProfessionMixin {
    @Inject(
        method = "lambda$create$0(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z",
        at = @At("HEAD"),
        cancellable = true,
        // Synthetic lambda methods are not in the mapping table; remap=false skips lookup and resolves the dev name at runtime
        remap = false
    )
    private static void villagerkeeper$skipDuringWindow(ServerLevel level, Villager villager, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        long windowEnd = villager.getPersistentData().getLong(CureProfessionKeeper.WINDOW_END_TAG);
        if (windowEnd > level.getGameTime()) {
            cir.setReturnValue(false);
        }
    }
}
