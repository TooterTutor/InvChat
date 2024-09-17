package io.github.tootertutor.invchat;

import io.github.tootertutor.invchat.mixin.accessor.HandledScreenAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvChat implements ClientModInitializer {
    public static final String MOD_ID = "invchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ConfigManager config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvChat Initialized!");
        // Register the config
        AutoConfig.register(ConfigManager.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ConfigManager.class).getConfig();
    }

    public static ConfigManager getConfig() {
        return config;
    }

    public static TextFieldWidget createChatWidget(MinecraftClient mc, Screen screen, TextRenderer textRenderer) {
        int screenWidth = screen.width;
        int screenHeight = screen.height;
        int widgetWidth = 200;
        int widgetHeight = 20;

        int x = screenWidth / 2 - widgetWidth / 2 + (int) config.xOffset;
        int y = screenHeight - widgetHeight - 10 + (int) config.yOffset;

        if (config.anchorBelowInventory && screen instanceof HandledScreen) {
            HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor) screen;
            y = handledScreenAccessor.getY() + handledScreenAccessor.getBackgroundHeight() + 25;
        }

        TextFieldWidget widget = new TextFieldWidget(textRenderer, x, y, widgetWidth, widgetHeight, Text.of("Chat ...")) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    String message = this.getText();
                    if (message.startsWith("/")) {
                        mc.getNetworkHandler().sendChatCommand(message.substring(1));
                    } else {
                        mc.getNetworkHandler().sendChatMessage(message);
                    }
                    this.setText("");
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                return super.charTyped(chr, modifiers);
            }
        };
        widget.setMaxLength(256);
        return widget;
    }
}