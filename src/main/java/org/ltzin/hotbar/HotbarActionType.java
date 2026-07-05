package org.ltzin.hotbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.ltzin.player.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Tipos de acao disponiveis pros botoes da hotbar.
 * Use addActionType(...) no onEnable pra registrar acoes especificas do Voidless
 * (ex: abrir menu de preferencias, alternar visibilidade, etc).
 */
public abstract class HotbarActionType {

  private static final Map<String, HotbarActionType> ACTION_TYPES = new HashMap<>();

  static {
    addActionType("comando", new HotbarActionType() {
      @Override
      public void execute(Profile profile, String action) {
        profile.getPlayer().performCommand(action);
      }
    });

    addActionType("console", new HotbarActionType() {
      @Override
      public void execute(Profile profile, String action) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.replace("%player%", profile.getPlayer().getName()));
      }
    });

    addActionType("mensagem", new HotbarActionType() {
      @Override
      public void execute(Profile profile, String action) {
        profile.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', action).replace("\\n", "\n"));
      }
    });
  }

  public static void addActionType(String name, HotbarActionType actionType) {
    ACTION_TYPES.put(name.toLowerCase(), actionType);
  }

  public static HotbarActionType fromName(String name) {
    return ACTION_TYPES.get(name.toLowerCase());
  }

  public abstract void execute(Profile profile, String action);
}