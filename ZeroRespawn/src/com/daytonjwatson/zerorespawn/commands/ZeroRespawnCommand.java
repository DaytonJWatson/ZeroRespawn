package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeroRespawnCommand implements CommandExecutor, TabCompleter {

    private final ZeroRespawnPlugin plugin;

    public ZeroRespawnCommand(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "info":
                // Info uses HardcoreManager + MessageUtil.color internally
                sender.sendMessage(plugin.getHardcoreManager().getInfoMessage());
                return true;

            case "reload":
                if (!sender.hasPermission("zerorespawn.admin")) {
                    MessageUtil.sendPrefixed(sender, "&cYou do not have permission to do that.");
                    return true;
                }
                plugin.getHardcoreManager().reload();
                MessageUtil.sendPrefixed(sender, "&aZeroRespawn configuration reloaded.");
                return true;

            default:
                sendHelp(sender, label);
                return true;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        // Using prefix + colors for a consistent look
        sender.sendMessage(
                MessageUtil.getPrefix()
                        + ChatColor.GOLD + "---- ZeroRespawn ----"
        );

        sender.sendMessage(
                ChatColor.YELLOW + "/" + label + " info "
                        + ChatColor.GRAY + "- Show hardcore rules info."
        );

        if (sender.hasPermission("zerorespawn.admin")) {
            sender.sendMessage(
                    ChatColor.YELLOW + "/" + label + " reload "
                            + ChatColor.GRAY + "- Reload the config."
            );
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(Arrays.asList("info"));
            if (sender.hasPermission("zerorespawn.admin")) {
                base.add("reload");
            }
            String current = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String s : base) {
                if (s.startsWith(current)) {
                    matches.add(s);
                }
            }
            return matches;
        }
        return new ArrayList<>();
    }
}
