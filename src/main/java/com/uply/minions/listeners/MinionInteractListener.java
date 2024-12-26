package com.uply.minions.listeners;

import com.uply.minions.MinionPlugin;
import com.uply.minions.gui.MinionGUI;
import com.uply.minions.minions.Minion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MinionInteractListener implements Listener {
    private final MinionPlugin plugin;

    public MinionInteractListener(MinionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMinionInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Minion minion = plugin.getMinionManager().getMinionAtLocation(event.getClickedBlock().getLocation());
        if (minion == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Vérifier si le joueur est le propriétaire ou un admin
        if (!minion.getOwner().equals(player.getUniqueId()) &&
                !player.hasPermission("minions.admin")) {
            player.sendMessage("§cCe minion ne vous appartient pas!");
            return;
        }

        // Ouvrir le GUI du minion
        new MinionGUI(plugin, minion).show(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§6Minion ") &&
                !title.equals("§6Boutique de Minions") &&
                !title.equals("§6Hôtel des ventes")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Gérer les clics dans le GUI du minion
        if (title.startsWith("§6Minion ")) {
            handleMinionGUIClick(event, player, clicked);
        }
        // Gérer les clics dans la boutique
        else if (title.equals("§6Boutique de Minions")) {
            handleShopGUIClick(event, player, clicked);
        }
        // Gérer les clics dans l'hôtel des ventes
        else if (title.equals("§6Hôtel des ventes")) {
            handleAuctionGUIClick(event, player, clicked);
        }
    }

    private void handleMinionGUIClick(InventoryClickEvent event, Player player, ItemStack clicked) {
        String minionType = event.getView().getTitle().substring(8); // Enlever "§6Minion "
        Minion minion = plugin.getMinionManager().getNearestMinion(player, 10);

        if (minion == null || !minion.getType().equals(minionType)) {
            player.closeInventory();
            return;
        }

        // Gérer le bouton d'amélioration
        if (clicked.getType() == Material.EXPERIENCE_BOTTLE) {
            minion.upgrade();
            new MinionGUI(plugin, minion).show(player); // Rafraîchir le GUI
            return;
        }

        // Gérer le bouton de collecte
        if (clicked.getType() == Material.HOPPER) {
            minion.collectStorage(player.getUniqueId());
            new MinionGUI(plugin, minion).show(player); // Rafraîchir le GUI
            return;
        }

        // Gérer le bouton de retrait
        if (clicked.getType() == Material.BARRIER) {
            plugin.getMinionManager().unregisterMinion(minion);
            plugin.getDatabaseManager().deleteMinion(minion);
            minion.cleanup();
            player.closeInventory();
            player.sendMessage("§aMinion retiré avec succès!");
        }
    }

    private void handleShopGUIClick(InventoryClickEvent event, Player player, ItemStack clicked) {
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String minionType = clicked.getItemMeta().getDisplayName()
                .substring(2) // Enlever le "§6"
                .toLowerCase()
                .replace(" ", "_");

        double price = plugin.getConfigManager().getMinionsConfig()
                .getDouble("minions." + minionType + ".admin_shop_price", -1);

        if (price < 0) {
            player.sendMessage("§cCe minion n'est pas disponible à l'achat.");
            return;
        }

        // Vérifier si le joueur a assez d'argent
        if (plugin.getEconomy().getBalance(player) < price) {
            player.sendMessage("§cVous n'avez pas assez d'argent pour acheter ce minion!");
            return;
        }

        // Retirer l'argent et donner le minion
        plugin.getEconomy().withdrawPlayer(player, price);
        // TODO: Donner l'item du minion au joueur
        player.sendMessage("§aVous avez acheté un minion " + minionType + " pour " + price + "$!");
    }

    private void handleAuctionGUIClick(InventoryClickEvent event, Player player, ItemStack clicked) {
        // TODO: Implémenter le système d'enchères
        player.sendMessage("§eLe système d'enchères est en cours de développement!");
    }
}
