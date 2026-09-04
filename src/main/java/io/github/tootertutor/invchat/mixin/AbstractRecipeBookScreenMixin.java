package io.github.tootertutor.invchat.mixin;

import io.github.tootertutor.invchat.render.InvChatContainerRenderBridge;
import io.github.tootertutor.invchat.render.InvChatRecipeBookScreenMarker;
//? if >=1.20 && <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
//? if >=1.21.9 {
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Newer recipe-book screens own their render/extraction path instead of reliably flowing through
 * the shared AbstractContainerScreen hook. Forward their tail render to InvChat's shared bridge.
 */
//? if >=1.21.9 {
@Mixin(AbstractRecipeBookScreen.class)
//?}
//? if <1.21.9 {
/*@Pseudo
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen")
*///?}
public abstract class AbstractRecipeBookScreenMixin implements InvChatRecipeBookScreenMarker {
    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void invchat$extractSuggestionOverlay(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        ((InvChatContainerRenderBridge) this).invchat$extractSuggestionOverlay(graphics, mouseX, mouseY);
    }
    //?}

    //? if >=1.21.9 && <26.1 {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void invchat$renderSuggestionOverlay(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        ((InvChatContainerRenderBridge) this).invchat$renderSuggestionOverlay(graphics, mouseX, mouseY);
    }
    *///?}
}
