package org.ltzin.tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.ltzin.Main;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;
import org.ltzin.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class TabManager {

    private TabManager() {}

    public static void setup() {
        Main.getInstance().getServer().getPluginManager()
                .registerEvents(new TabListener(), Main.getInstance());
    }

    private static Scoreboard scoreboardOf(Player player) {
        Scoreboard current = player.getScoreboard();
        if (current == null || current.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            Scoreboard fresh = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(fresh);
            return fresh;
        }
        return current;
    }

    public static void removeFromTeams(String playerName) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = viewer.getScoreboard();
            if (sb == null) continue;
            for (Team t : sb.getTeams()) {
                if (t.getName().startsWith("vp_") && t.hasEntry(playerName)) {
                    t.removeEntry(playerName);
                }
            }
        }
    }

    public static void rebuildAllTeams() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = viewer.getScoreboard();
            if (sb == null) continue;
            for (Team t : new ArrayList<>(sb.getTeams())) {
                if (t.getName().startsWith("vp_")) t.unregister();
            }
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            applyAllSync(viewer);
        }
    }

    public static void applyAll(Player target) {
        if (!Role.isReady()) return;

        Role role = RoleLookup.displayRole(target);

        Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                syncPlayerAcrossAllScoreboards(target, role));

        Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                applyDisplayName(target, role));
    }

    public static void applyAllSync(Player target) {
        if (!Role.isReady()) return;
        Role role = RoleLookup.displayRole(target);
        syncPlayerAcrossAllScoreboards(target, role);
        applyDisplayName(target, role);
    }

    public static void refresh(Player target) {
        applyAllSync(target);
    }

    private static void syncPlayerAcrossAllScoreboards(Player target, Role targetRole) {
        Scoreboard targetSb = scoreboardOf(target);

        for (Player other : Bukkit.getOnlinePlayers()) {
            Role otherRole = other.equals(target) ? targetRole : RoleLookup.displayRole(other);
            if (otherRole == null) continue;
            putInTeam(targetSb, other.getName(), otherRole);
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            Scoreboard viewerSb = scoreboardOf(viewer);
            putInTeam(viewerSb, target.getName(), targetRole);
        }
    }

    private static void putInTeam(Scoreboard sb, String entryName, Role role) {
        for (Team t : new ArrayList<>(sb.getTeams())) {
            if (t.getName().startsWith("vp_") && t.hasEntry(entryName)) {
                t.removeEntry(entryName);
            }
        }

        Team team = getOrCreateTeam(sb, role);
        team.addEntry(entryName);
    }

    private static java.lang.reflect.Method  METHOD_SET_OPTION   = null;
    private static Object                    OPTION_NAMETAG      = null;
    private static Object                    OPTION_STATUS_ALWAYS = null;
    private static boolean                   SET_OPTION_CHECKED  = false;

    private static Team getOrCreateTeam(Scoreboard sb, Role role) {
        String teamName = teamNameFor(role);

        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
        }

        String prefix = isLegacy() ? truncate(role.getPrefix(), 16) : role.getPrefix();
        team.setPrefix(prefix);
        team.setSuffix("");
        setNameTagVisibility(team);

        return team;
    }


    private static void setNameTagVisibility(Team team) {
        if (!SET_OPTION_CHECKED) {
            SET_OPTION_CHECKED = true;
            try {
                Class<?> optionClass  = Class.forName("org.bukkit.scoreboard.Team$Option");
                Class<?> statusClass  = Class.forName("org.bukkit.scoreboard.Team$OptionStatus");

                OPTION_NAMETAG       = Enum.valueOf((Class<Enum>) optionClass,  "NAME_TAG_VISIBILITY");
                OPTION_STATUS_ALWAYS = Enum.valueOf((Class<Enum>) statusClass, "ALWAYS");
                METHOD_SET_OPTION    = Team.class.getMethod("setOption", optionClass, statusClass);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            }
        }

        if (METHOD_SET_OPTION == null) return;

        try {
            METHOD_SET_OPTION.invoke(team, OPTION_NAMETAG, OPTION_STATUS_ALWAYS);
        } catch (Exception e) {
            Main.getInstance().getMyLogger().warning(
                    "[TabManager] Falha ao setar NAME_TAG_VISIBILITY: " + e.getMessage());
        }
    }

    private static void applyDisplayName(Player target, Role role) {
        String displayName = role.getPrefix() + target.getName();

        target.setDisplayName(displayName);

        final String listName;
        if (isLegacy()) {
            listName = target.getName();
        } else {
            int listLimit = 40;
            if (displayName.length() <= listLimit) {
                listName = displayName;
            } else {
                String nameOnly = target.getName();
                listName = nameOnly.length() <= listLimit ? nameOnly : truncate(nameOnly, listLimit);
            }
        }
        target.setPlayerListName(listName);

        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, target.getEntityId());

            if (isLegacy()) {
                List<WrappedWatchableObject> metadata = new ArrayList<>();
                metadata.add(new WrappedWatchableObject(2, displayName));
                metadata.add(new WrappedWatchableObject(3, (byte) 1));
                packet.getWatchableCollectionModifier().write(0, metadata);

            } else if (isModern()) {
                WrappedDataWatcher watcher = new WrappedDataWatcher();

                WrappedDataWatcher.Serializer chatSer =
                        WrappedDataWatcher.Registry.getChatComponentSerializer(true);
                WrappedDataWatcher.Serializer boolSer =
                        WrappedDataWatcher.Registry.get(Boolean.class);

                com.comphenix.protocol.wrappers.WrappedChatComponent comp =
                        com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(displayName);

                watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSer),
                        java.util.Optional.of(comp.getHandle()));
                watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, boolSer),
                        true);

                packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            } else {
                WrappedDataWatcher watcher = new WrappedDataWatcher();

                WrappedDataWatcher.Serializer strSer =
                        WrappedDataWatcher.Registry.get(String.class);
                WrappedDataWatcher.Serializer boolSer =
                        WrappedDataWatcher.Registry.get(Boolean.class);

                watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, strSer),
                        displayName);
                watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, boolSer),
                        true);

                packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            }

            for (Player observer : Bukkit.getOnlinePlayers()) {
                pm.sendServerPacket(observer, packet);
            }

        } catch (Exception e) {
            Main.getInstance().getMyLogger().warning(
                    "[TabManager] Falha ao aplicar display name para "
                            + target.getName() + ": " + e.getMessage());
        }
    }
    private static Integer MINOR_VERSION = null;

    private static int minorVersion() {
        if (MINOR_VERSION != null) return MINOR_VERSION;
        try {
            String ver = Bukkit.getBukkitVersion();
            String[] parts = ver.split("-")[0].split("\\.");
            MINOR_VERSION = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            MINOR_VERSION = 8;
        }
        return MINOR_VERSION;
    }

    private static boolean isLegacy() {
        return minorVersion() <= 8;
    }

    private static boolean isModern() {
        return minorVersion() >= 13;
    }


    private static String teamNameFor(Role role) {
        String padded = String.format("%02d", role.getId());
        String raw    = "vp_" + padded + "_" + StringUtils.stripColors(role.getName());
        return raw.length() > 16 ? raw.substring(0, 16) : raw;
    }

    private static String truncate(String str, int max) {
        if (str == null) return "";

        return str.length() <= max ? str : str.substring(0, max);
    }
}