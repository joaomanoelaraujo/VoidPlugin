package org.ltzin.player.role;

import org.ltzin.manager.Manager;
import org.ltzin.utils.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;

public final class Role {

    private static final List<Role> ROLES = new ArrayList<>();

    public static void register(Role role) {
        ROLES.add(role);
    }

    public static void clear() {
        ROLES.clear();
    }

    public static List<Role> listRoles() {
        return Collections.unmodifiableList(ROLES);
    }

    public static boolean isReady() {
        return !ROLES.isEmpty();
    }

    public static Role byName(String name) {
        return ROLES.stream()
                .filter(r -> StringUtils.stripColors(r.getName()).equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(Role::getDefault);
    }

    public static Optional<Role> byPermission(String permission) {
        return ROLES.stream()
                .filter(r -> r.getPermission().equals(permission))
                .findFirst();
    }
    public static Role getRoleByPermission(String permission) {
        Iterator<Role> var1 = ROLES.iterator();

        Role role;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            role = var1.next();
        } while (!role.getPermission().equals(permission));

        return role;
    }
    public static Role byPermission(Player player) {
        return ROLES.stream()
                .filter(r -> r.has(player))
                .findFirst()
                .orElseGet(Role::getDefault);
    }

    public static Role getDefault() {
        if (ROLES.isEmpty()) throw new IllegalStateException(
                "Nenhum rank registrado. Certifique-se de chamar RoleRegistry.setup() no onEnable().");
        return ROLES.get(ROLES.size() - 1);
    }

    public static String getColored(Player player) {
        if (!isReady()) return "§7" + player.getName();

        Role role = RoleLookup.roleForOnlinePlayer(player);
        if (role == null) return "§7" + player.getName();

        return StringUtils.getLastColor(role.getPrefix()) + player.getName();
    }

    public static String getTag(Player player) {
        if (!isReady()) return player.getName();

        Role role = RoleLookup.displayRole(player);

        String nameColor = role != null ? StringUtils.getLastColor(role.getPrefix()) : "§7";
        String tag       = role != null ? role.getTagDisplay() : "";

        if (!tag.isEmpty()) {
            return tag + nameColor + player.getName();
        }
        return nameColor + player.getName();
    }


    public static String getRole(Player player) {
        if (!isReady()) return "§7Membro";

        Role role = RoleLookup.roleForOnlinePlayer(player);
        if (role == null) return "§7Membro";

        String color   = StringUtils.getLastColor(role.getPrefix());
        String rawName = StringUtils.stripColors(role.getName());
        return color + rawName;
    }

    private final int     id;
    private final String  name;
    private final String  prefix;
    private final String  tagDisplay;
    private final String  permission;
    private final boolean alwaysVisible;
    private final boolean broadcast;
    private final boolean fly;

    public Role(String name, String prefix, String tagDisplay, String permission,
                boolean alwaysVisible, boolean broadcast, boolean fly) {
        this.id            = ROLES.size();
        this.name          = StringUtils.formatColors(name);
        this.prefix        = StringUtils.formatColors(prefix);
        this.tagDisplay    = tagDisplay != null ? StringUtils.formatColors(tagDisplay) : "";
        this.permission    = permission == null ? "" : permission;
        this.alwaysVisible = alwaysVisible;
        this.broadcast     = broadcast;
        this.fly           = fly;
    }

    public boolean has(Player player) {
        return isDefault() || Manager.hasPermission(player, this.permission);
    }

    public boolean hasTag() {
        return !tagDisplay.isEmpty();
    }

    public int     getId()           { return id; }
    public String  getName()         { return name; }
    public String  getPrefix()       { return prefix; }
    public String  getTagDisplay()   { return tagDisplay; }
    public String  getPermission()   { return permission; }
    public boolean isDefault()       { return permission.isEmpty(); }
    public boolean isAlwaysVisible() { return alwaysVisible; }
    public boolean isBroadcast()     { return broadcast; }
    public boolean canFly()          { return fly; }
}