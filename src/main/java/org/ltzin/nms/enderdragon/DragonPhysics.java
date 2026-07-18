package org.ltzin.nms.enderdragon;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class DragonPhysics {

  private static final double TICK_SECONDS = 1.0 / 20.0;

  private final DragonConfig config;

  public DragonPhysics(DragonConfig config) {
    this.config = config;
  }

  public void tick(DragonRide ride, Location eyeLocation) {
    Vector eyeDirection = eyeLocation.getDirection();

    double targetX = eyeLocation.getX() + eyeDirection.getX() * config.getLookaheadDistance();
    double targetY = eyeLocation.getY() + eyeDirection.getY() * config.getLookaheadDistance();
    double targetZ = eyeLocation.getZ() + eyeDirection.getZ() * config.getLookaheadDistance();

    Location virtual = ride.getVirtualLocation();
    double dx = targetX - virtual.getX();
    double dy = targetY - virtual.getY();
    double dz = targetZ - virtual.getZ();
    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

    double desiredYaw = Math.toDegrees(Math.atan2(-dx, dz));
    double desiredPitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));

    double yawDelta = normalizeAngle(desiredYaw - ride.getCurrentYaw());
    double clampedYawDelta = clampAngle(yawDelta, config.getYawTurnSpeedPerTick());

    double newYaw = normalizeAngle(ride.getCurrentYaw() + clampedYawDelta);
    double newPitch = ride.getCurrentPitch() + clampAngle(desiredPitch - ride.getCurrentPitch(), config.getPitchTurnSpeedPerTick());
    newPitch = Math.max(-89.0, Math.min(89.0, newPitch));

    ride.setCurrentYaw(newYaw);
    ride.setCurrentPitch(newPitch);

    double turnSharpness = config.getYawTurnSpeedPerTick() <= 0
            ? 0.0
            : Math.min(1.0, Math.abs(clampedYawDelta) / config.getYawTurnSpeedPerTick());

    double targetSpeed = lerp(config.getMaxSpeed(), config.getMinSpeed(), turnSharpness * 0.6);
    double speed = ride.getCurrentSpeed();
    if (speed < targetSpeed) {
      speed = Math.min(targetSpeed, speed + config.getAcceleration() * TICK_SECONDS);
    } else if (speed > targetSpeed) {
      speed = Math.max(targetSpeed, speed - config.getDeceleration() * TICK_SECONDS);
    }
    speed = Math.max(config.getMinSpeed(), Math.min(config.getMaxSpeed(), speed));
    ride.setCurrentSpeed(speed);

    double yawRad = Math.toRadians(newYaw);
    double pitchRad = Math.toRadians(newPitch);
    double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
    double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);
    double dirY = -Math.sin(pitchRad);

    Vector targetVelocity = new Vector(dirX, dirY, dirZ).multiply(speed * TICK_SECONDS);

    double inertia = config.getVelocityInertia();
    Vector newVelocity = ride.getVelocity().clone().multiply(1 - inertia)
            .add(targetVelocity.multiply(inertia));
    ride.setVelocity(newVelocity);

    virtual.add(newVelocity);
    virtual.setYaw((float) newYaw);
    virtual.setPitch((float) newPitch);
  }

  private static double lerp(double from, double to, double fraction01) {
    double f = Math.max(0.0, Math.min(1.0, fraction01));
    return from + (to - from) * f;
  }

  private static double normalizeAngle(double angle) {
    double a = angle % 360.0;
    if (a >= 180.0) {
      a -= 360.0;
    } else if (a < -180.0) {
      a += 360.0;
    }
    return a;
  }

  private static double clampAngle(double angle, double max) {
    return Math.max(-max, Math.min(max, angle));
  }
}