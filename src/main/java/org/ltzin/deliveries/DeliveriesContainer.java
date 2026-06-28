package org.ltzin.deliveries;

import org.ltzin.database.data.DataContainer;
import org.ltzin.database.data.interfaces.AbstractContainer;

import java.util.HashMap;
import java.util.Map;

public class DeliveriesContainer extends AbstractContainer {

    private final Map<Long, Long> claims = new HashMap<>();

    public DeliveriesContainer(DataContainer dataContainer) {
        super(dataContainer);
        deserialize(dataContainer.getAsString());
    }

    private void deserialize(String raw) {
        if (raw == null || raw.isEmpty()) return;

        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;
            try {
                long id        = Long.parseLong(parts[0].trim());
                long timestamp = Long.parseLong(parts[1].trim());
                claims.put(id, timestamp);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void flush() {
        if (claims.isEmpty()) {
            dataContainer.set("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, Long> e : claims.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(e.getKey()).append(":").append(e.getValue());
        }
        dataContainer.set(sb.toString());
    }

    public boolean alreadyClaimed(long deliveryId, long cooldownMillis) {
        if (!claims.containsKey(deliveryId)) return false;
        return (System.currentTimeMillis() - claims.get(deliveryId)) < cooldownMillis;
    }

    public void claim(long deliveryId) {
        claims.put(deliveryId, System.currentTimeMillis());
        flush();
    }

    public long getTimeUntilNextClaim(long deliveryId, long cooldownMillis) {
        if (!claims.containsKey(deliveryId)) return 0L;
        long elapsed   = System.currentTimeMillis() - claims.get(deliveryId);
        long remaining = cooldownMillis - elapsed;
        return Math.max(0L, remaining);
    }

    public long getClaimTime(long deliveryId) {
        return claims.getOrDefault(deliveryId, -1L);
    }
}