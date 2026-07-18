package org.ltzin.nms.enderdragon;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;


public final class DragonRide {

  private final UUID playerId;
  private final int entityId;
  private final DragonConfig config;

  private final Location virtualLocation;

  private double currentYaw;
  private double currentPitch;

  private double currentSpeed;

  private Vector velocity = new Vector(0, 0, 0);

  private final Location lastSentLocation;

  private int ticksSinceTeleport;

  public DragonRide(UUID playerId, int entityId, Location spawnLocation, DragonConfig config) {
    this.playerId = playerId;
    this.entityId = entityId;
    this.config = config;
    this.virtualLocation = spawnLocation.clone();
    this.lastSentLocation = spawnLocation.clone();
    this.currentYaw = spawnLocation.getYaw();
    this.currentPitch = spawnLocation.getPitch();
    this.currentSpeed = config.getMinSpeed();
    this.ticksSinceTeleport = 0;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public int getEntityId() {
    return entityId;
  }

  public DragonConfig getConfig() {
    return config;
  }

  public Location getVirtualLocation() {
    return virtualLocation;
  }

  public double getCurrentYaw() {
    return currentYaw;
  }

  public void setCurrentYaw(double currentYaw) {
    this.currentYaw = currentYaw;
  }

  public double getCurrentPitch() {
    return currentPitch;
  }

  public void setCurrentPitch(double currentPitch) {
    this.currentPitch = currentPitch;
  }

  public double getCurrentSpeed() {
    return currentSpeed;
  }

  public void setCurrentSpeed(double currentSpeed) {
    this.currentSpeed = currentSpeed;
  }

  public Vector getVelocity() {
    return velocity;
  }

  public void setVelocity(Vector velocity) {
    this.velocity = velocity;
  }

  public Location getLastSentLocation() {
    return lastSentLocation;
  }

  public void markPositionSent() {
    lastSentLocation.setX(virtualLocation.getX());
    lastSentLocation.setY(virtualLocation.getY());
    lastSentLocation.setZ(virtualLocation.getZ());
  }

  public int getTicksSinceTeleport() {
    return ticksSinceTeleport;
  }

  public void resetTeleportCounter() {
    this.ticksSinceTeleport = 0;
  }

  public void incrementTeleportCounter() {
    this.ticksSinceTeleport++;
  }
}