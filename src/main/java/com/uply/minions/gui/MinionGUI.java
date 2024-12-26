package com.uply.minions.gui;

import com.uply.minions.MinionPlugin;
import com.uply.minions.minions.Minion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinionGUI {
    private final MinionPlugin plugin;
    private final Inventory inventory;
    private final Minion minion;

    public MinionGUI(MinionPlugin plugin, Minion minion) {
        this.plugin = plugin;
        this.minion = minion;
        this.inventory = Bukkit.createInventory(null, 54, "§6Minion " + minion.getType());
        initializeItems();
    }

    private void initializeItems() {
        // Informations du minion
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6Informations");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Type: §f" + minion.getType());
        infoLore.add("§7Niveau: §f" + minion.getLevel());
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        // Bouton d'amélioration
        double upgradeCost = plugin.getConfigManager().getMinionsConfig()
                .getDouble("minions." + minion.getType() + ".upgrade_cost." + (minion.getLevel() + 1), -1);
        if (upgradeCost >= 0) {
            ItemStack upgradeItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
            ItemMeta upgradeMeta = upgradeItem.getItemMeta();
            upgradeMeta.setDisplayName("§aAméliorer le minion");
            List<String> upgradeLore = new ArrayList<>();
            upgradeLore.add("§7Coût: §6" + upgradeCost + "$");
            upgradeMeta.setLore(upgradeLore);
            upgradeItem.setItemMeta(upgradeMeta);
            inventory.setItem(49, upgradeItem);
        }

        // Affichage du stockage
        Map<Material, Integer> storage = minion.getStorage();
        int slot = 19;
        for (Map.Entry<Material, Integer> entry : storage.entrySet()) {
            if (slot >= 44) break;

            ItemStack storageItem = new ItemStack(entry.getKey(), Math.min(entry.getValue(), 64));
            ItemMeta storageMeta = storageItem.getItemMeta();
            storageMeta.setDisplayName("§f" + entry.getValue() + "x " + entry.getKey().name());
            storageItem.setItemMeta(storageMeta);
            inventory.setItem(slot++, storageItem);
        }

        // Bouton de collecte
        ItemStack collectItem = new ItemStack(Material.HOPPER);
        ItemMeta collectMeta = collectItem.getItemMeta();
        collectMeta.setDisplayName("§aTout collecter");
        collectItem.setItemMeta(collectMeta);
        inventory.setItem(45, collectItem);

        // Bouton de retrait
        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName("§cRetirer le minion");
        removeItem.setItemMeta(removeMeta);
        inventory.setItem(53, removeItem);
    }

    public void show(Player player) {
        player.openInventory(inventory);
    }
}
