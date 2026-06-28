package org.ltzin.player.role;


import org.ltzin.database.cache.RoleCache;
import org.ltzin.database.cache.RoleCacheEntry;
import org.bukkit.entity.Player;
import org.ltzin.manager.Manager;
import org.ltzin.player.Profile;

import java.util.Optional;

/**
 * Responsável exclusivamente por resolver o {@link Role} de um jogador,
 * seja ele online ou offline.
 *
 * <p><b>Ordem de resolução:</b>
 * <ol>
 *   <li>Jogador online → tag customizada → rank por permissão</li>
 *   <li>Cache offline</li>
 *   <li>Banco de dados</li>
 *   <li>Fallback: rank padrão</li>
 * </ol>
 * </p>
 */
public final class RoleLookup {

    private RoleLookup() {}

    /**
     * Resultado de um lookup: rank resolvido + nome real do jogador.
     */
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


    /**
     * Resolve o rank de um jogador pelo nome.
     * Se o jogador estiver online, prioriza tag customizada antes do rank por permissão.
     * Se estiver offline, consulta cache e depois o banco de dados.
     */
    public static Result resolve(String playerName) {
        // Online
        Player online = Manager.getPlayer(playerName);
        if (online != null) {
            return new Result(roleForOnlinePlayer(online), playerName);
        }

        // Cache + DB
        return resolveOffline(playerName);
    }

    /**
     * Resolve o rank de um jogador online.
     * Considera a tag customizada do profile antes do rank por permissão.
     */
    public static Result resolveOnline(Player player) {
        return new Result(roleForOnlinePlayer(player), player.getName());
    }

    /**
     * Retorna o Role de um jogador online, verificando tag customizada primeiro.
     */
    static Role roleForOnlinePlayer(Player player) {
        Optional<Role> tagRole = RoleTag.getTagRole(player.getName());
        if (tagRole.isPresent()) {
            return tagRole.get();
        }
        return Role.byPermission(player);
    }


    private static Result resolveOffline(String playerName) {
        // Cache
        Optional<RoleCacheEntry> cached = RoleCache.get(playerName);
        if (cached.isPresent()) {
            RoleCacheEntry entry = cached.get();
            return new Result(Role.byName(entry.getRoleName()), entry.getRealName());
        }

        // Banco de dados
        String dbResult = Profile.getProfile(playerName).getRole();
        if (dbResult != null) {
            String[] parts  = dbResult.split(" : ", 2);
            String roleName = parts[0];
            String realName = parts.length > 1 ? parts[1] : playerName;

            RoleCache.put(playerName, roleName, realName);
            return new Result(Role.byName(roleName), realName);
        }

        // Fallback
        return new Result(Role.getDefault(), playerName);
    }
}