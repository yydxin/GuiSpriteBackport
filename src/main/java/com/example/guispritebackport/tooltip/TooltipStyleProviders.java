package com.example.guispritebackport.tooltip;

/**
 * 当前生效的 {@link TooltipStyleProvider} 持有者。
 *
 * <p>现在只有一个默认实现；以后做"按物品映射样式"时，在客户端初始化阶段调用
 * {@link #set(TooltipStyleProvider)} 换成自己的实现即可。</p>
 */
public final class TooltipStyleProviders {
    private static final TooltipStyleProvider DEFAULT = new TooltipStyleProvider() {
    };

    private static volatile TooltipStyleProvider current = DEFAULT;

    private TooltipStyleProviders() {
    }

    public static TooltipStyleProvider get() {
        return current;
    }

    public static void set(TooltipStyleProvider provider) {
        current = (provider == null) ? DEFAULT : provider;
    }

    public static void reset() {
        current = DEFAULT;
    }
}
