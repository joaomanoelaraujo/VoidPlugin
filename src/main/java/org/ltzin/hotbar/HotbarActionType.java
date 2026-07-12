package org.ltzin.hotbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.ltzin.database.data.PreferencesContainer;
import org.ltzin.menus.MenuProfile;
import org.ltzin.player.Profile;
import org.ltzin.player.enums.PlayerVisibility;
import org.ltzin.player.preferences.PlayerPreference;
import org.ltzin.player.preferences.PreferencesApplier;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class HotbarActionType {

  private static final Map<String, HotbarActionType> ACTION_TYPES = new HashMap<>();
  private static final Map<String, Long> DELAY_PLAYERS = new HashMap<>();
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

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

    addActionType("voidless", new HotbarActionType() {
      @Override
      public void execute(Profile profile, String action) {
        Player player = profile.getPlayer();


        if (action.equalsIgnoreCase("minigames")) {
          // new MenuServers(profile);

        } else if (action.equalsIgnoreCase("perfil")) {
           new MenuProfile(profile);

        } else if (action.equalsIgnoreCase("jogadores")) {
          handleToggleVisibility(profile, player);
        }
      }

      private void handleToggleVisibility(Profile profile, Player player) {
        long start = DELAY_PLAYERS.getOrDefault(player.getName(), 0L);
        if (start > System.currentTimeMillis()) {
          double time = (start - System.currentTimeMillis()) / 1000.0;
          if (time > 0.1) {
            String timeString = DECIMAL_FORMAT.format(time).replace(",", ".");
            if (timeString.endsWith(".0")) {
              timeString = timeString.substring(0, timeString.lastIndexOf("."));
            }
            player.sendMessage("§cAguarde {more} para utilizar novamente.".replace("{more}", timeString));
            return;
          }
        }

        DELAY_PLAYERS.put(player.getName(), System.currentTimeMillis() + 3000);

        PreferencesContainer prefs = profile.getPreferences();
        PlayerVisibility newValue = prefs.toggle(PlayerPreference.PLAYER_VISIBILITY);

        PreferencesApplier.apply(player, PlayerPreference.PLAYER_VISIBILITY);

        switch (newValue) {
          case TODOS:
            player.sendMessage("§aOs jogadores foram ativados!");
            break;
          case NENHUM:
            player.sendMessage("§cOs jogadores foram desativados!");
            break;
        }

        profile.refreshPlayers();
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