package io.github.tootertutor.invchat.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}

/**
 * Prevents the physical key press that opens vanilla chat from also inserting
 * its trailing character event into the newly-created ChatScreen.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Unique
    private static final long INVCHAT_CHAT_OPEN_SUPPRESSION_NANOS = 1_000_000_000L;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean invchat$suppressOpeningChatCharacter;

    @Unique
    private long invchat$suppressOpeningChatCharacterUntil;

    //? if >=1.21.9 {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void invchat$trackChatOpenKey(long window, int action, KeyEvent event, CallbackInfo ci) {
        this.invchat$handleKeyPress(event.key(), action);
    }
    //?}

    //? if <1.21.9 {
    /*@Inject(method = "keyPress", at = @At("HEAD"))
    private void invchat$trackChatOpenKey(
            long window,
            int keyCode,
            int scanCode,
            int action,
            int modifiers,
            CallbackInfo ci) {
        this.invchat$handleKeyPress(keyCode, action);
    }
    *///?}

    @Unique
    private void invchat$handleKeyPress(int keyCode, int action) {
        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_T) {
            //? if >=26.2 {
            boolean noScreenOpen = this.minecraft.gui.screen() == null;
            //?}
            //? if <26.2 {
            /*boolean noScreenOpen = this.minecraft.screen == null;
            *///?}

            if (noScreenOpen) {
                this.invchat$suppressOpeningChatCharacter = true;
                this.invchat$suppressOpeningChatCharacterUntil =
                        System.nanoTime() + INVCHAT_CHAT_OPEN_SUPPRESSION_NANOS;
            } else {
                // T was intentionally pressed while a screen was already open.
                this.invchat$clearOpeningChatCharacterSuppression();
            }
            return;
        }

        // Any other intentional key press means the opening T event is no longer relevant.
        this.invchat$clearOpeningChatCharacterSuppression();
    }

    //? if >=1.21.9 {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void invchat$suppressOpeningChatCharacter(
            long window,
            CharacterEvent event,
            CallbackInfo ci) {
        if (this.invchat$shouldSuppressOpeningChatCharacter(event.codepoint())) {
            ci.cancel();
        }
    }
    //?}

    //? if <1.21.9 {
    /*@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void invchat$suppressOpeningChatCharacter(
            long window,
            int codePoint,
            int modifiers,
            CallbackInfo ci) {
        if (this.invchat$shouldSuppressOpeningChatCharacter(codePoint)) {
            ci.cancel();
        }
    }
    *///?}

    @Unique
    private boolean invchat$shouldSuppressOpeningChatCharacter(int codePoint) {
        if (!this.invchat$suppressOpeningChatCharacter) {
            return false;
        }

        if (System.nanoTime() > this.invchat$suppressOpeningChatCharacterUntil) {
            this.invchat$clearOpeningChatCharacterSuppression();
            return false;
        }

        //? if >=26.2 {
        boolean chatScreenOpen = this.minecraft.gui.screen() instanceof ChatScreen;
        //?}
        //? if <26.2 {
        /*boolean chatScreenOpen = this.minecraft.screen instanceof ChatScreen;
        *///?}

        if (!chatScreenOpen) {
            return false;
        }

        if (codePoint == 't' || codePoint == 'T') {
            // Keep the guard armed for the rest of this physical key press in case
            // the platform emits more than one character callback.
            return true;
        }

        this.invchat$clearOpeningChatCharacterSuppression();
        return false;
    }

    @Unique
    private void invchat$clearOpeningChatCharacterSuppression() {
        this.invchat$suppressOpeningChatCharacter = false;
        this.invchat$suppressOpeningChatCharacterUntil = 0L;
    }
}
