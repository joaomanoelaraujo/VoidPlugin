package org.ltzin.nms.enderdragon;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;


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


    List<Player> viewers = current.getWorld().getPlayers();

    boolean forceResync = ride.getTicksSinceTeleport() >= ride.getConfig().getTeleportResyncIntervalTicks();
    boolean fitsRelativeMove = packets.isWithinRelativeMoveRange(dx, dy, dz);

    boolean usedRelativeMove = false;
    if (!forceResync && fitsRelativeMove) {
      usedRelativeMove = packets.sendRelativeMove(
              viewers, ride.getEntityId(), dx, dy, dz,
              current.getYaw(), current.getPitch(), true);
    }

    if (!usedRelativeMove) {
      packets.sendTeleport(viewers, ride.getEntityId(), current);
      ride.resetTeleportCounter();
    } else {
      ride.incrementTeleportCounter();
    }

    ride.markPositionSent();

    packets.sendVelocity(viewers, ride.getEntityId(), ride.getVelocity(), ride.getConfig().getFlapVelocityBoost());

    Location seat = current.clone().add(0, ride.getConfig().getRiderSeatHeight(), 0);
    packets.syncRealPlayerPosition(viewers, player, seat);
  }
}