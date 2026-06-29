package org.ltzin.database.cache;

import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public final class RoleCache {

    private RoleCache() {}

    private static final int TTL_MINUTES = 30;
    private static final Map<String, RoleCacheEntry> CACHE = new ConcurrentHashMap<>();

    public static void put(String playerName, String roleName, String realName) {
        long expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TTL_MINUTES);
        CACHE.put(key(playerName), new RoleCacheEntry(roleName, realName, expiresAt));
    }


    public static Optional<RoleCacheEntry> get(String playerName) {
        RoleCacheEntry entry = CACHE.get(key(playerName));
        if (entry == null || entry.isExpired()) {
            CACHE.remove(key(playerName));
            return Optional.empty();
        }
        return Optional.of(entry);
    }


    public static boolean contains(String playerName) {
        return get(playerName).isPresent();
    }


    public static TimerTask evictExpired() {
        return new TimerTask() {
            @Override
            public void run() {
                CACHE.entrySet().removeIf(e -> e.getValue().isExpired());
            }
        };
    }

    public static void invalidate(String playerName) {
        CACHE.remove(key(playerName));
    }

    public static void clear() {
        CACHE.clear();
    }


    private static String key(String playerName) {
        return playerName.toLowerCase();
    }
}