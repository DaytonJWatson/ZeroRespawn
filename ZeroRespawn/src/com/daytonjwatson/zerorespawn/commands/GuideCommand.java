package com.daytonjwatson.zerorespawn.commands;

import com.daytonjwatson.zerorespawn.ZeroRespawnPlugin;
import com.daytonjwatson.zerorespawn.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class GuideCommand implements CommandExecutor {

    private final ZeroRespawnPlugin plugin;

    public GuideCommand(ZeroRespawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "&cOnly players may use this command.");
            return true;
        }
        if (!player.hasPermission("zerorespawn.guide")) {
            MessageUtil.sendPrefixed(player, "&cYou do not have permission.");
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(MessageUtil.color(plugin.getConfig().getString("guide.title", "ZeroRespawn Survival Guide")));
        meta.setAuthor("ZeroRespawn");
        meta.setPages(plugin.getConfig().getStringList("guide.pages").stream().map(MessageUtil::color).toList());
        book.setItemMeta(meta);

        player.getInventory().addItem(book);
        MessageUtil.sendPrefixed(player, "&aYou have been given a new guide book.");
        return true;
    }
}

