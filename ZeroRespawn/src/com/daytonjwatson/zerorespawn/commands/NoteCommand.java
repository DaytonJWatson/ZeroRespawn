package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class NoteCommand implements CommandExecutor {

    private final ZeroRespawnPlugin plugin;

    public NoteCommand(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "&cOnly players may use this command.");
            return true;
        }
        if (!player.hasPermission("zerorespawn.note")) {
            MessageUtil.sendPrefixed(player, "&cYou do not have permission.");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendPrefixed(player, "&cUsage: /note <add|list|remove> [name]");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add":
                if (args.length < 2) {
                    MessageUtil.sendPrefixed(player, "&cUsage: /note add <name>");
                    return true;
                }
                String name = args[1].toLowerCase();
                plugin.getPlayerDataManager().saveNote(player.getUniqueId(), name, player.getLocation());
                MessageUtil.sendPrefixed(player, "&aSaved note '&f" + name + "&a' at your current location.");
                return true;
            case "list":
                Map<String, Location> notes = plugin.getPlayerDataManager().getNotes(player.getUniqueId());
                if (notes.isEmpty()) {
                    MessageUtil.sendPrefixed(player, "&7You have no saved notes.");
                    return true;
                }
                MessageUtil.send(player, MessageUtil.getPrefix() + "&eSaved notes:");
                notes.forEach((key, loc) -> MessageUtil.send(player,
                        "&7- &f" + key + " &8@ &7" + loc.getWorld().getName() + " &f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
                return true;
            case "remove":
                if (args.length < 2) {
                    MessageUtil.sendPrefixed(player, "&cUsage: /note remove <name>");
                    return true;
                }
                plugin.getPlayerDataManager().removeNote(player.getUniqueId(), args[1].toLowerCase());
                MessageUtil.sendPrefixed(player, "&aRemoved note '&f" + args[1].toLowerCase() + "&a'.");
                return true;
            default:
                MessageUtil.sendPrefixed(player, "&cUsage: /note <add|list|remove> [name]");
                return true;
        }
    }
}

