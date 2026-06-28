package org.ltzin.manager;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class Manager {

    private Manager() {}


    public static Player getPlayer(String name) {
        return Bukkit.getPlayerExact(name);
    }

    public static String getName(Player player) {
        if (player == null) return null;
        return player.getName();
    }


    public static boolean hasPermission(Player player, String permission) {
        if (player == null || permission == null || permission.isEmpty()) return false;
        return player.hasPermission(permission);
    }

    public static void sendMessage(Player player, String message) {
        if (player == null || !player.isOnline()) return;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }


    public static void sendMessage(Player player, BaseComponent... components) {
        if (player == null || !player.isOnline()) return;
        player.spigot().sendMessage(components);
    }

    public static void sendJsonMessage(Player player, String message) {
        sendMessage(player, TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes('&', message)
        ));
    }

    public static final String DEFAULT_SKIN = "MHF_Alex";



}