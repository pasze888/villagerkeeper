package com.curekeep.mixin;

import com.curekeep.CureProfessionKeeper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 给 ResetProfession 的决策方法加一个条件：观察窗口期内跳过重置。
 *
 * 原版决策逻辑编译在合成方法 lambda$create$0(ServerLevel, Villager, long) 中，
 * 在「职业非 none/nitwit、无 JOB_SITE、xp==0、等级<=1」时重置职业。
 * 本 mixin 在方法开头拦截：持久数据里的窗口到期时间未到，则直接返回 false
 * 跳过整个行为，职业保留；到期后标记失效，原版逻辑恢复。
 */
@Mixin(ResetProfession.class)
public abstract class ResetProfessionMixin {
    @Inject(
        method = "lambda$create$0(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z",
        at = @At("HEAD"),
        cancellable = true,
        // 合成 lambda 方法不在映射表里，remap=false 跳过查找、运行时直接用开发名解析
        remap = false
    )
    private static void curekeep$skipDuringWindow(ServerLevel level, Villager villager, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        long windowEnd = villager.getPersistentData().getLong(CureProfessionKeeper.WINDOW_END_TAG);
        if (windowEnd > level.getGameTime()) {
            cir.setReturnValue(false);
        }
    }
}
