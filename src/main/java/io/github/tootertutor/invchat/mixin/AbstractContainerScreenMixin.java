package io.github.tootertutor.invchat.mixin;

import io.github.tootertutor.invchat.chat.ChatSender;
import io.github.tootertutor.invchat.chat.InventoryChatEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds InvChat's text field to vanilla container screens.
 *
 * <p>Chat submission is delegated to a small version-aware adapter so signed-chat networking
 * changes remain isolated from the GUI and focus code.</p>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Unique
    private static final int INVCHAT_WIDGET_WIDTH = 200;

    @Unique
    private static final int INVCHAT_WIDGET_HEIGHT = 20;

    @Unique
    private InventoryChatEditBox invchat$chatBox;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void invchat$addChatBox(CallbackInfo ci) {
        int x = (this.width - INVCHAT_WIDGET_WIDTH) / 2;
        int y = this.height - INVCHAT_WIDGET_HEIGHT - 10;

        this.invchat$chatBox = new InventoryChatEditBox(
                this.font,
                x,
                y,
                INVCHAT_WIDGET_WIDTH,
                INVCHAT_WIDGET_HEIGHT,
                this.title
        );
        this.invchat$chatBox.setMaxLength(256);

        //? if >=1.17 {
        this.addRenderableWidget(this.invchat$chatBox);
        //?} else {
        /*this.addButton(this.invchat$chatBox);
        *///?}
    }

    //? if >=1.21.9 {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invchat$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        this.invchat$handleKeyPressed(event.key(), event, cir);
    }
    //?} else {
    /*@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invchat$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        this.invchat$handleKeyPressed(keyCode, scanCode, modifiers, cir);
    }
    *///?}

    //? if >=1.21.9 {
    @Unique
    private void invchat$handleKeyPressed(int keyCode, KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (this.invchat$chatBox == null) {
            return;
        }

        if (this.invchat$isChatFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.invchat$setChatFocused(false);
                cir.setReturnValue(true);
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.invchat$submitChat();
                cir.setReturnValue(true);
                return;
            }

            this.invchat$chatBox.keyPressed(event);
            cir.setReturnValue(true);
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_T) {
            this.invchat$chatBox.focusFromChatKey();
            this.invchat$setChatFocused(true);
            cir.setReturnValue(true);
        }
    }
    //?} else {
    /*@Unique
    private void invchat$handleKeyPressed(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.invchat$chatBox == null) {
            return;
        }

        if (this.invchat$isChatFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.invchat$setChatFocused(false);
                cir.setReturnValue(true);
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.invchat$submitChat();
                cir.setReturnValue(true);
                return;
            }

            this.invchat$chatBox.keyPressed(keyCode, scanCode, modifiers);
            cir.setReturnValue(true);
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_T) {
            this.invchat$chatBox.focusFromChatKey();
            this.invchat$setChatFocused(true);
            cir.setReturnValue(true);
        }
    }
    *///?}

    @Unique
    private void invchat$submitChat() {
        if (this.invchat$chatBox == null || this.minecraft == null) {
            return;
        }

        String input = this.invchat$chatBox.getValue();
        if (input.trim().isEmpty()) {
            return;
        }

        if (ChatSender.send(this.minecraft, input)) {
            this.invchat$chatBox.setValue("");
        }
    }

    @Unique
    private boolean invchat$isChatFocused() {
        return this.invchat$chatBox != null && this.getFocused() == this.invchat$chatBox;
    }

    @Unique
    private void invchat$setChatFocused(boolean focused) {
        if (this.invchat$chatBox == null) {
            return;
        }

        if (focused) {
            this.setFocused(this.invchat$chatBox);
        } else if (this.getFocused() == this.invchat$chatBox) {
            this.setFocused(null);
        }

        // EditBox did not implement GuiEventListener#setFocused until 1.19.4.
        //? if >=1.19.4 {
        this.invchat$chatBox.setFocused(focused);
        //?} else {
        /*this.invchat$chatBox.setFocus(focused);
        *///?}
    }
}
