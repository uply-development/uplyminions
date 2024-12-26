package com.uply.minions.commands;

import com.uply.minions.MinionPlugin;
import com.uply.minions.gui.AuctionGUI;
import com.uply.minions.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCommands implements CommandExecutor, TabCompleter {
    private final MinionPlugin plugin;
    private final AdminCommands adminCommands;

    public PlayerCommands(MinionPlugin plugin) {
        this.plugin = plugin;
        this.adminCommands = new AdminCommands(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "shop" -> {
                if (!player.hasPermission("minions.shop")) {
                    player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                new ShopGUI(plugin).show(player);
            }
            case "hdv" -> {
                if (!player.hasPermission("minions.hdv")) {
                    player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                new AuctionGUI(plugin).show(player);
            }
            case "admin" -> {
                return adminCommands.onCommand(sender, command, label,
                        Arrays.copyOfRange(args, 1, args.length));
            }
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== Aide des Minions ===");
        player.sendMessage("§e/minions shop §7- Ouvre le shop des minions");
        player.sendMessage("§e/minions hdv §7- Ouvre l'hôtel des ventes");
        if (player.hasPermission("minions.admin")) {
            player.sendMessage("§e/minions admin §7- Commandes administrateur");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>(Arrays.asList("shop", "hdv"));
            if (sender.hasPermission("minions.admin")) {
                commands.add("admin");
            }
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
            return adminCommands.onTabComplete(sender, command, label,
                    Arrays.copyOfRange(args, 1, args.length));
        }

        return completions;
    }
}