package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor {

    private static class HelpEntry {
        private final String syntax;
        private final String description;
        private final String permission; // nullable

        HelpEntry(String syntax, String description, String permission) {
            this.syntax = syntax;
            this.description = description;
            this.permission = permission;
        }

        boolean canUse(CommandSender sender) {
            return permission == null || sender.hasPermission(permission);
        }
    }

    // Order is preserved
    private static final List<HelpEntry> ENTRIES = new ArrayList<>();

    static {
        ENTRIES.add(new HelpEntry(
                "zerorespawn info",
                "Show hardcore mode information.",
                "zerorespawn.command"
        ));
        ENTRIES.add(new HelpEntry(
                "zerorespawn reload",
                "Reload the plugin configuration. (Admin)",
                "zerorespawn.admin"
        ));
        ENTRIES.add(new HelpEntry(
                "msg <player> <message>",
                "Send a private message.",
                null // everyone
        ));
        ENTRIES.add(new HelpEntry(
                "r <message>",
                "Reply to the last private message.",
                null // everyone
        ));
        ENTRIES.add(new HelpEntry(
                "guide",
                "Receive the ZeroRespawn guide book.",
                "zerorespawn.guide"
        ));
        ENTRIES.add(new HelpEntry(
                "stats [player]",
                "View your survival statistics.",
                "zerorespawn.stats"
        ));
        ENTRIES.add(new HelpEntry(
                "note <add|list|remove>",
                "Manage coordinate notes.",
                "zerorespawn.note"
        ));
        ENTRIES.add(new HelpEntry(
                "help",
                "Show this help menu.",
                null // everyone
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }

        String cmdName = args[0].toLowerCase();

        HelpEntry matched = null;
        for (HelpEntry entry : ENTRIES) {
            // Match on the first token of the syntax
            String firstToken = entry.syntax.split(" ")[0].toLowerCase();
            if (firstToken.equals(cmdName) || entry.syntax.toLowerCase().startsWith(cmdName + " ")) {
                matched = entry;
                break;
            }
        }

        if (matched == null || !matched.canUse(sender)) {
            MessageUtil.sendPrefixed(sender, "&cUnknown command or you do not have access to it.");
            return true;
        }

        sendSingleCommandHelp(sender, matched);
        return true;
    }

    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.getPrefix() + ChatColor.GOLD + "ZeroRespawn Commands:");

        boolean anyShown = false;

        for (HelpEntry entry : ENTRIES) {
            if (!entry.canUse(sender)) continue;

            anyShown = true;
            sender.sendMessage(
                    ChatColor.YELLOW + "/" + entry.syntax
                            + ChatColor.GRAY + " - "
                            + ChatColor.WHITE + entry.description
            );
        }

        if (!anyShown) {
            MessageUtil.sendPrefixed(sender, "&7You do not have access to any ZeroRespawn commands.");
        }
    }

    private void sendSingleCommandHelp(CommandSender sender, HelpEntry entry) {
        sender.sendMessage(
                MessageUtil.getPrefix()
                        + ChatColor.GOLD
                        + "/" + entry.syntax
        );
        sender.sendMessage(
                ChatColor.GRAY + "Description: "
                        + ChatColor.WHITE + entry.description
        );
        sender.sendMessage(
            ChatColor.GRAY + "Usage: "
                    + ChatColor.YELLOW + "/" + entry.syntax
        );
    }
}
