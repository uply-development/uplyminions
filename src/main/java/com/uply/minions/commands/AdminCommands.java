package com.uply.minions.commands;

import com.uply.minions.MinionPlugin;
import com.uply.minions.minions.Minion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminCommands implements TabCompleter {
    private final MinionPlugin plugin;

    public AdminCommands(MinionPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (!player.hasPermission("minions.admin")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser les commandes admin.");
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(player, args);
            case "select" -> handleSelect(player);
            case "remove" -> handleRemove(player);
            case "tphere" -> handleTeleportHere(player);
            case "info" -> handleInfo(player);
            case "infoplayer" -> handleInfoPlayer(player, args);
            default -> sendAdminHelp(player);
        }

        return true;
    }

    private void handleGive(Player player, String[] args) {
        if (!player.hasPermission("minions.admin.give")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUtilisation: /minions admin give <joueur> <type>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        String minionType = args[2];
        if (!plugin.getConfigManager().getMinionsConfig().contains("minions." + minionType)) {
            player.sendMessage("§cType de minion invalide.");
            return;
        }

        // TODO: Créer et donner l'item du minion au joueur
        player.sendMessage("§aMinion donné à " + target.getName());
    }

    private void handleSelect(Player player) {
        if (!player.hasPermission("minions.admin.select")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        Minion nearest = plugin.getMinionManager().getNearestMinion(player, 10);
        if (nearest == null) {
            player.sendMessage("§cAucun minion trouvé à proximité.");
            return;
        }

        player.sendMessage("§aMinion sélectionné: " + nearest.getType() + " (Niveau " + nearest.getLevel() + ")");
    }

    private void handleRemove(Player player) {
        if (!player.hasPermission("minions.admin.remove")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        Minion nearest = plugin.getMinionManager().getNearestMinion(player, 10);
        if (nearest == null) {
            player.sendMessage("§cAucun minion trouvé à proximité.");
            return;
        }

        plugin.getMinionManager().unregisterMinion(nearest);
        plugin.getDatabaseManager().deleteMinion(nearest);
        nearest.cleanup();
        player.sendMessage("§aMinion retiré avec succès.");
    }

    private void handleTeleportHere(Player player) {
        if (!player.hasPermission("minions.admin.tphere")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        Minion nearest = plugin.getMinionManager().getNearestMinion(player, 50);
        if (nearest == null) {
            player.sendMessage("§cAucun minion trouvé à proximité.");
            return;
        }

        // TODO: Implémenter la téléportation du minion
        player.sendMessage("§aMinion téléporté à votre position.");
    }

    private void handleInfo(Player player) {
        if (!player.hasPermission("minions.admin.info")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        Minion nearest = plugin.getMinionManager().getNearestMinion(player, 10);
        if (nearest == null) {
            player.sendMessage("§cAucun minion trouvé à proximité.");
            return;
        }

        player.sendMessage("§6=== Informations du Minion ===");
        player.sendMessage("§7Type: §f" + nearest.getType());
        player.sendMessage("§7Niveau: §f" + nearest.getLevel());
        player.sendMessage("§7Propriétaire: §f" + Bukkit.getOfflinePlayer(nearest.getOwner()).getName());
        player.sendMessage("§7Position: §f" + nearest.getLocation().getBlockX() + ", "
                + nearest.getLocation().getBlockY() + ", " + nearest.getLocation().getBlockZ());
    }

    private void handleInfoPlayer(Player player, String[] args) {
        if (!player.hasPermission("minions.admin.info")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /minions admin infoplayer <joueur>");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        Set<Minion> minions = plugin.getMinionManager().getPlayerMinions(target.getUniqueId());
        player.sendMessage("§6=== Minions de " + target.getName() + " ===");
        player.sendMessage("§7Nombre total: §f" + minions.size());
        for (Minion minion : minions) {
            player.sendMessage("§7- " + minion.getType() + " (Niveau " + minion.getLevel() + ")");
        }
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage("§6=== Commandes Admin des Minions ===");
        player.sendMessage("§e/minions admin give <joueur> <type> §7- Donne un minion");
        player.sendMessage("§e/minions admin select §7- Sélectionne le minion le plus proche");
        player.sendMessage("§e/minions admin remove §7- Retire le minion sélectionné");
        player.sendMessage("§e/minions admin tphere §7- Téléporte le minion à votre position");
        player.sendMessage("§e/minions admin info §7- Affiche les infos du minion sélectionné");
        player.sendMessage("§e/minions admin infoplayer <joueur> §7- Affiche les minions d'un joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = List.of("give", "select", "remove", "tphere", "info", "infoplayer");
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return plugin.getConfigManager().getMinionsConfig().getConfigurationSection("minions").getKeys(false).stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}