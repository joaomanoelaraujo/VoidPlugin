package org.ltzin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SkinApplier {

    private final Logger logger;

    private String nmsVersion;

    public SkinApplier(Logger logger) {
        this.logger = logger;
    }

    public void apply(Player player, SkinData skin) {
        if (!player.isOnline()) return;

        try {
            GameProfile profile = getProfile(player);
            if (profile == null) {
                logger.warning("[SkinApplier] GameProfile nulo para " + player.getName());
                return;
            }

            profile.getProperties().removeAll("textures");

            Property property = (skin.getSignature() != null)
                    ? new Property("textures", skin.getValue(), skin.getSignature())
                    : new Property("textures", skin.getValue());

            profile.getProperties().put("textures", property);

            refreshPlayer(player);

        } catch (Exception e) {
            logger.log(Level.WARNING, "[SkinApplier] Erro ao aplicar skin em " + player.getName(), e);
        }
    }


    private void refreshPlayer(Player player) throws Exception {
        Object nmsPlayer = getNmsPlayer(player);

        Object removePacket = buildPlayerInfoPacket(nmsPlayer, 4);
        Object addPacket    = buildPlayerInfoPacket(nmsPlayer, 0);

        for (Player online : Bukkit.getOnlinePlayers()) {
            sendPacket(online, removePacket);
            sendPacket(online, addPacket);

            if (!online.equals(player)) {
                sendEntityDestroy(online, player.getEntityId());
                sendNamedEntitySpawn(online, nmsPlayer);
            }
        }

        player.teleport(player.getLocation());
        player.updateInventory();
    }


    private GameProfile getProfile(Player player) throws Exception {
        Object nmsPlayer = getNmsPlayer(player);

        try {
            Method m = nmsPlayer.getClass().getMethod("getProfile");
            return (GameProfile) m.invoke(nmsPlayer);
        } catch (NoSuchMethodException ignored) { }

        Class<?> clazz = nmsPlayer.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (GameProfile.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (GameProfile) field.get(nmsPlayer);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Object getNmsPlayer(Player player) throws Exception {
        Method getHandle = player.getClass().getMethod("getHandle");
        return getHandle.invoke(player);
    }

    @SuppressWarnings("unchecked")
    private Object buildPlayerInfoPacket(Object nmsPlayer, int actionOrdinal) throws Exception {
        Class<?> packetClass = getNmsClass("PacketPlayOutPlayerInfo");

        Class<?> enumActionClass = null;
        for (Class<?> inner : packetClass.getDeclaredClasses()) {
            if (inner.isEnum()) { enumActionClass = inner; break; }
        }
        if (enumActionClass == null) throw new IllegalStateException("Enum de ação não encontrado em PacketPlayOutPlayerInfo");

        Object enumAction = enumActionClass.getEnumConstants()[actionOrdinal];

        Constructor<?> ctor = packetClass.getConstructor(enumActionClass, Iterable.class);
        return ctor.newInstance(enumAction, Collections.singletonList(nmsPlayer));
    }

    private void sendEntityDestroy(Player target, int entityId) throws Exception {
        Class<?> packetClass = getNmsClass("PacketPlayOutEntityDestroy");
        Object packet = packetClass.getConstructor(int[].class)
                .newInstance(new Object[]{ new int[]{ entityId } });
        sendPacket(target, packet);
    }

    private void sendNamedEntitySpawn(Player target, Object nmsEntity) throws Exception {
        Class<?> packetClass = getNmsClass("PacketPlayOutNamedEntitySpawn");
        for (Constructor<?> ctor : packetClass.getConstructors()) {
            if (ctor.getParameterCount() == 1) {
                Object packet = ctor.newInstance(nmsEntity);
                sendPacket(target, packet);
                return;
            }
        }
    }

    private void sendPacket(Player target, Object packet) throws Exception {
        Object nmsTarget = getNmsPlayer(target);
        Field  connField = null;

        Class<?> clazz = nmsTarget.getClass();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals("playerConnection")) { connField = f; break; }
            }
            if (connField != null) break;
            clazz = clazz.getSuperclass();
        }

        if (connField == null) throw new IllegalStateException("Campo playerConnection não encontrado");
        connField.setAccessible(true);
        Object conn = connField.get(nmsTarget);

        Method sendPacketMethod = conn.getClass().getMethod("sendPacket", getNmsClass("Packet"));
        sendPacketMethod.invoke(conn, packet);
    }

    private Class<?> getNmsClass(String name) throws ClassNotFoundException {
        if (nmsVersion == null) {
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        return Class.forName("net.minecraft.server." + nmsVersion + "." + name);
    }
}