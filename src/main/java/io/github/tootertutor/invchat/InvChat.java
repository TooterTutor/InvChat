package io.github.tootertutor.invchat;

import io.github.tootertutor.invchat.config.ConfigManager;
import io.github.tootertutor.invchat.config.InvChatConfig;
import net.fabricmc.api.ClientModInitializer;

/**
 * InvChat 3.0 client entry point.
 *
 * <p>Minecraft-facing compatibility code lives in focused mixins and adapters so the entry point
 * remains stable across the complete Stonecutter version matrix.</p>
 */
public final class InvChat implements ClientModInitializer {
    public static final String MOD_ID = "invchat";

    private static InvChatConfig config = new InvChatConfig();

    @Override
    public void onInitializeClient() {
        config = ConfigManager.load();
    }

    public static InvChatConfig getConfig() {
        return config;
    }
}
