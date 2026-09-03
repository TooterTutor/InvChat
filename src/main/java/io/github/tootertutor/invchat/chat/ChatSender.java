package io.github.tootertutor.invchat.chat;

import net.minecraft.client.Minecraft;

/**
 * Sends chat and commands through the vanilla client path appropriate for each
 * Minecraft era.
 *
 * <p>
 * Keeping this logic out of the screen mixin prevents signed-chat compatibility
 * details from
 * leaking into the GUI/input layer.
 * </p>
 */
public final class ChatSender {
    private ChatSender() {
    }

    /**
     * Sends {@code input} as either chat or a command.
     *
     * @return {@code true} when a connected player/network handler was available
     *         and submission was
     *         attempted; {@code false} when the client is not currently connected
     *         to a world/server.
     */
    public static boolean send(Minecraft minecraft, String input) {
        if (input == null) {
            return false;
        }

        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return false;
        }

        boolean command = normalized.startsWith("/");
        String content = command ? normalized.substring(1) : normalized;

        if (command && content.isEmpty()) {
            return false;
        }

        // 1.19.3 moved chat/command submission onto ClientPacketListener. This remains
        // the vanilla
        // route through current releases and correctly handles message
        // signing/acknowledgements.
        //? if >=1.19.3 {
        if (minecraft.getConnection() == null) {
            return false;
        }

        if (command) {
            minecraft.getConnection().sendCommand(content);
        } else {
            minecraft.getConnection().sendChat(content);
        }
        return true;
        //?} else if >=1.19.1 {
        /*if (minecraft.player == null) {
            return false;
        }

        if (command) {
            // 1.19.1-1.19.2 can send commands without signing only when no signable arguments are present.
            // Fall back to the signed overload (with no preview) when the unsigned path rejects it.
            if (!minecraft.player.commandUnsigned(content)) {
                minecraft.player.commandSigned(content, null);
            }
        } else {
            minecraft.player.chatSigned(content, null);
        }
        return true;
        *///?} else if 1.19 {
        /*if (minecraft.player == null) {
            return false;
        }

        if (command) {
            minecraft.player.command(content);
        } else {
            minecraft.player.chat(content);
        }
        return true;
        *///?} else {
        /*if (minecraft.player == null) {
            return false;
        }

        // Prior to signed chat, LocalPlayer#chat accepts the complete input and vanilla/server-side
        // parsing handles a leading slash as a command.
        minecraft.player.chat(normalized);
        return true;
        *///?}
    }
}
