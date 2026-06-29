package org.ltzin.player.role;

import org.ltzin.utils.StringUtils;
import org.bukkit.entity.Player;

public final class RoleFormatter {

    private RoleFormatter() {}

    public static String withPrefix(String playerName) {
        return withPrefix(playerName, null);
    }

    public static String withPrefix(String playerName, String colorCode) {
        RoleLookup.Result result = RoleLookup.resolve(playerName);
        String prefix = applyColorOverride(result.getRole().getPrefix(), colorCode);
        return prefix + result.getResolvedName();
    }

    public static String withColor(String playerName) {
        RoleLookup.Result result = RoleLookup.resolve(playerName);
        String color = StringUtils.getLastColor(result.getRole().getPrefix());
        return color + result.getResolvedName();
    }

    public static String withPrefix(Player player) {
        return withPrefix(player, null);
    }

    public static String withPrefix(Player player, String colorCode) {
        RoleLookup.Result result = RoleLookup.resolveOnline(player);
        String prefix = applyColorOverride(result.getRole().getPrefix(), colorCode);
        return prefix + result.getResolvedName();
    }

    public static String withColor(Player player) {
        RoleLookup.Result result = RoleLookup.resolveOnline(player);
        String color = StringUtils.getLastColor(result.getRole().getPrefix());
        return color + result.getResolvedName();
    }

    private static String applyColorOverride(String prefix, String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) return prefix;
        return colorCode + prefix.replaceFirst("§[0-9a-fk-orA-FK-OR]", "");
    }
}