package com.daytonjwatson.zerorespawn.managers;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles tab list header/footer formatting and periodic refreshing.
 */
public class TabManager {

    private final ZeroRespawnPlugin plugin;

    public TabManager(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    apply(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 200L);
    }

    public void apply(Player player) {
        String header = plugin.getConfig().getString("tab.header",
                "&6ZeroRespawn\n&7True Hardcore: One Life Only\n&fOnline: {online}/{max}");
        String footer = plugin.getConfig().getString("tab.footer",
                "&7World: {world}\n&cDeath = Permanent Ban");
        String formattedHeader = MessageUtil.color(header
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{player}", player.getName())
                .replace("{world}", player.getWorld().getName())
                .replace("{days_survived}", String.format("%.1f", plugin.getPlayerDataManager().getDaysSurvived(player.getUniqueId()))));
        String formattedFooter = MessageUtil.color(footer
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{player}", player.getName())
                .replace("{world}", player.getWorld().getName())
                .replace("{days_survived}", String.format("%.1f", plugin.getPlayerDataManager().getDaysSurvived(player.getUniqueId()))));
        player.setPlayerListHeaderFooter(formattedHeader, formattedFooter);
    }
}

