package com.daytonjwatson.zerorespawn.managers;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class HardcoreManager {

    private final ZeroRespawnPlugin plugin;

    public HardcoreManager(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleDeath(Player player) {
        // Colored kick message (from config, with default)
        String deathMessage = MessageUtil.color(plugin.getConfig().getString(
                "messages.death_ban",
                "&cYou died in hardcore mode. You are banned from this server."
        ));

        // Reason must be plain text for the ban list
        String plainReason = MessageUtil.stripColors(deathMessage);

        // Modern API: profile-based ban
        // duration = null => permanent
        player.ban(
                plainReason,
                (Duration) null,
                "ZeroRespawn",
                false // don't auto-kick, we want custom kick message
        );

        // Kick with colored message next tick
        Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(deathMessage));
    }

    public String getInfoMessage() {
        return MessageUtil.color(plugin.getConfig().getString(
                "messages.info",
                "&6This server uses &eZeroRespawn &6hardcore rules. Die once, and you're done."
        ));
    }

    public void reload() {
        plugin.reloadConfig();
    }
}
