package com.uply.minions.listeners;

import com.uply.minions.MinionPlugin;
import com.uply.minions.minions.Minion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class MinionPlaceListener implements Listener {
    private final MinionPlugin plugin;

    public MinionPlaceListener(MinionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMinionPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.PLAYER_HEAD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "minion_type");

        if (!container.has(key, PersistentDataType.STRING)) return;

        String minionType = container.get(key, PersistentDataType.STRING);
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        // Vérifier si l'emplacement est valide
        if (!plugin.getMinionManager().canPlaceMinion(location)) {
            event.setCancelled(true);
            player.sendMessage("§cVous ne pouvez pas placer un minion ici.");
            return;
        }

        // Créer et enregistrer le minion
        Minion minion = new Minion(plugin, player.getUniqueId(), minionType, 1, location);
        plugin.getMinionManager().registerMinion(minion);
        minion.createPlatform();

        player.sendMessage("§aMinion placé avec succès!");
    }
}