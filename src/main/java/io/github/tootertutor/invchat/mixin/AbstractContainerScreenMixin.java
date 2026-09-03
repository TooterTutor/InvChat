package io.github.tootertutor.invchat.mixin;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import io.github.tootertutor.invchat.InvChat;
import io.github.tootertutor.invchat.chat.ChatHistory;
import io.github.tootertutor.invchat.chat.ChatSender;
import io.github.tootertutor.invchat.chat.CommandCompleter;
import io.github.tootertutor.invchat.chat.InventoryChatEditBox;
import io.github.tootertutor.invchat.render.InvChatContainerRenderBridge;
import io.github.tootertutor.invchat.render.InvChatRecipeBookScreenMarker;
//? if >=1.20 && <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds InvChat's text field and direct-Brigadier suggestion dropdown to container screens. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements InvChatContainerRenderBridge {
    @Unique
    private static final int INVCHAT_WIDGET_HEIGHT = 20;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageHeight;

    @Unique
    private InventoryChatEditBox invchat$chatBox;

    @Unique
    private CommandCompleter invchat$commandCompleter;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void invchat$addChatBox(CallbackInfo ci) {
        if (!InvChat.getConfig().enabled) {
            this.invchat$chatBox = null;
            this.invchat$commandCompleter = null;
            return;
        }

        int widgetWidth = InvChat.getConfig().width;
        int x = (this.width - widgetWidth) / 2 + InvChat.getConfig().xOffset;
        int y;

        if (InvChat.getConfig().anchorBelowInventory) {
            y = this.topPos + this.imageHeight + 25 + InvChat.getConfig().yOffset;
        } else {
            y = this.height - INVCHAT_WIDGET_HEIGHT - 10 + InvChat.getConfig().yOffset;
        }

        this.invchat$chatBox = new InventoryChatEditBox(
                this.font,
                x,
                y,
                widgetWidth,
                INVCHAT_WIDGET_HEIGHT,
                this.title
        );
        this.invchat$chatBox.setMaxLength(256);

        this.invchat$commandCompleter = null;
        if (this.minecraft != null) {
            this.invchat$commandCompleter = new CommandCompleter(
                    this.minecraft,
                    this,
                    this.invchat$chatBox,
                    this.font,
                    x,
                    y,
                    widgetWidth,
                    INVCHAT_WIDGET_HEIGHT
            );
            this.invchat$chatBox.setCommandCompleter(this.invchat$commandCompleter);
        }

        //? if >=1.17 {
        this.addRenderableWidget(this.invchat$chatBox);
        //?}
        //? if <1.17 {
        /*this.addButton(this.invchat$chatBox);
        *///?}
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void invchat$updateChatOpenKeySuppression(CallbackInfo ci) {
        if (this.invchat$chatBox == null || this.minecraft == null) {
            return;
        }

        //? if >=1.21.9 {
        long window = this.minecraft.getWindow().handle();
        //?}
        //? if <1.21.9 {
        /*long window = this.minecraft.getWindow().getWindow();
        *///?}
        boolean chatKeyDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_T) != GLFW.GLFW_RELEASE;
        this.invchat$chatBox.updateChatOpenKeyState(chatKeyDown);
    }

    //? if >=1.21.9 {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invchat$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        this.invchat$handleKeyPressed(event.key(), event.hasShiftDown(), event, cir);
    }
    //?}

    //? if <1.21.9 {
    /*@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invchat$onKeyPressed(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        boolean shiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        this.invchat$handleKeyPressed(keyCode, shiftDown, scanCode, modifiers, cir);
    }
    *///?}

    //? if >=1.21.9 {
    @Unique
    private void invchat$handleKeyPressed(
            int keyCode,
            boolean shiftDown,
            KeyEvent event,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.invchat$chatBox == null) {
            return;
        }

        if (this.invchat$isChatFocused()) {
            if (this.invchat$commandCompleter != null
                    && this.invchat$commandCompleter.keyPressed(keyCode, shiftDown)) {
                cir.setReturnValue(true);
                return;
            }

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
    //?}

    //? if <1.21.9 {
    /*@Unique
    private void invchat$handleKeyPressed(
            int keyCode,
            boolean shiftDown,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.invchat$chatBox == null) {
            return;
        }

        if (this.invchat$isChatFocused()) {
            if (this.invchat$commandCompleter != null
                    && this.invchat$commandCompleter.keyPressed(keyCode, shiftDown)) {
                cir.setReturnValue(true);
                return;
            }

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

    //? if >=1.21.9 {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void invchat$onMouseClicked(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.invchat$isChatFocused()
                && this.invchat$commandCompleter != null
                && this.invchat$commandCompleter.mouseClicked(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }
    //?}

    //? if <1.21.9 {
    /*@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void invchat$onMouseClicked(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.invchat$isChatFocused()
                && this.invchat$commandCompleter != null
                && this.invchat$commandCompleter.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
    *///?}

    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void invchat$extractCommandSuggestions(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        // AbstractRecipeBookScreen owns a separate extraction path in modern versions. Its
        // dedicated mixin forwards through the same bridge below, so skip it here.
        if ((Object) this instanceof InvChatRecipeBookScreenMarker) {
            return;
        }

        this.invchat$extractSuggestionOverlay(graphics, mouseX, mouseY);
    }

    @Override
    public void invchat$extractSuggestionOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.invchat$isChatFocused() && this.invchat$commandCompleter != null) {
            this.invchat$commandCompleter.extractRenderState(graphics, mouseX, mouseY);
        }
    }
    //?}

    //? if >=1.20 && <26.1 {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void invchat$renderCommandSuggestions(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        // 1.21.9+ recipe-book screens have their own render path and forward through a dedicated
        // mixin. Earlier versions continue using this already-working container hook.
        //? if >=1.21.9 {
        if ((Object) this instanceof InvChatRecipeBookScreenMarker) {
            return;
        }
        //?}

        this.invchat$renderSuggestionOverlay(graphics, mouseX, mouseY);
    }

    @Override
    public void invchat$renderSuggestionOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.invchat$isChatFocused() && this.invchat$commandCompleter != null) {
            this.invchat$commandCompleter.render(graphics, mouseX, mouseY);
        }
    }
    *///?}

    //? if <1.20 {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void invchat$renderCommandSuggestions(
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        this.invchat$renderSuggestionOverlay(poseStack, mouseX, mouseY);
    }

    @Override
    public void invchat$renderSuggestionOverlay(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.invchat$isChatFocused() && this.invchat$commandCompleter != null) {
            this.invchat$commandCompleter.render(poseStack, mouseX, mouseY);
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
            ChatHistory.add(input);
            this.invchat$chatBox.setValue("");
            this.invchat$chatBox.resetHistoryNavigation();
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

        if (this.invchat$commandCompleter != null) {
            if (focused) {
                this.invchat$commandCompleter.refresh();
            } else {
                this.invchat$commandCompleter.hide();
            }
        }

        //? if >=1.19.4 {
        this.invchat$chatBox.setFocused(focused);
        //?}
        //? if <1.19.4 {
        /*this.invchat$chatBox.setFocus(focused);
        *///?}
    }
}
