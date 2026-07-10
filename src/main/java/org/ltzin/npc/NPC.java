package org.ltzin.npc;

import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class NPC {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(900_000_000);

    private final String id;
    private final int entityId;
    private final UUID uuid;
    private String skinOwner;
    private String skinValue;
    private String skinSignature;
    private Location location;
    private String command;

    public NPC(String id, Location location) {
        this.id = id;
        this.location = location.clone();
        this.entityId = ID_COUNTER.incrementAndGet();
        this.uuid = UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location.clone();
    }

    public void setSkin(String ownerName, String value, String signature) {
        this.skinOwner = ownerName;
        this.skinValue = value;
        this.skinSignature = signature;
    }

    public String getSkinOwner() {
        return skinOwner;
    }

    public String getSkinValue() {
        return skinValue;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public boolean hasSkin() {
        return skinValue != null && skinSignature != null;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
