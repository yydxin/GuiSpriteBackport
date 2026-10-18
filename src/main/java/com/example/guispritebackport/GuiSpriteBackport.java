package com.example.guispritebackport;

import com.example.guispritebackport.config.GSBConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

/**
 * 模组入口，只在物理客户端构造（{@code dist = Dist.CLIENT}）。
 */
@Mod(value = GuiSpriteBackport.MOD_ID, dist = Dist.CLIENT)
public final class GuiSpriteBackport {
    public static final String MOD_ID = "guispritebackport";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GuiSpriteBackport(IEventBus modEventBus, ModContainer modContainer) {
        // 客户端配置：config/guispritebackport-client.toml
        modContainer.registerConfig(ModConfig.Type.CLIENT, GSBConfig.SPEC);

        // 在 Mods 列表里点 Config 时打开 NeoForge 自带的配置界面
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, parent) -> new ConfigurationScreen(container, parent));

        LOGGER.info("GuiSpriteBackport loaded (client only).");
    }
}
