package io.github.tootertutor.invchat.gui;

import io.github.tootertutor.invchat.InvChat;
import io.github.tootertutor.invchat.config.ConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
//? if <1.20.2 {
/*import net.minecraft.client.gui.components.ImageButton;
*///?}
//? if >=1.20.2 {
import net.minecraft.client.gui.components.SpriteIconButton;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if <1.19 {
/*import net.minecraft.network.chat.TextComponent;
*///?}
//? if <1.21.11 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?}

/** Builds InvChat's Cloth Config screen and inventory entry button. */
public final class InvChatConfigScreen {
    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ICON_SIZE = 16;

    // Minecraft 1.20.2+ loads SpriteIconButton images from the GUI sprite atlas.
    // Older ImageButton versions address the same PNG as a raw texture instead.
    //? if <1.20.2 {
    /*private static final ResourceLocation GEAR_TEXTURE =
            new ResourceLocation("invchat", "textures/gui/sprites/gear.png");
    *///?}
    //? if >=1.20.2 && <1.21 {
    /*private static final ResourceLocation GEAR_SPRITE =
            new ResourceLocation("invchat", "gear");
    *///?}
    //? if >=1.21 && <1.21.11 {
    /*private static final ResourceLocation GEAR_SPRITE =
            ResourceLocation.fromNamespaceAndPath("invchat", "gear");
    *///?}
    //? if >=1.21.11 {
    private static final Identifier GEAR_SPRITE =
            Identifier.fromNamespaceAndPath("invchat", "gear");
    //?}

    private InvChatConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(text("InvChat Settings"));

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory chat = builder.getOrCreateCategory(text("Chat"));
        ConfigCategory suggestions = builder.getOrCreateCategory(text("Suggestions"));

        InvChatSettingsPage.addChatSettings(chat, entries);
        InvChatSettingsPage.addSuggestionSettings(suggestions, entries);

        builder.setSavingRunnable(() -> ConfigManager.save(InvChat.getConfig()));
        return builder.build();
    }

    public static Button createButton(Screen parent, int x, int y) {
        //? if <1.20.2 {
        /*return new ImageButton(
                x,
                y,
                ICON_SIZE,
                ICON_SIZE,
                0,
                0,
                0,
                GEAR_TEXTURE,
                ICON_SIZE,
                ICON_SIZE,
                button -> open(parent)
        );
        *///?}
        //? if >=1.20.2 {
        SpriteIconButton button = SpriteIconButton.builder(
                        text("InvChat Settings"),
                        ignored -> open(parent),
                        true
                )
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .sprite(GEAR_SPRITE, ICON_SIZE, ICON_SIZE)
                .build();
        button.setPosition(x, y);
        return button;
        //?}
    }

    private static void open(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = create(parent);

        //? if >=26.2 {
        minecraft.gui.setScreen(screen);
        //?}
        //? if <26.2 {
        /*minecraft.setScreen(screen);
        *///?}
    }

    static Component text(String value) {
        //? if >=1.19 {
        return Component.literal(value);
        //?}
        //? if <1.19 {
        /*return new TextComponent(value);
        *///?}
    }
}
