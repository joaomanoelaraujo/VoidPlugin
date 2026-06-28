package org.ltzin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ltzin.Main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SkinApplier — compatível com 1.8 até 1.21+, sem ProtocolLib.
 *
 * Para o PRÓPRIO jogador no 1.8:
 *   PacketPlayOutRespawn(int dimension, EnumDifficulty, WorldType, EnumGamemode)
 *   → os parâmetros são resolvidos pelo TIPO, não pela posição assumida.
 *
 * Para 1.12+:
 *   hidePlayer(Plugin, self) + showPlayer(Plugin, self)
 */
public class SkinApplier {

    private final Logger logger;

    private String  nmsVersion;
    private boolean versionResolved = false;
    private boolean isNewPackaging;
    private Boolean isLegacyBukkit;

    public SkinApplier(Logger logger) {
        this.logger = logger;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public void apply(final Player player, final SkinData skin) {
        if (!player.isOnline()) return;

        try {
            GameProfile profile = getGameProfile(player);
            if (profile == null) {
                logger.warning("[SkinApplier] GameProfile nulo para " + player.getName());
                return;
            }
            profile.getProperties().removeAll("textures");
            Property property = (skin.getSignature() != null)
                    ? new Property("textures", skin.getValue(), skin.getSignature())
                    : new Property("textures", skin.getValue());
            profile.getProperties().put("textures", property);
        } catch (Exception e) {
            logger.log(Level.WARNING, "[SkinApplier] Erro ao injetar textura em " + player.getName(), e);
            return;
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            try { refreshForOthers(player); }
            catch (Exception e) {
                logger.log(Level.WARNING, "[SkinApplier] Erro ao atualizar outros para " + player.getName(), e);
            }
            refreshForSelf(player);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Outros jogadores
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshForOthers(Player player) throws Exception {
        Object nmsPlayer    = getNmsPlayer(player);
        Object removePacket = buildPlayerInfoPacket(nmsPlayer, false);
        Object addPacket    = buildPlayerInfoPacket(nmsPlayer, true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            sendPacket(online, removePacket);
            sendPacket(online, addPacket);
            sendEntityDestroy(online, player.getEntityId());
            sendNamedEntitySpawn(online, nmsPlayer);
        }

        sendPacket(player, removePacket);
        sendPacket(player, addPacket);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Próprio jogador
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshForSelf(Player player) {
        if (isLegacyBukkit == null) {
            try {
                player.getClass().getMethod("hidePlayer",
                        org.bukkit.plugin.Plugin.class, Player.class);
                isLegacyBukkit = false; // 1.12+
            } catch (NoSuchMethodException ignored) {
                isLegacyBukkit = true;  // 1.8–1.11
            }
        }

        if (isLegacyBukkit) {
            selfRespawn(player);
        } else {
            selfHideShow(player);
        }
    }


    private void selfRespawn(Player player) {
        try {
            Object nmsPlayer = getNmsPlayer(player);

            Class<?> respawnClass = findClass(
                    "net.minecraft.network.protocol.game.PacketPlayOutRespawn",
                    "PacketPlayOutRespawn");
            if (respawnClass == null) {
                logger.warning("[SkinApplier] PacketPlayOutRespawn não encontrado.");
                return;
            }

            Constructor<?> target = null;
            for (Constructor<?> ctor : respawnClass.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 4 && ctor.getParameterTypes()[0] == int.class) {
                    target = ctor;
                    break;
                }
            }

            if (target == null) {
                logger.warning("[SkinApplier] Construtor de PacketPlayOutRespawn não encontrado.");
                return;
            }

            target.setAccessible(true);

            Class<?>[] paramTypes = target.getParameterTypes();
            Object[]   args       = new Object[4];

            args[0] = player.getWorld().getEnvironment().getId();

            // Obtém os três objetos NMS necessários
            Object world     = getFieldInHierarchy(nmsPlayer, "world", "level", "serverLevel");
            Object worldData = invokeZeroArg(world, "getWorldData");

            Object difficulty = resolveByType(world, worldData, paramTypes[1]);
            Object worldType  = resolveByType(world, worldData, paramTypes[2]);
            Object gameMode   = resolveByType(world, worldData, paramTypes[3]);

            if (difficulty == null || worldType == null || gameMode == null) {
                logger.warning("[SkinApplier] selfRespawn: parâmetro NMS não resolvido para "
                        + player.getName()
                        + " (diff=" + difficulty + " type=" + worldType + " gm=" + gameMode + ")");
                return;
            }

            args[1] = difficulty;
            args[2] = worldType;
            args[3] = gameMode;

            Object respawnPacket = target.newInstance(args);
            sendPacket(player, respawnPacket);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!player.isOnline()) return;
                player.teleport(player.getLocation());
                player.updateInventory();
                player.setExp(player.getExp());
                player.setLevel(player.getLevel());
                player.setHealth(player.getHealth());
                player.setFoodLevel(player.getFoodLevel());
            }, 2L);

        } catch (Exception e) {
            logger.log(Level.WARNING, "[SkinApplier] selfRespawn falhou para " + player.getName(), e);
        }
    }


