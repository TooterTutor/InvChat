package io.github.tootertutor.invchat.chat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
//?}
import net.minecraft.network.chat.Component;

/**
 * InvChat's text field with one small piece of input-state handling.
 *
 * <p>GLFW delivers a character event after the T key event that opens/focuses the chat box. Since
 * the box becomes focused during the key event, vanilla would otherwise insert that same T as the
 * first character. This widget consumes only that matching first character event.</p>
 */
public final class InventoryChatEditBox extends EditBox {
    private boolean suppressChatOpenCharacter;

    public InventoryChatEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component message
    ) {
        super(font, x, y, width, height, message);
    }

    /** Marks the next matching T/t character event as the event that opened the field. */
    public void focusFromChatKey() {
        this.suppressChatOpenCharacter = true;
    }

    //? if >=1.21.9 {
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.consumeChatOpenCharacter(event.codepoint())) {
            return true;
        }

        return super.charTyped(event);
    }
    //?} else {
    /*@Override
    public boolean charTyped(char character, int modifiers) {
        if (this.consumeChatOpenCharacter(character)) {
            return true;
        }

        return super.charTyped(character, modifiers);
    }
    *///?}

    private boolean consumeChatOpenCharacter(int codePoint) {
        if (!this.suppressChatOpenCharacter) {
            return false;
        }

        // Clear this on the very next character event even if it is not T. That prevents the flag
        // from becoming stale when a platform/mod suppresses the character event for the opening key.
        this.suppressChatOpenCharacter = false;
        return codePoint == 't' || codePoint == 'T';
    }
}
