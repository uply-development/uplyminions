package com.uply.minions.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.uply.minions.MinionPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final MinionPlugin plugin;
    private FileConfiguration minionsConfig;
    private final Map<String, FileConfiguration> configs;

    public ConfigManager(MinionPlugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        loadConfigs();
    }

    private void loadConfigs() {
        File minionsFile = new File(plugin.getDataFolder(), "minions.yml");
        if (!minionsFile.exists()) {
            plugin.saveResource("minions.yml", false);
        }
        minionsConfig = YamlConfiguration.loadConfiguration(minionsFile);
        configs.put("minions", minionsConfig);
    }

    public FileConfiguration getMinionsConfig() {
        return minionsConfig;
    }

    public void reloadConfigs() {
        loadConfigs();
    }

    public void saveConfig(String configName) {
        FileConfiguration config = configs.get(configName);
        if (config == null) return;

        try {
            config.save(new File(plugin.getDataFolder(), configName + ".yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde de " + configName + ".yml");
            e.printStackTrace();
        }
    }
}