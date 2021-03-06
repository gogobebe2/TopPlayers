package com.gmail.gogobebe2.topplayers;

import com.gmail.gogobebe2.topplayers.record.OnlineRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

public class TopPlayers extends JavaPlugin implements Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("topplayer") && sender.hasPermission("topplayer.admin")) {
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Incorrect usage! Type /topplayer reload");
            return true;
        }
        return false;
    }

    SignUpdater signUpdater;

    @Override
    public void onEnable() {
        getLogger().info("Starting up TopPlayers. If you need me to update this plugin, email at gogobebe2@gmail.com");

        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(OnlineRecord.getListener(this), this);

        this.signUpdater = new SignUpdater(this);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, signUpdater,
                0L, getConfig().getLong("sign update rate (in ticks)"));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TopPlayers. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @EventHandler
    protected void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;

        String[] lines = event.getLines();
        if (lines[0].equalsIgnoreCase("[top player]")) {
            Player player = event.getPlayer();
            if (player.hasPermission("topsign.create")) {
                int placement;
                try {
                    placement = Integer.parseInt(lines[1]);
                    if (placement <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException eee) {
                    player.sendMessage(ChatColor.RED + "Error! line 2 does not have a placement number in it or it's below 1!");
                    event.setCancelled(true);
                    return;
                }
                Block sign = event.getBlock();
                new LocationData(sign.getLocation(), this).saveToConfig("signs." + placement + "." + UUID.randomUUID().toString());
                signUpdater.updateSigns();
                player.sendMessage(ChatColor.GREEN + "Top " + placement + " sign has been created!");
            } else {
                player.sendMessage(ChatColor.RED + "Error! You do not have permission to create head signs!");
            }
        }
    }
}
