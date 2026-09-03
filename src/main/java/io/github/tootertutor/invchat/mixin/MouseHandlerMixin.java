package io.github.tootertutor.invchat.mixin;

import io.github.tootertutor.invchat.render.InvChatContainerRenderBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes mouse-wheel input to InvChat suggestions before inventory-specific scroll handlers run. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void invchat$scrollSuggestions(
            long window,
            double horizontalAmount,
            double verticalAmount,
            CallbackInfo ci
    ) {
        if (verticalAmount == 0.0D) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        //? if >=26.2 {
        Screen screen = minecraft.gui.screen();
        //?}
        //? if <26.2 {
        /*Screen screen = minecraft.screen;
        *///?}

        if (screen instanceof InvChatContainerRenderBridge
                && ((InvChatContainerRenderBridge) screen)
                        .invchat$scrollSuggestionSelection(verticalAmount)) {
            // The dropdown consumed this wheel notch. Do not also scroll recipe books, creative
            // tabs, villager trades, bundles, or any other inventory-specific scroll target.
            ci.cancel();
        }
    }
}
