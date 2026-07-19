package org.ltzin.nms.enderdragon;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class DragonPackets {

  private final ProtocolManager protocol = ProtocolLibrary.getProtocolManager();
  private final Plugin plugin;
  private final Logger logger;

  private final int minorVersion;

  private final String nmsPackageVersion;

  private final double relativeMoveMaxDelta;

  private final Map<String, Long> lastWarnAt = new ConcurrentHashMap<>();
  private static final long WARN_COOLDOWN_MILLIS = 5_000L;

  private static float dragonVisualYaw(float realYaw) {
    float v = realYaw + 180.0F;
    while (v > 180.0F) {
      v -= 360.0F;
    }
    while (v < -180.0F) {
      v += 360.0F;
    }
    return v;
  }

  public DragonPackets(Plugin plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
    this.minorVersion = resolveMinorVersion();
    this.nmsPackageVersion = resolveNmsPackageVersion();
    this.relativeMoveMaxDelta = isLegacy() ? 3.9 : 7.9;
  }

  public boolean isLegacy() {
    return minorVersion <= 8;
  }

  private static int resolveMinorVersion() {
    try {
      String bukkitVersion = Bukkit.getBukkitVersion();
      String mcVersion = bukkitVersion.split("-")[0];
      String[] parts = mcVersion.split("\\.");
      return Integer.parseInt(parts[1]);
    } catch (Exception e) {
      return 8;
    }
  }

  private static String resolveNmsPackageVersion() {
    try {
      String pkg = Bukkit.getServer().getClass().getPackage().getName(); // org.bukkit.craftbukkit.v1_8_R3
      return pkg.substring(pkg.lastIndexOf('.') + 1);
    } catch (Exception e) {
      return "";
    }
  }

  public int spawnDragon(Collection<Player> viewers, Location location, int fakeEntityId, boolean invisible) {
    if (isLegacy()) {
      return spawnLegacyDragon(viewers, location, invisible);
    }

    UUID entityUuid = UUID.randomUUID();
    try {
      boolean modernSpawn = minorVersion >= 19;
      PacketContainer packet = protocol.createPacket(modernSpawn ? Server.SPAWN_ENTITY : Server.SPAWN_ENTITY_LIVING);
      packet.getModifier().writeDefaults();

      packet.getIntegers().write(0, fakeEntityId);
      packet.getUUIDs().write(0, entityUuid);
      packet.getEntityTypeModifier().write(0, EntityType.ENDER_DRAGON);
      packet.getDoubles()
              .write(0, location.getX())
              .write(1, location.getY())
              .write(2, location.getZ());

      float visualYaw = dragonVisualYaw(location.getYaw());
      packet.getBytes()
              .write(0, (byte) (visualYaw * 256.0F / 360.0F))
              .write(1, (byte) (location.getPitch() * 256.0F / 360.0F))
              .write(2, (byte) (visualYaw * 256.0F / 360.0F));

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "SPAWN_ENDER_DRAGON", e);
        }
      }
      sendMetadata(viewers, fakeEntityId, invisible);
      return fakeEntityId;
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote SPAWN_ENDER_DRAGON: " + e.getMessage());
      return -1;
    }
  }

  private void sendMetadata(Collection<Player> viewers, int entityId, boolean invisible) {
    try {
      WrappedDataWatcherObject watcherObject = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
      WrappedWatchableObject watchable = new WrappedWatchableObject(watcherObject, (byte) (invisible ? 0x20 : 0x00));

      PacketContainer packet = protocol.createPacket(Server.ENTITY_METADATA);
      packet.getIntegers().write(0, entityId);
      packet.getWatchableCollectionModifier().write(0, Collections.singletonList(watchable));

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DRAGON_METADATA", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DRAGON_METADATA: " + e.getMessage());
    }
  }

  public void mount(Collection<Player> viewers, int vehicleEntityId, int passengerEntityId) {
    try {
      PacketContainer packet;
      if (isLegacy()) {
        packet = protocol.createPacket(Server.ATTACH_ENTITY);
        packet.getIntegers()
                .write(0, 0)
                .write(1, passengerEntityId)
                .write(2, vehicleEntityId);
      } else {
        packet = protocol.createPacket(Server.MOUNT);
        packet.getIntegers().write(0, vehicleEntityId);
        packet.getIntegerArrays().write(0, new int[]{passengerEntityId});
      }

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "MOUNT_DRAGON", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote MOUNT_DRAGON: " + e.getMessage());
    }
  }

  public void dismount(Collection<Player> viewers, int vehicleEntityId, int passengerEntityId) {
    try {
      PacketContainer packet;
      if (isLegacy()) {
        packet = protocol.createPacket(Server.ATTACH_ENTITY);
        packet.getIntegers()
                .write(0, 0)                  // leashed = false
                .write(1, passengerEntityId)  // entidade que estava montada
                .write(2, -1);                // -1 = desmontar
      } else {
        packet = protocol.createPacket(Server.MOUNT);
        packet.getIntegers().write(0, vehicleEntityId);
        packet.getIntegerArrays().write(0, new int[0]); // sem passageiros
      }

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DISMOUNT", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DISMOUNT: " + e.getMessage());
    }
  }

  public void destroyEntity(Collection<Player> viewers, int entityId) {
    try {
      PacketContainer packet = protocol.createPacket(Server.ENTITY_DESTROY);
      packet.getIntegerArrays().write(0, new int[]{entityId});
      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "ENTITY_DESTROY", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote ENTITY_DESTROY: " + e.getMessage());
    }
  }


  public boolean isWithinRelativeMoveRange(double dx, double dy, double dz) {
    return Math.abs(dx) <= relativeMoveMaxDelta
            && Math.abs(dy) <= relativeMoveMaxDelta
            && Math.abs(dz) <= relativeMoveMaxDelta;
  }

  public boolean sendRelativeMove(Collection<Player> viewers, int entityId, double dx, double dy, double dz, float yaw, float pitch, boolean onGround) {
    try {
      PacketContainer packet = protocol.createPacket(Server.REL_ENTITY_MOVE_LOOK);
      packet.getIntegers().write(0, entityId);

      byte yawByte = (byte) (dragonVisualYaw(yaw) * 256.0F / 360.0F);
      byte pitchByte = (byte) (pitch * 256.0F / 360.0F);

      if (isLegacy()) {
        packet.getBytes()
                .write(0, encodeDeltaByte(dx))
                .write(1, encodeDeltaByte(dy))
                .write(2, encodeDeltaByte(dz))
                .write(3, yawByte)
                .write(4, pitchByte);
      } else {
        packet.getShorts()
                .write(0, encodeDeltaShort(dx))
                .write(1, encodeDeltaShort(dy))
                .write(2, encodeDeltaShort(dz));
        packet.getBytes()
                .write(0, yawByte)
                .write(1, pitchByte);
      }

      packet.getBooleans().write(0, onGround);

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DRAGON_REL_MOVE", e);
        }
      }
      return true;
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DRAGON_REL_MOVE: " + e.getMessage());
      return false;
    }
  }

  private static byte encodeDeltaByte(double blocks) {
    double clamped = Math.max(-3.96875, Math.min(3.96875, blocks));
    return (byte) Math.round(clamped * 32.0);
  }

  private static short encodeDeltaShort(double blocks) {
    double clamped = Math.max(-7.999755859375, Math.min(7.999755859375, blocks));
    return (short) Math.round(clamped * 4096.0);
  }


  public void sendTeleport(Collection<Player> viewers, int entityId, Location location) {
    try {
      PacketContainer packet = protocol.createPacket(Server.ENTITY_TELEPORT);
      packet.getIntegers().write(0, entityId);

      if (isLegacy()) {

        packet.getIntegers()
                .write(1, fixedPoint(location.getX()))
                .write(2, fixedPoint(location.getY()))
                .write(3, fixedPoint(location.getZ()));
      } else {
        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
      }

      packet.getBytes()
              .write(0, (byte) (dragonVisualYaw(location.getYaw()) * 256.0F / 360.0F))
              .write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
      packet.getBooleans().write(0, true);

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DRAGON_TELEPORT", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DRAGON_TELEPORT: " + e.getMessage());
    }
  }

  private static int fixedPoint(double coordinate) {
    return (int) Math.floor(coordinate * 32.0D);
  }


  public void syncRealPlayerPosition(Collection<Player> viewers, Player rider, Location location) {
    try {
      int riderEntityId = rider.getEntityId();
      PacketContainer packet = protocol.createPacket(Server.ENTITY_TELEPORT);
      packet.getIntegers().write(0, riderEntityId);

      if (isLegacy()) {
        packet.getIntegers()
                .write(1, fixedPoint(location.getX()))
                .write(2, fixedPoint(location.getY()))
                .write(3, fixedPoint(location.getZ()));
      } else {
        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
      }

      packet.getBytes()
              .write(0, (byte) (location.getYaw() * 256.0F / 360.0F))
              .write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
      packet.getBooleans().write(0, true);

      for (Player viewer : viewers) {
        if (viewer.equals(rider)) {
          continue;
        }
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DRAGON_RIDER_TELEPORT", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DRAGON_RIDER_TELEPORT: " + e.getMessage());
    }
  }

  public void sendVelocity(Collection<Player> viewers, int entityId, Vector velocityBlocksPerTick, double flapBoost) {
    try {
      Vector reported = velocityBlocksPerTick.clone().multiply(flapBoost);
      short vx = encodeVelocityComponent(reported.getX());
      short vy = encodeVelocityComponent(reported.getY());
      short vz = encodeVelocityComponent(reported.getZ());

      PacketContainer packet = protocol.createPacket(Server.ENTITY_VELOCITY);
      packet.getIntegers().write(0, entityId);

      if (isLegacy()) {

        packet.getIntegers().write(1, (int) vx).write(2, (int) vy).write(3, (int) vz);
      } else {
        packet.getShorts().write(0, vx).write(1, vy).write(2, vz);
      }

      for (Player viewer : viewers) {
        try {
          protocol.sendServerPacket(viewer, packet);
        } catch (Exception e) {
          warn(viewer, "DRAGON_VELOCITY", e);
        }
      }
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar pacote DRAGON_VELOCITY: " + e.getMessage());
    }
  }

  private static short encodeVelocityComponent(double blocksPerTick) {
    double clamped = Math.max(-3.9, Math.min(3.9, blocksPerTick));
    return (short) Math.round(clamped * 8000.0);
  }

  private int spawnLegacyDragon(Collection<Player> viewers, Location location, boolean invisible) {
    try {
      Object craftWorld = craftClass("CraftWorld").cast(location.getWorld());
      Object nmsWorld = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);

      Class<?> entityClass = nmsClass("Entity");
      Class<?> dragonClass = nmsClass("EntityEnderDragon");
      Object nmsDragon = dragonClass.getConstructor(nmsClass("World")).newInstance(nmsWorld);

      entityClass.getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)
              .invoke(nmsDragon, location.getX(), location.getY(), location.getZ(), dragonVisualYaw(location.getYaw()), location.getPitch());

      if (invisible) {
        try {
          entityClass.getMethod("setInvisible", boolean.class).invoke(nmsDragon, true);
        } catch (NoSuchMethodException ignored) {
        }
      }

      Object spawnPacket = nmsClass("PacketPlayOutSpawnEntityLiving")
              .getConstructor(nmsClass("EntityLiving"))
              .newInstance(nmsDragon);

      for (Player viewer : viewers) {
        try {
          sendRawPacket(viewer, spawnPacket);
        } catch (Exception e) {
          warn(viewer, "SPAWN_ENDER_DRAGON_LEGACY", e);
        }
      }

      return (int) entityClass.getMethod("getId").invoke(nmsDragon);
    } catch (Exception e) {
      logger.warning("[DragonPackets] Falha ao montar dragão legado: " + e.getMessage());
      return -1;
    }
  }

  private void sendRawPacket(Player player, Object nmsPacket) throws Exception {
    Object craftPlayer = craftEntityClass("CraftPlayer").cast(player);
    Object handle = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
    Object connection = handle.getClass().getField("playerConnection").get(handle);
    connection.getClass().getMethod("sendPacket", nmsClass("Packet")).invoke(connection, nmsPacket);
  }

  private Class<?> nmsClass(String name) throws ClassNotFoundException {
    return Class.forName("net.minecraft.server." + nmsPackageVersion + "." + name);
  }

  private Class<?> craftClass(String name) throws ClassNotFoundException {
    return Class.forName("org.bukkit.craftbukkit." + nmsPackageVersion + "." + name);
  }

  private Class<?> craftEntityClass(String name) throws ClassNotFoundException {
    return Class.forName("org.bukkit.craftbukkit." + nmsPackageVersion + ".entity." + name);
  }

  private void warn(Player player, String stage, Exception e) {
    String key = stage + "|" + player.getUniqueId();
    long now = System.currentTimeMillis();
    Long last = lastWarnAt.get(key);
    if (last != null && (now - last) < WARN_COOLDOWN_MILLIS) {
      return;
    }
    lastWarnAt.put(key, now);
    logger.warning("[DragonPackets] Falha em " + stage + " para " + player.getName() + ": " + e.getMessage());
  }
}