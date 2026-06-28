package org.ltzin.database.cache;

/**
 * Entrada tipada do cache de ranks offline.
 * Substitui o Object[] anterior, eliminando casts e erros em runtime.
 */
public final class RoleCacheEntry {

    private final String   roleName;
    private final String   realName;
    private final long     expiresAt;

    public RoleCacheEntry(String roleName, String realName, long expiresAt) {
        this.roleName  = roleName;
        this.realName  = realName;
        this.expiresAt = expiresAt;
    }

    public String getRoleName()  { return roleName;  }
    public String getRealName()  { return realName;  }
    public boolean isExpired()   { return System.currentTimeMillis() > expiresAt; }
}