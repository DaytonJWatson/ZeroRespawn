package com.daytonjwatson.zerorespawn.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static final String PREFIX =
            ChatColor.DARK_GRAY + "[" +
            ChatColor.GOLD + "ZeroRespawn" +
            ChatColor.DARK_GRAY + "] " +
            ChatColor.RESET;

    private MessageUtil() {
        // utility
    }

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendPrefixed(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + color(message));
    }

    public static String formatPrivateTo(String targetName, String message) {
        return ChatColor.GRAY + "[To " +
                ChatColor.AQUA + targetName +
                ChatColor.GRAY + "] " +
                ChatColor.WHITE + message;
    }

    public static String formatPrivateFrom(String senderName, String message) {
        return ChatColor.GRAY + "[From " +
                ChatColor.AQUA + senderName +
                ChatColor.GRAY + "] " +
                ChatColor.WHITE + message;
    }

    public static String stripColors(String text) {
        return ChatColor.stripColor(color(text));
    }

    public static String getPrefix() {
        return PREFIX;
    }
}
