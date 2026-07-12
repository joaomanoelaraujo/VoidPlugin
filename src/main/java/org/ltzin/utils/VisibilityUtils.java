package org.ltzin.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.ltzin.Main;
import org.ltzin.game.Game;
import org.ltzin.player.Profile;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;


public final class VisibilityUtils {

    private VisibilityUtils() {}

    private static final Method HIDE_PER_PLUGIN = findMethod("hidePlayer", Plugin.class, Player.class);
    private static final Method SHOW_PER_PLUGIN = findMethod("showPlayer", Plugin.class, Player.class);

    private static Method findMethod(String name, Class<?>... params) {
        try {
            return Player.class.getMethod(name, params);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static void hide(Player viewer, Player target) {
        if (viewer.equals(target)) return;

        try {
            if (HIDE_PER_PLUGIN != null) {
                HIDE_PER_PLUGIN.invoke(viewer, Main.getInstance(), target);
            } else {
                viewer.hidePlayer(target);
            }
        } catch (Exception ignored) { }

        sendTabInfo(viewer, target, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
    }

    public static void show(Player viewer, Player target) {
        if (viewer.equals(target)) return;

        try {
            if (SHOW_PER_PLUGIN != null) {
                SHOW_PER_PLUGIN.invoke(viewer, Main.getInstance(), target);
            } else {
                viewer.showPlayer(target);
            }
        } catch (Exception ignored) { }

        sendTabInfo(viewer, target, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
    }

    private static void sendTabInfo(Player viewer, Player target, EnumWrappers.PlayerInfoAction action) {
        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);

            packet.getPlayerInfoAction().write(0, action);

            WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(target);
            String listName = coloredListName(target);

            PlayerInfoData data = new PlayerInfoData(
                    gameProfile,
                    0,
                    EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                    WrappedChatComponent.fromText(listName)
            );

            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(data));

            pm.sendServerPacket(viewer, packet);
        } catch (Exception e) {
            Main.getInstance().getMyLogger().warning(
                    "[VisibilityUtils] Falha ao enviar pacote de tab list (" + action
                            + ") de " + target.getName() + " para " + viewer.getName()
                            + ": " + e.getMessage());
        }
    }


    private static String coloredListName(Player target) {
        if (!Role.isReady()) {
            return target.getName();
        }

        Role role = RoleLookup.displayRole(target);
        if (role == null) {
            return target.getName();
        }

        String name = role.getPrefix() + target.getName();
        return name.length() > 40 ? name.substring(0, 40) : name;
    }

    public static void updateVisibility(Player player) {
        Profile profile = Profile.getProfile(player.getName());
        Game<?> myGame = profile != null && profile.playingGame() ? profile.getGame() : null;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;

            Profile otherProfile = Profile.getProfile(other.getName());
            Game<?> otherGame = otherProfile != null && otherProfile.playingGame() ? otherProfile.getGame() : null;

            boolean sameRoom = Objects.equals(myGame, otherGame);

            if (sameRoom) {
                show(player, other);
                show(other, player);
            } else {
                hide(player, other);
                hide(other, player);
            }
        }
    }
}