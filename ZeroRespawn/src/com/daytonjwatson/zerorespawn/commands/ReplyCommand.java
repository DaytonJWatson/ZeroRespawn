package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cOnly players may use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            MessageUtil.sendPrefixed(player, "&cUsage: /r <message>");
            return true;
        }

        UUID targetUUID = MessageCommand.getLastMessaged(player.getUniqueId());

        if (targetUUID == null) {
            MessageUtil.sendPrefixed(player, "&cNo one to reply to.");
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null) {
            MessageUtil.sendPrefixed(player, "&cThat player is no longer online.");
            return true;
        }

        String message = String.join(" ", args);

        player.sendMessage(MessageUtil.formatPrivateTo(target.getName(), message));
        target.sendMessage(MessageUtil.formatPrivateFrom(player.getName(), message));

        MessageCommand.setLastMessaged(player.getUniqueId(), target.getUniqueId());
        MessageCommand.setLastMessaged(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
