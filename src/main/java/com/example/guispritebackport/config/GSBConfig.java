package com.example.guispritebackport.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置。只有一个开关：整模组功能的总闸。
 */
public final class GSBConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue SPRITE_TOOLTIP_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SPRITE_TOOLTIP_ENABLED = builder
                .comment(
                        "Use the 1.21.2+ GUI sprites (minecraft:tooltip/background / minecraft:tooltip/frame)",
                        "when the active resource pack provides them.",
                        "Set to false to always keep the vanilla 1.21.1 solid-colour tooltip background.")
                .translation("guispritebackport.configuration.sprite_tooltip_enabled")
                .define("sprite_tooltip_enabled", true);

        SPEC = builder.build();
    }

    private GSBConfig() {
    }

    /**
     * 配置在 FML 的 config load 阶段之前读取会抛异常；渲染时理论上一定已经加载完成，
     * 这里仍然兜底成 true，宁可多画一次 sprite 也不要因为配置时序把游戏搞崩。
     */
    public static boolean spriteTooltipEnabled() {
        try {
            return SPRITE_TOOLTIP_ENABLED.get();
        } catch (IllegalStateException | NullPointerException e) {
            return true;
        }
    }
}
