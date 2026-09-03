package io.github.tootertutor.invchat.render;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
//? if >=1.20 && <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}

/**
 * Render bridge exposed by the shared container mixin so render-path-specific mixins can ask the
 * same InvChat instance to draw its suggestion overlay.
 */
public interface InvChatContainerRenderBridge {
    /** Lets the global mouse handler offer wheel input to InvChat before the container consumes it. */
    boolean invchat$scrollSuggestionSelection(double amount);

    //? if >=26.1 {
    void invchat$extractSuggestionOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY);
    //?}

    //? if >=1.20 && <26.1 {
    /*void invchat$renderSuggestionOverlay(GuiGraphics graphics, int mouseX, int mouseY);
    *///?}

    //? if <1.20 {
    /*void invchat$renderSuggestionOverlay(PoseStack poseStack, int mouseX, int mouseY);
    *///?}
}
