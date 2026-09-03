package io.github.tootertutor.invchat.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

/** Loads and persists {@code config/invchat.json}. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("invchat.json");

    private ConfigManager() {
    }

    public static InvChatConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            InvChatConfig defaults = new InvChatConfig();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            InvChatConfig config = GSON.fromJson(reader, InvChatConfig.class);
            if (config == null) {
                config = new InvChatConfig();
            }

            config.sanitize();
            // Persist sanitized/defaulted fields so newly added config options appear in existing files.
            save(config);
            return config;
        } catch (IOException | JsonParseException exception) {
            System.err.println("[InvChat] Could not read " + CONFIG_PATH + ": " + exception.getMessage());
            System.err.println("[InvChat] Falling back to default configuration for this session.");
            return new InvChatConfig();
        }
    }

    public static void save(InvChatConfig config) {
        config.sanitize();

        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            System.err.println("[InvChat] Could not write " + CONFIG_PATH + ": " + exception.getMessage());
        }
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
