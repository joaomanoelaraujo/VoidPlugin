package org.ltzin.plugin;

import org.bukkit.ChatColor;

public final class LangColorUtils {

  private LangColorUtils() {}

  public static String formatColors(String input) {
    if (input == null) return null;
    return ChatColor.translateAlternateColorCodes('&', input);
  }

  public static String deformatColors(String input) {
    if (input == null) return null;
    return input.replace(ChatColor.COLOR_CHAR, '&');
  }
}