    private Object resolveByType(Object world, Object worldData, Class<?> expectedType) {
        String typeName = expectedType.getSimpleName().toLowerCase();

        if (typeName.contains("difficulty")) {
            Object v = invokeZeroArg(world, "getDifficulty", "difficulty");
            if (v != null && expectedType.isInstance(v)) return v;
        }

        if (typeName.contains("worldtype")) {
            if (worldData != null) {
                Object v = invokeZeroArg(worldData, "getType", "type");
                if (v != null && expectedType.isInstance(v)) return v;
            }
            Object v = invokeZeroArg(world, "getType");
            if (v != null && expectedType.isInstance(v)) return v;
        }

        if (typeName.contains("gamemode") || typeName.contains("enumgame")) {
            Object pm = getFieldInHierarchy(world, "playerInteractManager",
                    "interactManager", "gameMode");

            if (worldData != null) {
                Object v = invokeZeroArg(worldData, "getGameType", "getType");
                if (v != null && expectedType.isInstance(v)) return v;
            }
        }

        for (Object src : new Object[]{world, worldData}) {
            if (src == null) continue;
            Object v = getFieldOfType(src, expectedType);
            if (v != null) return v;
        }

        return null;
    }


    private Object resolveGameModeFromPlayer(Object nmsPlayer, Class<?> expectedType) {
        Object pm = getFieldInHierarchy(nmsPlayer,
                "playerInteractManager", "interactManager");
        if (pm == null) return null;

        Object gm = invokeZeroArg(pm, "getGameMode", "c", "b");
        if (gm == null) gm = getFieldOfType(pm, expectedType);
        if (gm != null && expectedType.isInstance(gm)) return gm;
        return null;
    }

    private void selfRespawn(Player player, boolean _unused) {
        selfRespawn(player);
    }


