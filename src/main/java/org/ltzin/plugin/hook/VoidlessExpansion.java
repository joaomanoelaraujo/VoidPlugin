package org.ltzin.plugin.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ltzin.Main;
import org.ltzin.player.Profile;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;

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
    } else if (params.equals("tag")) {
      return profile.getSelectedTag();
    } else if (params.equals("online")) {
      return format(Bukkit.getOnlinePlayers().size());
    }

    // ===================== STATS DE JOGO (SKYWARS ETC.) =====================
    // Falta confirmar o nome da DataTable e das colunas usadas pelo vSkyWars
    // (ex: "kills", "wins") para preencher esta parte com profile.getStats(...).
    // Assim que confirmar, adiciono algo como:
    //
    // else if (params.startsWith("skywars_")) {
    //   String stat = params.replace("skywars_", "");
    //   return format(profile.getStats("NOME_DA_TABELA", stat));
    // }

    return null;
  }

  private String format(long value) {
    return String.valueOf(value);
  }
}