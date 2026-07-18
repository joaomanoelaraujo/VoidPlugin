package org.ltzin.nms.enderdragon;


public final class DragonConfig {

  private final double maxSpeed;

  private final double minSpeed;

  private final double acceleration;

  private final double deceleration;

  private final double yawTurnSpeedPerTick;

  private final double pitchTurnSpeedPerTick;

  private final double velocityInertia;

  private final double lookaheadDistance;

  private final double flapVelocityBoost;

  private final int teleportResyncIntervalTicks;

  private DragonConfig(Builder builder) {
    this.maxSpeed = builder.maxSpeed;
    this.minSpeed = builder.minSpeed;
    this.acceleration = builder.acceleration;
    this.deceleration = builder.deceleration;
    this.yawTurnSpeedPerTick = builder.yawTurnSpeedPerTick;
    this.pitchTurnSpeedPerTick = builder.pitchTurnSpeedPerTick;
    this.velocityInertia = builder.velocityInertia;
    this.lookaheadDistance = builder.lookaheadDistance;
    this.flapVelocityBoost = builder.flapVelocityBoost;
    this.teleportResyncIntervalTicks = builder.teleportResyncIntervalTicks;
  }

  public double getMaxSpeed() {
    return maxSpeed;
  }

  public double getMinSpeed() {
    return minSpeed;
  }

  public double getAcceleration() {
    return acceleration;
  }

  public double getDeceleration() {
    return deceleration;
  }

  public double getYawTurnSpeedPerTick() {
    return yawTurnSpeedPerTick;
  }

  public double getPitchTurnSpeedPerTick() {
    return pitchTurnSpeedPerTick;
  }

  public double getVelocityInertia() {
    return velocityInertia;
  }

  public double getLookaheadDistance() {
    return lookaheadDistance;
  }

  public double getFlapVelocityBoost() {
    return flapVelocityBoost;
  }

  public int getTeleportResyncIntervalTicks() {
    return teleportResyncIntervalTicks;
  }

  public static DragonConfig defaults() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private double maxSpeed = 9.0;
    private double minSpeed = 2.5;
    private double acceleration = 6.0;
    private double deceleration = 10.0;
    private double yawTurnSpeedPerTick = 6.0;
    private double pitchTurnSpeedPerTick = 4.0;
    private double velocityInertia = 0.25;
    private double lookaheadDistance = 20.0;
    private double flapVelocityBoost = 3.5;
    private int teleportResyncIntervalTicks = 100; // 5s a 20 ticks/s

    private Builder() {
    }

    public Builder maxSpeed(double blocksPerSecond) {
      this.maxSpeed = blocksPerSecond;
      return this;
    }

    public Builder minSpeed(double blocksPerSecond) {
      this.minSpeed = blocksPerSecond;
      return this;
    }

    public Builder acceleration(double blocksPerSecondSquared) {
      this.acceleration = blocksPerSecondSquared;
      return this;
    }

    public Builder deceleration(double blocksPerSecondSquared) {
      this.deceleration = blocksPerSecondSquared;
      return this;
    }

    public Builder yawTurnSpeedPerTick(double degreesPerTick) {
      this.yawTurnSpeedPerTick = degreesPerTick;
      return this;
    }

    public Builder pitchTurnSpeedPerTick(double degreesPerTick) {
      this.pitchTurnSpeedPerTick = degreesPerTick;
      return this;
    }

    public Builder velocityInertia(double fraction0to1) {
      this.velocityInertia = fraction0to1;
      return this;
    }

    public Builder lookaheadDistance(double blocks) {
      this.lookaheadDistance = blocks;
      return this;
    }

    public Builder flapVelocityBoost(double multiplier) {
      this.flapVelocityBoost = multiplier;
      return this;
    }

    public Builder teleportResyncIntervalTicks(int ticks) {
      this.teleportResyncIntervalTicks = ticks;
      return this;
    }

    public DragonConfig build() {
      if (minSpeed > maxSpeed) {
        throw new IllegalStateException("minSpeed (" + minSpeed + ") não pode ser maior que maxSpeed (" + maxSpeed + ")");
      }
      return new DragonConfig(this);
    }
  }
}