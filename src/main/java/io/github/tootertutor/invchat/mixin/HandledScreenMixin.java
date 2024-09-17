package io.github.tootertutor.invchat.mixin;

import io.github.tootertutor.invchat.InvChat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    // Shadow method to handle hotbar key presses
    @Shadow protected abstract boolean handleHotbarKeyPressed(int keyCode, int scanCode);

    // Unique field to store the chat widget
    @Unique
    private TextFieldWidget chatWidget;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    // Initialize the chat widget when the screen is initialized
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (InvChat.getConfig().enabled) {
            this.chatWidget = InvChat.createChatWidget(this.client, this, this.textRenderer);
            this.addDrawableChild(this.chatWidget);
        }
    }

    // Handle key presses for the chat widget
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatWidget != null) {
            if (this.chatWidget.isFocused()) {
                // When the widget is focused, let it handle all key presses
                if (this.chatWidget.keyPressed(keyCode, scanCode, modifiers)) {
                    cir.setReturnValue(true);
                    return;
                }

                // Special handling for Escape key
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    this.chatWidget.setFocused(false);
                    cir.setReturnValue(true);
                    return;
                }

                // Prevent inventory from closing when typing
                if (this.client != null && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
                    cir.setReturnValue(true);
                    return;
                }
            } else if (keyCode == GLFW.GLFW_KEY_T) {
                // Focus the widget when 'T' is pressed and it's not already focused
                this.chatWidget.setFocused(true);
                cir.setReturnValue(true);
                return;
            }
        }

        // Handle hotbar key presses when the chat widget is not focused
        if (this.handleHotbarKeyPressed(keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    // Handle mouse clicks for the chat widget
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatWidget != null) {
            if (this.chatWidget.isMouseOver(mouseX, mouseY)) {
                // Focus and handle click when the widget is clicked
                this.chatWidget.setFocused(true);
                this.chatWidget.mouseClicked(mouseX, mouseY, button);
                cir.setReturnValue(true);
            } else {
                // Unfocus the widget when clicking outside
                this.chatWidget.setFocused(false);
            }
        }
    }

    // Prevent hotbar actions when the chat widget is focused
    @Inject(method = "handleHotbarKeyPressed", at = @At("HEAD"), cancellable = true)
    private void onHandleHotbarKeyPressed(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatWidget != null && this.chatWidget.isFocused()) {
            cir.setReturnValue(false);
        }
    }

    // Render the chat widget
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.chatWidget != null) {
            this.chatWidget.render(context, mouseX, mouseY, delta);
        }
    }

    // Handle mouse release for the chat widget
    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatWidget != null && this.chatWidget.isMouseOver(mouseX, mouseY)) {
            this.chatWidget.mouseReleased(mouseX, mouseY, button);
            cir.setReturnValue(true);
        }
    }
}