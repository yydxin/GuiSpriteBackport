package com.example.guispritebackport.tooltip;

import net.minecraft.resources.ResourceLocation;

/**
 * 提示框样式提供者：决定一次提示框渲染要用哪两张 GUI sprite。
 *
 * <p>本阶段（1.21.1 回移的第一步）只提供"全局默认样式"，即 1.21.2 原版使用的
 * {@code minecraft:tooltip/background} 与 {@code minecraft:tooltip/frame}。</p>
 *
 * <p>预留扩展：以后如果要按物品/按 NBT/按模组映射到自定义样式，实现本接口并在
 * {@link TooltipStyleProviders#set(TooltipStyleProvider)} 里替换即可，
 * 不需要再动 Mixin。</p>
 */
public interface TooltipStyleProvider {
    /** 1.21.2 原版提示框背景 sprite 的 id。 */
    ResourceLocation DEFAULT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");

    /** 1.21.2 原版提示框描边 sprite 的 id。 */
    ResourceLocation DEFAULT_FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/frame");

    /**
     * 背景 sprite。返回 {@code null} 表示"本次不使用 sprite，走原版纯色绘制"。
     */
    default ResourceLocation backgroundSprite() {
        return DEFAULT_BACKGROUND_SPRITE;
    }

    /**
     * 描边 sprite。返回 {@code null} 表示"本次不使用 sprite，走原版纯色绘制"。
     *
     * <p>注意：1.21.2 只有"背景 + 描边都存在"时才整体替换，缺一张就整体回退，
     * 所以这里单独返回 null 等同于整体回退。</p>
     */
    default ResourceLocation frameSprite() {
        return DEFAULT_FRAME_SPRITE;
    }
}
