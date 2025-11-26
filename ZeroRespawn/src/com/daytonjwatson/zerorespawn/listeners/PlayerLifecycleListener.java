package com.daytonjwatson.zerorespawn.listeners;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.managers.PlayerDataManager;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Handles player lifecycle events: join, quit, chat, death, sleep bonuses, grace period, etc.
 */
public class PlayerLifecycleListener implements Listener {

    private final ZeroRespawnPlugin plugin;
    private final PlayerDataManager dataManager;
    private final Random random = new Random();

    public PlayerLifecycleListener(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.handleJoin(player);
        handleInitialSpawn(player);
        applyTabFormatting(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        dataManager.handleQuit(event.getPlayer());
    }

    private void handleInitialSpawn(Player player) {
        UUID uuid = player.getUniqueId();
        boolean firstJoin = dataManager.isFirstJoin(uuid) || !dataManager.hasInitialSpawn(uuid);
        if (!firstJoin) {
            return;
        }

        World world = Bukkit.getWorld(plugin.getConfig().getString("hardcore.world", "world"));
        if (world == null) {
            world = player.getWorld();
        }

        Location spawn = findSafeRandomLocation(world);
        if (spawn != null) {
            player.teleport(spawn);
            if (spawn.getWorld().getEnvironment() == World.Environment.NORMAL) {
                player.setRespawnLocation(spawn, true);
            }
        }

        giveGuideBook(player);
        applyGrace(player);
        dataManager.markInitialSpawned(uuid);
    }

    private Location findSafeRandomLocation(World world) {
        int radius = plugin.getConfig().getInt("hardcore.spawn_radius", 5000);
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            Block block = loc.getBlock();
            if (!isSafeBlock(block)) continue;
            if (!isSafeBlock(block.getRelative(BlockFace.DOWN))) continue;
            if (isCliff(block)) continue;
            return loc.add(0, 1, 0);
        }
        return world.getSpawnLocation();
    }

    private boolean isSafeBlock(Block block) {
        Material type = block.getType();
        return type.isSolid() && type != Material.WATER && type != Material.LAVA;
    }

