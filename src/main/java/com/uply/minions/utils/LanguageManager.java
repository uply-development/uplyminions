package com.uply.minions.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * LanguageManager handles loading and retrieving messages for different languages.
 */
public class LanguageManager {

    private static final String LANG_FOLDER = "plugins/Minions/lang";
    private static final String DEFAULT_LANG = "en-US";

    private String currentLanguage;
    private final Map<String, String> messages;

    public LanguageManager(String language) {
        this.currentLanguage = language;
        this.messages = new HashMap<>();
        loadLanguage(language);
    }

    /**
     * Loads the language file.
     */
    public void loadLanguage(String language) {
        File langFile = new File(LANG_FOLDER, language + ".yml");

        if (!langFile.exists()) {
            System.out.println("Language file " + language + ".yml not found! Falling back to default.");
            langFile = new File(LANG_FOLDER, DEFAULT_LANG + ".yml");
        }

        if (!langFile.exists()) {
            System.out.println("Default language file not found! Using empty messages.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);

        for (String key : config.getKeys(true)) {
            messages.put(key, config.getString(key));
        }
    }

    /**
     * Retrieves a message by its key.
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }

    /**
     * Changes the language.
     */
    public void setLanguage(String language) {
        this.currentLanguage = language;
        messages.clear();
        loadLanguage(language);
    }

    /**
     * Gets the current language.
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
