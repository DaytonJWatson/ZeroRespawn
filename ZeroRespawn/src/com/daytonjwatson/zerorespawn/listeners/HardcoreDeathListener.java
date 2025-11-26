package com.daytonjwatson.zerorespawn.listeners;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class HardcoreDeathListener implements Listener {

    private final ZeroRespawnPlugin plugin;

    public HardcoreDeathListener(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String hardcoreWorld = plugin.getConfig().getString("hardcore.world", "world");
        if (!event.getEntity().getWorld().getName().equalsIgnoreCase(hardcoreWorld)) {
            return;
        }

        plugin.getHardcoreManager().handleDeath(event.getEntity());
    }
}