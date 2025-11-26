package com.daytonjwatson.zerorespawn.managers;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Handles a lightweight survival HUD either via action bar or a sidebar scoreboard.
 * Designed to be minimal and informative without being intrusive.
 */
public class SurvivalHudManager {

    public enum Mode {ACTION_BAR, SIDEBAR}

    private final ZeroRespawnPlugin plugin;
    private BukkitTask task;
    private Mode mode = Mode.ACTION_BAR;
    private long intervalTicks = 40L;
    private boolean showBiome = true;
    private boolean showTimeToNight = true;
    private boolean showArmor = true;

    public SurvivalHudManager(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadFromConfig() {
        this.mode = Mode.valueOf(plugin.getConfig().getString("features.survival_hud.mode", "ACTION_BAR").toUpperCase(Locale.ROOT));
        this.intervalTicks = plugin.getConfig().getLong("features.survival_hud.update_interval_ticks", 40L);
        this.showBiome = plugin.getConfig().getBoolean("features.survival_hud.show_biome", true);
        this.showTimeToNight = plugin.getConfig().getBoolean("features.survival_hud.show_time_to_night", true);
        this.showArmor = plugin.getConfig().getBoolean("features.survival_hud.show_armor_summary", true);
        restart();
    }

    public void restart() {
        if (task != null) {
            task.cancel();
        }

        if (!plugin.getConfig().getBoolean("features.survival_hud.enabled", true)) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastHud, intervalTicks, intervalTicks);
    }

    private void broadcastHud() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            if (mode == Mode.ACTION_BAR) {
                player.sendActionBar(MessageUtil.component(buildActionBar(player)));
            } else {
                renderSidebar(player);
            }
        }
    }

    private String buildActionBar(Player player) {
        StringBuilder builder = new StringBuilder();
        double health = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int hunger = player.getFoodLevel();
        builder.append("&c❤ ").append((int) Math.ceil(health)).append("/").append((int) maxHealth);
        builder.append(" &6🍖 ").append(hunger);

        if (showArmor) {
            builder.append(" &bArmor: ").append(summarizeArmor(player));
        }

        if (showTimeToNight) {
            builder.append(" &7").append(describeTime(player));
        }

        if (showBiome) {
            builder.append(" &2").append(formatBiome(player));
        }

        return builder.toString();
    }

    private String summarizeArmor(Player player) {
        Set<Material> diamond = EnumSet.of(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
        Set<Material> iron = EnumSet.of(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
        boolean hasDiamond = false;
        boolean hasIron = false;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;
            if (diamond.contains(armor.getType())) {
                hasDiamond = true;
            } else if (iron.contains(armor.getType())) {
                hasIron = true;
            }
        }
        if (hasDiamond) return "Diamond";
        if (hasIron) return "Iron";
        return "Basic";
    }

    private String describeTime(Player player) {
        long time = player.getWorld().getTime();
        if (time >= 0 && time < 12000) {
            return "Daytime";
        }
        if (time < 13800) {
            return "Sunset soon";
        }
        if (time < 22000) {
            return "Night";
        }
        return "Sunrise soon";
    }

    private String formatBiome(Player player) {
        String biome = player.getLocation().getBlock().getBiome().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return "Biome: " + biome;
    }

    private void renderSidebar(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("zrhud", Criteria.DUMMY, Component.text(ChatColor.GOLD + "ZeroRespawn"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(ChatColor.RED + "❤ " + (int) Math.ceil(player.getHealth()) + "/" + (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()).setScore(5);
        obj.getScore(ChatColor.GOLD + "Food: " + ChatColor.WHITE + player.getFoodLevel()).setScore(4);
        if (showArmor) {
            obj.getScore(ChatColor.AQUA + "Armor: " + ChatColor.WHITE + summarizeArmor(player)).setScore(3);
        }
        if (showTimeToNight) {
            obj.getScore(ChatColor.GRAY + describeTime(player)).setScore(2);
        }
        if (showBiome) {
            obj.getScore(ChatColor.DARK_GREEN + formatBiome(player)).setScore(1);
        }
        player.setScoreboard(board);
    }
}

