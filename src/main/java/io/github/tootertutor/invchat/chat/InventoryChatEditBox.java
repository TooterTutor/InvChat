package io.github.tootertutor.invchat.chat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** InvChat's input widget, history navigation, and T-key character suppression. */
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

    /** Marks T/t character events from the physical key press that opened the field. */
    public void focusFromChatKey() {
        this.suppressChatOpenCharacter = true;
    }

    /** Clears opening-key suppression once the physical T key has actually been released. */
    public void updateChatOpenKeyState(boolean chatKeyDown) {
        if (!chatKeyDown) {
            this.suppressChatOpenCharacter = false;
        }
    }

    /** Connects text changes to the independent Brigadier completion controller. */
    public void setCommandCompleter(CommandCompleter commandCompleter) {
        this.setResponder(commandCompleter::onTextChanged);
        commandCompleter.onTextChanged(this.getValue());
    }

    /** Resets navigation after a line has been successfully submitted and recorded. */
    public void resetHistoryNavigation() {
        this.historyCursor = ChatHistory.size();
        this.historyDraft = "";
    }

    //? if >=1.21.9 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.handleHistoryKey(event.key())) {
            return true;
        }

        return super.keyPressed(event);
    }
    //?}

    //? if <1.21.9 {
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
    //?}

    //? if <1.21.9 {
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

        if (codePoint == 't' || codePoint == 'T') {
            // Keep suppressing while the same physical T press is held. Some platforms can emit
            // more than one character event before key release. The screen tick clears this flag
            // as soon as GLFW reports that T has been released.
            return true;
        }

        // A different character means the opening-key event sequence has ended. Do not eat the
        // player's first real character even if key-release polling has not run yet.
        this.suppressChatOpenCharacter = false;
        return false;
    }
}
