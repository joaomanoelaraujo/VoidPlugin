package org.ltzin.player.role;

import org.bukkit.entity.Player;
import org.ltzin.manager.Manager;
import org.ltzin.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;


public final class Role {

    private static final List<Role> ROLES = new ArrayList<>();

    public static void clear() {
        ROLES.clear();
    }

    public static boolean isEmpty() {
        return ROLES.isEmpty();
    }

    public static void register(Role role) {
        ROLES.add(role);
    }

    public static List<Role> listRoles() {
        return Collections.unmodifiableList(ROLES);
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

    public static Role byPermission(Object onlinePlayer) {
        return ROLES.stream()
                .filter(r -> r.has(onlinePlayer))
                .findFirst()
                .orElseGet(Role::getDefault);
    }

    public static Role getDefault() {
        if (ROLES.isEmpty()) throw new IllegalStateException("Nenhum rank registrado.");
        return ROLES.get(ROLES.size() - 1);
    }

    private final int     id;
    private final String  name;
    private final String  prefix;
    private final String  permission;
    private final boolean alwaysVisible;
    private final boolean broadcast;
    private final boolean fly;


    public Role(String name, String prefix, String permission,
                boolean alwaysVisible, boolean broadcast, boolean fly) {
        this.id            = ROLES.size();
        this.name          = StringUtils.formatColors(name);
        this.prefix        = StringUtils.formatColors(prefix);
        this.permission    = permission == null ? "" : permission;
        this.alwaysVisible = alwaysVisible;
        this.broadcast     = broadcast;
        this.fly           = fly;
    }


    public boolean has(Object onlinePlayer) {
        return isDefault() || Manager.hasPermission((Player) onlinePlayer, this.permission);
    }

    public int     getId()            { return id; }
    public String  getName()          { return name; }
    public String  getPrefix()        { return prefix; }
    public String  getPermission()    { return permission; }
    public boolean isDefault()        { return permission.isEmpty(); }
    public boolean isAlwaysVisible()  { return alwaysVisible; }
    public boolean isBroadcast()      { return broadcast; }
    public boolean canFly()           { return fly; }
}