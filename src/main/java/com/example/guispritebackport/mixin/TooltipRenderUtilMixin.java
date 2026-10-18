package com.example.guispritebackport.mixin;

import com.example.guispritebackport.config.GSBConfig;
import com.example.guispritebackport.tooltip.TooltipSpriteRenderer;
import com.example.guispritebackport.tooltip.TooltipStyleProviders;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 目标类：{@code net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil}
 *
 * <p>1.21.1 + NeoForge 21.1.x 里这个方法有<b>两个</b>重载：</p>
 * <pre>
 * renderTooltipBackground(GuiGraphics, int x, int y, int width, int height, int z)
 *         // 描述符 (Lnet/minecraft/client/gui/GuiGraphics;IIIII)V
 *         // 方法体只有一句：带上 4 个默认颜色常量，转调下面的 10 参版本
 *
 * renderTooltipBackground(GuiGraphics, int x, int y, int width, int height, int z,
 *                         int backgroundStart, int backgroundEnd, int borderStart, int borderEnd)
 *         // 描述符 (Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIII)V
 *         // 真正画东西的那个；颜色来自 RenderTooltipEvent.Color
 * </pre>
 *
 * <p>NeoForge 打补丁后，{@code GuiGraphics} 内部（renderTooltipInternal 的 lambda）是<b>直接调用 10 参版本</b>的，
 * 所以只注入 6 参版本在 NeoForge 环境下等于没注入。这里两个都注入：6 参版本 {@code require = 0}
 * （原版 1.21.1 才有，NeoForge 上可能被别的东西替换掉），10 参版本 {@code require = 1}
 * （这是 NeoForge 实际调用的入口，找不到就说明环境不对，让它在启动时直接报错更容易定位）。</p>
 *
 * <p>只绘制一次：两个重载里只会有一个是本次调用的入口。6 参版本在 HEAD 被取消后就不会再去调 10 参版本；
 * 10 参版本被直接调用时，也只在它自己的 HEAD 被取消一次。</p>
 *
 * <p>priority = 1100（默认 1000）。priority <b>数值小的先应用</b>，所以 1100 表示本模组比默认优先级的
 * 其他 tooltip Mixin 更晚应用。如果整合包里有别的提示框模组抢着画背景，把 1100 调小（例如 900）
 * 可以让本模组更早应用；调大则相反。</p>
 */
@Mixin(value = TooltipRenderUtil.class, priority = 1100)
public abstract class TooltipRenderUtilMixin {

    /** 6 参重载（1.21.1 原版签名，NeoForge 里只是一个转发壳）。 */
    @Inject(
            method = "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void gsb$renderSpriteBackground6(GuiGraphics graphics,
                                                    int x, int y, int width, int height, int z,
                                                    CallbackInfo ci) {
        gsb$handle(graphics, x, y, width, height, z, ci);
    }

    /** 10 参重载（NeoForge 加的，带 RenderTooltipEvent.Color 的 4 个颜色值）。 */
    @Inject(
            method = "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIII)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private static void gsb$renderSpriteBackground10(GuiGraphics graphics,
                                                     int x, int y, int width, int height, int z,
                                                     int backgroundStart, int backgroundEnd,
                                                     int borderStart, int borderEnd,
                                                     CallbackInfo ci) {
        // 颜色参数在本模组里刻意忽略：1.21.2 的 sprite 提示框没有颜色概念，
        // 底色和描边完全由 sprite 贴图决定。
        gsb$handle(graphics, x, y, width, height, z, ci);
    }

    private static void gsb$handle(GuiGraphics graphics,
                                   int x, int y, int width, int height, int z,
                                   CallbackInfo ci) {
        if (!GSBConfig.spriteTooltipEnabled()) {
            return; // 配置关掉了 → 原样走原版
        }
        if (TooltipSpriteRenderer.render(graphics, x, y, width, height, z, TooltipStyleProviders.get())) {
            ci.cancel();
        }
    }
}
