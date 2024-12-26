package com.uply.minions.minions;

import com.uply.minions.MinionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Minion {
    private final MinionPlugin plugin;
    private final UUID owner;
    private final String type;
    private int level;
    private final Location location;
    private final Map<Material, Integer> storage;
    private long lastMiningTime;
    private BukkitRunnable miningTask;

    public Minion(MinionPlugin plugin, UUID owner, String type, int level, Location location) {
        this.plugin = plugin;
        this.owner = owner;
        this.type = type;
        this.level = level;
        this.location = location;
        this.storage = new HashMap<>();
        this.lastMiningTime = System.currentTimeMillis();
        startMining();
    }

    private void startMining() {
        ConfigurationSection config = plugin.getConfigManager().getMinionsConfig()
                .getConfigurationSection("minions." + type);
        if (config == null) return;

        int miningSpeed = config.getInt("mining_speed." + level, 100);
        miningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!canMine()) {
                    return;
                }
                mine();
            }
        };
        miningTask.runTaskTimer(plugin, miningSpeed, miningSpeed);
    }

    private boolean canMine() {
        int maxStorage = plugin.getConfigManager().getMinionsConfig()
                .getInt("minions." + type + ".storage." + level, 64);

        return storage.values().stream().mapToInt(Integer::intValue).sum() < maxStorage;
    }

    private void mine() {
        ConfigurationSection config = plugin.getConfigManager().getMinionsConfig()
                .getConfigurationSection("minions." + type);
        if (config == null) return;

        List<String> platformMaterials = config.getStringList("block_platform");
        int platformSize = config.getInt("platform_size", 3);

        // Obtenir les blocs minables autour du minion
        List<Block> mineableBlocks = new ArrayList<>();
        for (int x = -platformSize/2; x <= platformSize/2; x++) {
            for (int z = -platformSize/2; z <= platformSize/2; z++) {
                if (x == 0 && z == 0) continue; // Skip le bloc central (bedrock)

                Block block = location.clone().add(x, -1, z).getBlock();
                if (platformMaterials.contains(block.getType().name())) {
                    mineableBlocks.add(block);
                }
            }
        }

        if (mineableBlocks.isEmpty()) return;

        // Choisir un bloc aléatoire à miner
        Block blockToMine = mineableBlocks.get(new Random().nextInt(mineableBlocks.size()));
        Material blockType = blockToMine.getType();

        // Gérer les drops configurés
        ConfigurationSection drops = config.getConfigurationSection("drops." + blockType.name());
        if (drops != null) {
            for (String key : drops.getKeys(false)) {
                ConfigurationSection dropConfig = drops.getConfigurationSection(key);
                if (dropConfig == null) continue;

                Material dropMaterial = Material.valueOf(dropConfig.getString("item"));
                int amount = dropConfig.getInt("amount", 1);
                double chance = dropConfig.getDouble("chance", 100.0);

                if (new Random().nextDouble() * 100 <= chance) {
                    storage.merge(dropMaterial, amount, Integer::sum);
                }
            }
        }

        // Mettre à jour le temps de minage
        lastMiningTime = System.currentTimeMillis();
    }

    public void createPlatform() {
        ConfigurationSection config = plugin.getConfigManager().getMinionsConfig()
                .getConfigurationSection("minions." + type);
        if (config == null) return;

        List<String> platformMaterials = config.getStringList("block_platform");
        if (platformMaterials.isEmpty()) return;

        int platformSize = config.getInt("platform_size", 3);
        Location center = location.clone().subtract(0, 1, 0);

        // Placer le bedrock au centre
        center.getBlock().setType(Material.BEDROCK);

        // Créer la plateforme
        for (int x = -platformSize/2; x <= platformSize/2; x++) {
            for (int z = -platformSize/2; z <= platformSize/2; z++) {
                if (x == 0 && z == 0) continue; // Skip le bloc central

                Block block = center.clone().add(x, 0, z).getBlock();
                if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
                    Material platformMaterial = Material.valueOf(
                            platformMaterials.get(new Random().nextInt(platformMaterials.size()))
                    );
                    block.setType(platformMaterial);
                }
            }
        }
    }

    public void upgrade() {
        ConfigurationSection config = plugin.getConfigManager().getMinionsConfig()
                .getConfigurationSection("minions." + type);
        if (config == null) return;

        int maxLevel = config.getInt("max_level", 3);
        if (level >= maxLevel) return;

        double upgradeCost = config.getDouble("upgrade_cost." + (level + 1), 0);
        if (upgradeCost > 0) {
            if (plugin.getEconomy().getBalance(plugin.getServer().getOfflinePlayer(owner)) < upgradeCost) {
                return;
            }
            plugin.getEconomy().withdrawPlayer(plugin.getServer().getOfflinePlayer(owner), upgradeCost);
        }

        level++;

        // Redémarrer la tâche de minage avec la nouvelle vitesse
        if (miningTask != null) {
            miningTask.cancel();
        }
        startMining();
    }

    public void collectStorage(UUID collector) {
        if (!collector.equals(owner)) return;

        storage.forEach((material, amount) -> {
            ItemStack items = new ItemStack(material, amount);
            Location dropLocation = location.clone().add(0, 1, 0);
            dropLocation.getWorld().dropItemNaturally(dropLocation, items);
        });
        storage.clear();
    }

    public String getStorageAsString() {
        StringBuilder sb = new StringBuilder();
        storage.forEach((material, amount) ->
                sb.append(material.name()).append(":").append(amount).append(";"));
        return sb.toString();
    }

    public void loadStorageFromString(String storageString) {
        storage.clear();
        if (storageString == null || storageString.isEmpty()) return;

        String[] items = storageString.split(";");
        for (String item : items) {
            String[] parts = item.split(":");
            if (parts.length != 2) continue;

            try {
                Material material = Material.valueOf(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                storage.put(material, amount);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // Getters
    public UUID getOwner() {
        return owner;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public Location getLocation() {
        return location;
    }

    public Map<Material, Integer> getStorage() {
        return new HashMap<>(storage);
    }

    public long getLastMiningTime() {
        return lastMiningTime;
    }

    public void setLastMiningTime(long time) {
        this.lastMiningTime = time;
    }

    public void cleanup() {
        if (miningTask != null) {
            miningTask.cancel();
        }
    }
}