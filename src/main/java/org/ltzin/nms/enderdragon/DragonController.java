package org.ltzin.nms.enderdragon;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public final class DragonController {

  private final DragonPhysics physics;
  private final DragonPackets packets;

  public DragonController(DragonPhysics physics, DragonPackets packets) {
    this.physics = physics;
    this.packets = packets;
  }


  public void tick(Player player, DragonRide ride) {
    physics.tick(ride, player.getEyeLocation());

    Location current = ride.getVirtualLocation();
    double dx = current.getX() - ride.getLastSentLocation().getX();
    double dy = current.getY() - ride.getLastSentLocation().getY();
    double dz = current.getZ() - ride.getLastSentLocation().getZ();

    boolean forceResync = ride.getTicksSinceTeleport() >= ride.getConfig().getTeleportResyncIntervalTicks();
    boolean fitsRelativeMove = packets.isWithinRelativeMoveRange(dx, dy, dz);

    boolean usedRelativeMove = false;
    if (!forceResync && fitsRelativeMove) {
      usedRelativeMove = packets.sendRelativeMove(
              player, ride.getEntityId(), dx, dy, dz,
              current.getYaw(), current.getPitch(), true);
    }

    if (!usedRelativeMove) {
      packets.sendTeleport(player, ride.getEntityId(), current);
      ride.resetTeleportCounter();
    } else {
      ride.incrementTeleportCounter();
    }

    ride.markPositionSent();

    packets.sendVelocity(player, ride.getEntityId(), ride.getVelocity(), ride.getConfig().getFlapVelocityBoost());
  }
}