package io.github.tootertutor.invchat.gui;

import java.util.Locale;

import io.github.tootertutor.invchat.InvChat;
import io.github.tootertutor.invchat.config.InvChatConfig;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

/** Populates the Cloth Config categories with InvChat's persistent settings. */
public final class InvChatSettingsPage {
    private InvChatSettingsPage() {
    }

    public static void addChatSettings(ConfigCategory category, ConfigEntryBuilder entries) {
        InvChatConfig config = InvChat.getConfig();

        category.addEntry(entries.startBooleanToggle(
                        InvChatConfigScreen.text("Enabled"),
                        config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.enabled = value)
                .build());

        category.addEntry(entries.startEnumSelector(
                        InvChatConfigScreen.text("Chat Position"),
                        ChatPosition.class,
                        config.anchorBelowInventory ? ChatPosition.BELOW_INVENTORY : ChatPosition.BOTTOM_SCREEN)
                .setDefaultValue(ChatPosition.BOTTOM_SCREEN)
                .setEnumNameProvider(value -> InvChatConfigScreen.text(formatEnum(value.name())))
                .setSaveConsumer(value -> config.anchorBelowInventory = value == ChatPosition.BELOW_INVENTORY)
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("X Offset"),
                        config.xOffset)
                .setDefaultValue(0)
                .setSaveConsumer(value -> config.xOffset = value)
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("Y Offset"),
                        config.yOffset)
                .setDefaultValue(0)
                .setSaveConsumer(value -> config.yOffset = value)
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("Chat Width"),
                        config.width)
                .setDefaultValue(200)
                .setSaveConsumer(value -> config.width = value)
                .build());
    }

    public static void addSuggestionSettings(ConfigCategory category, ConfigEntryBuilder entries) {
        InvChatConfig config = InvChat.getConfig();

        category.addEntry(entries.startEnumSelector(
                        InvChatConfigScreen.text("Anchor"),
                        SuggestionAnchor.class,
                        SuggestionAnchor.fromConfig(config.suggestionAnchor))
                .setDefaultValue(SuggestionAnchor.ABOVE_CHAT)
                .setEnumNameProvider(value -> InvChatConfigScreen.text(formatEnum(value.name())))
                .setSaveConsumer(value -> config.suggestionAnchor = value.name())
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("X Offset"),
                        config.suggestionXOffset)
                .setDefaultValue(0)
                .setSaveConsumer(value -> config.suggestionXOffset = value)
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("Y Offset"),
                        config.suggestionYOffset)
                .setDefaultValue(0)
                .setSaveConsumer(value -> config.suggestionYOffset = value)
                .build());

        category.addEntry(entries.startIntField(
                        InvChatConfigScreen.text("Visible Suggestions"),
                        config.suggestionMaxVisible)
                .setDefaultValue(8)
                .setSaveConsumer(value -> config.suggestionMaxVisible = value)
                .build());
    }

    private static String formatEnum(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    private enum ChatPosition {
        BOTTOM_SCREEN,
        BELOW_INVENTORY
    }

    private enum SuggestionAnchor {
        ABOVE_CHAT,
        BELOW_CHAT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

        private static SuggestionAnchor fromConfig(String value) {
            if (value == null) {
                return ABOVE_CHAT;
            }

            try {
                return SuggestionAnchor.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return ABOVE_CHAT;
            }
        }
    }
}
