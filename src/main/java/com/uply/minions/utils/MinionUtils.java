package com.uply.minions.utils;

import com.uply.minions.MinionPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class MinionUtils {
    private static final MinionPlugin plugin = MinionPlugin.getInstance();

    public static ItemStack createMinionItem(String type) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        String name = plugin.getConfigManager().getMinionsConfig()
                .getString("minions." + type + ".name", "§6Minion " + type);
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("§7Type: §f" + type);
        lore.add("§7Niveau: §f1");
        lore.add("");
        lore.add("§eCliquez pour placer!");
        meta.setLore(lore);

        // Stocker le type de minion dans les données persistantes
        NamespacedKey key = new NamespacedKey(plugin, "minion_type");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type);

        item.setItemMeta(meta);
        return item;
    }
}