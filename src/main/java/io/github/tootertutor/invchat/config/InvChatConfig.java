package io.github.tootertutor.invchat.config;

import java.util.Locale;

/** Persistent InvChat configuration stored in {@code config/invchat.json}. */
public final class InvChatConfig {
    public boolean enabled = true;
    public boolean anchorBelowInventory = false;
    public int xOffset = 0;
    public int yOffset = 0;
    public int width = 200;

    /** Dropdown anchor: ABOVE_CHAT, BELOW_CHAT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT. */
    public String suggestionAnchor = "ABOVE_CHAT";
    public int suggestionXOffset = 0;
    public int suggestionYOffset = 0;
    public int suggestionMaxVisible = 8;

    /** Keeps manually edited values inside ranges useful to the UI. */
    public void sanitize() {
        if (this.width < 40) {
            this.width = 40;
        } else if (this.width > 1000) {
            this.width = 1000;
        }

        if (this.suggestionMaxVisible < 1) {
            this.suggestionMaxVisible = 1;
        } else if (this.suggestionMaxVisible > 20) {
            this.suggestionMaxVisible = 20;
        }

        if (this.suggestionAnchor == null) {
            this.suggestionAnchor = "ABOVE_CHAT";
            return;
        }

        String normalized = this.suggestionAnchor.toUpperCase(Locale.ROOT);
        if (!"ABOVE_CHAT".equals(normalized)
                && !"BELOW_CHAT".equals(normalized)
                && !"TOP_LEFT".equals(normalized)
                && !"TOP_RIGHT".equals(normalized)
                && !"BOTTOM_LEFT".equals(normalized)
                && !"BOTTOM_RIGHT".equals(normalized)) {
            normalized = "ABOVE_CHAT";
        }
        this.suggestionAnchor = normalized;
    }
}
