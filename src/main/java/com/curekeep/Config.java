package com.curekeep;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置（COMMON 类型，位于 run/config/curekeep-common.toml）。
 */
public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 治愈后职业保留的观察窗口时长（秒，默认 30 秒） */
    public static final ModConfigSpec.IntValue RESET_DELAY_SECONDS = BUILDER
            .comment(
                    "治愈后职业保留的观察窗口时长（秒，默认 30 秒）",
                    "到期后村民恢复原版逻辑：未交易过且无工作站的会重置为无业",
                    "设为 0 表示治愈瞬间就恢复原版逻辑"
            )
            .defineInRange("resetDelaySeconds", 30, 0, 3600);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
