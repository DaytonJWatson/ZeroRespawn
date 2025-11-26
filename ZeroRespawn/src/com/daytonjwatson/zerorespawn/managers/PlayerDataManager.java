package com.daytonjwatson.zerorespawn.managers;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages persistent player data and simple stats for ZeroRespawn.
 * <p>
 * Data is stored per-player in {@code plugins/ZeroRespawn/players/<uuid>.yml}
 * to keep it resilient across reloads and accessible for stats/notes.
 */
public class PlayerDataManager {

    private final ZeroRespawnPlugin plugin;
    private final File playerDir;
    private final Map<UUID, FileConfiguration> cache = new HashMap<>();

    public PlayerDataManager(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
        this.playerDir = new File(plugin.getDataFolder(), "players");
        if (!playerDir.exists()) {
            playerDir.mkdirs();
        }
    }

    public FileConfiguration getData(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            File file = new File(playerDir, id + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (!file.exists()) {
                // Prime defaults for first-time players
                config.set("first_join", true);
                config.set("has_initial_spawn", false);
                config.set("time_played", 0L);
                config.set("join_timestamp", Instant.now().toEpochMilli());
                config.set("last_seen", Instant.now().toEpochMilli());
                config.set("stats.kills", 0);
                config.set("stats.mob_kills", 0);
                config.set("stats.deaths", 0);
                config.set("notes", new HashMap<String, Object>());
            }
            return config;
        });
    }

    public void save(UUID uuid) {
        FileConfiguration config = cache.get(uuid);
        if (config == null) {
            return;
        }
        File file = new File(playerDir, uuid + ".yml");
        try {
            config.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save data for " + uuid + ": " + ex.getMessage());
        }
    }

    public void saveAll() {
        cache.keySet().forEach(this::save);
    }

    public void handleJoin(Player player) {
        FileConfiguration data = getData(player.getUniqueId());
        data.set("join_timestamp", Instant.now().toEpochMilli());
        data.set("last_seen", Instant.now().toEpochMilli());
        save(player.getUniqueId());
    }

    public void handleQuit(Player player) {
        FileConfiguration data = getData(player.getUniqueId());
        long joinTs = data.getLong("join_timestamp", Instant.now().toEpochMilli());
        long played = data.getLong("time_played", 0L);
        long delta = Math.max(0, Instant.now().toEpochMilli() - joinTs);
        data.set("time_played", played + delta);
        data.set("last_seen", Instant.now().toEpochMilli());
        save(player.getUniqueId());
    }

    public boolean isFirstJoin(UUID uuid) {
        return getData(uuid).getBoolean("first_join", true);
    }

    public void markInitialSpawned(UUID uuid) {
        FileConfiguration data = getData(uuid);
        data.set("first_join", false);
        data.set("has_initial_spawn", true);
        save(uuid);
    }

    public boolean hasInitialSpawn(UUID uuid) {
        return getData(uuid).getBoolean("has_initial_spawn", false);
    }

    public double getDaysSurvived(UUID uuid) {
        FileConfiguration data = getData(uuid);
        long played = data.getLong("time_played", 0L);
        long joinTs = data.getLong("join_timestamp", Instant.now().toEpochMilli());
        if (Bukkit.getPlayer(uuid) != null) {
            played += Math.max(0, Instant.now().toEpochMilli() - joinTs);
        }
        // Convert milliseconds to in-game days (20 min per MC day)
        double mcDayMillis = 20 * 60 * 1000D;
        return played / mcDayMillis;
    }

    public void addKill(UUID uuid) {
        FileConfiguration data = getData(uuid);
        data.set("stats.kills", data.getInt("stats.kills") + 1);
        save(uuid);
    }

    public void addMobKill(UUID uuid) {
        FileConfiguration data = getData(uuid);
        data.set("stats.mob_kills", data.getInt("stats.mob_kills") + 1);
        save(uuid);
    }

    public void addDeath(UUID uuid) {
        FileConfiguration data = getData(uuid);
        data.set("stats.deaths", data.getInt("stats.deaths") + 1);
        save(uuid);
    }

    public boolean canReceiveRested(UUID uuid, long cooldownMinutes) {
        FileConfiguration data = getData(uuid);
        long last = data.getLong("rested.last", 0L);
        long cooldownMillis = cooldownMinutes * 60_000L;
        return last == 0 || (Instant.now().toEpochMilli() - last) >= cooldownMillis;
    }

    public void markRested(UUID uuid) {
        FileConfiguration data = getData(uuid);
        data.set("rested.last", Instant.now().toEpochMilli());
        save(uuid);
    }

    public Map<String, Location> getNotes(UUID uuid) {
        Map<String, Location> notes = new HashMap<>();
        FileConfiguration data = getData(uuid);
        if (!data.isConfigurationSection("notes")) {
            return notes;
        }
        for (String key : data.getConfigurationSection("notes").getKeys(false)) {
            String base = "notes." + key + ".";
            String world = data.getString(base + "world");
            double x = data.getDouble(base + "x");
            double y = data.getDouble(base + "y");
            double z = data.getDouble(base + "z");
            float yaw = (float) data.getDouble(base + "yaw");
            float pitch = (float) data.getDouble(base + "pitch");
            if (Bukkit.getWorld(world) != null) {
                notes.put(key, new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
            }
        }
        return notes;
    }

    public void saveNote(UUID uuid, String name, Location location) {
        FileConfiguration data = getData(uuid);
        String base = "notes." + name + ".";
        data.set(base + "world", location.getWorld().getName());
        data.set(base + "x", location.getX());
        data.set(base + "y", location.getY());
        data.set(base + "z", location.getZ());
        data.set(base + "yaw", location.getYaw());
        data.set(base + "pitch", location.getPitch());
        save(uuid);
    }

    public void removeNote(UUID uuid, String name) {
        FileConfiguration data = getData(uuid);
        data.set("notes." + name, null);
        save(uuid);
    }
}

