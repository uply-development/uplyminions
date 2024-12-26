package com.uply.minions;

import com.uply.minions.commands.PlayerCommands;
import com.uply.minions.database.DatabaseManager;
import com.uply.minions.managers.ConfigManager;
import com.uply.minions.managers.MinionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MinionPlugin extends JavaPlugin {
    private static MinionPlugin instance;
    private Economy economy;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private MinionManager minionManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation des configurations
        saveDefaultConfig();
        saveResource("minions.yml", false);

        // Initialisation des managers
        configManager = new ConfigManager(this);
        if (!setupDatabase()) {
            getLogger().severe("ERR : Impossible de se connecter à la base de données! Désactivation du plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Planifier l'initialisation de l'économie après un délai de 20 ticks (1 seconde)
        getServer().getScheduler().runTaskLater(this, () -> {
            if (!setupEconomy()) {
                getLogger().severe("ERR : Veillez à ce que Vault et un plugin d'économie (comme EssentialsX) soient installés ! Désactivation du plugin...");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            minionManager = new MinionManager(this);

            // Enregistrement des commandes
            getCommand("minions").setExecutor(new PlayerCommands(this));
            getCommand("minions").setTabCompleter(new PlayerCommands(this));

            // Enregistrement des listeners
            getServer().getPluginManager().registerEvents(minionManager, this);
        }, 20L); // Attendre 1 seconde (20 ticks) avant de configurer Vault et l'économie
    }

    @Override
    public void onDisable() {
        if (minionManager != null) {
            minionManager.saveAllMinions();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private boolean setupDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            return databaseManager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public static MinionPlugin getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MinionManager getMinionManager() {
        return minionManager;
    }
}