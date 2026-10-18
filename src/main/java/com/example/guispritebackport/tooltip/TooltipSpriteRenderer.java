package com.example.guispritebackport.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * 真正干活的绘制类：把 1.21.2 的 "sprite 版提示框背景" 搬到 1.21.1。
 *
 * <h2>尺寸常量</h2>
 * 1.21.2 原版 {@code TooltipRenderUtil} 的常量：{@code MOUSE_OFFSET = 12}、
 * {@code PADDING / PADDING_LEFT / PADDING_RIGHT / PADDING_TOP / PADDING_BOTTOM = 3}、
 * {@code MARGIN = 9}。它的绘制等价于：
 * <pre>
 * x1 = x - 12;  y1 = y - 12;  w1 = w + 24;  h1 = h + 24;
 * translate(0, 0, z);
 * blitSprite(background, x1, y1, w1, h1);
 * blitSprite(frame,      x1, y1, w1, h1);
 * </pre>
 * 也就是 sprite 比文字区域每边多出 12 px（3 px 原版 padding + 9 px 九宫格边框余量）。
 * 九宫格切法由 sprite 自己的 {@code .png.mcmeta} 决定，1.21.1 的
 * {@code GuiGraphics#blitSprite(ResourceLocation,int,int,int,int)} 已经支持
 * {@code nine_slice / tile / stretch} 三种缩放。
 *
 * <p>注意：1.21.2 没有颜色参数，所以 NeoForge 的 10 参重载里那 4 个颜色值这里不参与计算。</p>
 */
public final class TooltipSpriteRenderer {
    // 与 1.21.2 TooltipRenderUtil 保持一致
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    public static final int MARGIN = 9;

    private TooltipSpriteRenderer() {
    }

    /**
     * 尝试用 sprite 绘制提示框背景 + 描边。
     *
     * @return {@code true} 表示已经画好了，调用方应取消原版绘制；
     *         {@code false} 表示这两张 sprite 不可用，请走原版纯色绘制。
     */
    public static boolean render(GuiGraphics graphics,
                                 int x, int y, int width, int height, int z,
                                 TooltipStyleProvider provider) {
        ResourceLocation background = provider.backgroundSprite();
        ResourceLocation frame = provider.frameSprite();
        if (background == null || frame == null) {
            return false;
        }
        // 1.21.2 的行为是"两张都在才替换"，缺任意一张整体回退原版
        if (!spriteExists(background) || !spriteExists(frame)) {
            return false;
        }

        int spriteX = x - (PADDING_LEFT + MARGIN);                              // x - 12
        int spriteY = y - (PADDING_TOP + MARGIN);                               // y - 12
        int spriteWidth = width + PADDING_LEFT + PADDING_RIGHT + MARGIN * 2;    // w + 24
        int spriteHeight = height + PADDING_TOP + PADDING_BOTTOM + MARGIN * 2;  // h + 24

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            // 1.21.1 的 blitSprite 没有 z 参数，用 pose 平移表达 z（和 1.21.2 的做法一致）
            pose.translate(0.0F, 0.0F, (float) z);
            graphics.blitSprite(background, spriteX, spriteY, spriteWidth, spriteHeight);
            graphics.blitSprite(frame, spriteX, spriteY, spriteWidth, spriteHeight);
        } finally {
            pose.popPose();
        }
        return true;
    }

    /**
     * 判断 GUI atlas 里是否真的存在这张 sprite。
     *
     * <p>1.21.1 的 {@code TextureAtlas#getSprite} 对不存在的 id 返回 missingno 占位图而不是抛异常，
     * 所以不能"能取到就算存在"，必须比对 sprite 自己的名字（占位图的名字是 {@code minecraft:missingno}）。</p>
     */
    public static boolean spriteExists(ResourceLocation id) {
        try {
            GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
            TextureAtlasSprite sprite = sprites.getSprite(id);
            return sprite.contents().name().equals(id);
        } catch (IllegalStateException e) {
            // atlas 还没 upload（极早期渲染），当作不存在，走原版
            return false;
        }
    }
}
