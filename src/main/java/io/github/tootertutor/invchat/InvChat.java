package io.github.tootertutor.invchat;

import net.fabricmc.api.ClientModInitializer;

/**
 * InvChat 3.0 client entry point.
 *
 * <p>Minecraft-facing compatibility code lives in focused mixins and adapters so the entry point
 * remains stable across the complete Stonecutter version matrix.</p>
 */
public final class InvChat implements ClientModInitializer {
    public static final String MOD_ID = "invchat";

    @Override
    public void onInitializeClient() {
        // The container-screen mixin is loaded through fabric.mod.json.
    }
}
