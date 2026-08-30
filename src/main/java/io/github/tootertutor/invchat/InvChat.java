package io.github.tootertutor.invchat;

import net.fabricmc.api.ClientModInitializer;

/**
 * InvChat 3.0 client entry point.
 *
 * <p>The foundation commit deliberately keeps Minecraft-facing code out of this class so every
 * Stonecutter target can prove its toolchain, mappings and loader configuration before the GUI
 * compatibility layer is introduced.</p>
 */
public final class InvChat implements ClientModInitializer {
    public static final String MOD_ID = "invchat";

    @Override
    public void onInitializeClient() {
        // Feature registration is added by the next 3.0 implementation patch.
    }
}
