package io.github.tootertutor.invchat.chat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
//?}
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * InvChat's text field with one small piece of input-state handling.
 *
 * <p>GLFW delivers a character event after the T key event that opens/focuses the chat box. Since
 * the box becomes focused during the key event, vanilla would otherwise insert that same T as the
 * first character. This widget consumes only that matching first character event.</p>
 */
public final class InventoryChatEditBox extends EditBox {
    private boolean suppressChatOpenCharacter;
    private int historyCursor = ChatHistory.size();
    private String historyDraft = "";

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


    /** Resets navigation after a line has been successfully submitted and recorded. */
    public void resetHistoryNavigation() {
        this.historyCursor = ChatHistory.size();
        this.historyDraft = "";
    }

    //? if >=1.21.9 {
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (this.handleHistoryKey(event.key())) {
            return true;
        }

        return super.keyPressed(event);
    }
    //?} else {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.handleHistoryKey(keyCode)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    private boolean handleHistoryKey(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            this.navigateHistory(-1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.navigateHistory(1);
            return true;
        }

        return false;
    }

    private void navigateHistory(int direction) {
        int historySize = ChatHistory.size();
        if (historySize == 0) {
            return;
        }

        // A new widget starts at the end of whatever history already exists. Clamp defensively in
        // case the bounded history discarded an old entry while this widget was alive.
        if (this.historyCursor > historySize) {
            this.historyCursor = historySize;
        }

        if (direction < 0) {
            if (this.historyCursor == historySize) {
                this.historyDraft = this.getValue();
            }

            if (this.historyCursor > 0) {
                this.historyCursor--;
                this.setValue(ChatHistory.get(this.historyCursor));
            }
            return;
        }

        if (this.historyCursor < historySize - 1) {
            this.historyCursor++;
            this.setValue(ChatHistory.get(this.historyCursor));
        } else if (this.historyCursor < historySize) {
            this.historyCursor = historySize;
            this.setValue(this.historyDraft);
        }
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
