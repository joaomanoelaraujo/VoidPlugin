package org.ltzin.player.preferences;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ltzin.Main;
import org.ltzin.database.data.PreferencesContainer;
import org.ltzin.player.Profile;
import org.ltzin.player.enums.FlyOnJoin;
import org.ltzin.player.enums.PlayerVisibility;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;


public final class PreferencesApplier {

    private PreferencesApplier() {}

    public static void apply(Player player, PlayerPreference preference) {
        Profile profile = Profile.getProfile(player.getName());
        if (profile == null) return;

        PreferencesContainer prefs = profile.getPreferences();

        switch (preference) {

            case FLY_ON_JOIN:
                boolean fly = prefs.get(PlayerPreference.FLY_ON_JOIN) == FlyOnJoin.TODOS;
                player.setAllowFlight(fly);
                if (!fly) player.setFlying(false);
                break;

            case PLAYER_VISIBILITY:
                applyVisibility(player, profile, prefs);
                break;


            default:
                break;
        }
    }

    private static void applyVisibility(Player player, Profile profile, PreferencesContainer prefs) {
        boolean seesAll = prefs.get(PlayerPreference.PLAYER_VISIBILITY) == PlayerVisibility.TODOS;
        Role playerRole = RoleLookup.roleForOnlinePlayer(player);

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;

                Role onlineRole = RoleLookup.roleForOnlinePlayer(online);

                if (onlineRole != null && onlineRole.isAlwaysVisible()) {
                    player.showPlayer(online);
                } else if (seesAll) {
                    player.showPlayer(online);
                } else {
                    player.hidePlayer(online);
                }

                Profile onlineProfile = Profile.getProfile(online.getName());
                if (onlineProfile == null) continue;

                boolean onlineSeesAll = onlineProfile.getPreferences()
                        .get(PlayerPreference.PLAYER_VISIBILITY) == PlayerVisibility.TODOS;

                if (playerRole != null && playerRole.isAlwaysVisible()) {
                    online.showPlayer(player);
                } else if (onlineSeesAll) {
                    online.showPlayer(player);
                } else {
                    online.hidePlayer(player);
                }
            }
        });
    }
}