package com.uply.minions.gui;

import com.uply.minions.MinionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final MinionPlugin plugin;
    private final Inventory inventory;

    public ShopGUI(MinionPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 54, "§6Boutique de Minions");
        initializeItems();
    }

    private void initializeItems() {
        ConfigurationSection minionsConfig = plugin.getConfigManager().getMinionsConfig()
                .getConfigurationSection("minions");
        if (minionsConfig == null) return;

        int slot = 0;
        for (String minionType : minionsConfig.getKeys(false)) {
            ConfigurationSection minionSection = minionsConfig.getConfigurationSection(minionType);
            if (minionSection == null || !minionSection.getBoolean("admin_shop", false)) continue;

            double price = minionSection.getDouble("admin_shop_price", 0);
            Material displayMaterial = Material.valueOf(
                    minionSection.getString("display_item", "DIAMOND_PICKAXE")
            );

            ItemStack shopItem = new ItemStack(displayMaterial);
            ItemMeta meta = shopItem.getItemMeta();
            meta.setDisplayName("§6" + minionSection.getString("name", minionType));
            List<String> lore = new ArrayList<>();
            lore.add("§7Prix: §6" + price + "$");
            lore.add("");
            lore.add("§eCliquez pour acheter!");
            meta.setLore(lore);
            shopItem.setItemMeta(meta);

            inventory.setItem(slot++, shopItem);
        }
    }

    public void show(Player player) {
        player.openInventory(inventory);
    }
}