    private void selfHideShow(Player player) {
        try {
            Method hide = player.getClass().getMethod("hidePlayer",
                    org.bukkit.plugin.Plugin.class, Player.class);
            Method show = player.getClass().getMethod("showPlayer",
                    org.bukkit.plugin.Plugin.class, Player.class);

            hide.invoke(player, Main.getInstance(), player);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!player.isOnline()) return;
                try {
                    show.invoke(player, Main.getInstance(), player);
                } catch (Exception e) {
                    logger.log(Level.WARNING,
                            "[SkinApplier] showPlayer falhou para " + player.getName(), e);
                }
                player.updateInventory();
            }, 2L);

        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "[SkinApplier] hidePlayer/showPlayer falhou, usando respawn para "
                            + player.getName(), e);
            selfRespawn(player);
        }
    }

    private Object buildPlayerInfoPacket(Object nmsPlayer, boolean add) throws Exception {
        if (!add) {
            Class<?> cls = findClass(
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket", null);
            if (cls != null) {
                java.util.UUID uuid = resolveUUID(nmsPlayer);
                return cls.getConstructor(java.util.List.class)
                        .newInstance(Collections.singletonList(uuid));
            }
        } else {
            Class<?> cls = findClass(
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket", null);
            if (cls != null) {
                try {
                    Method factory = cls.getMethod("createPlayerInitializing",
                            java.util.Collection.class);
                    return factory.invoke(null, Collections.singletonList(nmsPlayer));
                } catch (NoSuchMethodException ignored) { }
            }
        }

        Class<?> packetClass = findClass(
                "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo",
                "PacketPlayOutPlayerInfo");
        if (packetClass == null)
            throw new IllegalStateException("PacketPlayOutPlayerInfo não encontrado");

        Class<?> enumClass = null;
        for (Class<?> inner : packetClass.getDeclaredClasses()) {
            if (inner.isEnum()) { enumClass = inner; break; }
        }
        if (enumClass == null)
            throw new IllegalStateException("Enum de ação não encontrado");

        Object action = enumClass.getEnumConstants()[add ? 0 : 4];
        Constructor<?> ctor = packetClass.getConstructor(enumClass, Iterable.class);
        return ctor.newInstance(action, Collections.singletonList(nmsPlayer));
    }

    private void sendEntityDestroy(Player target, int entityId) throws Exception {
        Class<?> cls = findClass(
                "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy",
                "PacketPlayOutEntityDestroy");
        if (cls == null) return;

        Object packet = null;
        try { packet = cls.getConstructor(int.class).newInstance(entityId); }
        catch (NoSuchMethodException ignored) { }
        if (packet == null) {
            try { packet = cls.getConstructor(int[].class)
                    .newInstance(new Object[]{ new int[]{ entityId } }); }
            catch (NoSuchMethodException ignored) { }
        }
        if (packet != null) sendPacket(target, packet);
    }

    private void sendNamedEntitySpawn(Player target, Object nmsEntity) throws Exception {
        Class<?> cls = findClass(
                "net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn",
                "PacketPlayOutNamedEntitySpawn");
        if (cls == null) return;

        for (Constructor<?> ctor : cls.getConstructors()) {
            if (ctor.getParameterCount() == 1) {
                sendPacket(target, ctor.newInstance(nmsEntity));
                return;
            }
        }
    }

    private GameProfile getGameProfile(Player player) throws Exception {
        Object nmsPlayer = getNmsPlayer(player);
        try {
            return (GameProfile) nmsPlayer.getClass().getMethod("getProfile").invoke(nmsPlayer);
        } catch (NoSuchMethodException ignored) { }

        Class<?> clazz = nmsPlayer.getClass();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (GameProfile.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (GameProfile) f.get(nmsPlayer);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Object getNmsPlayer(Player player) throws Exception {
        return player.getClass().getMethod("getHandle").invoke(player);
    }

    private void sendPacket(Player target, Object packet) throws Exception {
        Object nmsPlayer = getNmsPlayer(target);
        Object conn      = findConnection(nmsPlayer);
        if (conn == null)
            throw new IllegalStateException("Conexão NMS não encontrada para " + target.getName());

        Class<?> packetIface = findClass("net.minecraft.network.protocol.Packet", "Packet");
        for (String name : new String[]{"sendPacket", "send", "a"}) {
            try {
                Method m = conn.getClass().getMethod(name, packetIface);
                m.invoke(conn, packet);
                return;
            } catch (NoSuchMethodException ignored) { }
        }
        throw new IllegalStateException("Método de envio não encontrado na conexão");
    }

    private Object findConnection(Object nmsPlayer) {
        for (String name : new String[]{"playerConnection", "connection", "b", "c"}) {
            Object val = getFieldInHierarchy(nmsPlayer, name);
            if (val != null) return val;
        }
        return null;
    }

    private Object getFieldInHierarchy(Object obj, String... names) {
        if (obj == null) return null;
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (String name : names) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    Object v = f.get(obj);
                    if (v != null) return v;
                } catch (Exception ignored) { }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /** Busca o primeiro campo cujo tipo é assignable ao tipo esperado. */
    private Object getFieldOfType(Object obj, Class<?> expectedType) {
        if (obj == null) return null;
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (expectedType.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    try {
                        Object v = f.get(obj);
                        if (v != null) return v;
                    } catch (Exception ignored) { }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Object invokeZeroArg(Object obj, String... names) {
        if (obj == null) return null;
        for (String name : names) {
            for (Method m : obj.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 0) {
                    try { return m.invoke(obj); } catch (Exception ignored) { }
                }
            }
        }
        return null;
    }

    private java.util.UUID resolveUUID(Object nmsPlayer) {
        try {
            return (java.util.UUID) nmsPlayer.getClass().getMethod("getUUID").invoke(nmsPlayer);
        } catch (Exception ignored) { }
        try {
            GameProfile gp = getGameProfile(
                    (Player) nmsPlayer.getClass().getMethod("getBukkitEntity").invoke(nmsPlayer));
            if (gp != null) return gp.getId();
        } catch (Exception ignored) { }
        return null;
    }


    private Class<?> findClass(String newFqn, String legacySimpleName) {
        if (newFqn != null) {
            try { return Class.forName(newFqn); } catch (ClassNotFoundException ignored) { }
        }

        if (!versionResolved) {
            String[] parts = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            if (parts.length >= 4 && parts[3].startsWith("v")) {
                nmsVersion     = parts[3];
                isNewPackaging = false;
            } else {
                isNewPackaging = true;
            }
            versionResolved = true;
        }

        if (!isNewPackaging && legacySimpleName != null) {
            try {
                return Class.forName("net.minecraft.server." + nmsVersion + "." + legacySimpleName);
            } catch (ClassNotFoundException ignored) { }
        }
        return null;
    }
}