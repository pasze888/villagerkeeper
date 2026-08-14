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
 * 给 ResetProfession 的决策方法加一个新条件：治愈观察窗口期内跳过重置。
 *
 * ResetProfession.create() 的决策逻辑编译在合成方法 lambda$create$0 中
 * （BehaviorBuilder DSL 的 lambda 被 javac 编译成普通静态方法），签名：
 *   lambda$create$0(ServerLevel, Villager, long) -> boolean
 * 原版条件（全部满足才重置）：职业非 none/nitwit、JOB_SITE 缺失、xp==0、等级<=1。
 *
 * 本 mixin 在方法开头拦截：若村民的持久数据里 curekeep_window_end（治愈时写入的
 * 观察窗口到期游戏时间）大于当前游戏时间，直接返回 false——整个行为跳过，
 * 职业在窗口期内保留；窗口到期后标记失效，原版条件照常生效。
 *
 * 到期时间存持久数据（随存档保存），服务器重启/区块卸载后窗口依然有效。
 */
@Mixin(ResetProfession.class)
public abstract class ResetProfessionMixin {
    @Inject(
        method = "lambda$create$0(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z",
        at = @At("HEAD"),
        cancellable = true,
        // NeoForge 1.21 运行时的类名与开发环境一致（官方映射名），
        // 合成 lambda 方法不在映射表里，remap=false 让 AP 跳过查找、
        // 运行时直接用开发名解析（生产环境同样有效，见 Placebo 等模组）
        remap = false
    )
    private static void curekeep$skipDuringWindow(ServerLevel level, Villager villager, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        long windowEnd = villager.getPersistentData().getLong(CureProfessionKeeper.WINDOW_END_TAG);
        if (windowEnd > level.getGameTime()) {
            cir.setReturnValue(false);
        }
    }
}
