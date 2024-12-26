package com.uply.minions.gui;

import com.uply.minions.MinionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AuctionGUI {
    private final MinionPlugin plugin;
    private final Inventory inventory;

    public AuctionGUI(MinionPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 54, "§6Hôtel des ventes");
        initializeItems();
    }

    private void initializeItems() {
        // TODO: Implémenter le système d'enchères
        // Cette fonctionnalité nécessitera une table supplémentaire dans la base de données
        // pour stocker les minions mis en vente et leurs prix
    }

    public void show(Player player) {
        player.openInventory(inventory);
    }
}