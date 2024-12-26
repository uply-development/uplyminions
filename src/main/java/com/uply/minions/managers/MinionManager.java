package com.uply.minions.managers;

import com.uply.minions.MinionPlugin;
import com.uply.minions.minions.Minion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class MinionManager implements Listener {
    private final MinionPlugin plugin;
    private final Map<UUID, Set<Minion>> playerMinions;
    private final Map<Location, Minion> minionLocations;

    public MinionManager(MinionPlugin plugin) {
        this.plugin = plugin;
        this.playerMinions = new HashMap<>();
        this.minionLocations = new HashMap<>();
        loadMinions();
    }

    public void loadMinions() {
        plugin.getDatabaseManager().loadAllMinions().forEach(this::registerMinion);
    }

    public void saveAllMinions() {
        minionLocations.values().forEach(minion ->
                plugin.getDatabaseManager().saveMinion(minion));
    }

    public void registerMinion(Minion minion) {
        UUID owner = minion.getOwner();
        playerMinions.computeIfAbsent(owner, k -> new HashSet<>()).add(minion);
        minionLocations.put(minion.getLocation(), minion);
    }

    public void unregisterMinion(Minion minion) {
        playerMinions.getOrDefault(minion.getOwner(), new HashSet<>()).remove(minion);
        minionLocations.remove(minion.getLocation());
    }

    public Minion getMinionAtLocation(Location location) {
        return minionLocations.get(location);
    }

    public Set<Minion> getPlayerMinions(UUID player) {
        return playerMinions.getOrDefault(player, new HashSet<>());
    }

    public Minion getNearestMinion(Player player, double maxDistance) {
        Location playerLoc = player.getLocation();
        return minionLocations.entrySet().stream()
                .filter(entry -> entry.getKey().getWorld().equals(playerLoc.getWorld()))
                .filter(entry -> entry.getKey().distance(playerLoc) <= maxDistance)
                .min(Comparator.comparingDouble(entry ->
                        entry.getKey().distance(playerLoc)))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public boolean canPlaceMinion(Location location) {
        // Vérification si l'emplacement est valide pour placer un minion
        return !minionLocations.containsKey(location);
    }
}