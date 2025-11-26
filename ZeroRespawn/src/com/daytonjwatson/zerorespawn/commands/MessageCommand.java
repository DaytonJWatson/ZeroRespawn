package com.daytonjwatson.zerorespawn.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.daytonjwatson.zerorespawn.util.MessageUtil;

public class MessageCommand implements CommandExecutor {

    private static final Map<UUID, UUID> lastMessageMap = new HashMap<>();

    public static UUID getLastMessaged(UUID uuid) {
        return lastMessageMap.get(uuid);
    }

    public static void setLastMessaged(UUID sender, UUID target) {
        lastMessageMap.put(sender, target);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cOnly players may use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            MessageUtil.sendPrefixed(player, "&cUsage: /msg <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            MessageUtil.sendPrefixed(player, "&cThat player is not online.");
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendPrefixed(player, "&cYou cannot message yourself.");
            return true;
        }

        String message = String.join(" ", args).substring(args[0].length()).trim();

        // Send private messages
        player.sendMessage(MessageUtil.formatPrivateTo(target.getName(), message));
        target.sendMessage(MessageUtil.formatPrivateFrom(player.getName(), message));

        setLastMessaged(player.getUniqueId(), target.getUniqueId());
        setLastMessaged(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
