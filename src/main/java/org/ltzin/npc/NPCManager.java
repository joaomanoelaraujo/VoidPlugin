package org.ltzin.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.ltzin.utils.VersionUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class NPCManager implements Listener {

    private static final Class<?> PLAYER_INFO_PACKET_CLASS;
    private static final Class<?> PLAYER_INFO_ENTRY_CLASS;
    private static final Class<?> PLAYER_INFO_ACTION_CLASS;
    private static final Constructor<?> PLAYER_INFO_ENTRY_CTOR;
    private static final Constructor<?> PLAYER_INFO_PACKET_CTOR;
    private static final boolean PLAYER_INFO_PACKET_CTOR_TAKES_COLLECTION;

    static {
        Class<?> packetClass = null, entryClass = null, actionClass = null;
        Constructor<?> entryCtor = null, packetCtor = null;
        boolean ctorTakesCollection = false;

        try {
            packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            entryClass  = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            actionClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");

            Constructor<?>[] entryCtors = entryClass.getDeclaredConstructors();
            if (entryCtors.length > 0) {
                entryCtor = entryCtors[0];
                entryCtor.setAccessible(true);
            }

            for (Constructor<?> c : packetClass.getDeclaredConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 2 && EnumSet.class.isAssignableFrom(params[0])) {
                    if (entryClass.isAssignableFrom(params[1])) {
                        packetCtor = c;
                        ctorTakesCollection = false;
                        c.setAccessible(true);
                        break;
                    }
                    if (Collection.class.isAssignableFrom(params[1]) && packetCtor == null) {
                        packetCtor = c;
                        ctorTakesCollection = true;
                        c.setAccessible(true);
                    }
                }
            }
        } catch (Throwable ignored) {

            packetClass = null; entryClass = null; actionClass = null;
            entryCtor = null; packetCtor = null;
        }

        PLAYER_INFO_PACKET_CLASS = packetClass;
        PLAYER_INFO_ENTRY_CLASS  = entryClass;
        PLAYER_INFO_ACTION_CLASS = actionClass;
        PLAYER_INFO_ENTRY_CTOR   = entryCtor;
        PLAYER_INFO_PACKET_CTOR  = packetCtor;
        PLAYER_INFO_PACKET_CTOR_TAKES_COLLECTION = ctorTakesCollection;
    }

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;
    private final Map<String, NPC> npcs = new LinkedHashMap<>();
    private final File file;

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.file = new File(plugin.getDataFolder(), "npcs.yml");

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                spawnAllFor(player);
            }
        }, 5L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                spawnAllFor(player);
            }
        }, 5L);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (NPC npc : npcs.values()) {
            String path = "npcs." + npc.getId();
            Location loc = npc.getLocation();
            config.set(path + ".mundo", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
            if (npc.hasSkin()) {
                config.set(path + ".skinOwner", npc.getSkinOwner());
                config.set(path + ".skinValue", npc.getSkinValue());
                config.set(path + ".skinSignature", npc.getSkinSignature());
            }
            if (npc.getCommand() != null) {
                config.set(path + ".comando", npc.getCommand());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar npcs.yml: " + e.getMessage());
        }
    }

    public void load() {
        npcs.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("npcs");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String path = "npcs." + id;
            String worldName = config.getString(path + ".mundo");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue;

            Location loc = new Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z"),
                    (float) config.getDouble(path + ".yaw"),
                    (float) config.getDouble(path + ".pitch")
            );

            NPC npc = new NPC(id, loc);
            if (config.contains(path + ".skinValue")) {
                npc.setSkin(config.getString(path + ".skinOwner"),
                        config.getString(path + ".skinValue"),
                        config.getString(path + ".skinSignature"));
            }
            if (config.contains(path + ".comando")) {
                npc.setCommand(config.getString(path + ".comando"));
            }
            npcs.put(id.toLowerCase(), npc);
        }
    }

    public NPC create(String id, Location location) {
        NPC npc = new NPC(id, location);
        npcs.put(id.toLowerCase(), npc);
        Bukkit.getScheduler().runTask(plugin, () -> {
            String npcWorld = location.getWorld() == null ? null : location.getWorld().getName();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (npcWorld == null || !p.getWorld().getName().equals(npcWorld)) {
                    continue;
                }
                spawnFor(npc, p);
            }
        });
        return npc;
    }

    public NPC get(String id) {
        return npcs.get(id.toLowerCase());
    }

    public NPC getByEntityId(int entityId) {
        for (NPC npc : npcs.values()) {
            if (npc.getEntityId() == entityId) return npc;
        }
        return null;
    }

    public boolean exists(String id) {
        return npcs.containsKey(id.toLowerCase());
    }

    public Collection<NPC> getAll() {
        return npcs.values();
    }

    public void remove(String id) {
        NPC npc = npcs.remove(id.toLowerCase());
        if (npc == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            destroyFor(npc, p);
        }
    }

    public void removeAll() {
        for (String id : new ArrayList<>(npcs.keySet())) {
            remove(id);
        }
    }

    public void spawnAllToEveryone() {
        for (NPC npc : npcs.values()) {
            String npcWorld = npc.getLocation().getWorld() == null ? null : npc.getLocation().getWorld().getName();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (npcWorld == null || !p.getWorld().getName().equals(npcWorld)) {
                    continue;
                }
                spawnFor(npc, p);
            }
        }
    }

    public void spawnAllFor(Player player) {
        String playerWorld = player.getWorld().getName();
        for (NPC npc : npcs.values()) {
            Location npcLocation = npc.getLocation();
            if (npcLocation.getWorld() == null || !npcLocation.getWorld().getName().equals(playerWorld)) {
                continue;
            }
            spawnFor(npc, player);
        }
    }

    private static String toProfileName(String rawId) {
        if (rawId.length() > 16) {
            return rawId.substring(0, 16);
        }
        return rawId;
    }

    @SuppressWarnings("deprecation")
    public void spawnFor(NPC npc, Player player) {
        try {
            String profileName = toProfileName(npc.getId());
            WrappedGameProfile profile = new WrappedGameProfile(npc.getUuid(), profileName);
            if (npc.hasSkin()) {
                profile.getProperties().put("textures",
                        new WrappedSignedProperty("textures", npc.getSkinValue(), npc.getSkinSignature()));
            }

            sendPlayerInfoAdd(player, profile);

            Location loc = npc.getLocation();

            if (PacketType.Play.Server.NAMED_ENTITY_SPAWN.isSupported()) {
                PacketContainer spawn = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
                spawn.getIntegers().write(0, npc.getEntityId());
                spawn.getUUIDs().write(0, npc.getUuid());

                if (VersionUtil.isLegacy()) {
                    spawn.getIntegers().write(1, floorFixedPoint(loc.getX()));
                    spawn.getIntegers().write(2, floorFixedPoint(loc.getY()));
                    spawn.getIntegers().write(3, floorFixedPoint(loc.getZ()));
                } else {
                    spawn.getDoubles().write(0, loc.getX());
                    spawn.getDoubles().write(1, loc.getY());
                    spawn.getDoubles().write(2, loc.getZ());
                }

                spawn.getBytes().write(0, (byte) (loc.getYaw() * 256 / 360));
                spawn.getBytes().write(1, (byte) (loc.getPitch() * 256 / 360));
                protocolManager.sendServerPacket(player, spawn);
            } else {

                try {
                    PacketContainer spawn = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
                    spawn.getIntegers().write(0, npc.getEntityId());
                    spawn.getUUIDs().write(0, npc.getUuid());
                    spawn.getEntityTypeModifier().write(0, EntityType.PLAYER);
                    spawn.getDoubles().write(0, loc.getX());
                    spawn.getDoubles().write(1, loc.getY());
                    spawn.getDoubles().write(2, loc.getZ());
                    spawn.getBytes().write(0, (byte) (loc.getPitch() * 256 / 360));
                    spawn.getBytes().write(1, (byte) (loc.getYaw() * 256 / 360));
                    spawn.getBytes().write(2, (byte) (loc.getYaw() * 256 / 360));
                    protocolManager.sendServerPacket(player, spawn);
                } catch (Exception modernSpawnError) {
                    plugin.getLogger().warning("Falha no spawn moderno (1.20.2+) do NPC '" + npc.getId()
                            + "': " + modernSpawnError.getMessage() + " — verifique os índices do SPAWN_ENTITY para sua versão de ProtocolLib.");
                }
            }

            sendHeadRotation(player, npc);

            hideNameTag(player, npc, profileName);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        sendPlayerInfoRemove(player, npc.getUuid());
                    }
                }
            }.runTaskLater(plugin, 60L);

        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao spawnar NPC '" + npc.getId() + "' para " + player.getName() + ": " + e.getMessage());
        }
    }

    private void sendHeadRotation(Player player, NPC npc) {
        try {
            PacketContainer headRotation = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            headRotation.getIntegers().write(0, npc.getEntityId());
            headRotation.getBytes().write(0, (byte) (npc.getLocation().getYaw() * 256 / 360));
            protocolManager.sendServerPacket(player, headRotation);
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao rotacionar a cabeça do NPC '" + npc.getId() + "': " + e.getMessage());
        }
    }

    private static String teamNameFor(NPC npc) {
        String teamName = "npc" + npc.getEntityId();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);
        return teamName;
    }


    private void hideNameTag(Player player, NPC npc, String profileName) {
        try {
            Scoreboard board = player.getScoreboard();
            if (board == null) return;

            String teamName = teamNameFor(npc);
            Team team = board.getTeam(teamName);
            if (team == null) team = board.registerNewTeam(teamName);

            try {
                team.setNameTagVisibility(NameTagVisibility.NEVER);
            } catch (Throwable ignored) {

            }

            if (!team.hasEntry(profileName)) {
                team.addEntry(profileName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Não foi possível esconder o nametag do NPC '" + npc.getId()
                    + "': " + e.getMessage());
        }
    }

    private void removeNameTagTeam(Player player, NPC npc) {
        try {
            Scoreboard board = player.getScoreboard();
            if (board == null) return;

            Team team = board.getTeam(teamNameFor(npc));
            if (team != null) team.unregister();
        } catch (Exception ignored) {
        }
    }

    private static int floorFixedPoint(double coordinate) {
        return (int) Math.floor(coordinate * 32.0D);
    }

    public void destroyFor(NPC npc, Player player) {
        try {
            PacketContainer destroy = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            try {
                destroy.getIntLists().write(0, Collections.singletonList(npc.getEntityId()));
            } catch (Exception ignoredModern) {
                destroy.getIntegerArrays().write(0, new int[]{npc.getEntityId()});
            }
            protocolManager.sendServerPacket(player, destroy);
            sendPlayerInfoRemove(player, npc.getUuid());
            removeNameTagTeam(player, npc);
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao remover NPC '" + npc.getId() + "' para " + player.getName() + ": " + e.getMessage());
        }
    }


    @SuppressWarnings("deprecation")
    private void sendPlayerInfoAdd(Player player, WrappedGameProfile profile) {
        try {

            try {
                PacketContainer legacy = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
                legacy.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
                PlayerInfoData data = new PlayerInfoData(profile, 0,
                        EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(profile.getName()));
                legacy.getPlayerInfoDataLists().write(0, Collections.singletonList(data));
                protocolManager.sendServerPacket(player, legacy);
                return;
            } catch (Exception legacyFormatUnavailable) {
            }

            if (PLAYER_INFO_PACKET_CTOR != null && PLAYER_INFO_ENTRY_CTOR != null && PLAYER_INFO_ACTION_CLASS != null) {
                Object rawPacket = buildModernPlayerInfoAddPacket(profile);
                if (rawPacket != null) {
                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO, rawPacket);
                    protocolManager.sendServerPacket(player, packet);
                    return;
                }
            }

            plugin.getLogger().warning("Não foi possível montar o pacote PLAYER_INFO (ADD) para '"
                    + profile.getName() + "' — nem o formato legado nem o moderno funcionaram nessa versão de servidor.");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro enviando PLAYER_INFO: " + e.getMessage());
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object buildModernPlayerInfoAddPacket(WrappedGameProfile profile) {
        try {
            Object gameProfileHandle = profile.getHandle();
            Object displayNameHandle = WrappedChatComponent.fromText(profile.getName()).getHandle();

            Class<?>[] entryParamTypes = PLAYER_INFO_ENTRY_CTOR.getParameterTypes();
            Object[] entryArgs = new Object[entryParamTypes.length];
            for (int i = 0; i < entryParamTypes.length; i++) {
                entryArgs[i] = buildEntryArg(entryParamTypes[i], profile.getUUID(), gameProfileHandle, displayNameHandle);
            }
            Object entry = PLAYER_INFO_ENTRY_CTOR.newInstance(entryArgs);

            EnumSet actions = EnumSet.noneOf((Class) PLAYER_INFO_ACTION_CLASS);
            addActionIfPresent(actions, "ADD_PLAYER");
            addActionIfPresent(actions, "UPDATE_LISTED");
            addActionIfPresent(actions, "UPDATE_LATENCY");
            addActionIfPresent(actions, "UPDATE_GAME_MODE");

            Object secondArg = PLAYER_INFO_PACKET_CTOR_TAKES_COLLECTION
                    ? Collections.singletonList(entry)
                    : entry;

            return PLAYER_INFO_PACKET_CTOR.newInstance(actions, secondArg);
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao montar pacote PLAYER_INFO moderno via reflection: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addActionIfPresent(EnumSet actions, String constantName) {
        for (Object c : PLAYER_INFO_ACTION_CLASS.getEnumConstants()) {
            if (((Enum<?>) c).name().equals(constantName)) {
                actions.add((Enum) c);
                return;
            }
        }
    }

    private static Object buildEntryArg(Class<?> type, UUID uuid, Object gameProfileHandle, Object displayNameHandle) {
        if (type == UUID.class) return uuid;
        if (type.getName().equals("com.mojang.authlib.GameProfile")) return gameProfileHandle;
        if (type == boolean.class || type == Boolean.class) return Boolean.TRUE;
        if (type == int.class || type == Integer.class) return 0;
        if (type == Optional.class) return Optional.empty();

        if (type.isEnum()) {
            for (Object c : type.getEnumConstants()) {
                if (((Enum<?>) c).name().equals("SURVIVAL")) return c;
            }
            Object[] constants = type.getEnumConstants();
            return constants.length > 0 ? constants[0] : null;
        }

        if (displayNameHandle != null && type.isInstance(displayNameHandle)) return displayNameHandle;

        return null;
    }

    @SuppressWarnings("deprecation")
    private void sendPlayerInfoRemove(Player player, UUID uuid) {
        try {
            if (PacketType.Play.Server.PLAYER_INFO_REMOVE.isSupported()) {
                PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
                packet.getUUIDLists().write(0, Collections.singletonList(uuid));
                protocolManager.sendServerPacket(player, packet);
            } else {
                PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
                packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
                WrappedGameProfile fakeProfile = new WrappedGameProfile(uuid, "");
                PlayerInfoData data = new PlayerInfoData(fakeProfile, 0, EnumWrappers.NativeGameMode.SURVIVAL, null);
                packet.getPlayerInfoDataLists().write(0, Collections.singletonList(data));
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro removendo player info: " + e.getMessage());
        }
    }

    public void teleport(NPC npc, Location location) {
        npc.setLocation(location);
        try {
            PacketContainer teleport = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            teleport.getIntegers().write(0, npc.getEntityId());

            if (VersionUtil.isLegacy()) {

                teleport.getIntegers().write(1, floorFixedPoint(location.getX()));
                teleport.getIntegers().write(2, floorFixedPoint(location.getY()));
                teleport.getIntegers().write(3, floorFixedPoint(location.getZ()));
            } else {
                teleport.getDoubles().write(0, location.getX());
                teleport.getDoubles().write(1, location.getY());
                teleport.getDoubles().write(2, location.getZ());
            }

            teleport.getBytes().write(0, (byte) (location.getYaw() * 256 / 360));
            teleport.getBytes().write(1, (byte) (location.getPitch() * 256 / 360));
            for (Player p : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(p, teleport);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao teleportar NPC: " + e.getMessage());
        }
    }
}