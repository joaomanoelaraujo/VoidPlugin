package org.ltzin.nms.enderdragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class DragonManager {

  private final AtomicInteger fakeEntityIdSequence = new AtomicInteger(Integer.MAX_VALUE - 10_000);

  private final Map<UUID, DragonRide> activeRides = new ConcurrentHashMap<>();

  private final Plugin plugin;
  private final DragonPackets packets;
  private final DragonController controller;
  private final DragonConfig defaultConfig;

  private BukkitTask flightTask;

  public DragonManager(Plugin plugin) {
    this(plugin, DragonConfig.defaults());
  }

  public DragonManager(Plugin plugin, DragonConfig defaultConfig) {
    this.plugin = plugin;
    this.packets = new DragonPackets(plugin);
    this.controller = new DragonController(new DragonPhysics(defaultConfig), packets);
    this.defaultConfig = defaultConfig;
  }

  public synchronized void init() {
    if (flightTask != null) {
      return;
    }
    flightTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickAllRides, 1L, 1L);
  }

  public synchronized void shutdown() {
    if (flightTask != null) {
      flightTask.cancel();
      flightTask = null;
    }
    activeRides.clear();
  }

  private void tickAllRides() {
    for (Map.Entry<UUID, DragonRide> entry : activeRides.entrySet()) {
      Player player = Bukkit.getPlayer(entry.getKey());
      if (player == null || !player.isOnline()) {
        continue;
      }
      try {
        controller.tick(player, entry.getValue());
      } catch (Exception e) {
        plugin.getLogger().warning("[DragonManager] Falha ao avançar voo de " + player.getName() + ": " + e.getMessage());
      }
    }
  }

  public int createDragonMount(Player player, Location location) {
    return createDragonMount(player, location, false, defaultConfig);
  }

  /** Igual a {@link #createDragonMount(Player, Location)}, com controle de invisibilidade e config de voo. */
  public int createDragonMount(Player player, Location location, boolean invisible, DragonConfig config) {
    int fakeEntityId = fakeEntityIdSequence.getAndIncrement();
    int spawnedEntityId = packets.spawnDragon(player, location, fakeEntityId, invisible);
    if (spawnedEntityId == -1) {
      return -1;
    }

    DragonRide ride = new DragonRide(player.getUniqueId(), spawnedEntityId, location, config);
    activeRides.put(player.getUniqueId(), ride);

    packets.mount(player, spawnedEntityId, player.getEntityId());
    return spawnedEntityId;
  }

  public void destroyMountableEntity(Player player) {
    DragonRide ride = activeRides.remove(player.getUniqueId());
    if (ride == null) {
      return;
    }
    player.teleport(ride.getVirtualLocation());
    packets.destroyEntity(player, ride.getEntityId());
  }

  public void dismount(Player player) {
    DragonRide ride = activeRides.remove(player.getUniqueId());
    if (ride == null) {
      return;
    }
    player.teleport(ride.getVirtualLocation());
    packets.dismount(player, ride.getEntityId(), player.getEntityId());
  }

  public boolean isRidingDragon(Player player) {
    return activeRides.containsKey(player.getUniqueId());
  }

  public Location getDragonLocation(Player player) {
    DragonRide ride = activeRides.get(player.getUniqueId());
    return ride == null ? null : ride.getVirtualLocation().clone();
  }

  public void syncPlayerToDragon(Player player) {
    DragonRide ride = activeRides.get(player.getUniqueId());
    if (ride != null) {
      player.teleport(ride.getVirtualLocation());
    }
  }
}