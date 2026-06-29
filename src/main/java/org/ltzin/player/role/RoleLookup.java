package org.ltzin.player.role;

import org.ltzin.database.cache.RoleCache;
import org.ltzin.database.cache.RoleCacheEntry;
import org.bukkit.entity.Player;
import org.ltzin.manager.Manager;
import org.ltzin.player.Profile;
import org.ltzin.utils.StringUtils;

import java.util.Optional;

public final class RoleLookup {

    private RoleLookup() {}

    public static final class Result {
        private final Role   role;
        private final String resolvedName;

        private Result(Role role, String resolvedName) {
            this.role         = role;
            this.resolvedName = resolvedName;
        }

        public Role   getRole()         { return role; }
        public String getResolvedName() { return resolvedName; }
    }

    public static Result resolve(String playerName) {
        Player online = Manager.getPlayer(playerName);
        if (online != null) {
            return new Result(roleForOnlinePlayer(online), playerName);
        }
        return resolveOffline(playerName);
    }

    public static Result resolveOnline(Player player) {
        return new Result(roleForOnlinePlayer(player), player.getName());
    }

    public static Role roleForOnlinePlayer(Player player) {
        if (Role.listRoles().isEmpty()) return Role.getDefault();

        Optional<Role> tagRole = RoleTag.getTagRole(player.getName());
        if (tagRole.isPresent()) {
            return tagRole.get();
        }
        return Role.byPermission(player);
    }

    /**
     * Retorna o {@link Role} correspondente à tag que o jogador escolheu
     * manualmente (comando /tag), desde que ele ainda tenha permissão para
     * usá-la. Retorna {@code null} se nenhuma tag estiver selecionada, se a
     * tag salva não existir mais, ou se a permissão tiver sido removida —
     * nesses casos o chamador deve usar o rank padrão (por permissão) no lugar.
     */
    public static Role getSelectedTagRole(Player player) {
        if (Role.listRoles().isEmpty()) return null;

        Profile profile = Profile.getProfile(player);
        if (profile == null) return null;

        String tagId = profile.getSelectedTag();
        if (tagId == null || tagId.isEmpty()) return null;

        for (Role role : Role.listRoles()) {
            if (role.hasTag() && StringUtils.stripColors(role.getName()).equalsIgnoreCase(tagId)) {
                return role.has(player) ? role : null;
            }
        }
        return null;
    }

    /**
     * Rank usado para QUALQUER exibição visual do jogador (chat, tablist,
     * nametag acima da cabeça). Se houver uma tag selecionada e ela ainda
     * for válida (permissão presente), ela é usada por completo — cor,
     * prefixo e tag — no lugar do rank por permissão. Caso contrário, cai
     * de volta para o rank normal.
     *
     * <p>Isso é só visual: o rank por permissão continua sendo a fonte de
     * verdade pra qualquer lógica de permissão/funcionalidade (fly, broadcast, etc).</p>
     */
    public static Role displayRole(Player player) {
        Role tagRole = getSelectedTagRole(player);
        return tagRole != null ? tagRole : roleForOnlinePlayer(player);
    }

    private static Result resolveOffline(String playerName) {
        Optional<RoleCacheEntry> cached = RoleCache.get(playerName);
        if (cached.isPresent()) {
            RoleCacheEntry entry = cached.get();
            return new Result(Role.byName(entry.getRoleName()), entry.getRealName());
        }

        Profile profile = Profile.getProfile(playerName);
        if (profile != null) {
            String dbResult = profile.getRole();
            if (dbResult != null && !dbResult.isEmpty()) {
                String[] parts  = dbResult.split(" : ", 2);
                String roleName = parts[0];
                String realName = parts.length > 1 ? parts[1] : playerName;

                RoleCache.put(playerName, roleName, realName);
                return new Result(Role.byName(roleName), realName);
            }
        }

        return new Result(Role.getDefault(), playerName);
    }
}