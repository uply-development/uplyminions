package com.uply.minions.database;

import com.uply.minions.MinionPlugin;
import com.uply.minions.minions.Minion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final MinionPlugin plugin;
    private Connection connection;
    private final String url;

    public DatabaseManager(MinionPlugin plugin) {
        this.plugin = plugin;

        // Récupération du type de base de données depuis config.yml
        if (plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("mysql")) {
            String host = plugin.getConfig().getString("database.mysql.host", "localhost");
            String port = plugin.getConfig().getString("database.mysql.port", "3306");
            String database = plugin.getConfig().getString("database.mysql.database", "minions");
            String username = plugin.getConfig().getString("database.mysql.username", "root");
            String password = plugin.getConfig().getString("database.mysql.password", "");
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&user=" + username + "&password=" + password;
        } else {
            this.url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/database.db";
        }
    }

    public boolean initialize() {
        try {
            if (url.startsWith("jdbc:mysql")) {
                Class.forName("com.mysql.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }

            connection = DriverManager.getConnection(url);
            createTables();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Table des minions
            stmt.execute("CREATE TABLE IF NOT EXISTS minions (" +
                    "id INTEGER PRIMARY KEY " + (url.startsWith("jdbc:mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                    "owner VARCHAR(36) NOT NULL," +
                    "type VARCHAR(50) NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "world VARCHAR(50) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "storage TEXT," +
                    "last_mining_time BIGINT" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveMinion(Minion minion) {
        String sql = "INSERT OR REPLACE INTO minions (owner, type, level, world, x, y, z, storage, last_mining_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, minion.getOwner().toString());
            pstmt.setString(2, minion.getType());
            pstmt.setInt(3, minion.getLevel());
            Location loc = minion.getLocation();
            pstmt.setString(4, loc.getWorld().getName());
            pstmt.setDouble(5, loc.getX());
            pstmt.setDouble(6, loc.getY());
            pstmt.setDouble(7, loc.getZ());
            pstmt.setString(8, minion.getStorageAsString());
            pstmt.setLong(9, minion.getLastMiningTime());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Minion> loadAllMinions() {
        List<Minion> minions = new ArrayList<>();
        String sql = "SELECT * FROM minions";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                String type = rs.getString("type");
                int level = rs.getInt("level");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                String storage = rs.getString("storage");
                long lastMiningTime = rs.getLong("last_mining_time");

                // Création du minion avec les données de la base
                ConfigurationSection minionConfig = plugin.getConfigManager()
                        .getMinionsConfig().getConfigurationSection("minions." + type);
                if (minionConfig != null) {
                    Location location = new Location(plugin.getServer().getWorld(world), x, y, z);
                    Minion minion = new Minion(plugin, owner, type, level, location);
                    minion.loadStorageFromString(storage);
                    minion.setLastMiningTime(lastMiningTime);
                    minions.add(minion);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return minions;
    }

    public void deleteMinion(Minion minion) {
        String sql = "DELETE FROM minions WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Location loc = minion.getLocation();
            pstmt.setString(1, loc.getWorld().getName());
            pstmt.setDouble(2, loc.getX());
            pstmt.setDouble(3, loc.getY());
            pstmt.setDouble(4, loc.getZ());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}