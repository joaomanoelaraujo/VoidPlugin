package org.ltzin.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SkinUtil {

    private SkinUtil() {}

    public static class SkinData {
        public final String value;
        public final String signature;
        public final UUID uuid;

        public SkinData(String value, String signature, UUID uuid) {
            this.value = value;
            this.signature = signature;
            this.uuid = uuid;
        }
    }

    public static CompletableFuture<SkinData> fetchSkin(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuidJson = get("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                if (uuidJson == null || uuidJson.isEmpty()) return null;
                JsonObject uuidObj = new JsonParser().parse(uuidJson).getAsJsonObject();
                String rawUuid = uuidObj.get("id").getAsString();
                UUID uuid = dashUUID(rawUuid);

                String profileJson = get("https://sessionserver.mojang.com/session/minecraft/profile/" + rawUuid + "?unsigned=false");
                JsonObject profileObj = new JsonParser().parse(profileJson).getAsJsonObject();
                JsonObject property = profileObj.getAsJsonArray("properties").get(0).getAsJsonObject();
                String value = property.get("value").getAsString();
                String signature = property.get("signature").getAsString();

                return new SkinData(value, signature, uuid);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        if (con.getResponseCode() != 200) return null;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private static UUID dashUUID(String raw) {
        String dashed = raw.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(dashed);
    }
}