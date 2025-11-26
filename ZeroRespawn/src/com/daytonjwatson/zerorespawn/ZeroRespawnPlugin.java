package com.daytonjwatson.zerorespawn;

import com.daytonjwatson.zerorespawn.commands.*;
import com.daytonjwatson.zerorespawn.listeners.PlayerLifecycleListener;
import com.daytonjwatson.zerorespawn.managers.HardcoreManager;
import com.daytonjwatson.zerorespawn.managers.PlayerDataManager;
import com.daytonjwatson.zerorespawn.managers.SurvivalHudManager;
import com.daytonjwatson.zerorespawn.managers.TabManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ZeroRespawnPlugin extends JavaPlugin {

    private HardcoreManager hardcoreManager;
    private PlayerDataManager playerDataManager;
    private SurvivalHudManager survivalHudManager;
    private TabManager tabManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerDataManager = new PlayerDataManager(this);
        this.hardcoreManager = new HardcoreManager(this);
        this.survivalHudManager = new SurvivalHudManager(this);
        this.tabManager = new TabManager(this);
        this.survivalHudManager.loadFromConfig();
        this.tabManager.start();

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);

        // Commands
        ZeroRespawnCommand mainCommand = new ZeroRespawnCommand(this);
        getCommand("zerorespawn").setExecutor(mainCommand);
        getCommand("zerorespawn").setTabCompleter(mainCommand);
        getCommand("help").setExecutor(new HelpCommand());
        getCommand("msg").setExecutor(new MessageCommand());
        getCommand("r").setExecutor(new ReplyCommand());
        getCommand("guide").setExecutor(new GuideCommand(this));
        getCommand("book").setExecutor(new GuideCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("note").setExecutor(new NoteCommand(this));

        getLogger().info("ZeroRespawn enabled.");
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAll();
        getLogger().info("ZeroRespawn disabled.");
    }

    public HardcoreManager getHardcoreManager() {
        return hardcoreManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SurvivalHudManager getSurvivalHudManager() {
        return survivalHudManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }
}

