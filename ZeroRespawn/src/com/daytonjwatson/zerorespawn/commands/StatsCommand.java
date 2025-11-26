package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    private final ZeroRespawnPlugin plugin;

    public StatsCommand(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                showStats(sender, player.getUniqueId(), player.getName());
            } else {
                MessageUtil.send(sender, "&cUsage: /stats <player>");
            }
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getName() == null) {
            MessageUtil.sendPrefixed(sender, "&cThat player has never joined.");
            return true;
        }
        if (sender instanceof Player player && !player.hasPermission("zerorespawn.stats.others") && !player.getUniqueId().equals(target.getUniqueId())) {
            MessageUtil.sendPrefixed(sender, "&cYou do not have permission to view others' stats.");
            return true;
        }
        showStats(sender, target.getUniqueId(), target.getName());
        return true;
    }

    private void showStats(CommandSender sender, UUID uuid, String name) {
        FileConfiguration data = plugin.getPlayerDataManager().getData(uuid);
        int kills = data.getInt("stats.kills", 0);
        int mobKills = data.getInt("stats.mob_kills", 0);
        int deaths = data.getInt("stats.deaths", 0);
        double days = plugin.getPlayerDataManager().getDaysSurvived(uuid);

        MessageUtil.send(sender, MessageUtil.getPrefix() + "&6Stats for &e" + name + "&6:");
        MessageUtil.send(sender, "&7Days survived: &f" + String.format("%.1f", days));
        MessageUtil.send(sender, "&7Player kills: &f" + kills);
        MessageUtil.send(sender, "&7Mob kills: &f" + mobKills);
        MessageUtil.send(sender, "&7Deaths: &f" + deaths);
        boolean banned = Bukkit.getBanList(BanList.Type.PROFILE).isBanned(Bukkit.getOfflinePlayer(uuid).getName());
        MessageUtil.send(sender, banned ? "&cStatus: BANNED" : "&aStatus: Alive");
    }
}

