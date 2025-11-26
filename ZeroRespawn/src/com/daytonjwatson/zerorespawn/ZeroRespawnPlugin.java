package com.daytonjwatson.zerorespawn;

import org.bukkit.plugin.java.JavaPlugin;

import com.daytonjwatson.zerorespawn.commands.HelpCommand;
import com.daytonjwatson.zerorespawn.commands.ZeroRespawnCommand;
import com.daytonjwatson.zerorespawn.listeners.HardcoreDeathListener;
import com.daytonjwatson.zerorespawn.managers.HardcoreManager;

public class ZeroRespawnPlugin extends JavaPlugin {

    private HardcoreManager hardcoreManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.hardcoreManager = new HardcoreManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new HardcoreDeathListener(this), this);

        // Register commands
        ZeroRespawnCommand mainCommand = new ZeroRespawnCommand(this);
        getCommand("help").setExecutor(new HelpCommand());
        getCommand("zerorespawn").setExecutor(mainCommand);
        getCommand("zerorespawn").setTabCompleter(mainCommand);

        getLogger().info("ZeroRespawn enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ZeroRespawn disabled.");
    }

    public HardcoreManager getHardcoreManager() {
        return hardcoreManager;
    }
}
