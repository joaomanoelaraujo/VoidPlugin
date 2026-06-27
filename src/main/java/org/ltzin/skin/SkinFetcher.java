package org.ltzin.skin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class SkinFetcher {

    private static final String UUID_API    = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final long   TTL_MS      = TimeUnit.MINUTES.toMillis(30);

    private final Logger logger;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SkinFetcher(Logger logger) {
        this.logger = logger;
    }


    public SkinData fetch(String nickname) {
        String key = nickname.toLowerCase();

        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data;
        }

        try {
            String uuidJson = get(UUID_API + nickname);
            if (uuidJson == null || uuidJson.isEmpty()) return null;

            JsonObject uuidObj = new JsonParser().parse(uuidJson).getAsJsonObject();
            if (!uuidObj.has("id")) return null;

            String uuid = uuidObj.get("id").getAsString();

            String profileJson = get(PROFILE_API + uuid + "?unsigned=false");
            if (profileJson == null || profileJson.isEmpty()) return null;

            JsonObject profileObj = new JsonParser().parse(profileJson).getAsJsonObject();
            if (!profileObj.has("properties")) return null;

            JsonArray properties = profileObj.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject prop = properties.get(i).getAsJsonObject();
                if (!"textures".equals(prop.get("name").getAsString())) continue;

                String value     = prop.get("value").getAsString();
                String signature = prop.has("signature") ? prop.get("signature").getAsString() : null;

                SkinData data = new SkinData(value, signature);
                cache.put(key, new CacheEntry(data));
                return data;
            }

        } catch (Exception e) {
            logger.warning("[SkinFetcher] Erro ao buscar skin de " + nickname + ": " + e.getMessage());
        }

        return null;
    }

    public void invalidate(String nickname) {
        cache.remove(nickname.toLowerCase());
    }

    public boolean isCached(String nickname) {
        CacheEntry e = cache.get(nickname.toLowerCase());
        return e != null && !e.isExpired();
    }

    public long cacheAgeSeconds(String nickname) {
        CacheEntry e = cache.get(nickname.toLowerCase());
        if (e == null || e.isExpired()) return -1L;
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - e.cachedAt);
    }


    private String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        conn.setRequestProperty("User-Agent", "LTZ-SkinPlugin/1.0");

        int status = conn.getResponseCode();
        if (status == 204 || status == 404) return null;
        if (status != 200) {
            logger.warning("[SkinFetcher] HTTP " + status + " → " + urlStr);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }


    private static final class CacheEntry {
        final SkinData data;
        final long     cachedAt;

        CacheEntry(SkinData data) {
            this.data     = data;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > TTL_MS;
        }
    }
}