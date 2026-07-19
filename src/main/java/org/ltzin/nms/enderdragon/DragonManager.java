package org.ltzin.nms.enderdragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;
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

  public int createDragonMount(Player player, Location location, boolean invisible, DragonConfig config) {
    int fakeEntityId = fakeEntityIdSequence.getAndIncrement();
    List<Player> viewers = location.getWorld().getPlayers();

    int spawnedEntityId = packets.spawnDragon(viewers, location, fakeEntityId, invisible);
    if (spawnedEntityId == -1) {
      return -1;
    }

    DragonRide ride = new DragonRide(player.getUniqueId(), spawnedEntityId, location, config);
    activeRides.put(player.getUniqueId(), ride);

    packets.mount(Collections.singletonList(player), spawnedEntityId, player.getEntityId());
    Location seat = location.clone().add(0, config.getRiderSeatHeight(), 0);
    packets.syncRealPlayerPosition(viewers, player, seat);
    return spawnedEntityId;
  }

  public void destroyMountableEntity(Player player) {
    DragonRide ride = activeRides.remove(player.getUniqueId());
    if (ride == null) {
      return;
    }
    List<Player> viewers = ride.getVirtualLocation().getWorld().getPlayers();
    player.teleport(ride.getVirtualLocation());
    packets.destroyEntity(viewers, ride.getEntityId());
  }

  public void dismount(Player player) {
    DragonRide ride = activeRides.remove(player.getUniqueId());
    if (ride == null) {
      return;
    }
    player.teleport(ride.getVirtualLocation());
    packets.dismount(Collections.singletonList(player), ride.getEntityId(), player.getEntityId());
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