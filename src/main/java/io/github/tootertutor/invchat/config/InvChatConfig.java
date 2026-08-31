package io.github.tootertutor.invchat.config;

/**
 * Persistent InvChat configuration.
 *
 * <p>The format deliberately contains only simple JSON primitives so it can remain stable across
 * every supported Minecraft/Java generation and later be edited by a config screen without
 * changing the on-disk representation.</p>
 */
public final class InvChatConfig {
    public boolean enabled = true;
    public boolean anchorBelowInventory = true;
    public int xOffset = 0;
    public int yOffset = 0;
    public int width = 200;

    /** Keeps manually edited values inside ranges that are useful to the widget. */
    public void sanitize() {
        if (this.width < 40) {
            this.width = 40;
        } else if (this.width > 1000) {
            this.width = 1000;
        }
    }
}
