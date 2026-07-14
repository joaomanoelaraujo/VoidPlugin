package org.ltzin.plugin.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ltzin.Main;
import org.ltzin.player.Profile;
import org.ltzin.player.enums.PlayerVisibility;
import org.ltzin.player.preferences.PlayerPreference;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;
import org.ltzin.utils.StringUtils;

public class VoidlessExpansion extends PlaceholderExpansion {

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String getAuthor() {
    return "ltzin";
  }

  @Override
  public String getIdentifier() {
    return "voidless";
  }

  @Override
  public String getVersion() {
    return Main.getInstance().getDescription().getVersion();
  }

  @Override
  public String onPlaceholderRequest(Player player, String params) {
    Profile profile = null;
    if (player == null || (profile = Profile.getProfile(player.getName())) == null) {
      return "";
    }

    if (params.equals("cash")) {
      return format(profile.getCash());
    } else if (params.equals("role")) {
     return Role.getRole(profile.getPlayer());
    } else if (params.equals("role_prefix")) {
      Role role = RoleLookup.displayRole(player);
      return role != null ? role.getPrefix() : "";
    } else if (params.equals("status_jogadores")) {
      return profile.getPreferences().getPlayerVisibility().getName();
    } else if (params.equals("status_jogadores_nome")) {
      return profile.getPreferences().getPlayerVisibility() == PlayerVisibility.TODOS ? "§aON" : "§cOFF";
    } else if (params.equals("status_jogadores_inksack")) {
      return profile.getPreferences().getPlayerVisibility().getInkSack();
    } else if (params.equals("tag")) {
      return profile.getSelectedTag();
    } else if (params.equals("online")) {
      return format(Bukkit.getOnlinePlayers().size());
    } else if (params.startsWith("Duels_")) {
      String table = "vPluginDuels";
      String value = params.replace("Duels_", "");
      if (value.equals("kills") || value.equals("deaths") || value.equals("assists") || value.equals("games") || value.equals("wins")) {
        return StringUtils.formatNumber(profile.getStats(table, "sumo" + value, "soup" + value));
      } else if (value.equals("sumokills") || value.equals("sumodeaths") || value.equals("sumoassists") || value.equals("sumogames") || value.equals("sumowins")) {
        return StringUtils.formatNumber(profile.getStats(table, value));
      } else if (value.equals("soupkills") || value.equals("soupdeaths") || value.equals("soupassists") || value.equals("soupgames") || value.equals("soupwins")) {
        return StringUtils.formatNumber(profile.getStats(table, value));
      } else if (value.equals("coins")) {
        return StringUtils.formatNumber(profile.getCoins(table));
      }
    } else if (params.startsWith("SkyWars_")) {
      String table = "vPluginSkyWars";
      String value = params.replace("SkyWars_", "");
      if (value.equals("kills") || value.equals("deaths") || value.equals("assists") || value.equals("games") || value.equals("wins")) {
        return StringUtils.formatNumber(profile.getStats(table, "1v1" + value, "2v2" + value));
      } else if (value.equals("1v1kills") || value.equals("1v1deaths") || value.equals("1v1assists") || value.equals("1v1games") || value.equals("1v1wins")) {
        return StringUtils.formatNumber(profile.getStats(table, value));
      } else if (value.equals("2v2kills") || value.equals("2v2deaths") || value.equals("2v2assists") || value.equals("2v2games") || value.equals("2v2wins")) {
        return StringUtils.formatNumber(profile.getStats(table, value));
      } else if (value.equals("coins")) {
        return StringUtils.formatNumber(profile.getCoins(table));
      }
    }

    return null;
  }

  private String format(long value) {
    return String.valueOf(value);
  }
}