    private boolean isCliff(Block block) {
        // If adjacent blocks fall more than 6 blocks, avoid
        int y = block.getY();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (Math.abs(block.getRelative(face).getY() - y) > 6) {
                return true;
            }
        }
        return false;
    }

    private void giveGuideBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(MessageUtil.color(plugin.getConfig().getString("guide.title", "ZeroRespawn Survival Guide")));
        meta.setAuthor("ZeroRespawn");
        meta.setPages(plugin.getConfig().getStringList("guide.pages").stream().map(MessageUtil::color).toList());
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        MessageUtil.sendPrefixed(player, "&aYou received the ZeroRespawn guide book.");
    }

    private void applyGrace(Player player) {
        if (!plugin.getConfig().getBoolean("features.first_join_grace.enabled", true)) return;
        int duration = plugin.getConfig().getInt("features.first_join_grace.duration_seconds", 90);
        int resistanceLevel = plugin.getConfig().getInt("features.first_join_grace.resistance_level", 1);
        boolean applyWeakness = plugin.getConfig().getBoolean("features.first_join_grace.apply_weakness", true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration * 20, Math.max(0, resistanceLevel - 1)));
        if (applyWeakness) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration * 20, 0));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * Math.min(duration, 30), 0));
        MessageUtil.sendPrefixed(player, "&eYou feel a brief grace period to find safety. PvP damage is reduced while it lasts.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        String hardcoreWorld = plugin.getConfig().getString("hardcore.world", "world");
        if (player.getWorld().getName().equalsIgnoreCase(hardcoreWorld)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String hardcoreWorld = plugin.getConfig().getString("hardcore.world", "world");
        if (!player.getWorld().getName().equalsIgnoreCase(hardcoreWorld)) {
            return;
        }

        dataManager.addDeath(player.getUniqueId());
        plugin.getHardcoreManager().handleDeath(player);

        double days = dataManager.getDaysSurvived(player.getUniqueId());
        String coords = String.format("%d, %d, %d", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        String cause = formatDamageCause(event.getDamageSource());
        String template = plugin.getConfig().getString("messages.death_broadcast", "&c{player} survived {days} days and died at {coords} to {cause}.");
        String formatted = MessageUtil.color(template
                .replace("{player}", player.getName())
                .replace("{days}", String.format("%.1f", days))
                .replace("{coords}", coords)
                .replace("{cause}", cause)
        );
        Bukkit.broadcastMessage(formatted);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        String format = plugin.getConfig().getString("chat.format", "{prefix}&f{player} &7» &f{message}");
        event.renderer((player, message, viewers) -> {
            String replaced = format
                    .replace("{prefix}", MessageUtil.getPrefix())
                    .replace("{player}", player.getName());
            int idx = replaced.indexOf("{message}");
            if (idx >= 0) {
                String before = replaced.substring(0, idx);
                String after = replaced.substring(idx + "{message}".length());
                return MessageUtil.component(before).append(message).append(MessageUtil.component(after));
            }
            return MessageUtil.component(replaced).append(Component.space()).append(message);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        // During first join grace, dampen PvP damage to prevent abuse
        if (plugin.getConfig().getBoolean("features.first_join_grace.enabled", true)) {
            FileConfiguration data = dataManager.getData(player.getUniqueId());
            long joined = data.getLong("join_timestamp", Instant.now().toEpochMilli());
            int duration = plugin.getConfig().getInt("features.first_join_grace.duration_seconds", 90);
            if (Instant.now().toEpochMilli() - joined < duration * 1000L) {
                event.setDamage(event.getDamage() * 0.5);
            }
            FileConfiguration dData = dataManager.getData(damager.getUniqueId());
            long dJoin = dData.getLong("join_timestamp", Instant.now().toEpochMilli());
            if (Instant.now().toEpochMilli() - dJoin < duration * 1000L) {
                event.setDamage(event.getDamage() * 0.5);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKill(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player killer && victim.getHealth() - event.getFinalDamage() <= 0) {
            dataManager.addKill(killer.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && event.getEntity().getType() != EntityType.PLAYER) {
            dataManager.addMobKill(event.getEntity().getKiller().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        if (!plugin.getConfig().getBoolean("features.rested_bonus.enabled", true)) return;
        Player player = event.getPlayer();
        long cooldown = plugin.getConfig().getLong("features.rested_bonus.cooldown_minutes", 30);
        if (!dataManager.canReceiveRested(player.getUniqueId(), cooldown)) return;

        int absorptionHearts = plugin.getConfig().getInt("features.rested_bonus.absorption_hearts", 2);
        int duration = plugin.getConfig().getInt("features.rested_bonus.duration_seconds", 120);
        int regenLevel = plugin.getConfig().getInt("features.rested_bonus.regeneration_level", 0);
        int regenDuration = plugin.getConfig().getInt("features.rested_bonus.regeneration_duration_seconds", 60);

        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration * 20, Math.max(0, (absorptionHearts / 2) - 1)));
        if (regenLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration * 20, regenLevel - 1));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 0));
        dataManager.markRested(player.getUniqueId());
        MessageUtil.sendPrefixed(player, "&aYou feel rested after sleeping.");
    }

    private void applyTabFormatting(Player player) {
        plugin.getTabManager().apply(player);
    }

    private String formatDamageCause(DamageSource source) {
        if (source == null || source.getDamageType() == null) {
            return "unknown";
        }
        String key = source.getDamageType().translationKey();
        int idx = Math.max(key.lastIndexOf('.'), key.lastIndexOf(':'));
        if (idx >= 0 && idx + 1 < key.length()) {
            key = key.substring(idx + 1);
        }
        key = key.replace('.', ' ').replace('_', ' ');
        StringBuilder builder = new StringBuilder();
        for (String part : key.split(" ")) {
            if (part.isEmpty()) continue;
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }
}

