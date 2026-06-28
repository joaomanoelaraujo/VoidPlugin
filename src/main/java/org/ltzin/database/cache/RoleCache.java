package org.ltzin.database.cache;

import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Cache de ranks para jogadores offline.
 *
 * <p>Cada entrada expira automaticamente após {@link #TTL_MINUTES} minutos.
 * A expiração é verificada tanto na limpeza periódica ({@link #evictExpired()})
 * quanto no acesso direto ({@link #get(String)}), evitando retornar dados velhos
 * mesmo que o timer ainda não tenha rodado.</p>
 */
public final class RoleCache {

    private RoleCache() {}

    private static final int TTL_MINUTES = 30;
    private static final Map<String, RoleCacheEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Insere ou atualiza a entrada do jogador no cache.
     *
     * @param playerName nome original do jogador (case-insensitive)
     * @param roleName   nome do rank (ex.: "VIP", "MVP")
     * @param realName   nome real do jogador (pode diferir se for fake)
     */
    public static void put(String playerName, String roleName, String realName) {
        long expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TTL_MINUTES);
        CACHE.put(key(playerName), new RoleCacheEntry(roleName, realName, expiresAt));
    }


    /**
     * Retorna a entrada do cache se existir e não estiver expirada.
     */
    public static Optional<RoleCacheEntry> get(String playerName) {
        RoleCacheEntry entry = CACHE.get(key(playerName));
        if (entry == null || entry.isExpired()) {
            CACHE.remove(key(playerName)); // remove entrada vencida imediatamente
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * Verifica se há entrada válida (não expirada) para o jogador.
     */
    public static boolean contains(String playerName) {
        return get(playerName).isPresent();
    }

    /**
     * Remove todas as entradas expiradas.
     * Deve ser agendado periodicamente (ex.: a cada 5 minutos).
     *
     * <pre>{@code
     *   timer.scheduleAtFixedRate(RoleCache.evictExpired(), 5 * 60_000L, 5 * 60_000L);
     * }</pre>
     */
    public static TimerTask evictExpired() {
        return new TimerTask() {
            @Override
            public void run() {
                CACHE.entrySet().removeIf(e -> e.getValue().isExpired());
            }
        };
    }

    /** Invalida a entrada de um jogador específico (ex.: ao mudar de rank). */
    public static void invalidate(String playerName) {
        CACHE.remove(key(playerName));
    }

    /** Limpa o cache inteiro (útil no shutdown ou em testes). */
    public static void clear() {
        CACHE.clear();
    }


    private static String key(String playerName) {
        return playerName.toLowerCase();
    }
}